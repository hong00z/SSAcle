package com.example.firstproject.ui.ai.face

import android.graphics.RectF

data class FaceExpressionDetection(
    val expression: String,
    val score: Float,
    val box: RectF
)

