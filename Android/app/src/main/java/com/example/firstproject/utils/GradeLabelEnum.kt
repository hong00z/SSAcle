package com.example.firstproject.utils

import androidx.compose.ui.graphics.Color

enum class GradeLabelEnum(val grade: Int, val color: Color) {
    BLUE(1, Color(0xFF39BCF0)),
    YELLOW(2, Color(0xFFFFCE2D)),
    ORANGE(3, Color(0xFFF2871C)),
    GREEN(4, Color(0xFF34EBC6)),
    PURPLE(0, Color(0xFFB086FA));

    companion object {
        fun selectColor(number: Int): Color {
            return entries.first() { it.grade == number % 5}.color
        }
    }
}