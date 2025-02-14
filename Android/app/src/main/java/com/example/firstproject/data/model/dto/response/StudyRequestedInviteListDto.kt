package com.example.firstproject.data.model.dto.response

// GET/api/studies/{studyId}/wishList 스터디내 초대 현황 (Item)으로 가면 data class 있음
class StudyRequestedInviteListDto : ArrayList<StudyRequestedInviteListDtoItem>()