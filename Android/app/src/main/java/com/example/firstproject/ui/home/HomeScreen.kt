package com.example.firstproject.ui.home

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.ui.common.CommonTopBar
import com.example.firstproject.ui.theme.pretendard

@Composable
fun HomeScreen() {

    Row(Modifier.fillMaxSize()) {
        TopBarMain()

        Row(Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
            Spacer(Modifier.height(20.dp))
            TitleTextView("내 스터디 목록")

        }

    }
}


@Composable
fun TopBarMain(
    tint: Color = colorResource(R.color.primary_color)
) {

    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp),
            text = "SSAcle",
            fontFamily = pretendard,
            fontSize = 28.sp,
            fontWeight = FontWeight(700),
            color = colorResource(R.color.primary_color)
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            onClick = {}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TitleTextView(title: String) {
    Text(
        text = title,
        fontFamily = pretendard,
        fontWeight = FontWeight(600),
        fontSize = 24.sp
    )
}

@Preview(showBackground = true)
@Composable
fun TestPreview() {
    TopBarMain()

}