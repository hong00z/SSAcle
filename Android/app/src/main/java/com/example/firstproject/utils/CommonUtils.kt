package com.example.firstproject.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtils {
    // 날짜 포맷터 (객체 재사용)
    private val dateFormatYMDHM = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    private val dateFormatYMD = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    // 1) 기본 파서 (서버에서 오는 "yyyy-MM-dd'T'HH:mm:ss.SSS" 형태)
    private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    // 2) 출력 포맷 (원하는 "yyyy.MM.dd. / HH:mm")
    private val outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")

    /**
     *  "2025-02-17T07:37:55.817" → "2025.02.17. / 07:37"
     */
    fun formatDateTime(input: String): String {
        return try {
            // 1) 문자열을 LocalDateTime으로 파싱
            val localDateTime = LocalDateTime.parse(input, inputFormatter)
            // 2) 원하는 포맷으로 변환
            localDateTime.format(outputFormatter)
        } catch (e: Exception) {
            // 파싱 에러 시, 예외 처리하거나 기본값 반환
            ""
        }
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

    fun formatKoreanTime(isoTime: String): String {
        // ISO 8601 형식 문자열을 Instant로 파싱 (UTC 기준)
        val instant = Instant.parse(isoTime)
        // Instant를 서울 시간대로 변환
        val seoulTime = instant.atZone(ZoneId.of("Asia/Seoul"))
        // 포맷 패턴 설정 (예: 오후 4:30 형식)
        val formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
        return seoulTime.format(formatter)
    }

    fun formatDateForHeader(isoTime: String): String {
        // ISO 8601 문자열을 Instant로 파싱 후 한국 시간대로 변환
        val instant = Instant.parse(isoTime)
        val seoulZonedDateTime = instant.atZone(ZoneId.of("Asia/Seoul"))
        // LocalDate로 변환 후 원하는 형식으로 포맷 (예: "2025년 2월 9일")
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN)
        return seoulZonedDateTime.toLocalDate().format(formatter)
    }

    fun parseIsoTimeToMillis(isoTime: String): Long {
        return try {
            Instant.parse(isoTime).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}