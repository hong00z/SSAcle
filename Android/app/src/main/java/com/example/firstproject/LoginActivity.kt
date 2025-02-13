package com.example.firstproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstproject.ui.LoginAuth.LoginScreen

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val composeView = findViewById<ComposeView>(R.id.login_compose_view)

        composeView.setContent {
            LoginScreen(
                onLoginSuccess = {
                    navigateToMain()
                    Log.d("LoginActivity", "MainActivity로 이동")
                }
            )
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}