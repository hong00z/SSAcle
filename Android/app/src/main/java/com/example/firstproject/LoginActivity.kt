package com.example.firstproject

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firstproject.ui.LoginAuth.AuthScreen
import com.example.firstproject.ui.LoginAuth.LoginScreen

class LoginActivity : AppCompatActivity() {
    lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val composeView = findViewById<ComposeView>(R.id.login_compose_view)

        composeView.setContent {
            navController = rememberNavController()

            NavHost(navController = navController, startDestination = "Login") {
                composable("Login") {
                    LoginScreen(
                        navController = navController
                    )
                }

                composable("Auth") {
                    AuthScreen(
                        navController = navController,
                        onAuthSuccess = {
                            navigateToMain()
                            Log.d("LoginActivity", "MainActivity로 이동")
                        }
                    )

                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}