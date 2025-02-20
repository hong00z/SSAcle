package com.example.firstproject.ui.mypage

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.firstproject.MyApplication.Companion.EMAIL
import com.example.firstproject.data.model.dto.request.EditProfileRequestDTO
import com.example.firstproject.data.repository.RemoteDataSource
import com.example.firstproject.databinding.FragmentEditMyPageBinding
import com.example.firstproject.ui.theme.TagAdapter
import com.rootachieve.requestresult.RequestResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class EditMyPageFragment : Fragment() {

    companion object {
        const val TAG = "EditMyPageFragment_TAG"
    }

    private var _binding: FragmentEditMyPageBinding? = null
    private val binding get() = _binding!!

    private val mypageViewModel: MypageViewModel by activityViewModels()
    private lateinit var myTags: List<String>
    private var newUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d(TAG, "uri: $uri")
            newUri = uri
            binding.ivProfileImage.load(uri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMyPageBinding.inflate(inflater, container, false)

        binding.apply {
            btnFinish.setOnClickListener {
                editUserProfile()
                findNavController().popBackStack()
            }

            ivAlbum.setOnClickListener {
                pickImage.launch("image/*")
            }
            btnCancel.setOnClickListener {
                findNavController().popBackStack()
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
                Log.d(TAG, "선택된 태그: $selectedTags")
                binding.tvSelectedTags.text = "선택된 태그: " + selectedTags.joinToString(", ")
                myTags = selectedTags
            }
        )

        binding.rvTags.apply {
            adapter = tagAdapter
            layoutManager = GridLayoutManager(context, 3)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = mypageViewModel.getProfileResult.first { it is RequestResult.Success }
            if (result is RequestResult.Success) {
                val profile = result.data.data!!
                binding.ivProfileImage.load(RemoteDataSource().getImageUrl(profile.image)) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
                binding.tvCampus.text = "${profile.campus} ${profile.term}"
                binding.etNickname.setText(profile.nickname)
                binding.tvEmail.text = EMAIL

                Log.d(TAG, "topics= ${profile.topics}")
                myTags = profile.topics
                (binding.rvTags.adapter as? TagAdapter)?.setSelectedTags(profile.topics)
            }
        }

    }

    private fun editUserProfile() {
        val imageFile: File? = newUri?.let { uri ->
            File(getRealPathFromURI(uri))
        }

        val nickname = binding.etNickname.text.toString().trim()
        val topics = myTags
        val meetingDays = listOf("월")

        val request = EditProfileRequestDTO(nickname, topics, meetingDays)

        mypageViewModel.editUserProfile(request, imageFile)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var filePath = ""
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
