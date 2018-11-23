package com.conversify.utils

import java.util.regex.Pattern

object AppUtils {
    private val hashTagsPattern = Pattern.compile("(#\\w+)\\b")

    fun getTagsFromString(input: String): List<String> {
        val tags = mutableListOf<String>()
        val matcher = hashTagsPattern.matcher(input)

        while (matcher.find()) {
            tags.add(matcher.group(1))
        }

        return tags.distinct()
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