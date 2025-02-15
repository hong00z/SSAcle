package com.example.firstproject.ui.LoginAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard

@Composable
fun OnboardScreen(
    navController: NavController,
    onAuthSuccess: () -> Unit, // 모든 인증 완료 시 메인 액티비티로 이동
) {

    // 기수가 Int인지 String인지 확인
    var gradeInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var nicknameInput by remember { mutableStateOf("") }
    var randomNickname by remember { mutableStateOf("") }

    var tmpAuthState by remember { mutableStateOf(false) }
    var tmpNextState by remember { mutableStateOf(false) }

    // 텍스트 필드에 값 입력 됐을 때 버튼 활성화
    val isAuthBtnEnabled = gradeInput.isNotEmpty() && nameInput.isNotEmpty()
    val isCheckBtnEnabled = nicknameInput.isNotEmpty()

    //
    var availableAuthBtn = gradeInput.isNotEmpty() && nameInput.isNotEmpty()
    var availableCheckBtn by remember {
        mutableStateOf(false)
    }
    var availableNextBtn by remember {
        mutableStateOf(false)
    }



    // 사용자 인증, 닉네임 중복 확인 상태
    var completeAuthUser by remember { mutableStateOf(false) }
    var completeAuthNickname by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = gradeInput,
            onValueChange = { newValue ->
                gradeInput = newValue
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                unfocusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                focusedContainerColor = Color(0x00FFFFFF),
                unfocusedContainerColor = Color(0x00FFFFFF)
            ),
            singleLine = true,
            suffix = {
                Text(
                    text = "기",
                    fontFamily = pretendard,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xCC9C9BA0),
                    modifier = Modifier.padding(start = 4.dp)
                )
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                textAlign = TextAlign.Right
            ),
            prefix = {
                Text(
                    text = "기수",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = colorResource(id = R.color.primary_color)
                )
            }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = nameInput,
            onValueChange = { newValue ->
                nameInput = newValue
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                unfocusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                focusedContainerColor = Color(0x00FFFFFF),
                unfocusedContainerColor = Color(0x00FFFFFF)
            ),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                textAlign = TextAlign.Right
            ),
            prefix = {
                Text(
                    text = "이름",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = colorResource(id = R.color.primary_color)
                )
            }
        )

        Button(
            onClick = {
                // 인증 통신
                tmpAuthState = !tmpAuthState
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isAuthBtnEnabled,
            colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color))
        ) {
            Text(
                "인증하기",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
            )
        }

        Spacer(Modifier.height(36.dp))

        // 인증 실패 시 나타남
        Text(
            "※ 인증에 실패했습니다. 다시 확인해주세요.",
            fontSize = 12.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = Color.Red
        )



        // 인증이 완료되면 닉네임 설정창 나옴
        if (tmpAuthState) {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    placeholder = {
                        Text(
                            text = "닉네임",
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = colorResource(id = R.color.primary_color)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                        unfocusedIndicatorColor = colorResource(id = R.color.textfield_stroke_color),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                Button(
                    onClick = {
                        nicknameInput = generateRandomNickname()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color))
                ) {
                    Text(
                        "랜덤 생성",
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                    )
                }
                Button(
                    onClick = {
                        // 닉네임 중복 확인 로직
                        tmpNextState = !tmpNextState

                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isCheckBtnEnabled,
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color))
                ) {
                    Text(
                        "중복 확인",
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))


        // 닉네임 중복확인이 끝남
        if (tmpNextState) {
            Button(
                onClick = {
                    // 다음 화면으로 넘어가기
                    onAuthSuccess()

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Black)
            ) {
                Text(
                    "다음 화면으로",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                )
            }
        }

    }

}




@Preview(showBackground = true)
@Composable
private fun PreviewAuth() {
//    AuthScreen(navController = navController, onAuthSuccess = {})
}