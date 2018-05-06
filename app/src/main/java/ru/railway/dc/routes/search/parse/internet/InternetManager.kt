package ru.railway.dc.routes.search.parse.internet

import org.apache.log4j.Logger
import ru.railway.dc.routes.App
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.parse.cash.QueryCash
import ru.railway.dc.routes.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class InternetManager() {

    private val logger = Logger.getLogger(InternetManager::class.java)

    private val bStation = App.requestData.bStation!!.name
    private val eStation = App.requestData.eStation!!.name
    private var bDateTime = App.requestData.bDateTime!!
    private var eDateTime = App.requestData.eDateTime!!
    private var date = DateUtils.format(bDateTime, DateUtils.FORMAT_DATE)
    private val stations = App.requestData.stationMap.keys.flatMap { listOf(it.name) }

    var saveToCash = ListRoute()
        private set


    // ============================= ПОДГОТОВКА ЗАПРОСОВ ===========================================

    // Подготовка запросов для загрузки данных на один день (без кэша)
    private fun getListQuery(list: List<QueryCash.PairStation>): List<QueryInternet> {
        logger.debug("START")
        val listQuery = ArrayList<QueryInternet>()
        // Добавляем все маршруты в список
        for (i in list.indices) {
            listQuery.add(QueryInternet(list[i].bStation,
                    list[i].eStation, date, false))
        }
        logger.debug("Запрос подготовлен")
        return listQuery
    }

    // Подготовка запросов для загрузки данных на несколько дней (с кэшем)
    private fun getListQuery(isCash: Boolean): List<QueryInternet> {
        val listQuery = ArrayList<QueryInternet>()

        var date: String? = null

        val sEDateTime = DateUtils.format(eDateTime, DateUtils.FORMAT_DATE)
        val calendar = DateUtils.createCalendar(bDateTime.timeInMillis)
        do {
            date = DateUtils.format(calendar, DateUtils.FORMAT_DATE)

            // Добавляем все маршруты в список
            // Маршруты отправления с первой станции
            // Маршруты прибытия на конечную станцию
            stations.mapTo(listQuery) {
                QueryInternet(bStation, it, date, isCash)
            }
            stations.mapTo(listQuery) {
                QueryInternet(it, eStation, date, isCash)
            }
            // Маршруты между промежуточными станциями
            for (i in stations.indices) {
                for (j in stations.indices) {
                    if (i != j) {
                        val iStation = stations[i]
                        val jStation = stations[j]
                        listQuery.add(QueryInternet(iStation, jStation, date, isCash))
                    }
                }
            }
            if (stations.isEmpty()) {
                listQuery.add(QueryInternet(bStation, eStation, date, isCash))
            }

            // Условие выхода из цикла
            if (date == sEDateTime) {
                break
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        } while (true)
        return listQuery
    }

    // ============================ ПОСЫЛКА ЗАПРОСОВ ===============================================

    private fun getRoutes(listQuery: List<QueryInternet>): ListRoute {
        logger.debug("START")
        val barrier = CountDownLatch(listQuery.size)
        // Выполняем запросы
        for (query in listQuery) {
            query.setBarrier(barrier)
            query.start()
        }
        logger.info("Потоки запущены")
        // Ждем окончания всех запросов
        try {
            barrier.await()
        } catch (e: InterruptedException) {
            System.err.println("Ошибка работы с барьером: " + e)
        }

        logger.info("Все потоки завершили работу")
        // Заполняем все данными
        val listRoute = ListRoute()
        for (query in listQuery) {
            val routes = query.listRoute
            if (routes != null && routes.size() != 0) {
                saveToCash.addAll(routes)
                // Добавляем для сохранения в кэш
                if (!query.isEmpty) {
                    listRoute.addAll(routes)
                }
            }
            query.interrupt()
        }
        logger.info("Информация со всем потоков получена, size = " + listRoute.size())
        return FilterUtils.filterByDateTime(listRoute, bDateTime, eDateTime)
    }

    // Загрузка данных без кэша (указаны маршруты, которых нет в кэше)
    fun getListRoute(list: List<QueryCash.PairStation>): ListRoute {
        return getRoutes(getListQuery(list))
    }

    // Загрузка данных с кэшем
    fun getListRoute(isCash: Boolean): ListRoute {
        return getRoutes(getListQuery(isCash))
    }

}