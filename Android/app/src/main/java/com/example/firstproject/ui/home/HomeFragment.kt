package com.example.firstproject.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentHomeBinding
import com.example.firstproject.ui.matching.RegisterStudyScreen

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
                val xmlNavController = findNavController()


                NavHost(
                    navController = navController,
                    startDestination = "homeScreen"
                ) {
                    composable("homeScreen") {
                        HomeScreen(
                            navController = navController,
                            onNavigateToFragment = {
                                xmlNavController.navigate(R.id.action_homeFragment_to_studyRegisterFragment)
                            },
                            onNotificationClick = {
                                xmlNavController.navigate(R.id.action_homeFragment_to_notificationFragment)
                            }
                        )
                    }

                    composable("studyDetailScreen") {
                        StudyDetailScreen(navController = navController,
                            onNavigateToVideo = {
                                xmlNavController.navigate(R.id.action_homeFragment_to_videoFragment)
                            }
                        )
                    }

                }
            }
        }

    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}