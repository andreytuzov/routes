package ru.railway.dc.routes.request.fragment.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.widget.DatePicker
import ru.railway.dc.routes.App
import ru.railway.dc.routes.utils.DateUtils
import java.util.*

class DateFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    companion object {
        private const val DATE_TIME = "date_time"

        fun newInstance(dateTime: Long) = DateFragment().apply {
            arguments = Bundle().apply {
                putLong(DATE_TIME, dateTime)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = DateUtils.createCalendar(arguments.getLong(DATE_TIME))
        return DatePickerDialog(activity, this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        App.requestData.bDateTime = DateUtils.composeDMY(App.requestData.bDateTime!!,
                dayOfMonth, monthOfYear, year)
    }
}