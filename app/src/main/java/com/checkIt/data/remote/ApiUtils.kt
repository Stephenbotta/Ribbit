package com.checkIt.data.remote

import com.checkIt.data.local.models.AppError
import org.json.JSONObject
import retrofit2.Response

object ApiUtils {
    private fun getErrorMessage(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) {
            return ""
        }

        return try {
            val errorJsonObject = JSONObject(errorJson)
            errorJsonObject.getString("message")
        } catch (exception: Exception) {
            ""
        }
    }

    fun getError(statusCode: Int, errorJson: String?): AppError {
        val message = getErrorMessage(errorJson)
        return if (statusCode == 401) {
            AppError.ApiUnauthorized(message)
        } else {
            AppError.ApiError(statusCode, message)
        }
    }
}

fun Throwable.failureAppError(): AppError {
    return AppError.ApiFailure(localizedMessage ?: "")
}

fun <T> Response<T>.getAppError(): AppError {
    return ApiUtils.getError(code(), errorBody()?.string())
}