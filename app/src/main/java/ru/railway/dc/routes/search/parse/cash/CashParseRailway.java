package ru.railway.dc.routes.search.parse.cash;

/**
 * Created by SQL on 28.12.2016.
 */

import org.apache.log4j.Logger;

import ru.railway.dc.routes.database.utils.CashTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.parse.internet.method.IParseRailway;

public class CashParseRailway implements IParseRailway {

    public static Logger logger = Logger.getLogger(CashParseRailway.class);

    @Override
    public ListRoute get(String bStation, String eStation, String date) {
        return CashTableUtils.loadData(bStation, eStation, date);
    }
}
