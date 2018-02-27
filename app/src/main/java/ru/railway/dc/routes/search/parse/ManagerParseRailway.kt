package ru.railway.dc.routes.search.parse

import io.reactivex.Maybe
import io.reactivex.Observable
import org.apache.log4j.Logger
import ru.railway.dc.routes.App
import ru.railway.dc.routes.database.utils.CashTableUtils
import ru.railway.dc.routes.request.data.RequestData
import ru.railway.dc.routes.search.model.ListRoute
import ru.railway.dc.routes.search.parse.cash.CashManager
import ru.railway.dc.routes.search.parse.internet.InternetManager
import ru.railway.dc.routes.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*


class ManagerParseRailway {
    private val logger = Logger.getLogger(ManagerParseRailway::class.java)

    companion object {
        private const val WITHOUT_CASH = 0
        private const val WITH_CASH_ONE_DAY = 1
        private const val WITH_CASH_MORE_DAY = 2
    }

    private fun getMethodSearch(): Int {
        val method: Int
        val bDateTime = App.requestData.bDateTime!!
        val eDateTime = App.requestData.eDateTime!!
        method = if (CashTableUtils.checkData(bDateTime.timeInMillis,
                eDateTime.timeInMillis)) {
            WITHOUT_CASH
        } else {
            if (DateUtils.format(bDateTime, DateUtils.FORMAT_DATE) == DateUtils.format(eDateTime, DateUtils.FORMAT_DATE)) {
                WITH_CASH_ONE_DAY
            } else {
                WITH_CASH_MORE_DAY
            }
        }
        return method
    }

    private fun saveCacheObservable(listRoute: ListRoute) =
            Observable.fromCallable {
                CashTableUtils.saveData(listRoute)
            }

    fun getListRoute(): ListRoute {
        val method = getMethodSearch()

        val internetManager = InternetManager()
        var result: ListRoute? = null
        // Если один день, то получаем сначала кэш
        if (method == WITH_CASH_ONE_DAY) {
            val cashManager = CashManager()
            val cash = cashManager.getListRoute()
            val forLoad = cashManager.getLoadFromInternet()
            // Если нет данных для загрузки
            if (forLoad.isEmpty()) {
                return cash
            }
            val load = internetManager.getListRoute(forLoad)
            result = cash
            result.addAll(load)
        } else {
            // Инача используем все вместе
            var isCash = true
            if (method == WITHOUT_CASH) {
                isCash = false
            }
            result = internetManager.getListRoute(isCash)
        }
        // Сохранение в кэш
        val saveToCash = internetManager.saveToCash
        if (!saveToCash.isEmpty) {
            saveCacheObservable(saveToCash)
                    .subscribe()
        }
        logger.debug("END")
        return result
    }

}