package com.example.firstproject.data.model.dto.response.common

data class CommonResponseDTO<T>(
    val code: Int,
    val message: String,
    val data: T?
)