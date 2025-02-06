package com.example.firstproject.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard

@Composable
fun HomeScreen() {

    var studyList = mutableListOf("첫 번쨰 스터디", "스프링 입문", "스터디 제목은 과연 몇 글자까지 가능할까요?")

    Column(Modifier.fillMaxSize()) {
        TopBarMain()

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            TitleTextView("내 스터디 목록")
            Spacer(Modifier.height(20.dp))

            MyStudyItem(studyList)

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

@Composable
fun MyStudyItem(
    // 스터디 리스트
    studyList: List<String>
) {
    val pagerState = rememberPagerState(initialPage = 0) {
        // 크기
        3
    }
    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이템 슬라이드
        Box(
            modifier = Modifier
                .wrapContentSize()

        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.wrapContentSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 28.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, colorResource(R.color.primary_color)),
                    elevation = CardDefaults.elevatedCardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = studyList[it])
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        PageIndicator(
            pageCount = 3,
            currentPage = pagerState.currentPage
        )
    }

}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row (
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) {
            IndicatorDots(isSelected = it == currentPage)
        }
    }
}

@Composable
private fun IndicatorDots(isSelected: Boolean) {
    val dotSize = animateDpAsState(targetValue = if (isSelected) 12.dp else 10.dp, label = "")
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(dotSize.value)
            .clip(CircleShape)
            .background(
                if (isSelected) colorResource(R.color.primary_color)
                else Color(0xFFD9D9D9)
            )
    )
}


@Preview(showBackground = true)
@Composable
fun TestPreview() {
    HomeScreen()
}