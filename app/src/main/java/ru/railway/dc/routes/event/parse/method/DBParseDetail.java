package ru.railway.dc.routes.event.parse.method;

import ru.railway.dc.routes.database.utils.CashDetailTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

/**
 * Created by SQL on 12.01.2017.
 */

public class DBParseDetail implements IParseDetail {

    @Override
    public ListRoute get(Route route) {
        String date = route.getBTimeString(Route.DATE_FORMAT);
        String numberTrain = route.getNumberTrain();
        return CashDetailTableUtils.loadData(date, numberTrain);
    }
}
