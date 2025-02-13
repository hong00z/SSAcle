package com.example.firstproject.ui.matching

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.firstproject.ui.home.JoinProfiles
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun FindStudyScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTopBar("", onBackPress = { })

        Image(painter = painterResource(R.drawable.img_find_study), null)
        Spacer(Modifier.height(36.dp))
        StudyInfoItem()

    }

}

@Composable
fun StackLabel(stackTitle: String, tint: Color) {
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
            fontSize = 11.sp
        )

    }
}

@Composable
private fun StudyInfoItem() {
    val weekList = listOf("월", "화", "목", "토")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            val label = TopicTagEnum.fromTitle("프론트엔드")
            StackLabel(stackTitle = label!!.title, tint = colorResource(label.colorId))
            Spacer(Modifier.width(16.dp))
            Text(
                text = "이문경의 강철 스터디",
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 20.sp,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp)
        ) {
            Text(
                text = "스터디 요일 :",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = weekList.joinToString(" "),
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
                letterSpacing = 2.sp,
                color = Color(0xFF1181F0)
            )
        }
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            JoinProfiles(6) // 스터디 참여중인 사람 수 넣기

            Spacer(Modifier.width(12.dp))
            Text(
                text = "인원 :  ",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
            )
            Text(
                text = "4 / 6명",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
            )

        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            SendRequestButton()
        }
        Spacer(Modifier.height(14.dp))
        Divider(color = Color(0xFF949494))
    }
}

@Composable
fun SendRequestButton() {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(28.dp)
            .background(color = Color.Black, shape = RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_add_plus),
                null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "신청하기", fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
fun CompleteRequestButton() {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(28.dp)
            .background(color = Color(0xFF15CD6C), shape = RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "신청완료", fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TEST() {
    FindStudyScreen()
}