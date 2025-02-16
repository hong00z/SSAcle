package com.example.firstproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.firstproject.data.repository.TokenManager
import com.example.firstproject.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            Log.d("메인 액티비티", "로그인 만료. 로그인 화면으로 이동")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2. DataStore에서 사용자의 '인증 완료 여부', '온보딩 완료 여부' 확인
        //   - isAuthCompleted(), isOnboardingCompleted()는 MyApplication.kt 등에서 정의해두었다 가정
        val isAuthCompleted = runBlocking {
            MyApplication.isAuthCompleted().first()  // Boolean
        }
        val isOnboardingCompleted = runBlocking {
            MyApplication.isOnboardingCompleted().first()  // Boolean
        }

        // 3. 인증/온보딩 미완료라면 -> LoginActivity로 다시 보냄
        if (!isAuthCompleted || !isOnboardingCompleted) {
            Log.d("메인 액티비티", "인증 혹은 온보딩 미완료 상태. 로그인 액티비티로 이동")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.studyRegisterFragment) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }

        }

        binding.bottomNavigationView.setupWithNavController(navController)

    }
}