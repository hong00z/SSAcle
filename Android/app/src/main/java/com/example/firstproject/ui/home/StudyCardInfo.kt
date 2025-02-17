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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firstproject.MyApplication
import com.example.firstproject.R
import com.example.firstproject.data.model.dto.response.Member
import com.example.firstproject.data.model.dto.response.MyJoinedStudyListDtoItem
import com.example.firstproject.data.model.dto.response.StudyInfo
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum
import kotlin.math.min

@Composable
fun StudyCardInfo(studyInfo: MyJoinedStudyListDtoItem) {
    val isHost = studyInfo.createdBy == MyApplication.USER_ID

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
                studyInfo.studyName,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                fontSize = 17.sp,
                modifier = Modifier.fillMaxWidth(0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))

            // 방장인지 표시
            Box(modifier = Modifier.size(22.dp)) {
                if (isHost) {
                    Image(
                        painter = painterResource(R.drawable.icon_host), null,
                        modifier = Modifier
                            .size(22.dp)
                    )
                }
            }
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

            JoinProfiles(studyInfo.memberCount, studyInfo.members)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${studyInfo.memberCount}명 참여 중",
                color = Color(0xFF666666),
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                fontSize = 11.sp,
            )

        }


    }
}

@Composable
fun JoinProfiles(personNum: Int, memberList: List<Member>) {

    val maxNum = 4
    val profileCount = min(personNum, maxNum)
    val showMoreIcon = personNum > maxNum


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {

        repeat(profileCount) { index ->

            ProfileImgItem(
                image = memberList[index].image,
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

@Composable
private fun ProfileImgItem(
    image: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(
                color = Color.Gray, shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(model = image, contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.clip(CircleShape).fillMaxSize(),
            placeholder = painterResource(R.drawable.img_default_profile), // 로딩 중 이미지
            error = painterResource(R.drawable.img_default_profile), // 실패 이미지


        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestPreview() {

}