package com.pulse.extensions

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.IsoFields

fun ZonedDateTime.isSameDay(other: ZonedDateTime): Boolean {
    return year == other.year && dayOfYear == other.dayOfYear
}

fun ZonedDateTime.isYesterday(other: ZonedDateTime): Boolean {
    return year == other.year && dayOfYear + 1 == other.dayOfYear
}

fun ZonedDateTime.isSameWeek(other: ZonedDateTime): Boolean {
    return year == other.year && get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == other.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
}

fun ZonedDateTime.isSameYear(other: ZonedDateTime): Boolean {
    return year == other.year
}