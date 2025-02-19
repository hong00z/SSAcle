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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.firstproject.MyApplication.Companion.EMAIL
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.Profile
import com.example.firstproject.data.repository.MainRepository
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.databinding.FragmentMypageBinding
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.launch

class MypageFragment : Fragment() {

    companion object {
        const val TAG = "MypageFragment_TAG"
    }

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by activityViewModels()

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

        binding.apply {
            editLayout.setOnClickListener {
                findNavController().navigate(R.id.editMyPageFragment)
            }
        }
        return binding.root
    }

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

                            binding.profileImage.load(RemoteDataSource().getImageUrl(userProfile!!.image)) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }

                            binding.campusText.text = "${userProfile?.campus} 캠퍼스"
                            binding.generation.text = "${userProfile?.term}"
                            binding.nicknameText.text = userProfile?.nickname ?: ""
                            binding.emailText.text = EMAIL

                            binding.tagsContainer.removeAllViews() // 기존 태그 초기화

                            userProfile?.topics?.let { topics ->
                                val tagsColors = mapOf(
                                    "웹 프론트" to R.color.frontend_stack_tag,
                                    "백엔드" to R.color.backend_stack_tag,
                                    "모바일" to R.color.mobile_stack_tag,
                                    "인공지능" to R.color.ai_stack_tag,
                                    "빅데이터" to R.color.data_stack_tag,
                                    "임베디드" to R.color.embaded_stack_tag,
                                    "인프라" to R.color.infra_stack_tag,
                                    "CS 이론" to R.color.cs_stack_tag,
                                    "알고리즘" to R.color.algo_stack_tag,
                                    "게임" to R.color.game_stack_tag,
                                    "기타" to R.color.etc_stack_tag
                                )
                                addTags(binding.tagsContainer, topics, tagsColors)
                            }
                            binding.meetingDaysContainer.removeAllViews() // 기존 날짜 초기화
                            userProfile?.meetingDays?.let { days ->
                                if (days.isNotEmpty()) {
                                    addMeetingDays(binding.meetingDaysContainer, days)
                                }
                            }
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

    private fun addMeetingDays(container: LinearLayout, days: List<String>) {
        for (day in days) {
            val textView = TextView(requireContext()).apply {
                text = day
                textSize = 16f
                setPadding(32, 16, 32, 16)  // 패딩 조정
                minWidth = 120
                minHeight = 56
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

                // 개별 날짜에 `back_ground.xml` 적용
//                background = ContextCompat.getDrawable(requireContext())

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(12, 8, 12, 8)  // 간격 조정
                }
            }
            container.addView(textView)
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
