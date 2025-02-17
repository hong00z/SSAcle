package com.example.firstproject.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firstproject.R
import com.example.firstproject.databinding.FragmentEditMyPageBinding
import com.example.firstproject.ui.theme.TagAdapter

class EditMyPageFragment : Fragment() {

    private var _binding: FragmentEditMyPageBinding? = null
    private val binding get() = _binding!!

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
