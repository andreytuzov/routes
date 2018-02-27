package ru.railway.dc.routes.search.core

import ru.railway.dc.routes.App
import ru.railway.dc.routes.search.core.model.IndexRoute
import ru.railway.dc.routes.search.core.model.IndexSchedule
import ru.railway.dc.routes.search.core.model.ListIndexRoute
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.model.Route
import ru.railway.dc.routes.search.model.Schedule
import java.util.ArrayList
import java.util.HashMap

// Сохраняем для кодирования и декодирования

// Определяем количество разных станций

// Выделяем память под данные

// Заполнение карты id станций
// Декодирование
class IndexDecoder(private val listRoute: ListRoute) {

    private var stationIDs: MutableMap<String, Int>? = null
    private var listIRoute: Array<Array<ListIndexRoute>>

    private var bStation = App.requestData.bStation!!.name
    private var eStation = App.requestData.eStation!!.name
    private var stations = App.requestData.stationMap.keys.flatMap { listOf(it.name) }

    init {
        val distinct = ArrayList<String>()
        distinct.add(bStation)
        if (!distinct.contains(eStation)) {
            distinct.add(eStation)
        }
        stations
                .filterNot { distinct.contains(it) }
                .forEach { distinct.add(it) }
        val count = distinct.size
        listIRoute = Array(count, {
            Array<ListIndexRoute>(count, {
                ListIndexRoute()
            })
        })
        convertStationToIDs()
        encode()
    }

    // Получаем маршрут
    fun getListIndexRoute(iBStation: Int, iEStation: Int): ListIndexRoute {
        return listIRoute[iBStation][iEStation]
    }

    // =============== ДЕКОДИРОВАНИЕ ========================================

    // Декодирование всего расписания
    fun decode(iSchedule: IndexSchedule?): Schedule? {
        if (iSchedule == null) {
            return null
        }
        val schedule = Schedule()
        for (listIRoute in iSchedule) {
            schedule.add(decode(listIRoute))
        }
        return schedule
    }

    // Декодирование маршрутов одной группы
    fun decode(listIRoute: ListIndexRoute): ListRoute {
        val listRoute = ListRoute()
        for (iRoute in listIRoute) {
            listRoute.add(decode(iRoute))
        }
        return listRoute
    }

    // Декодирование маршрута
    fun decode(iRoute: IndexRoute): Route {
        return listRoute.get(iRoute.getiLRoute())
    }

    // ================ КОДИРОВАНИЕ =========================================

    // Кодирование всех маршрутов
    private fun encode() {
        for (i in 0 until listRoute.size()) {
            encode(listRoute.get(i), i)
        }
    }

    // Кодирование одного маршрута
    private fun encode(route: Route, iLRoute: Int) {
        // Кодируем информацию
        // Получаем ID станций
        val iBStation = getIdByStation(route.bEnterStation)
        val iEStation = getIdByStation(route.eEnterStation)
        val iBTime = route.bTime.timeInMillis
        val iETime = route.eTime.timeInMillis
        // Добавляем закодированный маршрут
        val iRoute = IndexRoute(iBTime, iETime, iLRoute)
        listIRoute[iBStation][iEStation].add(iRoute)
    }

    // Создание карты id станций
    private fun convertStationToIDs() {
        // Создаем или очищаем карту id станций
        if (stationIDs != null) {
            stationIDs!!.clear()
        } else {
            stationIDs = HashMap()
        }
        var index = 0
        // Добавляем начальную станцию
        stationIDs!!.put(bStation, index)
        // Добавляем промежуточные станции
        for (station in stations) {
            if (!stationIDs!!.containsKey(station)) {
                stationIDs!!.put(station, ++index)
            }
        }
        // Добавляем конечную станцию
        if (!stationIDs!!.containsKey(eStation)) {
            stationIDs!!.put(eStation, ++index)
        }
    }

    fun getIdByStation(station: String) = stationIDs!![station]!!

}