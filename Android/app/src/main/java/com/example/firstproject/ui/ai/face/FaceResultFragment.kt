package com.example.firstproject.ui.ai.face

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentFaceResultBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter

class FaceResultFragment : Fragment() {

    private var _binding: FragmentFaceResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var positive = arguments?.getFloat("positive", 0f) ?: 0f
        var negative = arguments?.getFloat("negative", 0f) ?: 0f
        var neutral = arguments?.getFloat("neutral", 0f) ?: 0f
        val feedback = arguments?.getString("feedback") ?: ""

        // 합계가 100이 안 되면 나머지를 3등분
        if (positive + neutral + negative != 100.0f) {
            val rest = 100.0f - (positive + neutral + negative)
            positive += rest / 3f
            negative += rest / 3f
            neutral += rest / 3f
        }

        // UI 텍스트 설정
        binding.tvPositiveValue.text = "긍정적 표정 ${"%.1f".format(positive)}%"
        binding.tvNegativeValue.text = "부정적 표정 ${"%.1f".format(negative)}%"
        binding.tvNeutralValue.text = "무표정 ${"%.1f".format(neutral)}%"
        binding.tvFeedbackContent.text = feedback

        // ★ 도넛 차트 구현 부분
        setupDonutChart(positive, negative, neutral)

        // "결과 저장" 버튼 눌렀을 때 // 수정해야함.
        binding.btnSaveResult.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }



        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


    }

    /**
     * MPAndroidChart를 이용해 도넛 차트를 세팅하는 함수
     */
    // 이런게 있더라 (GPT 왈)
    private fun setupDonutChart(positive: Float, negative: Float, neutral: Float) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(positive, "긍정"))
            add(PieEntry(negative, "부정"))
            add(PieEntry(neutral, "무표정"))
        }

        val dataSet = PieDataSet(entries, "감정 비율").apply {
            // 색상 세팅 (원하는 색 지정 가능)
            colors = listOf(
                resources.getColor(R.color.chart_blue, null),
                resources.getColor(R.color.chart_red, null),
                resources.getColor(R.color.chart_green, null)
            )
            // 차트 가운데 구멍이 보이는 퍼센트 (두께 설정)
            sliceSpace = 2f            // 파이조각 사이 간격
            valueTextSize = 14f       // 파이조각 위에 표시되는 값 크기
            valueTextColor = resources.getColor(R.color.white, null) // 파이 위 텍스트 색
        }

        val data = PieData(dataSet).apply {
            // 소수점 처리 등
            setValueFormatter(DefaultValueFormatter(1)) // 소수점 1자리
        }

        binding.donutChart.apply {
            this.data = data
            description.isEnabled = false    // 차트 우하단 설명 끔
            isDrawHoleEnabled = true         // 도넛 모양 (가운데 구멍)
            holeRadius = 40f                // 도넛 구멍 반경 (%)
            setTransparentCircleAlpha(0)     // 투명한 원 제거
            setEntryLabelColor(resources.getColor(R.color.black, null)) // 라벨 색
            setEntryLabelTextSize(14f)

            // 차트 애니메이션 (0.5초)
            animateY(500)

            // 차트 갱신
            invalidate()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
