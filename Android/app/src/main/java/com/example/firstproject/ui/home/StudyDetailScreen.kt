package com.example.firstproject.ui.home

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.firstproject.MyApplication
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.request.AuthRequestDTO
import com.example.firstproject.data.model.dto.request.SendJoinRequestDTO
import com.example.firstproject.data.model.dto.response.Feed
import com.example.firstproject.data.model.dto.response.Member
import com.example.firstproject.data.model.dto.response.MyJoinedStudyListDtoItem
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.ui.matching.FindViewModel
import com.example.firstproject.ui.theme.gmarket
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.CommonUtils
import com.example.firstproject.utils.GradeLabelEnum
import com.example.firstproject.utils.TopicTagEnum
import com.example.firstproject.utils.skeletonComponent
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.delay

@Composable
fun StudyDetailScreen(
    navController: NavController,
//    id: String,
    studyDetailViewModel: StudyDetailViewModel = viewModel(),
    findViewModel: FindViewModel = viewModel(),
    onNavigateToVideo: (String, String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNotificationClick: (String) -> Unit
) {
    val context = LocalContext.current

    val getStudyInfo by studyDetailViewModel.studyDetailResult.collectAsStateWithLifecycle()
    val studyInfo = (getStudyInfo as? RequestResult.Success)?.data


    // 이전 BackStackEntry의 savedStateHandle에서 'studyItem'을 가져옴
    val studyId = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("studyId")

    Log.d("상세 화면", "스터디 아이디${studyId}")


    var studyName: String? = studyInfo?.studyName
    var studyTopic: String? = studyInfo?.topic
    var meetingDays: List<String>? = studyInfo?.meetingDays
    var content: String? = studyInfo?.studyContent
    var currentCount: Int? = studyInfo?.memberCont
    var totalCount: Int? = studyInfo?.count
    var membersList: List<Member>? = studyInfo?.members
    var feedsList: List<Feed>? = studyInfo?.feeds

    val isJoining = remember { mutableStateOf(false) }
    val isHostMember = remember { mutableStateOf(false) }

    var isInviteSent by remember { mutableStateOf(false) }

    LaunchedEffect(studyId) {
        studyId?.let {
            // 예: ViewModel 호출
            studyDetailViewModel.getStudyDetailInfo(it)
            Log.d("스터디 상세화면으로 옴", "${studyId}")
//            studyDetailViewModel.getStudyDetailInfo(studyId)

            studyName = studyInfo?.studyName
            studyInfo?.topic
            studyInfo?.meetingDays
            studyInfo?.studyContent
            studyInfo?.memberCont
            studyInfo?.count
            studyInfo?.members
            studyInfo?.feeds
        }
    }

    var isLoadingState by remember { mutableStateOf(false) }
    LaunchedEffect(getStudyInfo) {
        delay(700)
        if (getStudyInfo.isSuccess()) {
            isLoadingState = true
            Log.d("스터디 상세 갱신", "${studyInfo}")
        }

    }

    LaunchedEffect(membersList) {
        if (membersList != null) {
            isJoining.value = membersList.any { it.nickname == MyApplication.NICKNAME }
            isHostMember.value = membersList.any { member: Member ->
                member.nickname == MyApplication.NICKNAME && member.creator
            }
        }
        Log.d("스터디 멤버인가?", "${isJoining.value}")
        Log.d("방장 인가?", "${isHostMember.value}")
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Box {
                DetailTopBar(
                    title = "스터디 상세 정보",
                    onBackPress = {
                        // 뒤로가기
                        navController.navigate("homeScreen")
                    },
                    onNotificationClick = {
                        onNotificationClick(studyId ?: "")
                        Log.d("메시지 클릭", "전달 ${studyId}")
                    },
                    isMember = isJoining.value
                )
            }
            Spacer(Modifier.height(16.dp))

            if (!isLoadingState) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp)
                        .padding(bottom = 32.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(skeletonComponent()),
                )
            }
            else {
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
                        val tag = studyTopic?.let { TopicTagEnum.fromTitle(it) }
                        Text(
                            text = studyName ?: "",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(700),
                            fontSize = 22.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            maxLines = 2
                        )
                        Spacer(Modifier.weight(1f))

                        tag?.let {
                            StackTag(
                                stackTitle = it.title,
                                tint = colorResource(tag.colorId)
                            )
                        }
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
                                fontWeight = if (meetingDays?.contains(day) == true) FontWeight(700)
                                else FontWeight(600),
                                fontSize = if (meetingDays?.contains(day) == true) 17.sp
                                else 16.sp,
                                modifier = Modifier
                                    .weight(1f),
                                textAlign = TextAlign.Center,
                                color = if (meetingDays?.contains(day) == true) colorResource(R.color.primary_color)
                                else Color(0xFFC2C2C2)
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
                        if (content != null) {
                            ContentInfoCard(content)
                        }
                    }
                    Spacer(Modifier.height(36.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {

                        TitleText("스터디 구성원")
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${currentCount} / ${totalCount} 명",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            fontSize = 14.sp,
                            color = colorResource(R.color.primary_color)
                        )

                    }
                    Spacer(Modifier.height(24.dp))
                    if (membersList != null) {
                        JoinUserProfiles(membersList)
                    }

                    if (isJoining.value) {
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 44.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(124.dp)
                                    .height(100.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        clip = true
                                    )
                                    .clickable { onNavigateToChat(studyId ?: "") },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.border_light_color)
                                )
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
                                    .width(124.dp)
                                    .height(100.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        clip = true
                                    )
                                    .clickable {
                                        onNavigateToVideo(studyId ?: "", studyName ?: "")
                                        Log.d("실시간 모각공 버튼", "${studyId}")
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(
                                    1.dp,
                                    colorResource(R.color.border_light_color)
                                )
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
                                        text = "라이브 스터디",
                                        fontFamily = pretendard,
                                        fontWeight = FontWeight(600),
                                        fontSize = 14.5.sp
                                    )
                                }

                            }
                        }
                        //                Spacer(Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TitleText("공지사항")
                        Spacer(Modifier.weight(1f))

                        if (isHostMember.value) {
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(28.dp)
                                    .background(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(50.dp)
                                    )
                                    .clickable {

                                    },
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

                    }

                    if (feedsList != null) {
                        feedsList.forEachIndexed { index, feed ->
                            NoticeItem(feed)
                        }
                    }


                }
            }

        }

        if (!isJoining.value) {
            Button(
                onClick = {
                    findViewModel.sendJoinStudy(
                        SendJoinRequestDTO(studyId ?: "")) { success ->

                        if (success) {
                            isInviteSent = true
                        } else {
                            Toast.makeText(context, "죄송합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 36.dp),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color)),
                enabled = !isInviteSent
            ) {
                Text(
                    text = if(isInviteSent) "신청 완료" else "가입 신청",
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            }
        }

    }

}

@Composable
private fun DetailTopBar(
    title: String,
    tint: Color = colorResource(R.color.primary_color),
    onBackPress: () -> Unit,
    onNotificationClick: () -> Unit,
    isMember: Boolean
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp),

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
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title,
            fontFamily = pretendard,
            fontSize = 22.sp,
            fontWeight = FontWeight(700),
            color = tint

        )

        if (isMember) {
            IconButton(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterEnd),
                onClick = {
                    // 알림 목록 화면으로
                    onNotificationClick()
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
private fun ContentInfoCard(content: String) {
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
                    text = content,
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500)
                )

            }

        }
    }
}


@Composable
private fun JoinUserProfiles(userList: List<Member>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        userList.forEachIndexed { index, User ->
            UserProfileItem(User.image, User.nickname, User.creator)

            if (index < userList.size - 1) {
                Spacer(Modifier.width(16.dp))

            }
        }


    }

}

@Composable
private fun UserProfileItem(imageUrl: String, userName: String, isHost: Boolean) {
    Column(modifier = Modifier.width(52.dp)) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color = Color(0x00FFFFFF), shape = CircleShape)
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = Color.Gray, shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = RemoteDataSource().getImageUrl(imageUrl), contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxSize(),
                    placeholder = painterResource(R.drawable.img_default_profile), // 로딩 중 이미지
                    error = painterResource(R.drawable.img_default_profile), // 실패 이미지


                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
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
private fun NoticeItem(feed: Feed) {
    var isChecked by remember { mutableStateOf(false) }
    var checkCount by remember { mutableStateOf(0) }

    val backgroundColor = if (isChecked) colorResource(R.color.primary_color) else Color.Transparent
    val textColor = if (isChecked) Color.White else Color.Black
    val iconTint = if (isChecked) Color.White else Color.Black
    val buttonText = if (isChecked) "체크 완료" else "체크"

//    val content: String,
//    val createdAt: String,
//    val creatorInfo: CreatorInfo,
//    val study: String,
//    val title: Strin

    val noticeTitle = feed.title
    val noticeContent = feed.content
    val createTime = CommonUtils.formatDateTime(feed.createdAt)

    val writer = feed.creatorInfo
    val grade_writer = writer.term
    val nickname_writer = writer.nickname
    val campus_writer = writer.campus
    val profile_writer = writer.image


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

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color.Gray, shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = profile_writer, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        placeholder = painterResource(R.drawable.img_default_profile), // 로딩 중 이미지
                        error = painterResource(R.drawable.img_default_profile), // 실패 이미지

                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
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
                    GradeLabel(grade_writer)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = campus_writer,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 13.sp,
                        color = Color(0x99000000)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = nickname_writer,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(
            text = noticeTitle,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = noticeContent,
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

            Spacer(Modifier.weight(1f))

            // 글 작성 날짜, 시간
            Text(
                text = createTime,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                fontSize = 11.sp,
                color = Color(0xFFC2C2C2)
            )


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
fun GradeLabel(grade: String) {
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
            text = grade,
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
