package com.pulse.data.remote.models

data class ApiResponse<out T>(
        val message: String? = null,
        val data: T? = null)