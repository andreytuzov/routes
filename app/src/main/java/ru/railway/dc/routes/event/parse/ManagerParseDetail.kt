package ru.railway.dc.routes.event.parse

import ru.railway.dc.routes.database.utils.CashDetailTableUtils
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.model.Route
import ru.railway.dc.routes.search.model.Schedule
import ru.railway.dc.routes.tools.AppUtils
import java.util.concurrent.CountDownLatch

class ManagerParseDetail {

    companion object {

        fun getScheduleDetail(listRoute: ListRoute): Schedule? {
            val isNetwork = AppUtils.hasConnection()
            val barrier = CountDownLatch(listRoute.size())
            val listQuery = listRoute.map { QueryParseDetail(it, isNetwork, barrier) }
            // Выполняем запросы
            for (query in listQuery)
                query.start()
            // Ждем окончания всех запросов
            try {
                barrier.await()
            } catch (e: InterruptedException) {
                System.err.println("Ошибка работы с барьером: " + e)
            }

            // =========================== GETTING RESULT ==========================================
            // Хранение кэша
            val saveToCash = ListRoute()
            // Хранение расписания
            val schedule = Schedule()
            var isError = false
            for (query in listQuery) {
                if (!isError) {
                    val routes = query.listRoute
                    // Если данных нет
                    if (routes == null || routes.isEmpty) {
                        isError = true
                    } else {
                        schedule.add(routes)
                        // Добавляем в кэш
                        if (query.isDownload)
                            saveToCash.addAll(routes)
                    }
                }
                query.interrupt()
            }
            // If data is not correct with only one item
            if (isError)
                return null
            // Save data in cash
            if (!saveToCash.isEmpty)
                Thread {
                    CashDetailTableUtils.saveData(saveToCash)
                }.start()

            // ============================= ФИЛЬТРАЦИЯ ================================================
            val result = Schedule()
            for (i in 0 until listRoute.size()) {
                val route = listRoute.get(i)
                val routeFilter = filter(schedule.get(i), route.getBTimeString(Route.DATE_TIME_FORMAT),
                        route.getETimeString(Route.DATE_TIME_FORMAT))
                if (routeFilter.isEmpty)
                    return null
                result.add(routeFilter)
            }

            return result
        }

        // Filter for data
        private fun filter(listRoute: ListRoute, bTime: String, eTime: String): ListRoute {
            val list = ListRoute()
            var isStart = false
            for (route in listRoute) {
                if (route.getBTimeString(Route.DATE_TIME_FORMAT) == bTime)
                    isStart = true
                if (!isStart)
                    continue
                list.add(route)
                if (route.getETimeString(Route.DATE_TIME_FORMAT) == eTime)
                    break
            }
            return list
        }
    }
}