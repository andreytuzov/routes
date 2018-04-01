package ru.railway.dc.routes.utils

import java.text.SimpleDateFormat
import java.util.*

fun Calendar.compose(day: Int? = null, month: Int? = null, year: Int? = null, hour: Int? = null, minute: Int? = null): Calendar {
    day?.let { set(Calendar.DAY_OF_MONTH, it) }
    month?.let { set(Calendar.MONTH, it) }
    year?.let { set(Calendar.YEAR, it) }
    hour?.let { set(Calendar.HOUR_OF_DAY, it) }
    minute?.let { set(Calendar.MINUTE, it) }
    return this
}

fun Calendar.composeDMY(day: Int, month: Int, year: Int): Calendar {
    set(Calendar.DAY_OF_MONTH, day)
    set(Calendar.MONTH, month)
    set(Calendar.YEAR, year)
    return this
}

fun Calendar.composeDMY(calendarDMY: Calendar): Calendar {
    set(Calendar.DAY_OF_MONTH, calendarDMY.get(Calendar.DAY_OF_MONTH))
    set(Calendar.MONTH, calendarDMY.get(Calendar.MONTH))
    set(Calendar.YEAR, calendarDMY.get(Calendar.YEAR))
    return this
}

fun Calendar.composeHM(hour: Int, minute: Int): Calendar {
    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    return this
}

fun Calendar.composeHM(calendarDMY: Calendar): Calendar {
    set(Calendar.HOUR_OF_DAY, calendarDMY.get(Calendar.HOUR_OF_DAY))
    set(Calendar.MINUTE, calendarDMY.get(Calendar.MINUTE))
    return this
}

object DateUtils {

    const val FORMAT_DATE = "yyyy-MM-dd"
    const val FORMAT_SHORT_DATE = "dd.MM"
    const val FORMAT_TIME = "HH:mm"
    const val FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm"

    fun nowCalendar() = createCalendar(System.currentTimeMillis())

    fun createCalendar(timeInMillis: Long) =
            Calendar.getInstance().apply {
                setTimeInMillis(timeInMillis)
            }

    fun createCalendar(time: String, format: String) =
            Calendar.getInstance().apply {
                setTime(SimpleDateFormat(format).parse(time))
            }

    fun format(calendar: Calendar, format: String) =
            SimpleDateFormat(format).format(Date(calendar.timeInMillis))

    fun format(timeInMillis: Long, format: String) =
            SimpleDateFormat(format).format(Date(timeInMillis))

    fun composeDMY(calendar: Calendar, day: Int, month: Int, year: Int) =
            calendar.apply {
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.MONTH, month)
                set(Calendar.YEAR, year)
            }

    fun composeDMY(calendar: Calendar, calendarDMY: Calendar) =
            calendar.apply {
                set(Calendar.DAY_OF_MONTH, calendarDMY.get(Calendar.DAY_OF_MONTH))
                set(Calendar.MONTH, calendarDMY.get(Calendar.MONTH))
                set(Calendar.YEAR, calendarDMY.get(Calendar.YEAR))
            }

    fun composeHM(calendar: Calendar, hour: Int, minute: Int) =
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

    fun composeHM(calendar: Calendar, calendarDMY: Calendar) =
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, calendarDMY.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, calendarDMY.get(Calendar.MINUTE))
            }
}