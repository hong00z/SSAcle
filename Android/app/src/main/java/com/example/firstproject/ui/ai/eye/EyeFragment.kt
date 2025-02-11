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
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEyeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EyeFragment : Fragment() {

    private var _binding: FragmentEyeBinding? = null
    private val binding get() = _binding!!

    private var eyeDetector: EyeDetector? = null
    private var selectedVideoUri: Uri? = null

    companion object {
        private const val REQUEST_VIDEO_PICK = 101
        private const val FRAME_INTERVAL = 100_000L // 0.1초 프레임 추출
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

                Toast.makeText(requireContext(), "분석중입니다...", Toast.LENGTH_SHORT).show()

                binding.deleteImgEye.visibility =View.GONE
                binding.deleteTextEye.visibility =View.GONE
                // 코루틴으로 비디오 프레임 순회 + 분석
                viewLifecycleOwner.lifecycleScope.launch {
                    processVideoWithRetriever(selectedVideoUri!!)
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

    /**
     * 비디오 프레임을 순회하며 감정(시선) 분석을 수행하는 함수
     */
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

                // -------------------------------
                // 분석 "시작 시각" 기록
                // -------------------------------
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

                // -------------------------------
                // 분석 "종료 시각"에서 시작 시각 빼서 총 걸린 시간 계산
                // -------------------------------
                val totalAnalysisTimeMs = System.currentTimeMillis() - processingStartTime
                Log.d("EyeFragment", "총 분석 시간: $totalAnalysisTimeMs ms")

                // 최종 결과 Fragment로 이동
                withContext(Dispatchers.Main) {
                    goToResultFragment(totalAnalysisTimeMs)
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

    /**
     * 5초 전까지는 얼굴 인식 결과(박스/감정)를 보여주고,
     * 5초 이후에는 원형 프로그레스바(진행률)를 보여주는 함수
     */
    private fun updateUiDuringProcessing(
        bitmap: Bitmap,
        result: List<EyeDetection>,
        progress: Int,
        startTime: Long
    ) {
        val elapsed = System.currentTimeMillis() - startTime

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
        }
        // 5초 경과 이후: 프로그레스바 + 퍼센트
        else {
            binding.eyeImageView.visibility = View.GONE
            binding.eyeOverlayView.visibility = View.GONE

            binding.circleProgressBar.visibility = View.VISIBLE
            binding.tvCircleProgress.visibility = View.VISIBLE
            binding.txtV.visibility = View.VISIBLE

            binding.circleProgressBar.progress = progress
            binding.tvCircleProgress.text = "$progress%"
        }
    }

    /**
     * 모든 프레임 처리 완료 후, 결과 Fragment로 이동
     * 감정 비율을 계산하여 Bundle로 넘김
     */
    private fun goToResultFragment(totalAnalysisTimeMs: Long) {
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

        // 총 분석 시간을 초 단위로 표시하고 싶다면:
        val totalSec = totalAnalysisTimeMs / 1000.0
        // 예시: 소수점 1자리만
        val formattedSec = String.format("%.1f", totalSec)

        // 예시용 피드백 메시지
        val feedbackText = """
            분석이 완료되었습니다!
            총 프레임 수: $totalFrameCount
            총 분석 시간: ${formattedSec}초
        """.trimIndent()

        // 결과 화면으로 데이터 전달
        val fragment = EyeResultFragment().apply {
            arguments = Bundle().apply {
                putFloat("leftTrue", leftTrueRatio)
                putFloat("leftFalse", leftFalseRatio)
                putFloat("rightTrue", rightTrueRatio)
                putFloat("rightFalse", rightFalseRatio)
                putString("feedback", feedbackText)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
