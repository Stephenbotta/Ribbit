package com.conversify.utils

import android.content.Context
import android.text.format.DateUtils
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
    private val VENUE_FILTERS_DATE_LOCAL by lazy { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    private val VENUE_FILTERS_DATE_SERVER by lazy { DateTimeFormatter.ofPattern("MM/dd/yyyy") }

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

    private fun formatServerToLocalTimeZone(dateTimeMillis: Long?, displayFormatter: DateTimeFormatter): String? {
        // Return empty string if provided date is null or blank
        if (dateTimeMillis == null) return null

        return try {
            // Get zoned date time from server date (UTC)
            val zonedDateTime = parseServerToZonedDateTime(dateTimeMillis) ?: return null

            zonedDateTime.format(displayFormatter)
        } catch (exception: Exception) {
            Timber.w(exception)
            null
        }
    }

    fun formatChatMessageTime(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_SAME_DAY_FORMATTER) ?: ""
    }

    fun formatChatListingTime(zonedDateTime: ZonedDateTime?, context: Context): String {
        if (zonedDateTime == null) return ""

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

    fun formatChatDateHeader(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_DATE_HEADER_FORMATTER) ?: ""
    }

    fun formatVenueDateTime(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(CHAT_MESSAGE_FULL_DATE_TIME_FORMATTER) ?: ""
    }

    fun formatVenueFiltersDate(dateTimeMillis: Long?): String {
        return formatServerToLocalTimeZone(dateTimeMillis, VENUE_FILTERS_DATE_LOCAL) ?: ""
    }

    fun formatVenueFiltersDateForServer(dateTimeMillis: Long?): String? {
        return formatServerToLocalTimeZone(dateTimeMillis, VENUE_FILTERS_DATE_SERVER)
    }

    fun formatPeopleLocation(zonedDateTime: ZonedDateTime?): String {
        return zonedDateTime?.format(VENUE_FILTERS_DATE_LOCAL) ?: ""
    }

    fun formatPeopleRecentTime(zonedDateTime: String?): String {
        return DateUtils.getRelativeTimeSpanString(zonedDateTime?.toLong()
                ?: 0L, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString()
    }

}