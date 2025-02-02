package com.example.firstproject.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Context
import android.widget.Toast

object CommonUtils {
    // 날짜 포맷터 (객체 재사용)
    private val dateFormatYMDHM = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    private val dateFormatYMD = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    // Long 타입 날짜 포맷
    fun longDateFormatHHMM(time: Long): String {
        return dateFormatYMDHM.format(Date(time))
    }

    // Date 타입 날짜 포맷
    fun dateFormatHHMM(time: Date): String {
        return dateFormatYMDHM.format(time)
    }

    fun dateFormatYMD(time: Date): String {
        return dateFormatYMD.format(time)
    }

    // 토스트 메시지
    fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}