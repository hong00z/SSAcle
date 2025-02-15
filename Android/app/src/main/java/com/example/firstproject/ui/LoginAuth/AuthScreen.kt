package com.example.firstproject.ui.LoginAuth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.navigation.NavController
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard

@Composable
fun AuthScreen(
    navController: NavController,
) {

    var gradeInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    // 텍스트 필드에 값 입력 됐을 때 버튼 활성화
    val isAuthBtnEnabled = gradeInput.isNotEmpty() && nameInput.isNotEmpty()

    // 임시 결과
    var tmpAuthState by remember { mutableStateOf(false) }

    Box(
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
                text = "교육생 인증",
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 22.sp,
                color = colorResource(R.color.topbar_text_color)
            )
        }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
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
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.Right
                    ),
                    prefix = {
                        Text(
                            text = "학번",
                            fontSize = 22.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = colorResource(id = R.color.primary_color)
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
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
                        fontSize = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        textAlign = TextAlign.Right
                    ),
                    prefix = {
                        Text(
                            text = "이름",
                            fontSize = 22.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = colorResource(id = R.color.primary_color)
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "※ 오류 메시지 표시",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    fontSize = 14.sp,
                    color = colorResource(R.color.textfield_error_text_color)
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    // 인증 통신 수행
                    tmpAuthState = !tmpAuthState

                    // 통신 성공하면 화면 이동 -> LaunchedEffect에서 쓸 것
                    navController.navigate("Onboarding")

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 20.dp),
                enabled = isAuthBtnEnabled,
                colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_color))
            ) {
                Text(
                    "인증하기",
                    fontSize = 22.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthPreview() {
//    AuthScreen()
}