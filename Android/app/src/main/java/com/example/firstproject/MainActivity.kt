package com.example.firstproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.firstproject.MyApplication.Companion.EMAIL
import com.example.firstproject.MyApplication.Companion.USER_ID
import com.example.firstproject.MyApplication.Companion.requiredPermissions
import com.example.firstproject.MyApplication.Companion.tokenManager
import com.example.firstproject.client.RetrofitClient
import com.example.firstproject.databinding.ActivityMainBinding
import com.example.firstproject.dto.TokenUpdateRequest
import com.example.firstproject.utils.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.Base64

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        const val TAG = "MainActivity_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
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

        USER_ID = getUserIdFromToken(accessToken)
        Log.d(TAG, "USER_ID = $USER_ID")
        EMAIL = getUesrEmailFromToken(accessToken)
        Log.d(TAG, "EMAIL = $EMAIL")
        tokenManager.getFcmToken()?.let { sendRegistrationToServer(it) }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 체크 (카메라, 마이크 등)
        val checker = PermissionChecker(this)
        if (!checker.checkPermission(this, requiredPermissions)) {
            checker.setOnGrantedListener {
                // 권한이 허용되면 WebRTC 초기화 및 signaling 서버 연결
            }
            checker.requestPermissionLauncher.launch(requiredPermissions)
        } else {
            // 이미 권한이 있는 경우
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.homeFragment || destination.id == R.id.chatFragment
                || destination.id == R.id.aiFragment || destination.id == R.id.mypageFragment
                || destination.id == R.id.liveMainFragment
            ) {
                binding.bottomNavigationView.visibility = View.VISIBLE
            } else {
                binding.bottomNavigationView.visibility = View.GONE
            }

        }

        binding.bottomNavigationView.setupWithNavController(navController)

    }


    // User Id 토큰에서 참조
    private fun getUserIdFromToken(token: String): String {
        return decodeJwtPayload(token).optString("id")
    }

    // Email 가져오기
    private fun getUesrEmailFromToken(token: String): String {
        return decodeJwtPayload(token).optString("sub")
    }

    private fun decodeJwtPayload(token: String): JSONObject {
        // JWT는 보통 3개의 파트로 구성: header.payload.signature
        val parts = token.split(".")

        // Header: parts[0], Payload: parts[1], Signature: parts[2]
        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))

//        Log.d(TAG, "Header: $headerJson")
//        Log.d(TAG, "Payload: $payloadJson")

        return JSONObject(payloadJson)

    }

    /**
     * 새 토큰을 서버에 전송하는 메서드.
     */
    private fun sendRegistrationToServer(fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.userService.updateFcmToken(
                    USER_ID, TokenUpdateRequest(fcmToken)
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "서버에 토큰 등록 성공: ${response.body()}")
                } else {
                    Log.e(TAG, "서버 토큰 등록 실패, 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "서버에 토큰 전송 중 오류 발생: ${e.message}", e)
            }
        }
    }

}