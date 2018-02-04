package ru.railway.dc.routes.event.parse.method;

/**
 * Created by SQL on 28.12.2016.
 */

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

/**
 * Интерфейс для парсинга
 * @author SQL
 *
 */
public interface IParseDetail {
    ListRoute get(Route route);
}


