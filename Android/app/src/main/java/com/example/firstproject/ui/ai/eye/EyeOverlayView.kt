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
            // 박스 대신 타원 그리기
            canvas.drawOval(detection.box, boxPaint)

            val text = "${detection.expression}"
            val x = detection.box.left
            val y = detection.box.top - 10f
            canvas.drawText(text, x, y, textPaint)
        }
    }

}
