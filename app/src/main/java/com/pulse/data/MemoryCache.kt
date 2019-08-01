package com.pulse.data

import com.pulse.data.remote.models.loginsignup.InterestDto

object MemoryCache {
    private val INTERESTS by lazy { mutableListOf<InterestDto>() }

    fun hasInterests(): Boolean = INTERESTS.isNotEmpty()

    fun getInterests(): List<InterestDto> = INTERESTS.map { it.copy(selected = false) }

    fun updateInterests(interests: List<InterestDto>) {
        INTERESTS.clear()
        INTERESTS.addAll(interests)
    }
}