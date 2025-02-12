package com.example.firstproject.ui.ai.eye

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEyeResultBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import kotlin.random.Random
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.style.ForegroundColorSpan
import androidx.core.content.FileProvider
import com.example.firstproject.ui.ai.eyeimport.CustomPieChartRenderer
import com.example.firstproject.utils.CommonUtils
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

        binding.tvEyePositiveValue.apply {
            val trueValueFormatted = "%.1f".format(trueValue) // trueValue를 문자열로 변환
            text = "집중 $trueValueFormatted%"

            val spannable = SpannableString(text)
            val startIndex = text.indexOf(trueValueFormatted) // trueValue의 시작 위치
            val endIndex = startIndex + trueValueFormatted.length // trueValue의 끝 위치

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            ) // trueValue 부분만 Bold

            text = spannable
        }

        // 비율
        binding.tvEyeNegativeValue.apply {
            val FalseValueFormatted = "%.1f".format(falseValue)

            text = "흔들림 $FalseValueFormatted%"
            val spannable = SpannableString(text)

            val startIndex = text.indexOf(FalseValueFormatted)
            val endIndex = startIndex + FalseValueFormatted.length

            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannable
        }

        // 이게 피드백
        binding.tvEyeFeedbackContent.apply {

            text = randomMessage(trueValue)
        }

        setupDonutChart(trueValue, falseValue)

        binding.btnEyeEyeSaveResult.setOnClickListener {
            saveAndSharePdf()
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

        val dataSet = PieDataSet(entries, "").apply {  // 레이블 제거 (빈 문자열)
            // 색상 세팅 (원하는 색 지정 가능)
            colors = listOf(
                resources.getColor(R.color.chart_blue, null),
                resources.getColor(R.color.chart_red, null),
            )
            // 차트 가운데 구멍이 보이는 퍼센트 (두께 설정)
            sliceSpace = 2f            // 파이 조각 사이 간격
            valueTextSize = 14f        // 차트 위의 값 표시 (크기 설정)
            valueTextColor = resources.getColor(R.color.white, null) // 값 색상 흰색
        }

        val data = PieData(dataSet).apply {
            // 값 포맷을 설정하여 % 제거, 소수점 없이 정수만 표시
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f", value)  // 정수만 표시 (퍼센트 없음)
                }
            })
        }

        binding.eyedonutChart.apply {
            this.data = data
            description.isEnabled = false    // 차트 우하단 설명 끔
            isDrawHoleEnabled = true         // 도넛 모양 (가운데 구멍)
            holeRadius = 40f                // 도넛 구멍 반경 (%)
            setTransparentCircleAlpha(0)     // 투명한 원 제거
            setEntryLabelColor(Color.TRANSPARENT) // 차트 내부 라벨 숨김
            setEntryLabelTextSize(0f)       // 차트 내부 라벨 텍스트 크기 0으로 설정


            extraBottomOffset = -10f  // 차트와 범례 사이 간격 줄이기

            // 차트 애니메이션 (0.5초)
            animateY(500)
            // 감정 비율 레전드(범례) 설정 (차트 아래 오른쪽 정렬)
//            legend.apply {
//                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT // 오른쪽 정렬
//                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM   // 차트 아래 배치
//                orientation = Legend.LegendOrientation.HORIZONTAL            // 가로 정렬
//                textSize = 12f
//                textColor = resources.getColor(R.color.black, null)
//            }
            val customRenderer = CustomPieChartRenderer(
                this,
                this.animator,
                this.viewPortHandler
            ).apply {
                defaultValueTextSize = 32f
                selectedValueTextSize = 60f
            }
            renderer = customRenderer
            binding.eyedonutChart.legend.isEnabled = false

            // 슬라이스 선택 시 customRenderer의 selectedIndex 업데이트
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    customRenderer.selectedIndex = h?.x?.toInt() ?: -1
                    val selectedIndex =  h?.x?.toInt() ?: -1
                    when (selectedIndex) {
                        0 -> { // "집중" 영역 선택 시
                            binding.tvLegendFocus.apply {
                                setTypeface(null, Typeface.BOLD)
                                textSize = 16f
                            }
                            binding.tvLegendWaver.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }
                        1 -> { // "흔들림" 영역 선택 시
                            binding.tvLegendFocus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendWaver.apply {
                                setTypeface(null, Typeface.BOLD)
                                textSize = 16f
                            }
                        }
                        else -> {
                            // 기타: 기본 스타일로 복원
                            binding.tvLegendFocus.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                            binding.tvLegendWaver.apply {
                                setTypeface(null, Typeface.NORMAL)
                                textSize = 14f
                            }
                        }
                    }
                    invalidate() // 차트 다시 그리기
                }

                override fun onNothingSelected() {
                    customRenderer.selectedIndex = -1
                    binding.tvLegendFocus.apply {
                        setTypeface(null, Typeface.NORMAL)
                        textSize = 14f
                    }
                    binding.tvLegendWaver.apply {
                        setTypeface(null, Typeface.NORMAL)
                        textSize = 14f
                    }
                    invalidate()
                }
            })
            // 차트 갱신
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun randomMessage(score: Float): SpannableString {
        val messages = when (score) {
            in 0.00000..39.99999 -> listOf(
                "시선을 정면에 유지하면 더욱 자신감 있는 인상을 줄 수 있습니다." +
                        "다수의 면접관이 있는 경우에는 면접관과 자연스럽게 눈을 맞추며 답변하는 것이 좋습니다." +
                        " 하지만 특별한 이유 없이 눈동자가 많이 움직이면 불안해하는 듯한 느낌을 줄 수 있으니 주의하세요." +
                        " 정면을 바라보는 것은 신뢰감을 주는 요소입니다." +
                        " 면접관과 눈을 마주치는 시간이 부족하면 소극적인 인상을 줄 수 있으므로 시선 집중 연습을 해보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접에서 시선이 분산되는 경향이 있습니다." +
                        " 면접관과 눈을 맞추는 것은 자신감을 보여주고, 신뢰감을 줄 수 있는 중요한 요소입니다." +
                        " 답변할 때 카메라나 면접관을 의식적으로 바라보는 연습을 해보세요." +
                        " 특히, 중요한 질문에서 시선을 유지하면 더 신뢰감 있는 인상을 남길 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선이 자주 흔들리면 불안해 보일 수 있습니다." +
                        " 너무 긴장하거나 생각하느라 시선을 회피하는 습관이 있을 수 있는데, 의식적으로 정면을 응시하는 연습을 해보세요." +
                        " 면접관의 눈을 바라보기가 부담스럽다면 이마나 코 주변을 응시하는 것도 도움이 됩니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접에서는 눈 맞춤이 중요한데, 현재 시선 감지 비율이 낮아 면접관이 소통이 원활하지 않다고 느낄 수 있습니다." +
                        " 면접관과 자연스럽게 눈을 맞추는 연습을 하면 대화에 집중하는 모습이 강조되고, 더욱 적극적인 태도를 보일 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접관과의 시선 교환이 부족하면 소극적인 인상을 줄 수 있습니다." +
                        " 답변을 할 때, 면접관을 향해 의식적으로 시선을 두려는 노력이 필요합니다." +
                        " 정면 응시 연습을 거울이나 카메라를 보며 해보는 것도 좋은 방법입니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선이 자주 분산되면 면접관이 당신의 태도나 집중력에 의문을 가질 수도 있습니다." +
                        " 질문을 듣거나 답변할 때, 짧게라도 정면을 바라보는 습관을 들여보세요." +
                        " 자신감 있는 인상을 위해 의식적으로 연습하는 것이 중요합니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선이 낮거나 자주 돌아가면 면접관이 적극적인 자세를 느끼기 어려울 수 있습니다." +
                        " 눈을 마주치는것이 부담스럽다면, " +
                        " 상대방의 얼굴 중 이마나 코 부분을 바라보는 것도 좋은 방법이니," +
                        " 이를 활용하여 시선 고정을 점진적으로 연습해 보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!"
            )


            in 40.00000..59.99999 -> listOf(
                " 정면을 응시하는 것은 면접관에게 자신감 있는 인상을 줍니다." +
                        " '나는 준비된 사람입니다!'라는 메시지를 전달할 수 있도록, 눈을 마주치는 것을 두려워하지 마세요." +
                        " 거울을 보며 자연스럽게 면접관과 눈을 맞추는 연습을 해보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 어느 정도 면접관을 바라보고 있지만, 여전히 시선이 분산되는 경향이 있습니다." +
                        " 답변을 생각하는 과정에서 시선을 돌리는 것이 자연스럽긴 하지만, 너무 자주 그러면 자신감이 없어 보일 수도 있습니다." +
                        " 중요한 키워드를 말할 때는 정면을 응시하여 신뢰감을 높이는 연습을 해보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선을 유지하려는 노력은 보이지만, 아직은 부족한 부분이 있습니다." +
                        " 답변을 할 때 한 문장씩 끊어서 정면을 바라보며 말하는 연습을 해보면, 보다 안정적인 태도를 보일 수 있습니다." +
                        " 또한, 본인의 말에 확신이 있음을 전달할 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 현재 시선이 어느 정도 면접관에게 향하고 있지만, 좀 더 적극적인 시선 처리가 필요합니다." +
                        " 답변할 때 상대방을 바라보며 이야기하는 습관을 들이면 더 강한 인상을 남길 수 있습니다." +
                        " 자연스러운 대화를 위해 면접 전에 미리 질문을 준비하고, 연습하며 시선 고정을 연습해보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 기본적인 시선 유지가 이루어지고 있지만, 가끔 시선이 흔들려 집중력이 부족해 보일 수 있습니다." +
                        " 답변할 때는 문장 끝부분에서 한 번 더 면접관을 바라보며 마무리하면 더욱 안정적인 느낌을 줄 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 자연스럽게 면접관을 바라보려고 하지만, 아직은 시선이 불안정한 모습이 있습니다." +
                        " 답변할 때, 특정 키워드를 강조할 순간에는 정면 응시를 유지하면 더욱 설득력 있는 태도를 만들 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선이 비교적 분산되지 않고 있지만, 완전히 안정적인 느낌은 아닙니다." +
                        " 중요한 질문일수록 면접관과 눈을 맞추는 것이 도움이 될 수 있으므로," +
                        " 사전 연습을 통해 자연스럽게 응시하는 연습을 해보는 것이 좋습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!"
            )

            in 60.00000..79.99999 -> listOf(
                " 전반적으로 좋은 시선 처리를 보이고 있습니다." +
                        " 다만, 대답을 고민하는 순간에 시선이 자주 흔들릴 가능성이 있습니다." +
                        " 자연스럽게 면접관과 눈을 맞추되," +
                        " 생각하는 시간을 가질 때는 잠시 시선을 돌리는 정도로 조절하면 더욱 자연스러운 흐름이 만들어질 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선을 유지하는 능력이 좋습니다." +
                        " 추가적으로, 적절한 타이밍에 가벼운 시선 이동을 활용하면 더욱 자연스럽고 부드러운 인상을 줄 수 있습니다." +
                        " 눈 깜빡임이 너무 적다면 오히려 어색해 보일 수 있으므로, 편안한 표정도 함께 신경 써보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접관과의 시선 교환이 자연스럽게 이루어지고 있습니다." +
                        " 다만, 질문에 대한 답변을 준비할 때 마다 시선을 피하는 것이 반복된다면," +
                        " 더 확신을 가지고 응답하는 연습이 필요할 수 있습니다. 대화를 이어가며 리액션을 함께 활용하면 더욱 좋은 인상을 남길 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 좋은 시선 유지 습관을 갖고 있지만, 가끔 시선이 흔들리는 순간이 있습니다." +
                        " 긴 답변을 할 때는, 다른 면접관도 바라보는 습관을 들이면 더욱 집중력 있는 태도를 보일 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접에서 중요한 것은 자연스러운 소통입니다." +
                        " 현재 시선 유지가 비교적 좋은 편이지만," +
                        " 필요할 때 가볍게 고개를 끄덕이며 리액션을 추가하면 더욱 적극적이고 편안한 인상을 줄 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!"
            )

            in 80.0000..89.99999 -> listOf(
                "통상적으로 시선처리를 잘하고 있다고 할 수 있습니다." +
                        " 하지만 다대다, 다대일 면접인 경우 다수의 면접관과 시선을 맞추는 연습이 필요할 수 있습니다." +
                        " 주어진 모든 상황에 자연스럽게 대응할 수 있는 멋진 면접자가 되길 기원합니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "면접에서 매우 좋은 시선 처리를 보이고 있습니다." +
                        " 면접관과 자연스럽게 눈을 맞추면서 답변하는 모습은 자신감을 보여주며, 신뢰를 형성하는 데 도움이 됩니다." +
                        " 다만, 너무 정면을 응시하려고 의식하다 보면 긴장감이 느껴질 수 있으니, 가끔 자연스럽게 시선을 이동하는 것도 좋습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "훌륭한 시선 유지 능력을 갖추고 있습니다." +
                        " 시선을 적극적으로 활용하면서 면접관과 소통하는 모습이 긍정적으로 작용할 것입니다." +
                        " 하지만 면접관에 따라 부담스럽다고 느낄 수도 있으니, 적절한 리액션과 표정 변화를 함께 사용하면 더 좋은 효과를 볼 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "시선 처리가 안정적이라 신뢰감을 주는 답변 태도를 보이고 있습니다." +
                        " 다만, 너무 정적인 느낌을 줄 수도 있어요." +
                        " 가끔 시선을 부드럽게 이동하거나 고개를 끄덕이는 등의 제스처를 더하는건 어떨까요?" +
                        " 더욱 자연스러운 분위기를 연출할 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "면접관과의 아이 컨택이 원활하게 이루어지고 있어 신뢰감을 줄 수 있습니다." +
                        " 하지만 너무 응시에 집중하다 보면 표정이 굳어 보일 수 있으니," +
                        " 적절한 미소와 자연스러운 표정 변화를 함께 연습하면 더욱 효과적입니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "시선이 매우 안정적이어서 자신감 있는 모습을 보일 수 있습니다." +
                        " 다만, 너무 오랫동안 정면만 응시하면 면접관이 부담을 느낄 수 있으니," +
                        " 적절한 순간에 가볍게 시선을 움직이며 자연스러운 흐름을 유지하는 것도 중요합니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                "시선 유지가 좋지만, 너무 정적이면 감정이 부족해 보일 수 있습니다." +
                        " 대화의 흐름을 살리기 위해 리액션을 조금 더 활용하고," +
                        " 눈빛과 표정으로 감정을 전달하면 더욱 긍정적인 인상을 남길 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!"
            )

            else -> listOf(
                " 지나치게 정면만 응시하면 면접관에게 부담감을 줄 수 있습니다." +
                        " 답변을 생각할 때는 시선을 잠시 다른 곳에 두어 자연스러운 모습을 연출하는 것이 중요합니다." +
                        " 면접관과 자연스럽게 시선을 교환하는 연습을 해보세요." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접관을 꾸준히 바라보는 점이 매우 인상적입니다." +
                        " 다만, 시선이 너무 고정되면 부담스럽게 느껴질 수도 있으므로," +
                        " 자연스러운 표정 변화와 가벼운 시선 이동을 활용하면 더욱 부드럽고 친근한 인상을 줄 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 시선 집중도가 매우 높아, 강한 자신감을 보여줄 수 있는 장점이 있습니다." +
                        " 하지만 어느 순간에도 응시를 하고 있으면 면접관이 부담을 느낄 수도 있습니다." +
                        " 적절한 순간에 짧게 시선을 돌렸다가 다시 맞추는 리듬을 연습하면," +
                        " 더욱 자연스럽고 조화로운 면접 태도를 유지할 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 훌륭한 시선 집중력을 보이고 있습니다." +
                        " 그러나 지나치게 응시를 고집하면 다소 긴장한 느낌을 줄 수 있으므로," +
                        " 가끔은 자연스럽게 시선을 이동하며 답변의 흐름을 조절하는 것이 좋습니다." +
                        " 자연스러운 표정 변화와 리액션을 곁들이면 더욱 좋은 인상을 남길 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접관을 응시하는 태도가 매우 훌륭합니다." +
                        " 하지만 지나치게 응시하는 것이 부담을 줄 수도 있으므로," +
                        " 가끔씩 짧은 순간 시선을 이동하면서 자연스러운 분위기를 연출하는 것이 중요합니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 현재 시선 유지력이 강점이지만, 과한 눈맞춤은 오히려 긴장한 인상을 줄 수도 있습니다." +
                        " 면접 중 자연스럽게 표정을 바꾸고, 적절한 제스처를 추가하면 부드러운 분위기를 조성할 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!",
                " 면접관과의 시선 교환이 매우 안정적이지만, 간혹 면접관이 부담을 느낄 수도 있습니다." +
                        " 너무 과하게 시선을 맞추려 하기보다는" +
                        " 자연스럽게 흐름을 유지하며 부드러운 미소와 함께 응시하는 것이 더욱 좋은 인상을 줄 수 있습니다." +
                        " \n면접 준비에 도움이 되시길 바랍니다." +
                        " 파이팅!"
            )
        }
        val selectedMessage = messages[Random.nextInt(messages.size)]

        val spannable = SpannableString(selectedMessage)

        val boldKeywords = listOf(
            "면접관과 자연스럽게 눈을 맞추며 답변",
            "정면을 바라보는 것은 신뢰감을 주는 요소",
            "시선이 분산되는 경향",
            "카메라나 면접관을 의식적으로 바라보는 연습",
            "의식적으로 정면을 응시하는 연습",
            "면접관과 자연스럽게 눈을 맞추는 연습",
            "면접관을 향해 의식적으로 시선을 두려는 노력이 필요",
            " 정면을 바라보는 습관을 들여보세요",
            "상대방의 얼굴 중 이마나 코 부분을 바라보는 것도 좋은 방법",
            "눈을 마주치는 것을 두려워하지 마세요",
            "중요한 키워드를 말할 때는 정면을 응시",
            "한 문장씩 끊어서 정면을 바라보며 말하는 연습",
            " 좀 더 적극적인 시선 처리가 필요",
            "면접 전에 미리 질문을 준비",
            "문장 끝부분에서 한 번 더 면접관을 바라보며 마무리",
            "특정 키워드를 강조할 순간에는 정면 응시를 유지",
            "중요한 질문일수록 면접관과 눈을 맞추는 것이 도움",
            "생각하는 시간을 가질 때는 잠시 시선을 돌리는 정도로 조절",
            "눈 깜빡임이 너무 적다면 오히려 어색해 보일 수 있으므로, 편안한 표정",
            "대화를 이어가며 리액션을 함께 활용",
            "긴 답변을 할 때는, 다른 면접관도 바라보는 습관",
            "필요할 때 가볍게 고개를 끄덕이며 리액션을 추가",
            "다대일 면접인 경우 다수의 면접관과 시선을 맞추는 연습이 필요",
            "자연스럽게 시선을 이동하는 것도 좋습니다",
            " 적절한 리액션과 표정 변화를 함께 사용",
            "시선을 부드럽게 이동하거나 고개를 끄덕이는 등의 제스처를 더하는건",
            "적절한 미소와 자연스러운 표정 변화를 함께 연습",
            "자연스러운 흐름을 유지하는 것도 중요",
            "리액션을 조금 더 활용",
            "눈빛과 표정으로 감정을 전달",
            "답변을 생각할 때는 시선을 잠시 다른 곳에 두어 자연스러운 모습을 연출하는 것이 중요",
            "자연스러운 표정 변화와 가벼운 시선 이동을 활용",
            "짧게 시선을 돌렸다가 다시 맞추는 리듬을 연습",
            "응시를 고집하면 다소 긴장한 느낌",
            "자연스러운 표정 변화와 리액션을 곁들이면",
            "짧은 순간 시선을 이동",
            "과한 눈맞춤은 오히려 긴장한 인상",
            "자연스럽게 표정을 바꾸고, 적절한 제스처를 추가",
            "부드러운 미소와 함께 응시하는 것이 더욱 좋은 인상"
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


    private fun saveAndSharePdf() {
        // 1) PDF로 만들고 싶은 부모 레이아웃(혹은 스크롤 전체 Layout)을 잡는다.
        val targetView = binding.pdfCardView

        // 2) 레이아웃을 비트맵으로 변환
        val bitmap = getBitmapFromView(targetView)

        // 3) 비트맵을 PDF 문서로 변환 후 저장
        val pdfFile = createPdfFromBitmap(bitmap)

        // 4) 저장된 PDF를 카카오톡으로 공유
        sharePdfFile(pdfFile)
    }

    /**
     * View -> Bitmap추출
     */
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


    private fun createPdfFromBitmap(bitmap: Bitmap): File {
        val pdfDirPath = requireContext().getExternalFilesDir(null)?.absolutePath
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val pdfFile = File(pdfDirPath, "eye_result_feedBack_$timestamp.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()

        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        // 비트맵을 (0, 0)에 그려줍니다.
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        try {
            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            document.close()
        }

        return pdfFile
    }

    private fun sharePdfFile(pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 여러곳에 공유 가능
//        shareIntent.setPackage("com.kakao.talk")
        startActivity(Intent.createChooser(shareIntent, "공유하기"))
    }

}