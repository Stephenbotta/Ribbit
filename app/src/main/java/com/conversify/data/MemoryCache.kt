package com.conversify.data

import com.conversify.data.remote.models.loginsignup.InterestDto

object MemoryCache {
    private val INTERESTS by lazy { mutableListOf<InterestDto>() }

    fun getInterests(): List<InterestDto> = INTERESTS

    fun updateInterests(interests: List<InterestDto>) {
        INTERESTS.clear()
        INTERESTS.addAll(interests)
    }
}