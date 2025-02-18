package com.example.firstproject.ui.home

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.MyJoinedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.StudyInfo
import com.example.firstproject.ui.theme.notosans
import com.example.firstproject.ui.theme.pretendard
import com.rootachieve.requestresult.RequestResult

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToFragment: () -> Unit,
    // 이거 추가
    onNotificationClick: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()

) {
    val context = LocalContext.current

    val allStudyListResult by homeViewModel.allStudyListResult.collectAsStateWithLifecycle()

    val getJoinedStudyResult by homeViewModel.joinedStudyResult.collectAsStateWithLifecycle()
    val myJoinedStudyList = (getJoinedStudyResult as? RequestResult.Success)?.data


    // 모든 스터디 리스트
    val allStudyList by homeViewModel.allStudyList.collectAsStateWithLifecycle()


//    myStudyListResult.onNone {
//        homeViewModel.
//    }

    // 홈 화면 들어올 때 통신
    LaunchedEffect(Unit) {
        Log.d("홈 화면", "모든 스터디 불러오기 ")

        // 모집 중인 스터디 목록 불러오기
        homeViewModel.getAllStudyInfo(context)

        // 내가 참여 중인 스터디 목록 불러오기
        homeViewModel.getJoinedStudy()
        if (myJoinedStudyList != null) Log.d("홈 화면", "내 스터디 목록: $myJoinedStudyList ")
        else Log.d("홈 화면", "내 스터디 목록 null ㅋㅋ ")
    }

    LaunchedEffect(getJoinedStudyResult) {
        Log.d("참여 중 상태 변경", "내 스터디 목록: $myJoinedStudyList ")
        if (myJoinedStudyList == null) Log.d("참여 중 상태 변경", "내 스터디 목록 null ㅋㅋ ")
    }


//
//    LaunchedEffect(myStudyListResult) {
//        myStudyListResult.onSuccess { data ->
//
//        }
//        myStudyListResult.onFailure { code, e ->
//
//        }
//    }

    Scaffold(
        topBar = {
            // 여기 추가
            TopBarMain(onNotificationClick = onNotificationClick)
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {

                FloatingActionButton(
                    onClick = {
                        // 스터디 등록 화면으로 이동
                        onNavigateToFragment()
                    },
                    backgroundColor = colorResource(id = R.color.primary_color),
                    modifier = Modifier.border(
                        (0.5).dp, Color(0xFFBFE0EF),
                        RoundedCornerShape(30.dp)
                    ),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_plus),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                }

            }
        }

    ) { contentPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                TitleTextView("내 스터디 목록")
                Spacer(Modifier.height(16.dp))

                // 내 스터디 목록이 null이 아닐 때
                if (myJoinedStudyList != null) {
                    if (myJoinedStudyList.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .padding(horizontal = 12.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    clip = true,
                                ),
                            shape = RoundedCornerShape(5.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                1.dp,
                                colorResource(R.color.border_light_color)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F4)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "스터디에 참가해 보는 건 어떠세요?",
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight(500),
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFFA9A9AB)
                                )
                            }

                        }
                    } else {
                        MyStudyItem(myJoinedStudyList, navController = navController)
                    }
                }
                // 내 스터디 목록이 null일 때
                else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .padding(horizontal = 12.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(10.dp),
                                clip = true,
                            ),
                        shape = RoundedCornerShape(5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            colorResource(R.color.border_light_color)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F4)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "스터디에 참가해 보는 건 어떠세요?",
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                fontSize = 15.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFFA9A9AB)
                            )
                        }

                    }
                }

                Spacer(Modifier.height(32.dp))

                TitleTextView("스터디 매칭")
                Spacer(Modifier.height(16.dp))

                FindActionButton(
                    onNavigatePerson = {
                        // 내가 참여 중인 스터디 목록 화면으로 이동
                        navController.navigate("chooseStudyScreen")

                    },
                    onNavigateStudy = {
                        // 스터디 추천 화면으로 이동
                        navController.navigate("findStudyScreen")

                    }
                )
                Spacer(Modifier.height(32.dp))


                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TitleTextView("모집 중인 스터디")

                    Spacer(Modifier.weight(1f))
                    Text(
                        "전체보기",
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            // 클릭 시 모집 중인 스터디 화면으로 이동
                            navController.navigate("allStudyListScreen")

                        }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_right_arrow),
                        null,
                        Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                StudyListCard(openStudyList = allStudyList, navController = navController)
                Spacer(Modifier.height(8.dp))
            }

        }

    }

}


@Composable
private fun TopBarMain(
    tint: Color = colorResource(R.color.primary_color),
    onNotificationClick: () -> Unit

) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp),
            text = "SSAcle",
            fontFamily = notosans,
            fontSize = 30.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF1181F0)
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            onClick = { onNotificationClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TitleTextView(title: String) {
    Text(
        text = title,
        fontFamily = pretendard,
        fontWeight = FontWeight(700),
        fontSize = 24.sp,
        letterSpacing = 1.sp
    )
}

@Composable
fun MyStudyItem(
    // 스터디 리스트
    itemList: List<MyJoinedStudyListDtoItem>,
    navController: NavController
) {
    val pagerState = rememberPagerState(initialPage = 0) {
        // 크기
        itemList.size
    }
    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아이템 슬라이드
        Box(
            modifier = Modifier
                .wrapContentSize()

        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.wrapContentSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(horizontal = 12.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(10.dp),
                            clip = true,
                        )
                        .clickable {
                            // 현재 NavBackStackEntry의 savedStateHandle에 'studyItem' 키로 데이터 저장
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("studyId", itemList[it].id)

                            Log.d("홈 화면에서 누름","스터디아이디: ${itemList[it].id}")

                            // 해당 스터디 상세정보 화면으로 이동
                            navController.navigate("studyDetailScreen")

                        },
                    shape = RoundedCornerShape(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(
                        1.dp,
                        colorResource(R.color.border_light_color)
                    )
                ) {
                    StudyCardInfo(itemList[it])
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        PageIndicator(
            pageCount = itemList.size,
            currentPage = pagerState.currentPage
        )
    }

}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) {
            IndicatorDots(isSelected = it == currentPage)
        }
    }
}

@Composable
private fun IndicatorDots(isSelected: Boolean) {
    val dotSize = animateDpAsState(targetValue = if (isSelected) 10.dp else 8.dp, label = "")
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(dotSize.value)
            .clip(CircleShape)
            .background(
                if (isSelected) colorResource(R.color.primary_color)
                else Color(0xFFD9D9D9)
            )
    )
}

@Composable
private fun FindActionButton(onNavigatePerson:() -> Unit, onNavigateStudy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 32.dp)
    ) {
        Card(
            modifier = Modifier
                .width(130.dp)
                .height(140.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = true
                )
                .clickable {
                    onNavigatePerson()
                },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, colorResource(R.color.border_light_color)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_person),
                    "",
                    modifier = Modifier.size(68.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "스터디원 구인",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Card(
            modifier = Modifier
                .width(130.dp)
                .height(140.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = true
                )
                .clickable {
                    onNavigateStudy()
                },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, colorResource(R.color.border_light_color)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_study),
                    "",
                    modifier = Modifier.size(68.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "스터디 찾기",
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    fontSize = 14.sp
                )
            }
        }
    }
}

