package com.example.firstproject.ui.mypage

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.Profile
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.databinding.FragmentMypageBinding
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by viewModels()

    private var userProfile: Profile? = null

    val repository = MainRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)

        // 서버와 통신
        observeViewModel()
        mypageViewModel.getUserProfile()


        val tags = listOf("알고리즘", "백엔드", "CS 이론", "인프라")

        val tagsColors = mapOf(
            "알고리즘" to R.color.algo_stack_tag,
            "백엔드" to R.color.backend_stack_tag,
            "CS 이론" to R.color.cs_stack_tag,
            "인프라" to R.color.infra_stack_tag
        )

        addTags(binding.tagsContainer, tags, tagsColors)

        binding.apply {
            editLayout.setOnClickListener {
                findNavController().navigate(R.id.editMyPageFragment)
            }
        }
        return binding.root
    }

    // viewModel 데이터 변화 탐지
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mypageViewModel.getProfileResult.collect { result ->
                    when (result) {
                        is RequestResult.Progress -> {
                            Log.d("Mypage", "로딩 중...")
                        }
                        is RequestResult.Success -> {
                            userProfile = result.data.data
                            Log.d("Mypage", "사용자 정보: $userProfile")
                        }
                        is RequestResult.Failure -> {
                            Log.e("Mypage", "오류 발생: ${result.exception?.message}")
                        }
                        else -> Unit
                    }
                }
            }
        }
    }


    private fun addTags(container: LinearLayout, tags: List<String>, tagsColors: Map<String, Int>) {
        for (tag in tags) {
            val textView = TextView(requireContext()).apply {
                text = tag
                textSize = 16f  // 글자 크기 약간 증가
                setPadding(32, 16, 32, 16)  // 패딩을 키워 버튼 크기 증가
                minWidth = 120  // 최소 너비 설정
                minHeight = 56  // 최소 높이 설정
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                // 배경 drawable을 가져와서 색상 및 테두리 적용
                val backgroundDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.tag_background
                ) as GradientDrawable
                val tagColor = ContextCompat.getColor(
                    requireContext(),
                    tagsColors[tag] ?: R.color.algo_stack_tag
                )
                backgroundDrawable.setColor(tagColor)  // 배경색 변경
                backgroundDrawable.setStroke(3, tagColor)  // 테두리 색 동일하게 적용
                background = backgroundDrawable

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 8, 12, 8)  // 태그 간 여백 증가
                }
            }
            container.addView(textView)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
