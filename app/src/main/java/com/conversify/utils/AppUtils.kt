package com.conversify.utils

import java.util.regex.Pattern

object AppUtils {
    private val hashTagsPattern by lazy { Pattern.compile("(#\\w+)\\b") }
    private val usernameMentionsPattern by lazy { Pattern.compile("(@\\w+)\\b") }

    fun getHashTagsFromString(input: String): List<String> {
        return getMatchedResultFromString(input, hashTagsPattern)
    }

    fun getMentionsFromString(input: String): List<String> {
        return getMatchedResultFromString(input, usernameMentionsPattern)
    }

    private fun getMatchedResultFromString(input: String, pattern: Pattern, distinctResults: Boolean = true): List<String> {
        val results = mutableListOf<String>()
        val matcher = pattern.matcher(input)

        while (matcher.find()) {
            results.add(matcher.group(1))
        }

        return if (distinctResults) {
            results.distinct()
        } else {
            results
        }
    }

    fun getFormattedAddress(locationName: String?, locationAddress: String?): String {
        return when {
            !locationName.isNullOrBlank() && !locationAddress.isNullOrBlank() -> {
                String.format("%s, %s", locationName, locationAddress)
            }

            !locationName.isNullOrBlank() -> locationName

            !locationAddress.isNullOrBlank() -> locationAddress

            else -> ""
        }
    }
}