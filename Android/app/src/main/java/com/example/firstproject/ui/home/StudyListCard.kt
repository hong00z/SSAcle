package com.example.firstproject.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun StudyListCard() {
    val openStudyList: MutableList<List<String>> = mutableListOf(
        listOf("스터디 제목입니다", "인공지능"),
        listOf("스프링 입문 스터디", "백엔드"),
        listOf("알고리즘 스터디", "프론트엔드"),
        listOf("모바일 개발 스터디", "모바일")

    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(horizontal = 8.dp)
    ) {
        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight(),
            shape = RoundedCornerShape(5.dp),
//            border = BorderStroke(1.dp, color = colorResource(id = R.color.primary_color)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                openStudyList.take(3).forEachIndexed { index, study ->
                    Log.d("에러 추적", study[1])
                    StudyItem(title = study.first(), topic = study[1])

                    if (index < 2) {
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colorResource(R.color.border_light_color),
                            thickness = 1.dp
                        )
                    }
                }

            }

        }
    }
}

@Composable
private fun StudyItem(title: String, topic: String) {
    val tag = TopicTagEnum.fromTitle(topic)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListStackTag(
            stackTitle = tag!!.title,
            tint = colorResource(tag.colorId)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 14.sp
        )



    }

}

@Composable
fun ListStackTag(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(20.dp)
            .background(tint, RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            color = Color.White,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 10.sp
        )

    }
}


@Preview(showBackground = true)
@Composable
fun PreivewStudy() {
//    ListStackTag("프론트엔드", colorResource(R.color.primary_color))
    StudyListCard()
}