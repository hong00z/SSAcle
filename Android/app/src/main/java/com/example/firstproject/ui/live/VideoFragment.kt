package com.example.firstproject.ui.live

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.firstproject.databinding.FragmentVideoBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!


    private lateinit var yoloDetector: HumanDetector

    private lateinit var cameraExecutor: ExecutorService

    // 탐지 시간 측정을 위한 변수들 (추가)
    @Volatile
    private var detectionActive = false
    private var detectionStartTime: Long = 0
    private var totalDetectedTime: Long = 0
    private var lastFrameTimestamp: Long = 0

    private var isProcessingFrame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        binding.apply {

            btnStartFocus.setOnClickListener {

            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 모델 로드
        yoloDetector = HumanDetector("yolov8n.tflite", isQuantized = false)
        yoloDetector.loadModel(requireContext().assets)

        // 2) 권한 체크 후 카메라 시작
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // [start] 버튼
        binding.btnStartFocus.setOnClickListener {
            if (!detectionActive) {
                detectionActive = true
                detectionStartTime = System.currentTimeMillis()
                lastFrameTimestamp = detectionStartTime
                totalDetectedTime = 0
                Log.d("VideoFragment", "탐지 시작: $detectionStartTime")
            }
        }

        // [stop] 버튼
        binding.btnEndLive.setOnClickListener {
            if (detectionActive) {
                detectionActive = false
                val detectionEndTime = System.currentTimeMillis()
                val totalMonitoringDuration = detectionEndTime - detectionStartTime

                val detectionPercentage = if (totalMonitoringDuration > 0) {
                    (totalDetectedTime.toDouble() / totalMonitoringDuration.toDouble()) * 100.0
                } else 0.0

                val totalTimeStr = formatTime(totalMonitoringDuration)
                val detectedTimeStr = formatTime(totalDetectedTime)

                AlertDialog.Builder(requireContext())
                    .setTitle("탐지 결과")
                    .setMessage(
                        "전체 감지 시간: $totalTimeStr\n" +
                                "객체 감지된 시간: $detectedTimeStr\n" +
                                "전체 시간 대비 ${"%.2f".format(detectionPercentage)}% 동안 객체 탐지됨"
                    )
                    .setPositiveButton("확인", null)
                    .show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Analyzer 설정
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (isProcessingFrame) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                isProcessingFrame = true

                val bitmap = imageToBitmap(imageProxy)

                // YOLO 추론
                val detections = yoloDetector.detect(bitmap)

                // 사람 감지 시간 누적
                if (detectionActive) {
                    val now = System.currentTimeMillis()
                    val frameInterval = now - lastFrameTimestamp
                    lastFrameTimestamp = now
                    if (detections.isNotEmpty()) {
                        // "사람"이 하나 이상이면 누적
                        totalDetectedTime += frameInterval
                    }
                }


                isProcessingFrame = false
                imageProxy.close()
            }


            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("VideoFragment", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun imageToBitmap(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // NV21 배열 구성
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        uBuffer.get(nv21, ySize, uSize)
        vBuffer.get(nv21, ySize + uSize, vSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpegBytes = out.toByteArray()
        var bmp = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        val matrix = Matrix()

        // 미러링 (좌우)
        matrix.postRotate(270f)
        matrix.postScale(-1f, 1f)

        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)

        // 640 640
        bmp = Bitmap.createScaledBitmap(bmp, 640, 640, false)
        return bmp
    }

    /**
     * 시/분/초 변환
     */
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // 카메라 권한 체크 여기서 합니다.
    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        yoloDetector.close()
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        fun newInstance(): VideoFragment {
            return VideoFragment()
        }

    }
}