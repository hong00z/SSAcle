package com.example.firstproject.ui.ai.eye

import android.graphics.RectF

data class EyeDetection(
    val expression: String,
    val score: Float,
    val box: RectF
) {
    // 나중에 계산 최적화 할때 쓸거에요.
    val cached: Float = box.width() * box.height()
}
