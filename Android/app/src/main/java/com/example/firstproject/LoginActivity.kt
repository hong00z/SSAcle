package com.example.firstproject

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.firstproject.data.repository.TokenManager
import com.example.firstproject.ui.LoginAuth.AuthScreen
import com.example.firstproject.ui.LoginAuth.LoginScreen
import com.example.firstproject.ui.LoginAuth.OnboardScreen
import com.example.firstproject.ui.LoginAuth.OnboardingScreen

class LoginActivity : AppCompatActivity() {
    lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val composeView = findViewById<ComposeView>(R.id.login_compose_view)

        composeView.setContent {
            navController = rememberNavController()

            // DataStore에서 카카오 토큰, 인증 여부, 온보딩 여부 가져오기
            val tokenManager = TokenManager(this@LoginActivity)
            val accessToken = tokenManager.getAccessToken()

            // 아래 두 값은 collectAsState로 관찰 가능
            val isAuthCompleted by MyApplication.isAuthCompleted().collectAsState(initial = false)
            val isOnboardingCompleted by MyApplication.isOnboardingCompleted()
                .collectAsState(initial = false)

            // startDestination을 결정할 변수
            val startDestination = when {
                // 토큰이 없으면 -> 카카오 로그인 화면
                accessToken.isNullOrEmpty() -> "Login"

                // 토큰은 있으나 싸피 인증이 안 된 상태면 -> AuthScreen
                !isAuthCompleted -> "Auth"

                // 인증은 끝났지만 온보딩(닉네임/관심 주제)이 안 된 상태면 -> Onboard (또는 Onboarding)
                !isOnboardingCompleted -> "Onboarding"

                // 전부 완료된 경우 -> 곧바로 MainActivity 이동
                else -> {
                    navigateToMain()
                    null
                }
            }

            if (startDestination != null) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("Login") {
                        LoginScreen(
                            navController = navController
                        )
                    }

                    composable("Auth") {
                        AuthScreen(
                            navController = navController
                        )
                    }

                    composable(route = "Onboarding") {
                        OnboardingScreen(
                            navController = navController
                        )
                    }

//                composable("Onboard") {
//                    OnboardScreen(
//                        navController = navController,
//                        onAuthSuccess = {
//                            navigateToMain()
//                            Log.d("LoginActivity", "MainActivity로 이동")
//                        }
//                    )
//                }
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}