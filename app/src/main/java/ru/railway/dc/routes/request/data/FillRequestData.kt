package ru.railway.dc.routes.request.data

import android.content.Context
import ru.railway.dc.routes.App
import ru.railway.dc.routes.R
import ru.railway.dc.routes.request.model.Station
import java.text.SimpleDateFormat
import java.util.*

class FillRequestData {

    companion object {
        const val KEY_PREF_B_STATION = "b_station"
        const val KEY_PREF_E_STATION = "e_station"

        private const val CURRENT = "current"

        fun newInstance(c: Context): RequestData {
            val rd = RequestData()
            // Start date
            var bDate = c.resources.getString(R.string.search_value_bdate)
            if (bDate == CURRENT) {
                bDate = SimpleDateFormat(RequestData.FORMAT_DATE).format(Date(System.currentTimeMillis()))
            }
            rd.setbDate(bDate)
            // End date
            var eDate = c.resources.getString(R.string.search_value_edate)
            if (eDate == CURRENT) {
                eDate = SimpleDateFormat(RequestData.FORMAT_DATE).format(Date(System.currentTimeMillis()))
            }
            rd.seteDate(eDate)
            // Begin station
            val bStationDef = c.resources.getString(R.string.search_placeholder_bString)
            rd.setbStation(Station(0, App.pref.getString(KEY_PREF_B_STATION, bStationDef), null))
            // End station
            val eStationDef = c.resources.getString(R.string.search_placeholder_eString)
            rd.seteStation(Station(0, App.pref.getString(KEY_PREF_E_STATION, eStationDef), null))
            return rd
        }
    }


}