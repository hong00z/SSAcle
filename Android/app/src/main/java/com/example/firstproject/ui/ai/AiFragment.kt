package com.example.firstproject.ui.ai

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentAiBinding

class AiFragment : Fragment() {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentAiBinding.inflate(inflater, container, false)

        // 카드 데이터 3개 생성
        val items = listOf(
            CardItem(
                title = "자소서 피드백",
                description = "AI를 통해 자소서 첨삭을\n받아보세요",
                buttonText = "자소서 피드백 받기",
                imageRes = R.drawable.feedback_image,
                backgroundColorRes = R.color.card_white,
                textRes = 1,
                buttonRes = 1
            ),
            CardItem(
                title = "시선 피드백",
                description = "사소한 시선처리가\n신경쓰이시나요?",
                buttonText = "시선 피드백 받기",
                imageRes = R.drawable.emotion_image,
                backgroundColorRes = R.color.card_face,
                textRes = 1,
                buttonRes = 2
            ),
            CardItem(
                title = "표정 피드백",
                description = "차가운 면접장,\n표정 피드백이 필요하신가요?",
                buttonText = "표정 피드백 받기",
                imageRes = R.drawable.eye_image,
                backgroundColorRes = R.color.card_eye,
                textRes = 1,
                buttonRes = 3
            )
        )

        binding.viewPager.apply {
            offscreenPageLimit = 1  // 미리 로드할 페이지 개수
            clipToPadding = false   // 양쪽 아이템이 보이도록 설정
            clipChildren = false    // 자식 뷰 잘리지 않도록 설정
            setPageTransformer { page, position ->
                val scaleFactor = 0.85f + (1 - Math.abs(position)) * 0.15f
                page.scaleY = scaleFactor  // 스와이프 시 크기 조정 (부드러운 효과)
                page.alpha = 0.5f + (1 - Math.abs(position)) * 0.5f  // 반투명 효과
            }
        }

        // ViewPagerAdapter 설정
        val adapter = ViewPagerAdapter(items)
        binding.viewPager.adapter = adapter

        // DotsIndicator 연결
        binding.dotsIndicator.attachTo(binding.viewPager)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
