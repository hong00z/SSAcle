package com.example.firstproject.utils

import com.example.firstproject.R

enum class TopicTagEnum(val title: String, val colorId: Int) {
    FRONTEND("웹 프론트", R.color.frontend_stack_tag),
    BACKEND("백엔드", R.color.backend_stack_tag),
    MOBILE("모바일", R.color.mobile_stack_tag),
    AI("인공지능", R.color.ai_stack_tag),
    BIGDATA("빅데이터", R.color.data_stack_tag),
    EMBADED("임베디드", R.color.embaded_stack_tag),
    INFRA("인프라", R.color.infra_stack_tag),
    CS("CS 이론", R.color.cs_stack_tag),
    ALGO("알고리즘", R.color.algo_stack_tag),
    GAME("게임", R.color.game_stack_tag),
    ETC("기타", R.color.etc_stack_tag);

    companion object {
        fun fromTitle(title: String): TopicTagEnum? {
            return entries.find { it.title == title }
        }
    }
}