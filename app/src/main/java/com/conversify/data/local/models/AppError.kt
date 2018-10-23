package com.conversify.data.local.models

sealed class AppError {
    data class ApiError(val statusCode: Int, val message: String) : AppError()
    data class ApiUnauthorized(val message: String) : AppError()
    data class ApiFailure(val message: String) : AppError()
}