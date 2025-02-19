package com.example.firstproject.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.firstproject.MyApplication.Companion.EMAIL
import com.example.firstproject.R
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.databinding.FragmentEditMyPageBinding
import com.example.firstproject.ui.theme.TagAdapter
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.launch

class EditMyPageFragment : Fragment() {

    companion object {
        const val TAG = "EditMyPageFragment_TAG"
    }

    private var _binding: FragmentEditMyPageBinding? = null
    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by activityViewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.profileImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMyPageBinding.inflate(inflater, container, false)

        binding.apply {
            editFinishText.setOnClickListener {
                findNavController().navigate(R.id.mypageFragment)

            }

            cameraIcon.setOnClickListener {
                pickImage.launch("image/*")
            }
            txtCancle.setOnClickListener {
                findNavController().navigate(R.id.mypageFragment)
            }
        }

        val tagList = listOf(
            "웹 프론트", "백엔드", "모바일", "인공지능", "빅데이터",
            "임베디드", "인프라", "CS 이론", "알고리즘", "게임", "기타"
        )

        val tagAdapter = TagAdapter(
            requireContext(),
            tagList,
            onSelectionChanged = { selectedCount, showWarning ->
                if (showWarning) {
                    Toast.makeText(requireContext(), "최대 4개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            onSelectedTagsUpdated = { selectedTags ->
                binding.txtSelectedTags.text = "선택된 태그: " + selectedTags.joinToString(", ")
            }
        )

        binding.tagsRecyclerView.apply {
            adapter = tagAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel의 프로필 데이터를 수집하여 UI 업데이트
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mypageViewModel.getProfileResult.collect { result ->
                    when (result) {
                        is RequestResult.Progress -> {
                            // 로딩 중 UI 처리
                            // 예: ProgressBar 보이기 등
                        }

                        is RequestResult.Success -> {
                            // 성공적으로 프로필 데이터를 받아온 경우 UI 업데이트
                            val profile = result.data.data!!
                            binding.profileImage.load(RemoteDataSource().getImageUrl(profile.image)) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                            binding.tvCampus.text = "${profile.campus} ${profile.term}"
                            binding.etNickname.setText(profile.nickname)
                            binding.tvEmail.text = EMAIL // 혹은 MyApplication.EMAIL 등

                            Log.d(TAG, "topics= ${profile.topics}")
                            // 태그, 미팅일 등 다른 UI 업데이트
                            (binding.tagsRecyclerView.adapter as? TagAdapter)?.setSelectedTags(
                                profile.topics
                            )

                        }

                        is RequestResult.Failure -> {
                            Toast.makeText(
                                requireContext(),
                                "오류 발생: ${result.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
