package com.example.firstproject.ui.live

import android.graphics.RectF

data class HumanDetection(
    val classId: Int,
    val score: Float,
    val box: RectF
)