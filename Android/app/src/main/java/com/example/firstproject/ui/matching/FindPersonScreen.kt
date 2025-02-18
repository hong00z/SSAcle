package com.example.firstproject.ui.matching

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.Top3RecommendedUsersDtoItem
import com.example.firstproject.ui.common.CommonTopBar
import com.example.firstproject.ui.theme.gmarket
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.GradeLabelEnum
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun FindPersonScreen(
    navController: NavController,
    findViewModel: FindViewModel = viewModel()
) {
    val studyId = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("studyId")

    val personRecommandResult by findViewModel.personRecommandResult.collectAsStateWithLifecycle()

    val personRecommandList by findViewModel.personRecommandList.collectAsStateWithLifecycle()

    // 통신 상태
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(studyId) {
        studyId?.let {
            Log.d("사람 추천 화면", "스터디 아이디 : ${studyId}")
            findViewModel.getPersonRecommandList(it)
        }
    }

    LaunchedEffect(personRecommandResult) {
        when {
            personRecommandResult.isProgress() -> {
                isLoading = true
                Log.d("사람 추천", "로딩 중")
            }

            personRecommandResult.isSuccess() -> {
                isLoading = false
                Log.d("사람 추천", "통신 성공")
            }

            personRecommandResult.isFailure() -> {
                isLoading = false
                Log.d("사람 추천", "통신 실패")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonTopBar("",
            onBackPress = {
                // 뒤로 가기
                navController.popBackStack()
            }
        )

        Image(
            painter = painterResource(R.drawable.img_find_person), null,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.075f)
        )
        Spacer(Modifier.height(36.dp))

        personRecommandList.forEach { personInfo ->
            PersonInfoItem(personInfo)
        }


    }
}

@Composable
private fun PersonInfoItem(personInfo: Top3RecommendedUsersDtoItem) {
    val weekList = personInfo.meetingDays
    val tagList = personInfo.topics

    Column(modifier = Modifier.fillMaxWidth()) {
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
                        .size(52.dp)
                        .align(Alignment.Center),
                )
            }
            Spacer(Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradeTag(personInfo.term)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = personInfo.campus,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 14.sp,
                        color = Color(0x99000000)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = personInfo.nickname,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "참여 중인 스터디: ${personInfo.countJoinedStudies}개",
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
//                if (label != null) {
//                    val color = colorResource(label.colorId)
//                    StackLabel(stackTitle = title, tint = color)
//                }
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

            // 버튼 눌렀을 때 초대 요청 수행
            SendRequestButton()
        }
        Spacer(Modifier.height(14.dp))
        Divider(color = Color(0xFF949494))
        Spacer(Modifier.height(20.dp))
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
            fontSize = 11.sp,
            color = Color.White
        )

    }

}
