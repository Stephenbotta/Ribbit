package com.ribbit.extensions

import android.util.Log
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.IsoFields
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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


fun parseDob(double:Long?):String {

    if (double == null || double.toInt() == 0){

        return "Select date of birth"
    }


    val date = Date(double)
    val formatter: DateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)

    Log.d("ddoobb",formatter.format(date))

    return formatter.format(date)
}