package com.example.firstproject.ui.ai.face

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEmotionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FaceExpressionFragment : Fragment() {

    private var _binding: FragmentEmotionBinding? = null
    private val binding get() = _binding!!

    private var faceExpressionDetector: FaceExpressionDetector? = null
    private var selectedVideoUri: Uri? = null

    companion object {
        private const val REQUEST_VIDEO_PICK = 101
        private const val FRAME_INTERVAL = 100_000L // 0.1초 프레임 추출
    }

    // 감정 카운팅용 변수들
    private var positiveCount = 0
    private var negativeCount = 0
    private var neutralCount = 0
    private var totalFrameCount = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmotionBinding.inflate(inflater, container, false)
        val view = binding.root

        // 동영상 선택 버튼
        binding.BtnSelectVideoemotion.setOnClickListener {
            openVideoGallery()
        }

        // 피드백 받기 버튼
        binding.btnFeedbackemotion.setOnClickListener {
            if (selectedVideoUri != null) {
                // 1) 모델이 아직 null이라면 로드
                if (faceExpressionDetector == null) {
                    faceExpressionDetector = FaceExpressionDetector(
                        modelPath = "best_face_emotion_float16_u.tflite",
                        isQuantized = false
                    )
                    faceExpressionDetector?.loadModel(requireContext().assets)
                }

                Toast.makeText(requireContext(), "분석중입니다...", Toast.LENGTH_SHORT).show()

                viewLifecycleOwner.lifecycleScope.launch {
                    processVideoWithRetriever(selectedVideoUri!!)
                    binding.feedbackFrame.visibility = View.VISIBLE
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
     * 비디오 프레임을 순회하며 감정 분석을 수행하는 함수
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
                positiveCount = 0
                negativeCount = 0
                neutralCount = 0
                totalFrameCount = 0

                // 분석 시작 시각
                val processingStartTime = System.currentTimeMillis()

                // 예: 100ms(0.1초)마다 프레임 추출
                for (timeMs in 0 until durationMs step 100) {
                    val bitmap = retriever.getFrameAtTime(
                        timeMs * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    ) ?: continue

                    // 감정 분석 결과
                    val result = faceExpressionDetector?.detect(bitmap, null)
                    // result.detections => List<FaceExpressionDetection>

                    // 여러 얼굴이 감지된 경우, score가 가장 높은 감정(1개)만 카운트
                    if (result != null) {
                        if (result.detections.isNotEmpty()) {
                            val topDetection = result.detections.maxByOrNull { it.score }
                            topDetection?.let { detection ->
                                when (detection.expression.lowercase()) {
                                    "positive" -> positiveCount++
                                    "negative" -> negativeCount++
                                    "neutral" -> neutralCount++
                                    // 그 외의 감정 태그가 있으면 필요한 만큼 추가
                                    else -> {}
                                }
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

                    // 화면에 잠시 표시를 위해 지연 (0.1초)
                    delay(100L)
                }

                retriever.release()

                // 모든 프레임 처리 완료 후 결과 화면으로 이동
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

    /**
     * 5초 전까지는 얼굴 인식 결과(박스/감정)를 보여주고,
     * 5초 이후에는 원형 프로그레스바(진행률)를 보여주는 함수
     */
    private fun updateUiDuringProcessing(
        bitmap: Bitmap,
        result: List<FaceExpressionDetection>,
        progress: Int,
        startTime: Long
    ) {
        val elapsed = System.currentTimeMillis() - startTime

        // 5초 이전: 얼굴 박스 / 감정 표시
        if (elapsed < 5000) {
            binding.feedbackFrame.visibility = View.VISIBLE
            binding.emotionImageView.visibility = View.VISIBLE
            binding.emotionOverlayView.visibility = View.VISIBLE

            binding.circleProgressBar.visibility = View.GONE
            binding.tvCircleProgress.visibility = View.GONE

            // 현재 프레임 표시
            binding.emotionImageView.setImageBitmap(bitmap)
            // 감지된 얼굴들의 박스/라벨을 그리도록 오버레이에 전달
            binding.emotionOverlayView.setDetections(result)
        }
        // 5초 경과 이후: 프로그레스바 + 퍼센트
        else {
            binding.emotionImageView.visibility = View.GONE
            binding.emotionOverlayView.visibility = View.GONE

            binding.circleProgressBar.visibility = View.VISIBLE
            binding.tvCircleProgress.visibility = View.VISIBLE
            binding.txtV.visibility= View.VISIBLE

            binding.circleProgressBar.progress = progress
            binding.tvCircleProgress.text = "$progress%"
        }
    }

    /**
     * 모든 프레임 처리 완료 후, 결과 Fragment로 이동
     * 감정 비율을 계산하여 Bundle로 넘김
     */
    private fun goToResultFragment() {
        // 감정 비율 계산
        val positiveRatio = if (totalFrameCount > 0) {
            positiveCount * 100f / totalFrameCount
        } else 0f

        val negativeRatio = if (totalFrameCount > 0) {
            negativeCount * 100f / totalFrameCount
        } else 0f

        val neutralRatio = if (totalFrameCount > 0) {
            neutralCount * 100f / totalFrameCount
        } else 0f

        // 예시: 임의의 피드백 메시지
        val feedbackText = "분석이 완료되었습니다!\n" +
                "총 프레임: $totalFrameCount"

        // 결과 화면으로 데이터 전달
        val fragment = FaceResultFragment().apply {
            arguments = Bundle().apply {
                putFloat("positive", positiveRatio)
                putFloat("negative", negativeRatio)
                putFloat("neutral", neutralRatio)
                putString("feedback", feedbackText)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
