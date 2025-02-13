package com.example.firstproject.ui.ai.face

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentFaceResultBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

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

        // 합계가 100이 안 되면 나머지를 3등분
        if (positive + neutral + negative != 100.0f) {
            val rest = 100.0f - (positive + neutral + negative)
            positive += rest / 3f
            negative += rest / 3f
            neutral += rest / 3f
        }

        // text UI
        binding.tvPositiveValue.apply {
            val positiveValueFormatted = "%.1f".format(positive)
            text = "긍정 $positiveValueFormatted%"

            val spannable = SpannableString(text)
            val startIndex = text.indexOf(positiveValueFormatted)
            val endIndex = startIndex + positiveValueFormatted.length

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = spannable
        }

        binding.tvNegativeValue.apply {
            val negativeValueFormatted = "%.1f".format(negative)
            text = "부정 $negativeValueFormatted%"

            val spannable = SpannableString(text)
            val startIndex = text.indexOf(negativeValueFormatted)
            val endIndex = startIndex + negativeValueFormatted.length

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannable
        }

        binding.tvNeutralValue.apply {
            val neutralValueFormatted = "%.1f".format(neutral)
            text = "중립 $neutralValueFormatted%"
            val spannable = SpannableString(text)
            val startIndex = text.indexOf(neutralValueFormatted)
            val endIndex = startIndex + neutralValueFormatted.length

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannable

        }
        binding.tvFeedbackContent.apply {
            text = getFeedback(positive, negative, neutral)
        }

        // ★ 도넛 차트 구현 부분
        setupDonutChart(positive, negative, neutral)

        binding.detectText.apply {
            val detValue = max(max(positive, negative), neutral)
            var detText = ""
            if (detValue == positive) {
                detText = "긍정"
            } else if (detValue == negative) {
                detText = "부정"
            } else {
                detText = "중립"
            }
            val valueFormatted = "%.1f".format(detValue)

            // 결과 텍스트 생성
            val resultText = "영상 분석 결과, ${detText}이 ${valueFormatted}% 로 \n 가장 많이 감지되었습니다."

            // SpannableStringBuilder 사용
            val spannable = SpannableStringBuilder(resultText)

            // 글자
            val detTextStart = resultText.indexOf(detText)
            val detTextEnd = detTextStart + detText.length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                detTextStart,
                detTextEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                AbsoluteSizeSpan(18,true),
                detTextStart,
                detTextEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            // detValue(숫자) 굵게 처리
            val detValueStart = resultText.indexOf(valueFormatted)
            val detValueEnd = detValueStart + valueFormatted.length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                detValueStart,
                detValueEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                detValueStart,
                detValueEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                AbsoluteSizeSpan(18,true),
                detValueStart,
                detValueEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = spannable // 굵게 적용된 SpannableStringBuilder 설정
        }

        binding.btnSaveResult.setOnClickListener {
            saveAndShareImage()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun saveAndShareImage() {
        val targetView = binding.imageCardView
        val bitmap = getBitmapFromView(targetView)
        val imageFile = saveBitmapAsImage(bitmap)
        shareImageFile(imageFile)
    }

    private fun getBitmapFromView(view: View): Bitmap {
        // 실제 크기로 측정
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Bitmap 생성
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapAsImage(bitmap: Bitmap): File {
        val imageDirPath = requireContext().getExternalFilesDir(null)?.absolutePath
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFile = File(imageDirPath, "eye_result_feedback_$timestamp.png")

        try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // PNG 형식으로 저장
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return imageFile
    }

    private fun shareImageFile(imageFile: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png" // 이미지 형식 지정
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "이미지 공유하기"))
    }


    /**
     * MPAndroidChart를 이용해 도넛 차트를 세팅하는 함수
     */
    // 이런게 있더라 (GPT 왈)
    private fun setupDonutChart(positive: Float, negative: Float, neutral: Float) {
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(positive, "긍정"))
            add(PieEntry(negative, "부정"))
            add(PieEntry(neutral, "중립"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            // 색상 세팅 (원하는 색 지정 가능)
            colors = listOf(
                resources.getColor(R.color.face_chart_plus, null),
                resources.getColor(R.color.face_chart_minus, null),
                resources.getColor(R.color.face_chart_middle, null)
            )
            // 차트 가운데 구멍이 보이는 퍼센트 (두께 설정)
            sliceSpace = 2f            // 파이조각 사이 간격
            valueTextSize = 14f       // 파이조각 위에 표시되는 값 크기
            valueTextColor = resources.getColor(R.color.white, null) // 파이 위 텍스트 색
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f", value)  // 정수만 표시 (퍼센트 없음)
                }
            })
        }

        binding.donutChart.apply {
            this.data = data
            description.isEnabled = false    // 설명 삭제
            isDrawHoleEnabled = true         // 구멍
            holeRadius = 40f                // 도넛 구멍 반경 (%)
            setTransparentCircleAlpha(0)     // 투명한 원 제거
            setEntryLabelColor(Color.TRANSPARENT) // 차트 내부 라벨 숨김
            setEntryLabelTextSize(0f)       // 차트 내부 라벨 텍스트 크기 0으로 설정

            // 차트 애니메이션 (0.5초)
            animateY(500)

            val customRenderer = FaceCustomPieChartRenderer(
                this,
                this.animator,
                this.viewPortHandler
            ).apply {
                defaultValueTextSize = 32f
                selectedValueTextSize = 60f
            }

            renderer = customRenderer

            binding.donutChart.legend.isEnabled = false

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    customRenderer.selectedIndex = h?.x?.toInt() ?: -1
                    val selectedIndex = h?.x?.toInt() ?: -1
                    when (selectedIndex) {
                        0 -> { // "plus" 영역 선택 시
                            binding.tvLegendPlus.apply {
                                setTypeface(null, Typeface.BOLD)
                                textSize = 16f
                            }
                            binding.tvLegendMinus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendMiddle.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }

                        1 -> { // "부정" 영역 선택 시
                            binding.tvLegendMinus.apply {
                                setTypeface(null, Typeface.BOLD)
                                textSize = 16f
                            }
                            binding.tvLegendPlus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendMiddle.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }
                        // 중립 선택시
                        2 -> {
                            binding.tvLegendMiddle.apply {
                                setTypeface(null, Typeface.BOLD)
                                textSize = 16f
                            }
                            binding.tvLegendPlus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendMinus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }

                        else -> {
                            // 기타: 기본 스타일로 복원
                            binding.tvLegendPlus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendMinus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendMiddle.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }
                    }
                    invalidate() // 차트 다시 그리기
                }

                override fun onNothingSelected() {
                    customRenderer.selectedIndex = -1
                    binding.tvLegendPlus.apply {
                        setTypeface(null, Typeface.NORMAL)
                        textSize = 14f
                    }
                    binding.tvLegendMinus.apply {
                        setTypeface(null, Typeface.NORMAL)
                        textSize = 14f
                    }
                    binding.tvLegendMiddle.apply {
                        setTypeface(null, Typeface.NORMAL)
                        textSize = 14f
                    }
                    invalidate()
                }
            })
            // 차트 갱신
            invalidate()
            // 차트 갱신
        }
    }


    fun getFeedback(positive: Float, negative: Float, neutral: Float): SpannableString {
        val messages = when {
            positive >= 80.000 -> listOf(
                "표정이 매우 밝고 긍정적이어서 면접관도 대화하는 데 편안함을 느낄 것입니다." +
                        " 다만, 과한 밝은 표정은 지원자의 진지함을 의심하게 만들 수 있습니다." +
                        " 중요한 질문에 대한 답변이라면, 표정을 조금 더 차분하게 유지하며, 감정을 잘 조절하는 모습을 보여주는 것이 좋습니다." +
                        " 이 점을 고려하여 신뢰감을 더하는 모습을 연습해보세요.",
                "밝은 표정은 면접에서 강력한 장점이 됩니다." +
                        " 면접관은 지원자가 자신감을 가지고 있음을 느낄 수 있으며, 더 쉽게 편안함을 느낍니다." +
                        " 다만, 모든 질문에서 같은 표정을 유지하는 것보다는, 질문의 성격에 맞게 적절한 표정을 변화시켜주면 더욱 신뢰를 줄 수 있습니다." +
                        " 진지한 답변을 할 때는 미소를 줄이고 차분하게 표정을 조절해보세요.",
                "면접에서 긍정적인 표정은 강점이지만, 지나치게 밝거나 과한 미소는 때때로 과도한 자신감처럼 보일 수 있습니다." +
                        " 특히, 중요한 질문이나 어려운 질문에 대한 답변이라면, 표정의 변화를 주어 상대방에게 신뢰감을 주는 것이 중요합니다." +
                        " 자연스럽게 미소를 지으며 답변을 할 때도, 질문에 따른 표정의 변화를 통해 신중함과 자신감을 동시에 표현하는 것이 좋습니다.",
                "지원자의 밝은 표정은 면접관에게 긍정적인 인상을 주며, 면접 분위기를 부드럽게 만들 수 있습니다." +
                        " 자연스럽게 미소를 유지하는 것은 강점이지만, 모든 질문에 일관되게 미소를 짓는 것은 신뢰성을 떨어뜨릴 수도 있습니다." +
                        " 특히, 논리적인 설명이 필요한 질문에서는 조금 더 진중한 표정을 유지하는 것이 좋습니다." +
                        " 약간 차분한 표정을 시도해보세요. 감정 표현의 균형을 맞추면 더욱 성숙하고 자신감 있는 이미지를 줄 수 있습니다.",
                "긍정적인 표정은 면접관이 지원자와의 대화를 즐길 수 있도록 도와줍니다." +
                        " 하지만 모든 질문에 같은 표정을 유지하면 면접관이 지원자의 감정을 정확히 파악하기 어려울 수 있습니다." +
                        " 직무와 관련된 중요한 질문에서는 진지한 표정을," +
                        " 자기소개나 강점을 이야기할 때는 밝은 미소를 유지하는 것이 좋습니다." +
                        " 질문의 성격에 따라 적절하게 표정을 조절하면, 보다 신뢰감 있는 인상을 심어줄 수 있습니다.",
                "지원자의 표정은 면접관에게 매우 좋은 첫인상을 남길 가능성이 큽니다." +
                        " 하지만 지나치게 밝은 표정이 지속되면 긴장감이 부족해 보일 수도 있습니다." +
                        " 면접관은 지원자가 상황에 따라 적절한 반응을 보이는지를 중요하게 평가할 수 있기 때문에," +
                        " 고민이 필요한 질문에 대한 답변일 경우, 차분한 표정을 유지하는 연습이 필요합니다." +
                        " 표정과 감정을 적절히 조절하는 것이 면접의 신뢰도를 높이는 방법입니다.",
                " 은은한 미소는 긍정적인 인상을 주지만, 상황에 맞는 표정 변화를 주는 것이 더욱 중요합니다." +
                        " 특히 다대다 면접에서는 다른 지원자의 발언에 과도한 미소를 짓지 않도록 주의하세요." +
                        " 자신의 미소가 부자연스럽다고 느껴진다면 거울을 보며 눈과 입이 함께 자연스럽게 움직이는지 확인하는 연습을 해보세요."
            )

            positive in 60.000..79.99999 -> listOf(
                "지원자의 표정은 면접관에게 매우 긍정적인 첫인상을 남길 수 있습니다." +
                        " 하지만 모든 상황에서 긍정적인 표정이 항상 효과적이지는 않습니다." +
                        " 예를 들어, 최적화와 같은 주제와 같이 중요한 문제에 대해 논의할 때는 " +
                        "밝은 표정보다는 신중하고 집중하는 모습을 보여주는 것이 더 효과적일 수 있습니다." +
                        " 이를 통해 면접관에게 지원자의 진지함과 직무에 대한 준비성을 보여줄 수 있습니다.",
                "밝고 긍정적인 표정은 면접관이 지원자에게 편안한 느낌을 줄 수 있습니다. " +
                        "그러나 논리적이고 고민이 필요한 답변이 필요한 순간에는 미소가 부정적인 영향을 미칠 수 있습니다." +
                        " 이러한 순간에는 표정을 좀 더 진지하게 유지하고," +
                        " 필요한 경우 질문에 대한 답변을 차분하게 준비해보세요. 균형 잡힌 표정이 면접의 중요한 포인트입니다.",
                "현재 표정은 매우 긍정적이고 좋은 인상을 주지만, 때로는 감정의 변화를 주는 것이 필요할 수 있습니다." +
                        " 중요한 문제를 다룰 때는 표정이 지나치게 밝거나 활기차 보이면 면접관이 지원자의 신뢰성에 대해 의문을 가질 수 있습니다." +
                        " 표정의 조절을 통해 면접에서 긍정적인 이미지를 더욱 강화할 수 있습니다.",
                "지원자의 표정은 긍정적인 에너지를 면접관에게 전달하며, 대화가 부드럽게 진행될 가능성이 높습니다." +
                        " 하지만 중요한 것은 표정의 일관성이 아니라, 상황에 맞는 적절한 감정 표현입니다." +
                        " 만약, 고민이 필요한 질문일 경우, 밝은 표정보다는 살짝 미소를 줄이고 신중한 태도를 보이는 것이" +
                        " 신뢰도를 높이는 데 도움이 될 수 있습니다." +
                        " 표정의 변화는 지원자의 면접 태도를 더욱 풍부하게 만들어줍니다.",
                "밝은 표정 덕분에 면접관이 지원자를 편안하게 느낄 가능성이 큽니다." +
                        " 하지만 지나치게 웃는 것은 오히려 가벼운 인상을 줄 수 있으므로, 표정에 대한 변화를 답변에 따라 다르게 하는 것이 좋습니다." +
                        " 특히, 자신의 경험이나 강점을 이야기할 때 미소를 적절히 추가하면 더욱 설득력 있는 답변이 될 수 있습니다.",
                "지원자의 표정은 면접관에게 좋은 인상을 줄 수 있으며, 편안하고 자신감 있는 분위기를 형성하는 데 도움이 됩니다." +
                        " 하지만 모든 순간에서 웃음이 필요한 것은 아닙니다." +
                        " 직무와 관련된 심도 있는 질문이라면 약간의 미소와 함께 당당한 태도를 보이는 것이 중요합니다." +
                        " 이를 통해 지원자의 면접에 대해 진지한 태도를 가지고 있다는 점을 어필할 수 있습니다."
            )

            positive in 40.000..59.99999 -> listOf(

                "현재 표정은 면접관에게 신뢰감을 주는 균형 잡힌 상태입니다." +
                        " 하지만 면접에서는 감정을 적극적으로 표현하는 것도 중요한 요소입니다." +
                        " 예를 들어, 자신이 어필할 경험을 이야기할 때는 자연스러운 미소를 추가해주면 더 긍정적인 반응을 이끌어낼 수 있습니다.",
                "현재 표정은 매우 안정적이고 균형 잡힌 인상을 주며, 면접관에게 긍정적인 이미지를 남길 수 있습니다." +
                        " 그러나 일부 순간에는 감정을 좀 더 표현하는 것이 필요할 수 있습니다." +
                        " 중요하다 생각되는 질문의 답변은 표정을 약간 더 진지하게 하여 자신감을 표현하는 것이 좋습니다." +
                        " 이는 면접관이 지원자의 태도에 대한 신뢰도를 더욱 높여줄 것입니다.",
                "현재 표정은 면접에서 신뢰감을 줄 수 있는 수준입니다." +
                        " 그러나 때때로 감정 표현이 조금 더 필요할 수 있습니다." +
                        " 중요한 질문에 답할 때는 약간의 미소를 띈 상태로 당당하게 말하는 것이 중요합니다." +
                        " 이를 통해 면접관은 지원자가 면접에 적극적으로 참여하고 있다는 느낌을 받을 수 있습니다.",
                "지원자의 표정은 면접에서 가장 이상적인 상태일 가능성이 큽니다." +
                        " 지나치게 밝거나 너무 무표정하지 않고, 상황에 맞는 표정을 유지하고 있습니다." +
                        " 추가적으로 일부 순간에는 감정 표현을 조금 더 적극적으로 하는 것이 좋을 수 있습니다." +
                        " 자신의 성취나 강점을 이야기할 때는 자신감 있는 표정을 유지하고, 살짝 미소를 더해보세요.",
                "현재의 표정은 면접에서 신뢰감을 줄 수 있는 적절한 수준을 유지하고 있습니다." +
                        " 추가적으로, 강조해야 할 부분에서는 표정을 조금 더 활용하면 더욱 강한 인상을 남길 수 있습니다." +
                        " 예를 들어, 답변에 대한 면접관이 고개를 끄덕이는 등의 긍정적인 반응을 보일 때 살짝 미소를 짓는 연습을 하면," +
                        " 면접이 더욱 자연스럽고 긍정적인 분위기로 진행될 수 있습니다."
            )

            neutral in 40.000..59.99999 -> listOf(
                "지원자의 표정은 매우 차분하고 신중한 인상을 주고 있습니다." +
                        " 그러나 때때로 침착한 표정이 지속되면 면접관이 지원자의 감정을 파악하기 어려울 수 있습니다." +
                        " 약간의 미소를 띄어 편한 분위기를 가져가는 연습을 해보세요." +
                        " 순발력있는 지원자 분의 모습을 본다면 면접관과의 긍정적인 반응을 끌어낼 수 있습니다.",
                "현재의 표정은 면접에서 신뢰감을 줄 수 있는 인상을 줍니다." +
                        " 조금 더 효율적인 의사소통을 위해서는" +
                        " 답변 과정에서 이해를 도울 수 있는 제스처를 사용해보는게 어떨까요?" +
                        " 이런 작은 변화만으로도 면접 분위기가 크게 달라질 수 있습니다.",
                "지원자의 표정은 차분하고 신중한 인상을 주지만, 면접관이 감정을 읽기 어려울 수도 있습니다." +
                        " 면접에서는 표정을 활용하여 상대방과 대화하는 느낌을 주는 것이 중요합니다." +
                        " 편하게 이야기라는 느낌으로, 미소를 짓는 표정을 지어보는건 어떨까요?" +
                        " 이런 모습은 면접관이 지원자의 태도를 긍정적으로 평가할 수 있도록 돕습니다.",
                "현재의 표정은 차분하고 신뢰감을 주지만, 일부 순간에서는 감정 표현이 부족해 보일 수 있습니다." +
                        " 특히, 답변 과정에서 너무 무표정 하거나 억양에 대한 변화가 없다면" +
                        " 면접관이 지원자가 긴장한 것으로 오해할 수 있습니다." +
                        " 따라서, 중요한 부분에서는 미소를 추가하거나 고개를 살짝 끄덕이는 등의 리액션을 더하는 것이 면접에 긍정적인 영향을 미칠 수 있습니다.",
            )

            neutral >= 60.000 -> listOf(
                "무표정이 지나치게 높으면 면접관이 지원자가 긴장하거나 소극적이라고 느낄 수 있습니다." +
                        " 면접에서는 작은 표정의 변화를 통해 감정과 신뢰감을 전달하는 것이 중요합니다." +
                        " 답변 중에 가볍게 미소를 짓는 연습을 통해 보다 친근한 인상을 줄 수 있습니다." +
                        " 이런 작은 변화가 면접 결과에 큰 영향을 미칠 수 있습니다.",
                "현재 표정은 다소 차가워 보일 수 있으며, 이는 면접관이 지원자의 기분을 오해할 수도 있습니다." +
                        " 지금보다는 좀 더 살짝 미소를 짓거나, 표정을 부드럽게 유지하면서 말하는 것이 효과적입니다." +
                        " 자연스러운 표정 변화는 면접의 분위기를 더욱 긍정적으로 이끌 수 있습니다.",
                "너무 무표정이면 면접관이 지원자의 태도에 대해 불편함을 느낄 수 있습니다." +
                        " 면접에서는 면접관과의 대화를 통해 신뢰를 쌓는 것이 중요합니다." +
                        " 감정 표현을 조금 더 살리면서 면접을 진행하는 것이 면접에서 좋은 결과를 얻는 데 도움이 될 것입니다.",
                "현재 표정이 무표정인 경향이 강합니다. 이는 면접관이 긴장감을 느끼거나, 지원자가 면접에 관심이 없다고 오해할 수도 있습니다." +
                        " 특히, 답변을 마친 후에도 무표정하게 있는 경우 면접관이 다음 질문을 던지는 데 부담을 느낄 수 있습니다." +
                        " 따라서, 답변을 마칠 때 짧은 미소를 짓거나 부드러운 표정을 유지하는 연습을 해보는 것이 좋습니다."
            )

            negative in 40.000..59.9999 -> listOf(
                "부정적인 표정이 많이 감지되고 있습니다. 이는, 면접관이 지원자가 긴장하고 있거나 면접에 대한 부담을 크게 느끼고 있다고 생각할 수 있습니다." +
                        " 특히, 인상을 찌푸리거나 얼굴을 찡그리는 표정은 면접관에게 부정적인 영향을 줄 수 있습니다." +
                        " 질문에 대한 답변을 시작하기 전, 여유롭게 숨을 들이마시고, 표정을 조금 더 부드럽게 유지하는 연습을 하면 더욱 좋은 인상을 줄 수 있습니다.",
                "현재의 표정은 지나치게 경직되어 있을 가능성이 있습니다." +
                        " 면접관은 표정을 통해 지원자의 자신감을 판단하기 때문에, 부정적인 표정이 많으면 면접관이 지원자가 스트레스를 받고 있다고 해석할 수도 있습니다." +
                        " 미소를 너무 크게 짓지 않더라도, 적어도 신뢰감을 주는 부드러운 표정을 유지하면 면접이 더욱 원활하게 진행될 수 있습니다.",
                "부정적인 표정이 많이 감지되고 있으며, 이는 면접관이 지원자의 태도를 부정적으로 평가할 가능성이 있습니다." +
                        " 특히, 고민이 필요한 질문에 답변을 할때, 표정이 경직되어 있거나 반응이 적으면 소극적으로 보일 수도 있습니다." +
                        " 이런 경우라면, 살짝 미소를 유지하거나 간단한 손 제스처 등의 작은 변화를 시도해보세요.",
                "부정적인 표정은 면접관이 지원자의 긴장 상태를 읽을 수 있게 할 수 있습니다." +
                        " 면접에서는 긴장을 풀고, 자연스러운 표정을 유지하는 것이 중요합니다." +
                        " 예를 들어, 면접관의 질문에 답할 때 가벼운 미소나 고개를 끄덕이는 등의 작은 리액션을 추가하면 면접관이 지원자와 소통하는 데 더 편안함을 느낄 수 있습니다." +
                        " 이런 작은 표정 변화가 면접 결과에 긍정적인 영향을 미칠 수 있습니다.",
                "부정적인 표정이 많이 감지되고 있어요. 이는 면접관이 지원자가 긴장하거나 불편함을 느끼고 있다고 생각할 수 있습니다." +
                        " 따라서, 면접에서는 자신감을 표현하는 표정이 중요합니다." +
                        " 예를 들어, 면접관이 질문을 던졌을 때 고개를 끄덕이며 답변을 시작하고, 웃음을 유지하는 것이 좋습니다." +
                        " 이러한 반응이 면접관에게 더 긍정적인 인상을 줄 수 있습니다.",
                "부정적인 표정은 면접에서 지원자가 소극적이거나 긴장하고 있다는 인상을 줄 수 있습니다." +
                        " 면접관은 지원자가 감정을 솔직하게 표현하는지에 주목하기 때문에, 부정적인 표정을 자주 짓지 않도록 주의해야 합니다." +
                        " 긍정적인 감정을 조금 더 자유롭게 표현하고, 미소나 고개를 끄덕이는 작은 변화로 면접관과 대화하는 느낌을 받을 수 있습니다."
            )


            negative >= 60.0000 -> listOf(
                "부정적인 표정이 많이 감지되고 있어요. 이는 면접관이 지원자가 매우 긴장하거나 불안해한다고 느낄 수 있습니다." +
                        " 이러한 인상을 피하기 위해서는 질문을 받을 때 고개를 끄덕이거나 가볍게 미소를 지으며 반응하는 것이 좋습니다." +
                        " 이를 통해 면접관은 지원자가 면접에 대해 적극적으로 참여하고 있다는 인식을 가질 수 있습니다.",
                "지원자의 표정이 부정적인 면이 많이 보입니다. 이는, 면접관이 지원자가 긴장했다는 것을 빠르게 느낄 수 있습니다." +
                        " 면접에서 중요한 것은 표정을 자연스럽게 조절하는 것입니다." +
                        " 긴장한 순간에도 부드러운 표정을 유지하고, 은은한 미소를 잃지 않아보세요." +
                        " 이는 면접관에게 긍정적인 반응을 이끌어 낼 수 있습니다." +
                        " 표정에 약간의 변화를 주는 것이 면접에서 유리할 수 있습니다.",
                "부정적인 표정은 면접에서 소극적인 인상을 줄 수 있습니다." +
                        " 답변을 할때, 표정 변화가 적고 경직되어 있으면 면접관이 불편함을 느낄 수 있습니다." +
                        " 면접에서 자신감을 나타내기 위해서는 작은 표정 변화를 시도해야 합니다." +
                        " 미소를 지으며 답변을 하거나, 간단한 제스쳐 등의 자연스러운 반응을 보여주세요.",
                "현재의 표정은 지나치게 부정적인 인상을 줄 가능성이 있습니다." +
                        " 면접관은 지원자의 표정을 보고 긴장감을 느끼거나, 면접에 대한 흥미가 부족하다고 오해할 수 있습니다." +
                        " 답변을 시작하기 전, 호흡을 가지런히 하고, 자연스러운 표정을 유지하는 연습을 해보세요." +
                        " 감정을 너무 숨기기보다는, 가볍게 긍정적인 표정을 짓는 것이 더 좋은 평가를 받을 수 있습니다.",
                "부정적인 표정이 많으면 면접관이 지원자와의 소통이 어렵다고 느낄 수 있습니다." +
                        " 특히, 자신도 모르게 인상을 찌푸리거나 긴장한 표정을 지을 수 있습니다. 이는 대화의 흐름이 자연스럽지 않게 될 수 있습니다." +
                        " 면접에서는 자신감을 표현하는 것이 중요하기 때문에, 거울을 보며 부드러운 표정을 연습하는 것이 도움이 될 것입니다.",
                "지나치게 부정적인 표정이 지속되면 면접관이 지원자의 태도를 오해할 수 있습니다." +
                        " 면접은 상호작용이 중요한 자리입니다. 의도적으로라도 미소를 살짝 유지하고, 긍정적인 리액션을 해보세요." +
                        " 이는 면접관이 편안함을 느낄 수 있도록 유도할 수 있는 가장 기본이 될겁니다. 또한 이런 사소한 행동이 면접의 결과에 긍정적인 영향을 미칠 수 있습니다."
            )

            else -> listOf("표정 분석 결과를 해석할 수 없습니다. 다시 시도해주세요.")
        }
        val selectedMessage = messages[Random.nextInt(messages.size)]

        val spannable = SpannableString(selectedMessage)

        val boldKeywords = listOf(
            "중요한 질문에 대한 답변이라면, 표정을 조금 더 차분하게 유지",
            "질문의 성격에 맞게 적절한 표정을 변화",
            "질문에 따른 표정의 변화",
            "논리적인 설명이 필요한 질문에서는 조금 더 진중한 표정을 유지",
            "직무와 관련된 중요한 질문에서는 진지한 표정",
            "고민이 필요한 질문을 받을 때는 차분한 표정을 유지",
            "다대다 면접에서는 다른 지원자의 발언에 과도한 미소를 짓지 않도록 주의",

            "밝은 표정보다는 신중하고 집중하는 모습을 보여주는 것",
            "균형 잡힌 표정이 면접의 중요한 포인트",
            "표정의 조절을 통해 면접에서 긍정적인 이미지를 더욱 강화",
            "고민이 필요한 질문일 경우, 밝은 표정보다는 살짝 미소를 줄이고 신중한 태도",
            "직무와 관련된 심도 있는 질문이라면 약간의 미소와 함께 당당한 태도를 보이는 것이 중요",
            "고민이 필요한 질문에 대한 답변일 경우, 차분한 표정을 유지하는 연습",


            "자신이 어필할 경험을 이야기할 때는 자연스러운 미소를 추가",
            "중요하다 생각되는 질문의 답변은 표정을 약간 더 진지하게 하여 자신감을 표현하는 것",
            "중요한 질문에 답할 때는 약간의 미소를 띈 상태로 당당하게 말하는 것이 중요",
            "자신의 성취나 강점을 이야기할 때는 자신감 있는 표정을 유지",
            "면접관이 고개를 끄덕이는 등의 긍정적인 반응을 보일 때 살짝 미소를 짓는 연습",


            "약간의 미소를 띄어 편한 분위기를 가져가는 연습",
            "이해를 도울 수 있는 제스처를 사용",
            "편하게 이야기라는 느낌으로, 미소를 짓는 표정",
            "중요한 부분에서는 미소를 추가하거나 고개를 살짝 끄덕이는 등의 리액션을 더하는 것",

            "답변 중에 가볍게 미소를 짓는 연습",
            "좀 더 살짝 미소를 짓거나, 표정을 부드럽게 유지하면서 말하는 것",
            "감정 표현을 조금 더 살리면서 면접을 진행",
            "답변을 마칠 때 짧은 미소를 짓거나 부드러운 표정을 유지하는 연습",

            "표정을 조금 더 부드럽게 유지하는 연습",
            "신뢰감을 주는 부드러운 표정을 유지",
            "살짝 미소를 유지하거나 간단한 손 제스처 등의 작은 변화",
            "면접관의 질문에 답할 때 가벼운 미소나 고개를 끄덕이는 등의 작은 리액션을 추가",
            "질문을 던졌을 때 고개를 끄덕이며 답변을 시작하고, 웃음을 유지하는 것",
            "미소나 고개를 끄덕이는 작은 변화",
            "질문을 받을 때 고개를 끄덕이거나 가볍게 미소를 지으며 반응",
            "부드러운 표정을 유지하고, 은은한 미소",
            "미소를 지으며 답변을 하거나, 간단한 제스쳐",
            "호흡을 가지런히 하고, 자연스러운 표정을 유지",
            "거울을 보며 부드러운 표정을 연습",
            "의도적으로라도 미소를 살짝 유지하고, 긍정적인 리액션"
        )
        for (keyword in boldKeywords) {
            val startIndex = selectedMessage.indexOf(keyword)
            if (startIndex != -1) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + keyword.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    startIndex,
                    startIndex + keyword.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
