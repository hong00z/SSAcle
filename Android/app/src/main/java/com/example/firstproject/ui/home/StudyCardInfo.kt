package com.example.firstproject.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StudyCardInfo(studyTitle: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = studyTitle)
    }
}