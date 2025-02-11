package com.example.firstproject.ui.ai.eye

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class EyeOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var detections: List<EyeDetection> = emptyList()

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.GREEN
        isAntiAlias = true

    }

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 32f
        isAntiAlias = true

    }

    /**
     * 검출된 영역들을 설정하고 뷰를 갱신합니다.
     */
    fun setDetections(detections: List<EyeDetection>) {
        this.detections = detections
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (detection in detections) {
            // 박스 그리기
            canvas.drawRect(detection.box, boxPaint)
            // 텍스트 표시: expression과 score를 박스의 왼쪽 위에 표시
            val text = "${detection.expression}"
            // 텍스트 위치는 박스의 왼쪽 위에서 약간 위로 올린 위치로 조정
            val x = detection.box.left
            val y = detection.box.top - 10f  // 텍스트 높이에 맞춰 약간 올림
            canvas.drawText(text, x, y, textPaint)
        }
    }
}
