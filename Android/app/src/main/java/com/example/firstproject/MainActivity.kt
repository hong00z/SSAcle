package com.example.firstproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.firstproject.databinding.ActivityMainBinding
import io.socket.client.Socket

const val TAG = "MainActivity_TAG"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var chatSocket: Socket
    val STUDY_ID = "67a43b80a9314e94fbb7f8f8" // 스터디 1
    val USER_ID = "67a43b80a9314e94fbb7f8ee" // 유저2_스터디1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

    }


}