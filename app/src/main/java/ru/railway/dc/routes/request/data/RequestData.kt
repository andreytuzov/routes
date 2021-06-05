package ru.railway.dc.routes.request.data

import android.content.Context
import ru.railway.dc.routes.App
import ru.railway.dc.routes.Constant
import ru.railway.dc.routes.R
import ru.railway.dc.routes.request.model.Station
import ru.railway.dc.routes.utils.DateUtils
import java.util.*

class RequestData {

    // Properties
    var duration: Int = 0
        set(value) {
            field = value
            notification(DURATION)
        }

    var bStation: Station? = null
        set(value) {
            field = value
            notification(B_STATION)
        }

    var eStation: Station? = null
        set(value) {
            field = value
            notification(E_STATION)
        }

    var bDateTime: Calendar? = null
        set(value) {

            val maxBValue = DateUtils.composeHM(DateUtils.createCalendar(value!!.timeInMillis), 23, 29)
            field = if (value.after(maxBValue)) {
                maxBValue
            } else {
                value
            }

            if (eDateTime != null) {
                eDateTime = DateUtils.composeDMY(eDateTime!!, field!!)
                val minEValue = DateUtils.createCalendar(field!!.timeInMillis).apply {
                    add(Calendar.MINUTE, 30)
                }
                val maxEValue = DateUtils.composeHM(DateUtils.createCalendar(value.timeInMillis), 23, 59)
                if (eDateTime!!.before(minEValue)) {
                    eDateTime = minEValue
                } else if (eDateTime!!.after(maxEValue)) {
                    eDateTime = maxEValue
                }
            }
            notification(DATETIME)
        }

    var eDateTime: Calendar? = null
        set(value) {
            field = value
            notification(DATETIME)
        }

    var currentDateTime: Boolean = true
        set(value) {
            field = value
            notification(DATETIME)
        }

    var stationMap = mutableMapOf<Station, Int>()
        private set

    fun addStation(station: Station, duration: Int = this.duration) {
        stationMap.put(station, duration)
        notification(I_STATION)
    }

    // Listeners

    private var listeners = mutableListOf<OnChangeDataListener>()

    interface OnChangeDataListener {
        fun changeData(param: String)
    }

    fun register(listener: OnChangeDataListener) {
        listeners.filter { it.javaClass.name == listener.javaClass.name }
                .forEach { listeners.remove(it) }
        listeners.add(listener)
    }

    fun unregister(listener: OnChangeDataListener) {
        listeners.remove(listener)
    }

    private fun notification(param: String) {
        listeners.forEach {
            it.changeData(param)
        }
    }

    fun notificationAll() {
        listeners.forEach {
            it.changeData(B_STATION)
            it.changeData(E_STATION)
            it.changeData(DATETIME)
        }
    }

    companion object {

        private val DEFAULT_DURATION = 30

        const val B_STATION = "b_station"
        const val E_STATION = "e_station"
        const val I_STATION = "i_station"
        const val DURATION = "duration"
        const val DATETIME = "datetime"
        const val CURRENT_DATETIME = "current_datetime"

        fun newInstance(c: Context): RequestData {
            val rd = RequestData()

            rd.duration = App.pref.getInt(Constant.KEY_PREF_DURATION, DEFAULT_DURATION)

            // Start date
            val currentDate = App.pref.getBoolean(Constant.KEY_PREF_CURRENT_TIME, true)
            rd.bDateTime = if (currentDate)
                DateUtils.nowCalendar()
            else
                DateUtils.createCalendar(App.pref.getLong(Constant.KEY_PREF_B_DATETIME, System.currentTimeMillis()))

            // End date
            val eDateDef = DateUtils.composeHM(DateUtils.nowCalendar(), 23, 59).timeInMillis
            rd.eDateTime = DateUtils.createCalendar(App.pref.getLong(Constant.KEY_PREF_E_DATETIME, eDateDef))

            // Begin station
            val bStationDef = c.resources.getString(R.string.b_station_placeholder)
            rd.bStation = Station(0, App.pref.getString(Constant.KEY_PREF_B_STATION, bStationDef), null)

            // End station
            val eStationDef = c.resources.getString(R.string.e_station_placeholder)
            rd.eStation = Station(0, App.pref.getString(Constant.KEY_PREF_E_STATION, eStationDef), null)
            return rd
        }
    }
}