package com.example.firstproject.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstproject.R
import com.example.firstproject.ui.theme.pretendard
import com.example.firstproject.utils.TopicTagEnum

@Composable
fun SelectTopicCard(
    selectedTag: String?,
    onTagSelected: (String) -> Unit
) {

    val tagList = mutableListOf(
        "웹 프론트",
        "백엔드",
        "모바일",
        "인공지능",
        "빅데이터",
        "임베디드",
        "인프라",
        "CS 이론",
        "알고리즘",
        "게임",
        "기타"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        border = BorderStroke(1.dp, color = colorResource(R.color.border_input_color)),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TagGrid(
                tagList = tagList,
                selectedTag = selectedTag,
                onTagSelected = onTagSelected
            )
        }

    }
}

@Composable
fun TagGrid(
    tagList: List<String>,
    selectedTag: String?,
    onTagSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), // 행 간 간격
    ) {
        tagList.chunked(3).forEach { rowTags ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp) // 열 간 간격
            ) {
                rowTags.forEach { tagName ->
                    val tag = TopicTagEnum.fromTitle(tagName)
                    if (selectedTag == tagName) {
                        if (tag != null) {
                            SelectedStackTag(
                                stackTitle = tag.title,
                                tint = colorResource(tag.colorId)
                            )
                        }
                    } else {
                        CommonStackTag(stackTitle = tagName, onClick = { onTagSelected(tagName) })
                    }
                }
            }
        }
    }
}

// 태그 아이템
@Composable
fun SelectedStackTag(stackTitle: String, tint: Color) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(28.dp)
            .background(tint, RoundedCornerShape(50.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            color = Color.White,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 13.5.sp
        )

    }
}

@Composable
fun CommonStackTag(stackTitle: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(28.dp)
            .background(Color.White, RoundedCornerShape(50.dp))
            .border(BorderStroke(0.5.dp, Color.Black), shape = RoundedCornerShape(50.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stackTitle,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            fontSize = 13.5.sp
        )

    }
}
