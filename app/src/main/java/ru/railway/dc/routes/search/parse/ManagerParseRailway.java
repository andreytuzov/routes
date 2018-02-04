package ru.railway.dc.routes.search.parse;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ru.railway.dc.routes.database.utils.CashTableUtils;
import ru.railway.dc.routes.request.data.RequestData;
import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.data.Data;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.parse.cash.CashManager;
import ru.railway.dc.routes.search.parse.cash.QueryCash;
import ru.railway.dc.routes.search.parse.internet.InternetManager;

/**
 * Created by SQL on 17.01.2017.
 */

public class ManagerParseRailway {

    private final static Logger logger = Logger.getLogger(ManagerParseRailway.class);

    private static final int WITHOUT_CASH = 0;
    private static final int WITH_CASH_ONE_DAY = 1;
    private static final int WITH_CASH_MORE_DAY = 2;

    private static int getMethodSearch() {
        int method;
        Data data = Data.getInstance();
        Calendar bDateTime = (Calendar) data.getParam(ComposeData.B_DATETIME);
        Calendar eDateTime = (Calendar) data.getParam(ComposeData.E_DATETIME);
        if (CashTableUtils.checkData(bDateTime.getTimeInMillis(),
                eDateTime.getTimeInMillis())) {
            method = WITHOUT_CASH;
        } else {
            SimpleDateFormat format = new SimpleDateFormat(RequestData.FORMAT_DATE);

            if (format.format(bDateTime.getTime())
                    .equals(format.format(eDateTime.getTime()))) {
                method = WITH_CASH_ONE_DAY;
            } else {
                method = WITH_CASH_MORE_DAY;
            }
        }
        return method;
    }

    public static ListRoute getListRoute() {
        logger.debug("START");
        int method = getMethodSearch();
        logger.debug("Выбран метод для парсинга: " + method);

        InternetManager internetManager = new InternetManager();
        ListRoute result = null;
        // Если один день, то получаем сначала кэш
        if (method == WITH_CASH_ONE_DAY) {
            CashManager cashManager = new CashManager();
            ListRoute cash = cashManager.getListRoute();
            List<QueryCash.PairStation> forLoad = cashManager.getLoadFromInternet();
            // Если нет данных для загрузки
            if (forLoad == null || forLoad.isEmpty()) {
                return cash;
            }
            ListRoute load = internetManager.getListRoute(forLoad);
            result = cash;
            result.addAll(load);
        } else {
            // Инача используем все вместе
            boolean isCash = true;
            if (method == WITHOUT_CASH) {
                isCash = false;
            }
            result = internetManager.getListRoute(isCash);
        }
        // Сохранение в кэш
        ListRoute saveToCash = internetManager.getSaveToCash();
        if (saveToCash != null && !saveToCash.isEmpty()) {
            new Thread(new SaveHash(saveToCash)).start();
        }
        logger.debug("END");
        return result;
    }

    static class SaveHash implements Runnable {

        private ListRoute listRoute;

        public SaveHash(ListRoute listRoute) {
            this.listRoute = listRoute;
        }

        @Override
        public void run() {
            CashTableUtils.saveData(listRoute);
            logger.debug("Расписание сохранено в кэш " + listRoute.size());
        }
    }
}
