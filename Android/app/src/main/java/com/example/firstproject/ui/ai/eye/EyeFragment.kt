package com.example.firstproject.ui.ai.eye

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEyeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EyeFragment : Fragment() {

    private var _binding: FragmentEyeBinding? = null
    private val binding get() = _binding!!

    private var eyeDetector: EyeDetector? = null
    private var selectedVideoUri: Uri? = null


    // 애니메이션 관련 전역 변수
    private var analysisAnimationJob: Job? = null

    // updateUiDuringProcessing()에서 최신 진행 정보를 저장
    private var latestElapsedTime: Long = 0L
    private var latestProgress: Int = 0


    companion object {
        private const val REQUEST_VIDEO_PICK = 101
    }

    // 시선 처리 카운팅용 변수들
    private var leftTrueCount = 0
    private var leftFalseCount = 0
    private var rightTrueCount = 0
    private var rightFalseCount = 0
    private var totalFrameCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEyeBinding.inflate(inflater, container, false)
        val view = binding.root

        // 동영상 선택 버튼
        binding.BtnSelectVideoEye.setOnClickListener {
            openVideoGallery()
        }

        // 분석(피드백 받기) 버튼
        binding.btnFeedbackEye.setOnClickListener {
            if (selectedVideoUri != null) {
                // 모델 로드
                if (eyeDetector == null) {
                    eyeDetector = EyeDetector(
                        modelPath = "best_eye_tracking_0210_float16.tflite",
                        isQuantized = false
                    )
                    eyeDetector?.loadModel(requireContext().assets)
                }
                binding.apply {
                    const1.visibility = View.GONE
                    deleteImgEye.visibility =View.GONE
                    deleteTextEye.visibility = View.GONE

                    deleteLinear.visibility = View.GONE
                    binding.tvAnalysisStatus.text = "분석 중입니다."
                    binding.tvAnalysisStatus.visibility = View.VISIBLE
                }
                analysisAnimationJob = viewLifecycleOwner.lifecycleScope.launch {
                    animateAnalysisStatus()
                }
                // 코루틴으로 비디오 프레임 순회 + 분석
                viewLifecycleOwner.lifecycleScope.launch {
                    processVideoWithRetriever(selectedVideoUri!!)
                    analysisAnimationJob?.cancel()
                    binding.eyefeedbackFrame.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(requireContext(), "비디오를 등록해주셔야 해요 ㅠ", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun openVideoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_VIDEO_PICK)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { videoUri ->
                selectedVideoUri = videoUri
                Toast.makeText(requireContext(), "영상이 업로드 되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun processVideoWithRetriever(videoUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(requireContext(), videoUri)
                val durationMs =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0L

                // 분석 전 카운터 초기화
                leftTrueCount = 0
                leftFalseCount = 0
                rightTrueCount = 0
                rightFalseCount = 0
                totalFrameCount = 0

                val processingStartTime = System.currentTimeMillis()

                // 100ms(0.1초) 간격으로 프레임 추출
                for (timeMs in 0 until durationMs step 500) {
                    val bitmap = retriever.getFrameAtTime(
                        timeMs * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    ) ?: continue

                    // 감정 분석 결과
                    val result = eyeDetector?.detect(bitmap, null)
                    if (result != null && result.detections.isNotEmpty()) {
                        // 여러 얼굴이 감지되었다면, 스코어가 가장 높은 감정만 카운트
                        val topDetection = result.detections.maxByOrNull { it.score }
                        topDetection?.let { detection ->
                            when (detection.expression.lowercase()) {
                                "left_true" -> leftTrueCount++
                                "left_false" -> leftFalseCount++
                                "right_true" -> rightTrueCount++
                                "right_false" -> rightFalseCount++
                                else -> {}
                            }
                        }
                    }

                    totalFrameCount++

                    // 진행률 계산
                    val progress = ((timeMs.toFloat() / durationMs) * 100)
                        .toInt().coerceAtMost(100)

                    // 메인 스레드에서 UI 갱신
                    withContext(Dispatchers.Main) {
                        if (result != null) {
                            updateUiDuringProcessing(
                                bitmap = bitmap,
                                result = result.detections,
                                progress = progress,
                                startTime = processingStartTime
                            )
                        }
                    }
                    // 화면에 잠시 표시
                    delay(100L)
                }

                // 분석 완료
                retriever.release()

                // 최종 결과 Fragment로 이동
                withContext(Dispatchers.Main) {
                    goToResultFragment()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.e("MediaMetadataRetriever", "프레임 추출 중 오류 발생: ${e.message}")
                    Toast.makeText(requireContext(), "분석 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUiDuringProcessing(
        bitmap: Bitmap,
        result: List<EyeDetection>,
        progress: Int,
        startTime: Long
    ) {
        val elapsed = System.currentTimeMillis() - startTime

        latestElapsedTime = elapsed
        latestProgress = progress
        // 5초 이전: 얼굴 박스 / 감정 표시
        if (elapsed < 5000) {
            binding.eyefeedbackFrame.visibility = View.VISIBLE
            binding.eyeImageView.visibility = View.VISIBLE
            binding.eyeOverlayView.visibility = View.VISIBLE

            binding.circleProgressBar.visibility = View.GONE
            binding.tvCircleProgress.visibility = View.GONE

            // 현재 프레임 표시
            binding.eyeImageView.setImageBitmap(bitmap)
            // 감지된 얼굴들의 박스/라벨을 그리도록 오버레이에 전달
            binding.eyeOverlayView.setDetections(result)
        } else {
            binding.eyeImageView.visibility = View.GONE
            binding.eyeOverlayView.visibility = View.GONE

            binding.circleProgressBar.visibility = View.VISIBLE
            binding.tvCircleProgress.visibility = View.VISIBLE
            binding.txtV.visibility = View.VISIBLE

            binding.circleProgressBar.progress = progress
            binding.tvCircleProgress.text = "$progress%"
        }
    }

    private fun goToResultFragment() {
        // 감정 비율 계산
        val leftTrueRatio = if (totalFrameCount > 0) {
            leftTrueCount * 100f / totalFrameCount
        } else 0f

        val leftFalseRatio = if (totalFrameCount > 0) {
            leftFalseCount * 100f / totalFrameCount
        } else 0f

        val rightTrueRatio = if (totalFrameCount > 0) {
            rightTrueCount * 100f / totalFrameCount
        } else 0f

        val rightFalseRatio = if (totalFrameCount > 0) {
            rightFalseCount * 100f / totalFrameCount
        } else 0f

        val feedbackText = ""

        // 전달할 데이터 Bundle
        val bundle = Bundle().apply {
            putFloat("leftTrue", leftTrueRatio)
            putFloat("leftFalse", leftFalseRatio)
            putFloat("rightTrue", rightTrueRatio)
            putFloat("rightFalse", rightFalseRatio)
            putString("feedback", feedbackText)
        }

        // ** Navigation Component로 화면 전환 **
        findNavController().navigate(
            R.id.action_eyeFragment_to_eyeResultFragment,
            bundle
        )
    }


    private suspend fun animateAnalysisStatus() {
        val baseText = "분석 중입니다."
        var dotCount = 0
        while (isActive) {
            // dot 개수에 따른 문자열 생성
            val dots = ".".repeat(dotCount)
            // 진행률에 따라 남은 시간 예측 (진행률이 0일 경우 계산 중 메시지)
            val remainingText = if (latestProgress > 0) {
                val estimatedTotalTime = latestElapsedTime / (latestProgress / 100.0)
                val estimatedRemainingSeconds =((estimatedTotalTime - latestElapsedTime) / 1000).toInt()
                "예상 남은 시간: ${estimatedRemainingSeconds}초"
            } else {
                "예상 남은 시간 : 계산 중..."
            }
            binding.tvAnalysisStatus.text = "$baseText$dots $remainingText"
            dotCount = if (dotCount < 2) dotCount + 1 else 0
            delay(500L)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
