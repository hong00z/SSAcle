package com.example.firstproject.ui.home

import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.StudyDTO
import com.example.firstproject.ui.common.CommonTopBar
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum
import kotlinx.coroutines.delay

@Composable
fun AllStudyListScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val allStudyListResult by homeViewModel.allStudyListResult.collectAsStateWithLifecycle()
    // 모든 스터디 리스트
    val allStudyList by homeViewModel.allStudyList.collectAsStateWithLifecycle()

    // 통신 상태
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 모집 중인 스터디 목록 불러오기
        homeViewModel.getAllStudyInfo(context)
    }

    // 응답 상태 탐지
    LaunchedEffect(allStudyListResult) {
        if (allStudyListResult.isProgress()) {
            // 로딩 중
            isLoading = true
            delay(1000)
        } else if (allStudyListResult.isSuccess()) {
            // 통신 성공
            isLoading = false
        }


    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTopBar(
            title = "모집 중인 스터디",
            onBackPress = {
                // 뒤로 가기
                navController.navigate("homeScreen")
            }
        )

//        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            items(allStudyList) { study ->
                StudyInfoItem(navController, study)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun StackLabel(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(22.dp)
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
private fun StudyInfoItem(navController: NavController, studyInfo: StudyDTO) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val label = TopicTagEnum.fromTitle(studyInfo.topic)
            if (label != null) {
                StackLabel(
                    stackTitle = label.title,
                    tint = colorResource(label.colorId)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = studyInfo.studyName,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth(0.7f),
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .height(24.dp)
                    .clickable {
                        // 클릭하면 스터디 상세정보화면으로
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("studyId", studyInfo.studyId)
                        Log.d("전체 스터디 목록에서 누름", "스터디아이디: ${studyInfo.studyId}")

                        navController.navigate("studyDetailScreen")
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "자세히 보기",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    fontSize = 13.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    null,
                    Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(16.dp))
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
                text = studyInfo.meetingDays.joinToString(" "),
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

            JoinProfiles(studyInfo.memberCount, studyInfo.members) // 스터디 참여중인 사람 수 넣기

            Spacer(Modifier.width(16.dp))
            Text(
                text = "인원 :  ",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
            )
            Text(
                text = "${studyInfo.memberCount} / ${studyInfo.count}명",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 15.sp,
            )

        }
        Spacer(Modifier.height(14.dp))

        Divider(color = Color(0xFF949494), modifier = Modifier.padding(horizontal = 8.dp))
    }
}
