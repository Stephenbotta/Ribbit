package com.checkIt.data.local.models

sealed class AppError {
    object WaitingForNetwork : AppError()
    data class GeneralError(val message: String) : AppError()
    data class ApiError(val statusCode: Int, val message: String) : AppError()
    data class ApiUnauthorized(val message: String) : AppError()
    data class ApiFailure(val message: String) : AppError()
    data class FileUploadFailed(val id: String) : AppError()
}