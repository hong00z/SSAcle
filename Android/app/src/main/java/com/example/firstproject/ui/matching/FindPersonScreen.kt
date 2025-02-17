package com.example.firstproject.ui.matching

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
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
import com.example.firstproject.ui.home.GradeLabel
import com.example.firstproject.ui.theme.gmarket
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.GradeLabelEnum
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun FindPersonScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTopBar("", onBackPress = {
            // 뒤로 가기
        })

        Image(
            painter = painterResource(R.drawable.img_find_person), null,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.07f)
        )
        Spacer(Modifier.height(36.dp))
        PersonInfoItem()

    }
}

@Composable
private fun PersonInfoItem() {
    val weekList = listOf("월", "화", "목", "토")
    val tagList = listOf("웹 프론트", "알고리즘", "백엔드", "CS 이론")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = Color(0x00FFFFFF), shape = CircleShape)
            ) {

                Image(
                    painter = painterResource(R.drawable.img_default_profile_5),
                    null,
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.Center),
                )
            }
            Spacer(Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradeTag("12기")
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "구미",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 14.sp,
                        color = Color(0x99000000)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "사용자 닉네임",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "참여 중인 스터디: 1개",
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp)
        ) {
            Text(
                text = "희망 스터디 요일 :",
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
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

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "관심 주제 :",
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                fontSize = 15.sp,
            )
            Spacer(Modifier.width(8.dp))
//            val label = TopicTagEnum.fromTitle("알고리즘")
//            StackLabel(stackTitle = label!!.title, tint = colorResource(label.colorId))
//            Spacer(Modifier.width(8.dp))

            tagList.forEach { title ->
                val label = TopicTagEnum.fromTitle(title)
                val color = colorResource(label!!.colorId)
                StackLabel(stackTitle = title, tint = color)
                Spacer(Modifier.width(8.dp))
            }
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
private fun SendRequestButton() {
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
                "초대하기", fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun CompleteRequestButton() {
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
                "초대완료", fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
fun GradeTag(grade: String) {
    val labelColor = GradeLabelEnum.selectColor(grade)
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(22.dp)
            .background(
                color = labelColor,
                RoundedCornerShape(5.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${grade}",
            fontFamily = gmarket,
            fontWeight = FontWeight(400),
            fontSize = 12.sp,
            color = Color.White
        )

    }

}

@Preview(showBackground = true)
@Composable
private fun TEST() {
    FindPersonScreen()
}
