package com.example.firstproject.ui.ai.eyeimport

import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class EyeCustomPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : PieChartRenderer(chart, animator, viewPortHandler) {

    // 선택된 슬라이스 인덱스 (-1이면 선택된 항목 없음)
    var selectedIndex: Int = -1

    // 선택된 슬라이스와 기본 슬라이스의 텍스트 크기 (픽셀 단위)
    var selectedValueTextSize: Float = 32f
    var defaultValueTextSize: Float = 18f

    override fun drawValues(c: Canvas) {
        val pieData = mChart.data ?: return

        // PieChart는 일반적으로 단일 데이터셋을 사용함
        val dataSet = pieData.getDataSetByIndex(0) as? PieDataSet ?: return

        // 기존의 shouldDrawValues(dataSet) 대신 데이터셋의 drawValues 여부를 체크
        if (!dataSet.isDrawValuesEnabled)
            return

        // mChart.drawAngles: 각 슬라이스의 각도를 담은 배열
        // mChart.rotationAngle: 차트의 시작 회전 각도
        val drawAngles = mChart.drawAngles
        val rotationAngle = mChart.rotationAngle
        val center = mChart.centerCircleBox
        val radius = mChart.radius

        // 슬라이스의 각도를 누적할 변수
        var offset = 0f

        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i)

            // 현재 슬라이스의 중앙 각도 계산
            // 예: rotationAngle + (누적된 각도) + (현재 슬라이스 각도의 절반)
            val angle = rotationAngle + offset + (drawAngles[i] / 2f)
            offset += drawAngles[i]

            // 각도를 라디안 단위로 변환
            val radians = Math.toRadians(angle.toDouble())

            // 텍스트 위치 계산 (반지름의 70% 거리)
            val x = center.x + (radius * 0.7f * Math.cos(radians)).toFloat()
            val y = center.y + (radius * 0.7f * Math.sin(radians)).toFloat()

            // 선택된 슬라이스면 큰 텍스트, 아니면 기본 텍스트 크기로 설정
            mValuePaint.textSize =
                if (i == selectedIndex) selectedValueTextSize else defaultValueTextSize

            // 포매터를 사용하여 텍스트 생성 (예: 소수점 둘째 자리까지)
            val valueText = dataSet.valueFormatter.getFormattedValue(entry.value)
            c.drawText(valueText, x, y, mValuePaint)
        }
    }
}
