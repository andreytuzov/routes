package ru.railway.dc.routes.event.parse.method

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.model.Route
import ru.railway.dc.routes.utils.*
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

class HTMLParseDetail : IParseDetail {

    private var mCalendar = Calendar.getInstance()
    private var mCorrFactor = 0

    // Params after request
    private lateinit var bTimeElements: Elements
    private lateinit var eTimeElements: Elements
    private lateinit var stationElements: Elements

    private fun getFormatURL(url: String): String {
        var url = url
        val p = Pattern.compile("(?:train=)([^&]+).+(?:from=)([^&]+).+(?:to=)([^&]+)")
        val m = p.matcher(url)
        if (m.find())
            for (i in 1..m.groupCount())
                url = url.replace("=" + m.group(i), "=" + URLEncoder.encode(m.group(i), "UTF-8"))
        return url
    }

    private fun loadPage(url: String) {
        val doc = Jsoup.connect(url)
                .timeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .get()
        bTimeElements = doc.select(TAG_START_TIME)
        eTimeElements = doc.select(TAG_END_TIME)
        stationElements = doc.select(TAG_STATION)

        clearEmptyStations()
    }

    // Remove empty elements (train doesn't stop here)
    private fun clearEmptyStations() {
        val dBTimeElements = Elements()
        val dETimeElements = Elements()
        val dStationElements = Elements()
        for (i in bTimeElements.indices) {
            if (bTimeElements[i].text().trim().isEmpty() && eTimeElements[i].text().trim().isEmpty()) {
                dBTimeElements.add(bTimeElements[i])
                dETimeElements.add(eTimeElements[i])
                dStationElements.add(stationElements[i])
            }
        }
        bTimeElements.removeAll(dBTimeElements)
        eTimeElements.removeAll(dETimeElements)
        stationElements.removeAll(dStationElements)
    }

    // It's used in some ring routes
    private fun getCorrectFactor(calendar: Calendar): Int {
        val lastCalendar = parseCalendar(eTimeElements[eTimeElements.size - 1].text())
        val corrFactor = Math.round((calendar.timeInMillis - lastCalendar.timeInMillis).toFloat() / COUNT_MILLIS_IN_DAY)
        if (corrFactor < 0)
            return 0
        return corrFactor
    }

    override fun get(route: Route): ListRoute {
        loadPage(getFormatURL(String.format(URL_DETAIL, route.detailURI)))

        val routes = ListRoute()
        val bStation = route.bStation
        val eStation = route.eStation
        mCalendar = Calendar.getInstance().composeDMY(route.bTime).composeHM(0, 0)
        mCorrFactor = getCorrectFactor(route.eTime)

        // Data parsing
        for (i in 0 until bTimeElements.size - 1) {
            val r = Route()
            r.bEnterStation = bStation
            r.eEnterStation = eStation
            r.numberTrain = route.numberTrain
            r.typeTrain = route.typeTrain
            r.bStation = stationElements[i].text()
            r.eStation = stationElements[i + 1].text()

            val eCalendar = parseCalendar(eTimeElements[i + 1].text())
            r.setEDateTime(eCalendar)
            mCalendar = eCalendar

            val bCalendar = parseCalendar(bTimeElements[i].text())
            r.setBDateTime(bCalendar)
            if (eCalendar.before(bCalendar))
                bCalendar.composeDMY(eCalendar)
            if (eCalendar.before(bCalendar))
                bCalendar.add(Calendar.DAY_OF_MONTH, -1)

            routes.add(r)
        }

        return routes
    }

    // Parse data if need
    private fun parseCalendar(time: String): Calendar {
        val colon = time.indexOf(':')
        val hour = Integer.valueOf(time.substring(0, colon).trim())
        val minute = Integer.valueOf(time.substring(colon + 1, colon + 3).trim())
        val calendar = Calendar.getInstance().compose(hour = hour, minute = minute, year = mCalendar.get(Calendar.YEAR))
        if (time.contains(",")) {
            val time = time.replace(160.toChar(), ' ').trim()
            val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
            // Parse day, month and year
            val comma = time.indexOf(',')
            val space = time.lastIndexOf(' ')
            val day = Integer.valueOf(time.substring(comma + 1, space).trim())
            val sMonth = time.substring(space, time.length).trim()
            var month = -1
            for (i in 0 until months.size) {
                if (months[i].startsWith(sMonth)) {
                    month = i
                    break
                }
            }
            calendar.compose(day = day, month = month)
        } else {
            calendar.compose(day = mCalendar.get(Calendar.DAY_OF_MONTH), month = mCalendar.get(Calendar.MONTH))
            if (calendar.before(mCalendar))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        if (mCorrFactor != 0)
            calendar.add(Calendar.DAY_OF_MONTH, mCorrFactor)
        return calendar
    }

    companion object {
        private const val URL_DETAIL = "http://rasp.rw.by%1\$s"
        private const val USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"

        // Tags for parsing
        private const val TAG_START_TIME = "td.train_item.train_start"
        private const val TAG_END_TIME = "td.train_item.train_end"
        private const val TAG_STATION = "a.train_text"

        // Max time connection
        private const val TIMEOUT = 40000
        private const val COUNT_MILLIS_IN_DAY = 86_400_000
    }
}