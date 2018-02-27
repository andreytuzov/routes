package ru.railway.dc.routes.setting

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast
import ru.railway.dc.routes.App
import ru.railway.dc.routes.R
import ru.railway.dc.routes.database.utils.CashDetailTableUtils
import ru.railway.dc.routes.database.utils.CashTableUtils
import ru.railway.dc.routes.database.utils.EventTableUtils
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.tools.AppUtils

class MyPreferenceFragment : PreferenceFragment(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    companion object {
        private val TASK_CLEAR_CASH = 0
        private val TASK_CLEAR_EVENT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.activity_preference)

        findPreference(getString(R.string.pref_name_clearcash)).onPreferenceClickListener = this
        findPreference(getString(R.string.pref_name_clearevent)).onPreferenceClickListener = this
        findPreference(getString(R.string.pref_name_period)).onPreferenceChangeListener = this
        findPreference(getString(R.string.pref_name_stationtime)).onPreferenceChangeListener = this
        findPreference(getString(R.string.pref_name_btime)).onPreferenceChangeListener = this
        findPreference(getString(R.string.pref_name_etime)).onPreferenceChangeListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val title = preference.key.toString()
        var key = -1
        if (title == getString(R.string.pref_name_clearcash)) {
            key = TASK_CLEAR_CASH
        } else if (title == getString(R.string.pref_name_clearevent)) {
            key = TASK_CLEAR_EVENT
        }
        MyTask(getActivity()).execute(*arrayOf(key))
        return false
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val title = preference.key.toString()
        val value = newValue as String
        if (title == getString(R.string.pref_name_period) && value.matches("^\\d+$".toRegex())) {
            // Проверка корректности введенных данных
            val period = Integer.valueOf(value)!!
            if (period < 1) {
                return false
            }
            updatePeriodTime(period)
        } else if (title == getString(R.string.pref_name_stationtime) && value.matches("^\\d+$".toRegex())) {
            updateStationTime(Integer.valueOf(value)!!)
        } else if ((title == getString(R.string.pref_name_btime) || title == getString(R.string.pref_name_etime)) && value.matches("^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$".toRegex())) {
        } else {
            return false
        }
        return true
    }

    private fun updateStationTime(stationTime: Int) {
        App.requestData.duration = stationTime
    }

    private fun updatePeriodTime(period: Int) {
        AppUtils.stopEventService()
        AppUtils.startEventService()
    }


    class MyTask(private val context: Context) : AsyncTask<Int, Void, Int>() {

        override fun doInBackground(vararg params: Int?): Int {
            val key = params[0]
            when (key) {
                TASK_CLEAR_CASH -> {
                    CashTableUtils.clearData()
                    CashDetailTableUtils.clearData()
                }
                TASK_CLEAR_EVENT -> EventTableUtils.removeAll()
            }
            return key!!
        }

        override fun onPostExecute(key: Int?) {
            when (key) {
                TASK_CLEAR_CASH -> Toast.makeText(context, "Очистка кэша завершена", Toast.LENGTH_SHORT).show()
                TASK_CLEAR_EVENT -> Toast.makeText(context, "Очистка событий завершена", Toast.LENGTH_SHORT).show()
            }
        }
    }
}