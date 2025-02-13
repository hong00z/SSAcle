package com.example.firstproject.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.StudyInfo
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum
import kotlin.math.min

@Composable
fun StudyCardInfo(studyInfo: StudyInfo) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                studyInfo.title,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 17.sp,
                modifier = Modifier.fillMaxWidth(0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.icon_host), null,
                modifier = Modifier
                    .size(22.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tag = TopicTagEnum.fromTitle(studyInfo.topic)
            ListStackTag(stackTitle = tag!!.title, tint = colorResource(tag.colorId))
            Spacer(Modifier.weight(1f))

            JoinProfiles(studyInfo.personNum)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${studyInfo.personNum}명 참여 중",
                color = Color(0xFF666666),
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                fontSize = 11.sp,
            )

        }


    }
}

@Composable
fun JoinProfiles(personNum: Int) {
    val joinList = mutableListOf(
        R.drawable.img_default_profile,
        R.drawable.img_default_profile_5,
        R.drawable.img_default_profile,
        R.drawable.img_default_profile_5,
        R.drawable.img_default_profile,
        R.drawable.img_default_profile_5,
        R.drawable.img_default_profile,
        R.drawable.img_default_profile_5,
        R.drawable.img_default_profile,
        R.drawable.img_default_profile_5,
    )

    val maxNum = 4
    val profileCount = min(personNum, maxNum)
    val showMoreIcon = personNum > maxNum


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        repeat(profileCount) { index ->
            ProfileItem(
                imgId = joinList[index],
                modifier = Modifier.offset(x = (6 * (profileCount - index)).dp)
            )
        }

        if (showMoreIcon) {
            Image(
                painter = painterResource(R.drawable.img_more),
                null,
                modifier = Modifier
                    .size(14.dp)
            )
        }
    }
}

@Composable
private fun ProfileItem(
    imgId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(color = Color.LightGray, shape = CircleShape)
    ) {
        Image(painter = painterResource(imgId), null)
    }
}

@Preview(showBackground = true)
@Composable
fun TestPreview() {
    val myStudyList = mutableListOf<StudyInfo>(
        StudyInfo("스프링 입문 스터디", "백엔드", 8, true, true),

        StudyInfo("스터디 제목은 과연 몇 글자까지 가능할까요?", "알고리즘", 3, false, false),

        StudyInfo("스터디 제목", "모바일", 5, true, false),
    )


    StudyCardInfo(myStudyList[0])
//    JoinProfiles(myStudyList[0].personNum)

}