package com.example.firstproject.ui.matching

import android.util.Log
import android.view.LayoutInflater
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.request.RegisterStudyRequestDTO
import com.example.firstproject.ui.common.CommonTopBar
import com.example.firstproject.ui.common.SelectTopicCard

import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.CommonUtils

@Composable
fun RegisterStudyScreen(
    xmlNavController: NavController,
    registerStudyViewModel: RegisterStudyViewModel = viewModel()
) {
    val context = LocalContext.current

    val registerStateResult by registerStudyViewModel.registerResult.collectAsStateWithLifecycle()

    var weekFlag by remember { mutableStateOf(0) }

    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var minValue by remember { mutableStateOf(3) }
    var maxValue by remember { mutableStateOf(3) }

    LaunchedEffect(registerStateResult) {
        when {
            registerStateResult.isProgress() -> {
                Log.d("스터디 개설", "로딩 중")
            }

            registerStateResult.isSuccess() -> {
                Log.d("스터디 개설", "개설 성공")
                Toast.makeText(context, "스터디를 개설했습니다!", Toast.LENGTH_SHORT).show()
            }

            registerStateResult.isFailure() -> {
                Log.d("스터디 개설", "실패")
            }
        }
    }

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

    val isBtnEnabled = titleInput.isNotEmpty() && contentInput.isNotEmpty()
            && !selectedTag.isNullOrEmpty() && selectedDays.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Box {
            CommonTopBar(
                title = "스터디 개설",
                onBackPress = {
                    xmlNavController.navigate(R.id.action_studyRegisterFragment_to_homeFragment)
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                Spacer(Modifier.height(12.dp))
                TitleText("스터디 제목")
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.Start,
                        color = Color(0xFF201704)
                    ),
                    placeholder = {
                        Text(
                            text = "제목을 입력해주세요. (최대 15글자)",
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(400),
                            color = colorResource(R.color.textfile_placeholder_color)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.primary_color),
                        unfocusedBorderColor = colorResource(R.color.border_input_color)
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                TitleText("스터디 주제")
                Spacer(Modifier.height(16.dp))
                SelectTopicCard(
                    selectedTag = selectedTag,
                    onTagSelected = { newTag ->
                        // 같은 태그 누르면 null, 아니면 newTag
                        selectedTag = if (selectedTag == newTag) null else newTag
                    }
                )

                Spacer(Modifier.height(24.dp))

                TitleText("모임 요일")
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 0)) == (1 shl 0)),
                        text = "일"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 0))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 1)) == (1 shl 1)),
                        text = "월"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 1))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 2)) == (1 shl 2)),
                        text = "화"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 2))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 3)) == (1 shl 3)),
                        text = "수"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 3))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 4)) == (1 shl 4)),
                        text = "목"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 4))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 5)) == (1 shl 5)),
                        text = "금"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 5))
                    }
                    Spacer(Modifier.weight(1f))
                    SettingWeekComponent(
                        isChecked = ((weekFlag and (1 shl 6)) == (1 shl 6)),
                        text = "토"
                    ) {
                        weekFlag = ((weekFlag) xor (1 shl 6))
                    }
                }


                Spacer(Modifier.height(24.dp))
                TitleText("참여 인원")
                Spacer(Modifier.height(20.dp))

                NumberPickerView(
                    onMinValueChange = { newMin ->
                        minValue = newMin
                    },
                    onMaxValueChange = { maxValue = it }
                )
                Spacer(Modifier.height(24.dp))

                TitleText("스터디 소개")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.Start,
                        color = Color(0xFF201704)
                    ),
                    placeholder = {
                        Text(
                            text = "스터디에 대한 소개글을 작성해주세요.",
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(400),
                            color = colorResource(R.color.textfile_placeholder_color)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.primary_color),
                        unfocusedBorderColor = colorResource(R.color.border_input_color)
                    ),
                    maxLines = Int.MAX_VALUE
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                        registerStudyViewModel.sendRegisterStudy(
                            request = RegisterStudyRequestDTO(
                                studyName = titleInput,
                                topic = selectedTag!!,
                                meetingDays = selectedDays,
                                count = maxValue,
                                studyContent = contentInput
                            )
                        )
                        xmlNavController.navigate(R.id.action_studyRegisterFragment_to_homeFragment)
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFECECEC)),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.primary_color)),
                    enabled = isBtnEnabled
                ) {
                    Text(
                        text = "등록하기",
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {

                }
            }
        }

    }

}


@Composable
private fun TitleText(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SettingWeekComponent(isChecked: Boolean, text: String, onClick: () -> Unit) {
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
            }
    ) {
        Text(
            text = text,
            color = if (isChecked) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun NumberPickerView(
    onMinValueChange: (Int) -> Unit,
    onMaxValueChange: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.number_picker_layout, null).apply {
                val minNumberPicker = findViewById<NumberPicker>(R.id.min_num_picker)
                val maxNumberPicker = findViewById<NumberPicker>(R.id.max_num_picker)
                minNumberPicker.minValue = 3
                minNumberPicker.maxValue = 99
                minNumberPicker.setOnValueChangedListener { _, _, newValue ->
                    onMinValueChange(newValue)
                }
                maxNumberPicker.minValue = 3
                maxNumberPicker.maxValue = 99
                maxNumberPicker.setOnValueChangedListener { _, _, newValue ->
                    onMaxValueChange(newValue)
                }

            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    )
}