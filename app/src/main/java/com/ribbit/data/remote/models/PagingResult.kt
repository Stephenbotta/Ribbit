package com.ribbit.data.remote.models

data class PagingResult<out T>(
        val isFirstPage: Boolean,
        val result: T? = null)