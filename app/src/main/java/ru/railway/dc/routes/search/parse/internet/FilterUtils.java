package ru.railway.dc.routes.search.parse.internet;

import org.apache.log4j.Logger;

import java.util.Calendar;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

/**
 * Created by SQL on 30.12.2016.
 */

public class FilterUtils {

    private final static Logger logger = Logger.getLogger(FilterUtils.class);

    public static ListRoute filterByDateTime(ListRoute listRoute, Calendar bDateTime,
                                             Calendar eDateTime) {
        logger.info("START, size = " + listRoute.size());
        long bDateTimeInMillis = bDateTime.getTimeInMillis();
        long eDateTimeInMillis = eDateTime.getTimeInMillis();
        // Получаем list, который нужно удалить
        ListRoute list = new ListRoute();
        for (Route route : listRoute) {
            if (route.getETime().getTimeInMillis() <= eDateTimeInMillis
                    && route.getBTime().getTimeInMillis() >= bDateTimeInMillis) {
                list.add(route);
            }
        }
        logger.info("End, size = " + list.size());
        return list;
    }
}
