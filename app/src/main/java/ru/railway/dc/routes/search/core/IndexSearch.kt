package ru.railway.dc.routes.search.core

import ru.railway.dc.routes.App
import ru.railway.dc.routes.search.core.model.IndexSchedule
import ru.railway.dc.routes.search.core.model.ListIndexRoute
import ru.railway.dc.routes.search.core.search.UpIndexSearch
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.model.Schedule
import java.util.*

class IndexSearch(listRoute: ListRoute) {

    companion object {
        private const val COUNT_MILLIS_IN_MINUTE = 60000
    }

    private var time: Long = 0
    private var iDecoder: IndexDecoder
    private var durationIDs: MutableMap<Int, Long>? = null
    private lateinit var iStations: IntArray

    init {
        val durations = mutableMapOf<String, Int>()
        App.requestData.stationMap.entries.forEach {
            durations.put(it.key.name, it.value)
        }
        time = App.requestData.bDateTime!!.timeInMillis
        iDecoder = IndexDecoder(listRoute)
        convertDurationWithID(durations)
    }

    private fun loadIStations() {
        // Станции, которые используются в маршруте
        val bStation = App.requestData.bStation!!.name
        val eStation = App.requestData.eStation!!.name
        val stations = App.requestData.stationMap.keys.flatMap { listOf(it.name) }
        val count = stations.size

        iStations = IntArray(count + 2)
        // Начальная и конечная станции
        iStations[0] = iDecoder.getIdByStation(bStation)
        iStations[count + 1] = iDecoder.getIdByStation(eStation)

        // Промежуточные станции
        for (i in 0 until count) {
            iStations[i + 1] = iDecoder.getIdByStation(stations[i])
        }

        Arrays.sort(iStations, 1, count + 1)
    }

    // Преобразуем названия станции в их ID
    private fun convertDurationWithID(durations: Map<String, Int>) {
        // Создаем или очищаем карту
        if (durationIDs != null) {
            durationIDs!!.clear()
        } else {
            durationIDs = HashMap()
        }
        for (station in durations.keys) {
            val id = iDecoder.getIdByStation(station)
            durationIDs!!.put(id, durations[station]!!.toLong() * COUNT_MILLIS_IN_MINUTE)
        }
    }

    // Основной алгоритм поиска
    private fun getListIRoute(aISearch: UpIndexSearch): List<ListIndexRoute>? {
        aISearch.loadStations(iStations)

        val list = ArrayList<ListIndexRoute>()
        var listIRoute: ListIndexRoute? = null
        do {
            listIRoute = aISearch.listIRoute
            if (listIRoute != null) {
                list.add(listIRoute)
            }
        } while (listIRoute != null)

        return if (list == null || list.size == 0) {
            null
        } else list

    }

    private fun print() {
        val str = StringBuilder()
        // Станции
        str.append("\niStations = ")
        if (iStations != null) {
            for (i in iStations!!) {
                str.append(" " + i)
            }
        }
        println(str.toString())
    }

    // Алгоритм перебора
    operator fun next(): Boolean {
        val N = iStations!!.size - 1
        val S = 1
        var i = N - 2
        val temp: Int
        var index: Int
        while (i >= S && iStations[i] >= iStations[i + 1]) {
            i--
        }
        // Последняя перестановка
        if (i < S) {
            return false
        }
        // Обмен с минимальным, большим данного
        index = i + 1
        for (x in i + 1 until N) {
            if (iStations[i] < iStations[x] && iStations[index] > iStations[x]) {
                index = x
            }
        }

        temp = iStations[i]
        iStations[i] = iStations[index]
        iStations[index] = temp

        // Сортировка остальных
        Arrays.sort(iStations, i + 1, N)
        return true
    }

    fun getSchedule(): Schedule? {
        loadIStations()

        val aISearch = UpIndexSearch(iDecoder, durationIDs, time)
        val iSchedule = IndexSchedule()

        do {
            val list = getListIRoute(aISearch)
            if (list != null) {
                iSchedule.addAll(list)
            }
        } while (next())

        return iDecoder.decode(iSchedule)
    }


}