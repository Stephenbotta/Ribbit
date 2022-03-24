package com.ribbit.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import org.threeten.bp.ZonedDateTime

class DateTimePicker(private val context: Context,
                     private val previousDateTime: ZonedDateTime? = null,
                     private val minDateMillis: Long? = null,
                     private val maxDateMillis: Long? = null,
                     private val callback: (ZonedDateTime) -> Unit) {
    private var result = ZonedDateTime.now()

    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        /*
        * Update the date field in start and end time. Date picker's month is (0-11) and
        * ZonedDateTime's month is (1-12), therefore adding 1 to the date picker's month.
        * */
        result = result.withYear(year)
                .withMonth(month + 1)
                .withDayOfMonth(dayOfMonth)

        showTimePicker()
    }

    fun show() {
        val datePickerDialog = if (previousDateTime == null) {
            // If previous date is not selected then set today's date
            DatePickerDialog(context, dateSetListener, result.year, result.monthValue - 1, result.dayOfMonth)
        } else {
            // If previous date is selected then use it as displayed date
            DatePickerDialog(context, dateSetListener, previousDateTime.year, previousDateTime.monthValue - 1, previousDateTime.dayOfMonth)
        }
        if (minDateMillis != null) {
            datePickerDialog.datePicker.minDate = minDateMillis
        }
        if (maxDateMillis != null) {
            datePickerDialog.datePicker.maxDate = maxDateMillis
        }
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val displayedHour: Int
        val displayedMinute: Int

        if (previousDateTime == null) {
            displayedHour = result.hour
            displayedMinute = result.minute
        } else {
            displayedHour = previousDateTime.hour
            displayedMinute = previousDateTime.minute
        }

        val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            result = result.withHour(hourOfDay)
                    .withMinute(minute)
                    .withSecond(0)
            callback(result)
        }, displayedHour, displayedMinute, false)
        timePickerDialog.show()
    }
}