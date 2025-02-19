package com.example.firstproject.data.model.dto.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyAppliedStudyListDtoItem(
    val count: Int,
    val meetingDays: ArrayList<String>,
    val members: ArrayList<String>,
    val studyId: String,
    val studyName: String,
    val topic: String
) : Parcelable