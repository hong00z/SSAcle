package com.example.firstproject.ui.LoginAuth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firstproject.MyApplication
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.KakaoTokenDTO
import com.rootachieve.requestresult.RequestResult

@Composable
fun LoginScreen(
    viewModel: LoginAuthViewModel = viewModel(),
    navController: NavController,
    onAuthSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(loginState) {

        if (loginState is RequestResult.Success) {
            Log.d("로그인 스크린: ", "onSuccess: ${loginState}")

            val response = (loginState as RequestResult.Success<KakaoTokenDTO>).data

            Log.d("로그인 스크린: ", "인증 완료 여부: ${response.auth}")
            // 사용자 인증이 완료된 상태 확인
            if (response.auth) {
                onAuthSuccess()
                MyApplication.setAuthCompleted(true)
                MyApplication.setOnboardingCompleted(true)
            } else {
                navController.navigate("Auth")
                MyApplication.setAuthCompleted(false)
                MyApplication.setOnboardingCompleted(false)
            }

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ssacle_logo), null,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(110.dp)
                )


            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.kakao_login_button), null,
                modifier = Modifier
                    .clickable { viewModel.loginWithKakao(context) }
            )
        }
    }
}