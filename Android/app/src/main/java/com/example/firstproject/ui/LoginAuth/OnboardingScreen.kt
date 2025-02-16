package com.example.firstproject.ui.LoginAuth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.MyApplication
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.request.NicknameRequestDTO
import com.example.firstproject.ui.matching.SettingWeekComponent
import com.example.firstproject.ui.theme.TagAdapter
import com.example.firstproject.ui.theme.pretendard
import com.rootachieve.requestresult.RequestResult

@Composable
fun OnboardingScreen(
    navController: NavController,
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    var nicknameInput by remember { mutableStateOf("") }
    var weekFlag by remember { mutableStateOf(0) }

    val checkNicknameStateResult by onboardingViewModel.checkUserNickname.collectAsStateWithLifecycle()
    val checkStateInfo = (checkNicknameStateResult as? RequestResult.Success)?.data

    var checkMessage by remember { mutableStateOf("") }

    // 중복확인 통신 후 나타날 메시지 상태
    var isCheckMessageState by remember { mutableStateOf(false) }

    // 닉네임 중복확인이 완료됐는지 확인
    var isCheckNicknameComplete by remember { mutableStateOf(false) }


    // 관심 주제 선택
    var selectedTopics by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCount by remember { mutableStateOf(0) }
    var showWarning by remember { mutableStateOf(false) }


    // 선택한 선호 요일 리스트
    val selectedDays: List<String> = remember(weekFlag) {
        val allDays = listOf("일", "월", "화", "수", "목", "금", "토")
        val result = mutableListOf<String>()

        for (i in 0..6) {
            if ((weekFlag and (1 shl i)) != 0) {
                result.add(allDays[i])
            }
        }
        result
    }

    // 닉네임 중복확인 완료 && 관심주제 선택 완료 시 버튼 활성화
    val isBtnEnabled = isCheckNicknameComplete && selectedTopics.isNotEmpty() && selectedDays.isNotEmpty()

    LaunchedEffect(checkNicknameStateResult) {

        if (checkNicknameStateResult.isProgress()) {
            Log.d("닉네임 중복확인", "통신 로딩 중")
        } else if (checkNicknameStateResult.isSuccess()) {
            Log.d("닉네임 중복확인", "통신 완료! : ${checkStateInfo}")
            checkMessage = checkStateInfo?.message!!
            isCheckNicknameComplete = true
            Log.d("닉네임 중복확인", "data 상태 ${checkStateInfo?.data}")
            isCheckMessageState = true
        } else if (checkNicknameStateResult.isFailure()) {
            checkMessage = "서버와 통신에 실패했습니다."
            isCheckNicknameComplete = false
            isCheckMessageState = false
        }

    }


    val topicList = listOf(
        "웹 프론트", "백엔드", "모바일", "인공지능", "빅데이터",
        "임베디드", "인프라", "CS 이론", "알고리즘", "게임", "기타"
    )

    val userGradeNum by MyApplication.getUserGrade().collectAsState(initial = "")
    val userName by MyApplication.getUserName().collectAsState(initial = "")

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "사용자 정보 입력",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 22.sp,
                color = colorResource(R.color.topbar_text_color)
            )
        }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                userGradeNum?.let { InfoTextComponent(title = "학번", content = it) }
                Spacer(Modifier.height(24.dp))
                userName?.let { InfoTextComponent(title = "이름", content = it) }
                Spacer(Modifier.height(20.dp))
                Divider(
                    color = colorResource(id = R.color.textfield_stroke_color),
                    thickness = 2.dp
                )

                Spacer(Modifier.height(32.dp))
                Text(
                    "닉네임",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    fontSize = 16.sp,
                    color = colorResource(R.color.primary_color)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        value = nicknameInput,
                        onValueChange = { newValue ->
                            nicknameInput = newValue
                            isCheckNicknameComplete = false
                            checkMessage = ""
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color(0x00FFFFFF),
                            unfocusedIndicatorColor = Color(0x00FFFFFF),
                            focusedContainerColor = Color(0x00FFFFFF),
                            unfocusedContainerColor = Color(0x00FFFFFF)
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            textAlign = TextAlign.Left
                        ),
                        placeholder = {
                            Text(
                                text = "닉네임을 입력하세요",
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                fontSize = 20.sp,
                                color = colorResource(R.color.textfile_placeholder_color)
                            )
                        }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "중복 확인",
                        fontFamily = pretendard,
                        fontWeight =
                        if (nicknameInput.isNotEmpty()) FontWeight(500)
                        else FontWeight(400),
                        fontSize = 15.sp,
                        color =
                        if (nicknameInput.isNotEmpty()) Color(0xFF666666)
                        else colorResource(R.color.textfile_placeholder_color),

                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                // 닉네임 필드에 입력값 있을 때만 실행되도록
                                if (nicknameInput.isNotEmpty()) {
                                    onboardingViewModel.checkNickname(
                                        NicknameRequestDTO(
                                            nicknameInput
                                        )
                                    )
                                    checkMessage = ""

                                }
                            }
                    )

                }
                Divider(color = colorResource(id = R.color.textfield_stroke_color))

                if (!checkMessage.isNullOrEmpty()) {
                    NicknameStateMessage(
                        isState = isCheckMessageState
                    )
                }


                Spacer(Modifier.height(32.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "관심 주제 선택",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = colorResource(R.color.primary_color)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "(최대 4개까지 선택할 수 있어요.)",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        fontSize = 12.sp,
                        color = colorResource(R.color.border_input_color)
                    )
                }

                Spacer(Modifier.height(16.dp))
                TopicLabelSelectionView(
                    labelList = topicList,
                    onSelectionChanged = { count, warning ->
                        selectedCount = count
                        showWarning = warning
                    },
                    onSelectedTagsUpdated = { newList ->
                        selectedTopics = newList
                    }
                )
                // 테스트용 표시
                Text(text = "선택된 주제: ${selectedTopics.joinToString()}")

                Spacer(Modifier.height(32.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "선호 요일 선택",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        fontSize = 16.sp,
                        color = colorResource(R.color.primary_color)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "(희망 스터디 진행 요일을 선택해 주세요.)",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        fontSize = 12.sp,
                        color = colorResource(R.color.border_input_color)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 0)) == (1 shl 0)),
                        text = "일"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 0))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 1)) == (1 shl 1)),
                        text = "월"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 1))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 2)) == (1 shl 2)),
                        text = "화"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 2))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 3)) == (1 shl 3)),
                        text = "수"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 3))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 4)) == (1 shl 4)),
                        text = "목"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 4))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 5)) == (1 shl 5)),
                        text = "금"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 5))
                    }
                    Spacer(Modifier.weight(1f))
                    SelectWeekComponent(
                        isChecked = ((weekFlag and (1 shl 6)) == (1 shl 6)),
                        text = "토"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 6))
                    }
                }
            }

            // 테스트용 표시
            Text("선택된 요일: ${selectedDays.joinToString()}")

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    // 인증 통신 수행


                    // 통신 성공하면 화면 이동 -> LaunchedEffect에서 쓸 것
//                        navController.navigate("Onboarding")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 20.dp),
                enabled = isBtnEnabled,
                colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color))
            ) {
                Text(
                    "싸클하러 가기",
                    fontSize = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            }
            Spacer(Modifier.height(16.dp))
        }

    }
}

@Composable
private fun InfoTextComponent(title: String, content: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 20.sp,
            color = colorResource(R.color.primary_color)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = content,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun NicknameStateMessage(isState: Boolean) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        if (isState) {
            Text(
                text = "사용 가능한 닉네임입니다.",
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                fontSize = 14.sp,
                color = colorResource(R.color.textfield_success_text_color)
            )
        } else {
            Text(
                text = "※ 중복된 닉네임입니다.",
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                fontSize = 14.sp,
                color = colorResource(R.color.textfield_error_text_color)
            )
        }
    }
}

@Composable
private fun SelectWeekComponent(isChecked: Boolean, text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(
                (0.8).dp,
                colorResource(id = R.color.primary_color),
                RoundedCornerShape(50.dp)
            )
            .background(
                if (isChecked) {
                    colorResource(id = R.color.primary_color)
                } else {
                    Color.Unspecified
                },
                RoundedCornerShape(50.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isChecked) Color.White else Color.Black,
            fontSize = 15.sp,
            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -1.dp)
        )
    }
}

@Composable
fun TopicLabelSelectionView(
    labelList: List<String>,
    onSelectionChanged: (selectedCount: Int, showWarning: Boolean) -> Unit,
    onSelectedTagsUpdated: (List<String>) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = TagAdapter(
                    context,
                    labelList,
                    onSelectionChanged,
                    onSelectedTagsUpdated
                )
            }
        }
    )
}

fun generateRandomNickname(): String {
    val adjectives = listOf(
        "즐거운", "희망찬", "행복한", "기쁜", "유쾌한",
        "상쾌한", "활기찬", "빛나는", "설레는", "포근한",
        "따뜻한", "반짝이는", "든든한", "사랑스러운", "멋진",
        "소중한", "평온한", "친절한", "용감한", "달콤한",
        "화려한", "날렵한", "놀라운", "강력한", "총명한",
        "위대한", "건실한", "성실한"
    )

    val regions = listOf("서울", "구미", "대전", "광주", "부울경")

    val animals = listOf(
        "다람쥐", "고양이", "강아지", "여우", "토끼", "호랑이", "코끼리",
        "기린", "펭귄", "부엉이", "수달", "고래", "판다", "너구리", "고슴도치",
        "하마", "물개", "코알라", "치타", "늑대", "캥거루", "사슴", "참새",
        "앵무새", "올빼미", "돌고래", "두더지", "코뿔소", "거북이", "반딧불",
        "까치", "바다표범", "미어캣", "알파카", "북극곰", "청설모", "북극여우",
        "오랑우탄", "아나콘다", "바다사자", "사막여우", "고라니", "노루", "뱁새"
    )

//    return "${adjectives.random()} ${regions.random()} ${animals.random()}"
    return "${adjectives.random()} ${animals.random()}"
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {

//    OnboardingScreen()
}