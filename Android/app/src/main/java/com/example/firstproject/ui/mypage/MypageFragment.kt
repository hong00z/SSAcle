package com.example.firstproject.ui.mypage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.databinding.FragmentMypageBinding
import com.example.firstproject.ui.chat.TAG
import timber.log.Timber
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {
    private var _binding : FragmentMypageBinding? = null
    private val binding get() = _binding!!

    val repository = MainRepository

        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        binding.apply {
            loginButton.setOnClickListener {



                val context = requireContext()
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    val accessToken = {token?.accessToken}
                    if(error != null) {

                    } else if (token != null) {
                        Timber.tag(TAG).e("로그인 성공 %s",token.accessToken)
                        // 통신
                        viewLifecycleOwner.lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                repository.loginWithKakao(token.accessToken)
                            }
                        }
                    }
                }
            }

        }
        return binding.root
    }

    companion object {
        fun newInstance() : MypageFragment {
            return MypageFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}