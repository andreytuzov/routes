package ru.railway.dc.routes.search.parse.cash

import ru.railway.dc.routes.App
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.utils.DateUtils
import java.util.*
import java.util.concurrent.CountDownLatch


class CashManager {

    private val bStation = App.requestData.bStation!!.name
    private val eStation = App.requestData.eStation!!.name
    private var bDateTime = App.requestData.bDateTime!!
    private var eDateTime = App.requestData.eDateTime!!
    private val stations = App.requestData.stationMap.keys.flatMap { listOf(it.name) }
    private var loadFromInternet = ArrayList<QueryCash.PairStation>()

    fun getListRoute(): ListRoute {
        val listQuery = ArrayList<QueryCash>()

        // Добавляем все маршруты в список
        // Маршруты отправления с первой станции
        for (i in stations.indices) {
            listQuery.add(QueryCash(bStation, stations[i], bDateTime, eDateTime))
        }
        // Маршруты прибытия на конечную станцию
        for (i in stations.indices) {
            listQuery.add(QueryCash(stations[i], eStation, bDateTime, eDateTime))
        }
        // Маршруты между промежуточными станциями
        for (i in stations.indices) {
            for (j in stations.indices) {
                if (i != j) {
                    val iStation = stations[i]
                    val jStation = stations[j]
                    listQuery.add(QueryCash(iStation, jStation, bDateTime, eDateTime))
                }
            }
        }
        if (stations.isEmpty()) {
            listQuery.add(QueryCash(bStation, eStation, bDateTime, eDateTime))
        }
        val barrier = CountDownLatch(listQuery.size)
        // Выполняем запросы
        for (query in listQuery) {
            query.setBarrier(barrier)
            query.start()
        }
        // Ждем окончания всех запросов
        try {
            barrier.await()
        } catch (e: InterruptedException) {
            System.err.println("Ошибка работы с барьером: " + e)
        }

        // Заполняем все данными
        val listRoute = ListRoute()
        for (query in listQuery) {
            val routes = query.listRoute
            if (routes != null && routes.size() != 0) {
                listRoute.addAll(routes)
                // Не загружено из кэша
            } else if (!query.isDownload) {
                // Добавляем для загрузки
                loadFromInternet.add(query.pairStation)
            }
            query.interrupt()
        }
        return listRoute
    }

    fun getLoadFromInternet(): List<QueryCash.PairStation> {
        return loadFromInternet
    }
}