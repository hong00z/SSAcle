package com.example.firstproject.ui.LoginAuth

import android.util.Log
import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firstproject.MyApplication
import com.example.firstproject.R
import com.example.firstproject.data.repository.MainRepository
import com.rootachieve.requestresult.RequestResult
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginAuthViewModel = viewModel(),
    navController: NavController
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(loginState) {

        if (loginState is RequestResult.Success) {
            Log.d("로그인 화면","onSuccess: ${loginState}")
            navController.navigate("Auth")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            viewModel.loginWithKakao(context)

        }, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("카카오 로그인")
        }

    }
}