package com.example.firstproject.ui.home.detail

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.StudyRequestedInviteListDtoItem
import com.example.firstproject.ui.theme.gmarket
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.GradeLabelEnum
import com.rootachieve.requestresult.RequestResult

@Composable
fun InvitingListScreen(
    notificationViewModel: StudyNotificationViewModel = viewModel(),
    studyId: String
) {
    val getInvitedResult by notificationViewModel.studyInvitedResult.collectAsStateWithLifecycle()
    val originalInvitedList = (getInvitedResult as? RequestResult.Success)?.data ?: emptyList()

    val invitedList =
        remember {
            mutableStateListOf<StudyRequestedInviteListDtoItem>().apply {
                addAll(
                    originalInvitedList
                )
            }
        }


    LaunchedEffect(Unit) {
        notificationViewModel.getStudyInvitedMember(studyId)
    }

    LaunchedEffect(originalInvitedList) {
        invitedList.clear()
        invitedList.addAll(originalInvitedList)
    }

    LazyColumn(Modifier.fillMaxSize()) {
        items(invitedList, key = { it.nickname }) { user ->
            Column(modifier = Modifier.fillMaxWidth())
            {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(color = Color(0x00FFFFFF), shape = CircleShape)
                    ) {

                        // Asyc로 변경
                        Image(
                            painter = painterResource(R.drawable.img_default_profile_5),
                            null,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                        )
                    }
                    Spacer(Modifier.width(12.dp))

                    Column {
                        val predictionText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = pretendard,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight(600)
                                )
                            ) {
                                // 닉네임
                                append(user.nickname)
                            }

                            append(" 님을 스터디에 초대했어요.")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 기수
                            GradeTag1(user.term)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                // 캠퍼스
                                text = user.campus,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                fontSize = 13.sp,
                                color = Color(0x99000000)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = predictionText,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            fontSize = 14.sp
                        )
                    }

                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 72.dp)
                        .clickable {
                            // 클릭 시 삭제
                            invitedList.remove(user)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "취소하기",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 15.sp,
                        color = Color(0xFF9D9D9D)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

        }
    }

}

@Composable
private fun GradeTag1(grade: String) {
    val labelColor = GradeLabelEnum.selectColor(grade)
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(20.dp)
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
            fontSize = 10.5.sp,
            color = Color.White
        )

    }

}