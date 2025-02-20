package com.example.firstproject.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.StudyDTO
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun StudyListCard(openStudyList: List<StudyDTO>, navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            openStudyList.take(4).forEachIndexed { index, study ->

                StudyItem(title = study.studyName, topic = study.topic,
                    modifier = Modifier.clickable {
                        // 클릭하면 스터디 상세정보화면으로
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("studyId", study.studyId)
                        Log.d("홈 목록에서 누름", "스터디아이디: ${study.studyId}")

                        navController.navigate("studyDetailScreen")
                    },
                    study = study
                )
                Spacer(Modifier.height(20.dp))
            }

        }
    }
}

@Composable
private fun StudyItem(title: String, topic: String, modifier: Modifier, study: StudyDTO) {
    val tag = TopicTagEnum.fromTitle(topic)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListStackTag(
            stackTitle = tag!!.title,
            tint = colorResource(tag.colorId)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            title,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 16.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "${study.memberCount} / ${study.count}명",
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 15.sp,
        )
        Spacer(Modifier.width(12.dp))
    }

}

@Composable
private fun ListStackTag(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(24.dp)
            .background(tint, RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            color = Color.White,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 11.2.sp
        )

    }
}
