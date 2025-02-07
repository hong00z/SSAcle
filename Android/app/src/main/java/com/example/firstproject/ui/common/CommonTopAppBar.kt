package com.example.firstproject.ui.common


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard

@Composable
fun CommonTopBar(
    title: String,
    tint: Color = colorResource(R.color.primary_color),
    onBackPress: (() -> Unit)?
) {
    Box (
        Modifier
            .fillMaxWidth()
            .height(48.dp),

        ) {
        IconButton(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterStart),
            onClick = {
                onBackPress
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title,
            fontFamily = pretendard,
            fontSize = 20.sp,
            fontWeight = FontWeight(700),
            color = tint

        )
    }
}