package com.example.firstproject.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    @SerializedName("_id") val id: String,
    @SerializedName("studyId") val studyId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("message") val message: String,
    @SerializedName("isInOut") val isInOut: Boolean,
    @SerializedName("createdAt") val createdAt: String
) : Parcelable