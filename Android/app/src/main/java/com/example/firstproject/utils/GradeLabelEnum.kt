package com.example.firstproject.utils

import androidx.compose.ui.graphics.Color

enum class GradeLabelEnum(val grade: String, val color: Color) {
    BLUE("11기", Color(0xFF39BCF0)),
    YELLOW("12기", Color(0xFFFFCE2D)),
    ORANGE("13기", Color(0xFFF2871C)),
    GREEN("14기", Color(0xFF34EBC6)),
    PURPLE("10기", Color(0xFFB086FA));

    companion object {
        fun selectColor(grade: String): Color {
            return entries.find { it.grade == grade }?.color ?: Color.White
        }
    }
}