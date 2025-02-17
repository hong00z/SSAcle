package com.example.firstproject.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.ui.theme.gmarket
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.GradeLabelEnum
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun StudyDetailScreen(
    navController: NavController,
    onNavigateToVideo: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box {
            DetailTopBar(
                title = "스터디 상세보기",
                onBackPress = {
                    // 뒤로가기
                    navController.popBackStack()
                }
            )
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tag = TopicTagEnum.fromTitle("CS 이론")
                Text(
                    "스터디 제목 들어감",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    fontSize = 22.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    maxLines = 2
                )
                Spacer(Modifier.weight(1f))

                StackTag(
                    stackTitle = tag!!.title,
                    tint = colorResource(tag.colorId)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("월", "화", "수", "목", "금", "토", "일")

                days.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFB2B2B2)
                    )

                    if (index < 6) {
                        VerticalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFD9D9D9)
                        )

                    }

                }
            }
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 36.dp)
            ) {
                ContentInfoCard()
            }
            Spacer(Modifier.height(36.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                var currentNum = 4
                var totalNum = 6
                TitleText("스터디 구성원")
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${currentNum} / ${totalNum} 명",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    fontSize = 14.sp,
                    color = colorResource(R.color.primary_color)
                )

            }
            Spacer(Modifier.height(24.dp))
            JoinUserProfiles()

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true
                        )
                        .clickable { onNavigateToVideo(studyId)  },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, colorResource(R.color.border_light_color))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_chatting),
                            null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "스터디 채팅방",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            fontSize = 14.5.sp
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true
                        )
                        .clickable { onNavigateToChat(studyId) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, colorResource(R.color.border_light_color))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_mic),
                            null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "실시간 모각공",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            fontSize = 14.5.sp
                        )
                    }

                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleText("공지사항")
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(28.dp)
                        .background(color = Color.Black, shape = RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_plus),
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "글쓰기", fontFamily = pretendard,
                            fontWeight = FontWeight(700),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            NoticeItem()


        }

    }

}

@Composable
private fun DetailTopBar(
    title: String,
    tint: Color = colorResource(R.color.primary_color),
    onBackPress: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp),

        ) {
        IconButton(
            modifier = Modifier
                .padding(start = 4.dp)
                .align(Alignment.CenterStart),
            onClick = {
                onBackPress()
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
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

        IconButton(
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.CenterEnd),
            onClick = {
                // 알림 목록 화면으로

            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mail),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }

    }
}

@Composable
private fun TitleText(title: String) {
    Box {
        Text(
            text = title,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            fontSize = 19.sp,
            letterSpacing = 1.sp
        )
    }
}

// 태그 아이템
@Composable
private fun StackTag(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(28.dp)
            .background(tint, RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            color = Color.White,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 13.5.sp
        )

    }
}

@Composable
private fun ContentInfoCard() {
    Box(
        modifier = Modifier
            .wrapContentSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, colorResource(R.color.border_light_color))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "스터디 설명 등 아주 긴 글이 들어갈 예정...\n들어갈 예정...\n들어갈 예정...\n스터디 설명 등 아주 긴 글이 들어갈 예정...\n" +
                            "들어갈 예정...\n" +
                            "들어갈 예정...",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500)
                )

            }

        }
    }
}

private data class TmpUser(
    val userName: String,
    val imgId: Int,
    val isHost: Boolean
)

@Composable
private fun JoinUserProfiles() {
    val userList = mutableListOf<TmpUser>(
        TmpUser(userName = "사용자1", imgId = R.drawable.img_default_profile, true),
        TmpUser(userName = "닉네임은", imgId = R.drawable.img_default_profile_5, false),
        TmpUser(userName = "닉네임이좀길다", imgId = R.drawable.img_default_profile, false),
        TmpUser(userName = "12기 구미생", imgId = R.drawable.img_default_profile_5, false),
        TmpUser(userName = "11기 싸피생", imgId = R.drawable.img_default_profile_5, false),
        TmpUser(userName = "교육생 화이팅", imgId = R.drawable.img_default_profile_5, false),
        TmpUser(userName = "흠냐륑", imgId = R.drawable.img_default_profile_5, false)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        userList.forEachIndexed { index, User ->
            UserProfileItem(User.imgId, User.userName, User.isHost)

            if (index < userList.size - 1) {
                Spacer(Modifier.width(16.dp))

            }
        }


    }

}

@Composable
private fun UserProfileItem(profileId: Int, userName: String, isHost: Boolean) {
    Column(modifier = Modifier.width(52.dp)) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color = Color(0x00FFFFFF), shape = CircleShape)
        ) {

            Image(
                painter = painterResource(profileId),
                null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
            ) {
                if (isHost) {
                    Image(
                        painter = painterResource(R.drawable.icon_host),
                        null
                    )
                }

            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = userName,
            fontSize = 10.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@Composable
private fun NoticeItem() {
    var isChecked by remember { mutableStateOf(false) }
    var checkCount by remember { mutableStateOf(0) }

    val backgroundColor = if (isChecked) colorResource(R.color.primary_color) else Color.Transparent
    val textColor = if (isChecked) Color.White else Color.Black
    val iconTint = if (isChecked) Color.White else Color.Black
    val buttonText = if (isChecked) "체크 완료" else "체크"

    // 이전 상태를 추적하기 위한 변수
    var previousCheckedState by remember { mutableStateOf(false) }

    LaunchedEffect(isChecked) {
        if (isChecked && !previousCheckedState) {
            checkCount++
        } else if (!isChecked && previousCheckedState) {
            checkCount--
        }
        previousCheckedState = isChecked
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0x00FFFFFF), shape = CircleShape)
            ) {

                Image(
                    painter = painterResource(R.drawable.img_default_profile_5),
                    null,
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center),
                )
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    if (true) {
                        Image(
                            painter = painterResource(R.drawable.icon_host),
                            null
                        )
                    }

                }
            }
            Spacer(Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradeLabel(12)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "구미",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 13.sp,
                        color = Color(0x99000000)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "사용자 닉네임",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(
            text = "공지 제목",
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "공지 제목",
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            fontSize = 14.sp,
            color = Color(0x99000000),
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .fillMaxWidth()
        )


        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
                    .border(
                        1.5.dp, color = colorResource(R.color.primary_color),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    //                modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    //                horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.img_check), null,
                        tint = colorResource(R.color.primary_color),
                        modifier = Modifier
                            .size(11.5.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${checkCount}",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 12.5.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE4E4E4))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(backgroundColor)
                .clickable { isChecked = !isChecked },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = buttonText,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                fontSize = 13.sp,
                color = textColor
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_check_bold), null,
                modifier = Modifier
                    .size(12.dp),
                tint = iconTint,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE4E4E4))
        Spacer(Modifier.height(20.dp))


    }
}

@Composable
fun GradeLabel(grade: Int) {
    val labelColor = GradeLabelEnum.selectColor(grade)
    Box(
        modifier = Modifier
            .size(23.dp)
            .background(
                color = labelColor,
                RoundedCornerShape(5.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${grade}기",
            fontFamily = gmarket,
            fontWeight = FontWeight(400),
            fontSize = 9.sp,
            color = Color.White
        )

    }

}

@Preview(showBackground = true)
@Composable
fun PreViewDetailScreen() {

}
