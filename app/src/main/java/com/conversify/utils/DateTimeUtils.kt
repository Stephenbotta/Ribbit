package com.conversify.utils

import android.content.Context
import com.conversify.R
import com.conversify.extensions.isSameDay
import com.conversify.extensions.isSameWeek
import com.conversify.extensions.isSameYear
import com.conversify.extensions.isYesterday
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

object DateTimeUtils {
    private val CHAT_MESSAGE_FULL_DATE_TIME_FORMATTER by lazy { DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a") }
    private val CHAT_MESSAGE_SAME_DAY_FORMATTER by lazy { DateTimeFormatter.ofPattern("hh:mm a") }
    private val CHAT_MESSAGE_SAME_WEEK_FORMATTER by lazy { DateTimeFormatter.ofPattern("EEEE") }
    private val CHAT_MESSAGE_SAME_YEAR_FORMATTER by lazy { DateTimeFormatter.ofPattern("MMM dd") }
    private val CHAT_MESSAGE_DATE_HEADER_FORMATTER by lazy { DateTimeFormatter.ofPattern("EEE · MMM dd") }
    private val CHAT_MESSAGE_OTHER_FORMATTER by lazy { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    fun formatMillisToDuration(input: Long): String {
        val seconds = input / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun parseServerToZonedDateTime(dateTimeMillis: Long): ZonedDateTime? {
        return try {
            Instant.ofEpochMilli(dateTimeMillis)
                    .atZone(ZoneId.systemDefault())
        } catch (exception: Exception) {
            Timber.w(exception)
            null
        }
    }

    private fun formatServerToLocalTimeZone(dateTimeMillis: Long?, displayFormatter: DateTimeFormatter): String {
        // Return empty string if provided date is null or blank
        if (dateTimeMillis == null) return ""

        return try {
            // Get zoned date time from server date (UTC)
            val zonedDateTime = parseServerToZonedDateTime(dateTimeMillis) ?: return ""

            zonedDateTime.format(displayFormatter)
        } catch (exception: Exception) {
            Timber.w(exception)
            ""
        }
    }

    fun getFormattedChatMessageTime(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_SAME_DAY_FORMATTER) ?: ""
    }

    fun getFormattedChatListingTime(dateTimeMillis: Long?, context: Context): String {
        if (dateTimeMillis == null) return ""

        val zonedDateTime = parseServerToZonedDateTime(dateTimeMillis)
        zonedDateTime ?: return ""

        val zonedDateTimeNow = ZonedDateTime.now()
        return when {
            zonedDateTime.isSameDay(zonedDateTimeNow) -> {
                zonedDateTime.format(CHAT_MESSAGE_SAME_DAY_FORMATTER)
            }

            zonedDateTime.isYesterday(zonedDateTimeNow) -> {
                context.getString(R.string.yesterday)
            }

            zonedDateTime.isSameWeek(zonedDateTimeNow) -> {
                zonedDateTime.format(CHAT_MESSAGE_SAME_WEEK_FORMATTER)
            }

            zonedDateTime.isSameYear(zonedDateTimeNow) -> {
                zonedDateTime.format(CHAT_MESSAGE_SAME_YEAR_FORMATTER)
            }

            else -> {
                zonedDateTime.format(CHAT_MESSAGE_OTHER_FORMATTER)
            }
        }
    }

    fun getFormattedChatDateHeader(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_DATE_HEADER_FORMATTER) ?: ""
    }

    fun getFormattedVenueDateTime(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_FULL_DATE_TIME_FORMATTER) ?: ""
    }
}