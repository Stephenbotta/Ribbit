package com.pulse.utils

import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.models.notifications.NotificationDto
import java.util.regex.Pattern

object AppUtils {
    private const val PREFIX_HASHTAG = "#"
    private const val PREFIX_MENTION = "@"
    private val PATTERN_HASHTAGS by lazy { Pattern.compile("(#\\w+)\\b") }
    private val PATTERN_MENTIONS by lazy { Pattern.compile("(@\\w+)\\b") }

    /**
     * @param includePrefix - Whether to keep the prefix (#) in the result list
     * */
    fun getHashTagsFromString(input: String, includePrefix: Boolean = true): List<String> {
        return getMatchedResultFromString(input, PATTERN_HASHTAGS, PREFIX_HASHTAG, includePrefix)
    }

    /**
     * @param includePrefix - Whether to keep the prefix (@) in the result list
     * */
    fun getMentionsFromString(input: String, includePrefix: Boolean = true): List<String> {
        return getMatchedResultFromString(input, PATTERN_MENTIONS, PREFIX_MENTION, includePrefix)
    }

    private fun getMatchedResultFromString(input: String, pattern: Pattern, prefix: String,
                                           includePrefix: Boolean = true, distinctResults: Boolean = true): List<String> {
        val results = mutableListOf<String>()
        val matcher = pattern.matcher(input)

        while (matcher.find()) {
            val result = if (includePrefix) {
                matcher.group(1)
            } else {
                matcher.group(1).removePrefix(prefix)
            }
            results.add(result)
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

    fun isRequestForVenue(notification: NotificationDto): Boolean {
        return notification.type == ApiConstants.NOTIFICATION_TYPE_REQUEST_JOIN_VENUE
                || notification.type == ApiConstants.NOTIFICATION_TYPE_INVITE_JOIN_VENUE
    }

    fun fixHashTags(hashTags: List<String>): List<String> {
        return hashTags.map { hashTag ->
            if (hashTag.startsWith(PREFIX_HASHTAG)) {
                hashTag
            } else {
                PREFIX_HASHTAG.plus(hashTag)
            }
        }
    }

    fun fixUsernameMentions(mentions: List<String>): List<String> {
        return mentions.map { mention ->
            if (mention.startsWith(PREFIX_MENTION)) {
                mention
            } else {
                PREFIX_MENTION.plus(mention)
            }
        }
    }
}