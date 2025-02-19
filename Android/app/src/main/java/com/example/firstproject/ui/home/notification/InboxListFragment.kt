package com.example.firstproject.ui.home.notification

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.request.InviteUserRequestDTO
import com.example.firstproject.data.model.dto.request.SendJoinRequestDTO
import com.example.firstproject.data.model.dto.response.MyInvitedStudyListDtoItem
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum
import com.rootachieve.requestresult.RequestResult

class InboxListFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = requireActivity().findNavController(R.id.fragment_container)


                InboxListScreen(
                    onStudyClick = { studyId ->
                        val bundle = bundleOf("studyId" to studyId)
                        try {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("studyId", studyId)
                            navController.navigate(R.id.action_notificationFragment_to_homeFragment, bundle)
                        } catch (e: IllegalArgumentException) {
                            Log.e("NavigationError", "잘못된 Navigation 요청: ${e.message}")
                        }

                    }

                )
            }
        }
    }
}

@Composable
fun InboxListScreen(
    notificationViewModel: NotificationViewModel = viewModel(),
    onStudyClick: (String) -> Unit
) {
    val getSecondResult by notificationViewModel.inviteResult.collectAsStateWithLifecycle()
    val originalStudyList = (getSecondResult as? RequestResult.Success)?.data ?: emptyList()

    val studyList = remember {
        mutableStateListOf<MyInvitedStudyListDtoItem>().apply {
            addAll(originalStudyList)
        }
    }

    LaunchedEffect(Unit) {
        notificationViewModel.getInviteStudyInfo()
    }
    LaunchedEffect(originalStudyList) {
        studyList.clear()
        studyList.addAll(originalStudyList)
        Log.d("내 초대 현황", "${studyList}")
    }
    LazyColumn(
        Modifier
        .fillMaxSize()
        .background(Color.White)) {

        items(studyList) { studyInfo ->

            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = TopicTagEnum.fromTitle(studyInfo.topic)
                    if (label != null) {
                        StackLabel1(
                            stackTitle = label.title,
                            tint = colorResource(label.colorId)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = studyInfo.studyName,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .height(24.dp)
                            .clickable {
                                onStudyClick(studyInfo.studyId)

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "자세히 보기",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(400),
                            fontSize = 13.sp
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_right_arrow),
                            null,
                            Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = studyInfo.meetingDays.joinToString(" "),
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        fontSize = 15.sp,
                        letterSpacing = 2.sp,
                        color = Color(0xFF1181F0),
                    )

                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "인원 :  ",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        fontSize = 15.sp,
                    )
                    Text(
                        text = "${studyInfo.members.size} / ${studyInfo.count}명",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        fontSize = 15.sp,
                    )
                }


                Text(
                    text = "스터디에 초대 받았어요.",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 8.dp)
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 36.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                // 수락하는 통신
                                notificationViewModel.acceptJoin(
                                    request = SendJoinRequestDTO(
                                        studyId = studyInfo.studyId
                                    )
                                )

                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "수락",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            fontSize = 15.sp,
                            color = colorResource(R.color.primary_color)
                        )
                    }
                    VerticalDivider()
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                // 클릭 시 삭제
                                studyList.remove(studyInfo)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "거절",
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            fontSize = 15.sp,
                            color = Color(0xFF9D9D9D)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))


            }
        }


    }
}

@Composable
private fun StackLabel1(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(22.dp)
            .background(tint, RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            color = Color.White,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 11.sp
        )

    }
}