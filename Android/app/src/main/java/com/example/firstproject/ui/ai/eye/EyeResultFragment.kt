package com.example.firstproject.ui.ai.eye

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEyeResultBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter


class EyeResultFragment : Fragment() {

    private var _binding: FragmentEyeResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEyeResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var leftTrue = arguments?.getFloat("leftTrue", 0f) ?: 0f
        var leftFalse = arguments?.getFloat("leftFalse", 0f) ?: 0f
        var rightTrue = arguments?.getFloat("rightTrue", 0f) ?: 0f
        var rightFalse = arguments?.getFloat("rightFalse", 0f) ?: 0f
        val feedback = arguments?.getString("feedback") ?: ""

        Log.d("TAG", "onViewCreated: $leftTrue, $leftFalse, $rightTrue, $rightFalse")
        if (leftTrue + leftFalse + rightTrue + rightFalse != 100.0f) {
            val rest = 100.0f - (leftTrue + leftFalse + rightTrue + rightFalse)

            leftTrue += rest / 4.0f
            leftFalse += rest / 4.0f
            rightTrue += rest / 4.0f
            rightFalse += rest / 4.0f
        }
        val trueValue = (leftTrue + rightTrue)
        val falseValue = (leftFalse + rightFalse)

        binding.tvEyePositiveValue.text = "집중 ${"%.1f".format(trueValue)}"
        binding.tvEyeNegativeValue.text = "흔들림 ${"%.1f".format(falseValue)}"
        binding.tvEyeFeedbackContent.text = feedback

        setupDonutChart(trueValue, falseValue)

        binding.btnEyeEyeSaveResult.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }


        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


    }

    private fun setupDonutChart(positive: Float, negative: Float) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(positive, "집중"))
            add(PieEntry(negative, "흔들림"))
        }

        val dataSet = PieDataSet(entries, "감정 비율").apply {
            // 색상 세팅 (원하는 색 지정 가능)
            colors = listOf(
                resources.getColor(R.color.chart_blue, null),
                resources.getColor(R.color.chart_red, null),
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

        binding.eyedonutChart.apply {
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