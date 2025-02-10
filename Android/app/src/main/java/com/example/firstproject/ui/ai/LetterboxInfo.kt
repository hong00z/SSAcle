package com.example.firstproject.ui.ai

import android.graphics.Bitmap

data class LetterboxInfo(
    val bitmap: Bitmap,  // 640×640 letterbox 이미지
    val scale: Float,    // 원본→letterbox 축소 비율
    val padLeft: Float,  // letterbox 내 왼쪽 패딩
    val padTop: Float    // letterbox 내 상단 패딩
)