package ru.railway.dc.routes.search.parse.internet.method;

/**
 * Created by SQL on 28.12.2016.
 */

import ru.railway.dc.routes.search.model.ListRoute;

/**
 * Интерфейс для парсинга
 * @author SQL
 *
 */
public interface IParseRailway {
    ListRoute get(String bStation, String eStation, String date);
}


