package ru.railway.dc.routes.display.model;

/**
 * Created by SQL on 29.12.2016.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

public class ListRouteView implements Iterable<RouteView> {

    private int id;
    private List<RouteView> list;

    // Дополнительные параметры
    private RouteView externRoute;

    public ListRouteView(ListRoute listRoute, int id) {
        this.id = id;
        list = new ArrayList<RouteView>();
        // Добавляем дополнительную информацию в маршруты
        Route prevRoute = null;
        for (Route route : listRoute.getList()) {
            if (prevRoute != null) {
                list.add(new RouteView(prevRoute, route));
            }
            prevRoute = route;
        }
        list.add(new RouteView(prevRoute, null));
        // Создаем внешний маршрут на базе списка маршрутов
        calcExternRoute();
    }

    // Расчет внешнего маршрута
    private void calcExternRoute() {
        int size = list.size();
        if (size == 0) {
            return;
        }
        Route route = new Route();
        // Заполняем внешний маршрут свойствами
        route.setBEnterStation(list.get(0).getBEnterStation());
        route.setEEnterStation(list.get(size - 1).getEEnterStation());
        route.setBStation(list.get(0).getBStation());
        route.setEStation(list.get(size - 1).getEStation());
        route.setBDateTime(list.get(0).getBTime());
        route.setEDateTime(list.get(size - 1).getETime());
        // Создаем маршрут
        externRoute = new RouteView(route);
        // Заполняем маршрут дополнительными свойствами (время)
        int stationTime = 0,
                trainTime = 0,
                totalTime = 0;
        for (RouteView r : list) {
            stationTime += r.getStationTime();
            trainTime += r.getTrainTime();
        }
        totalTime = stationTime + trainTime;
        externRoute.setStationTime(stationTime);
        externRoute.setTrainTime(trainTime);
        externRoute.setTotalTime(totalTime);
    }

    public List<RouteView> getList() {
        return Collections.unmodifiableList(list);
    }

    public RouteView getExternRoute() {
        return externRoute;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("externRoute - " + externRoute + " endExternRoute\n");
        for (int i = 0; i < list.size(); i++) {
            s.append(list.get(i) + "\n");
        }
        return s.toString();
    }

    public int size() {
        return list.size();
    }

    public int getCountRoutes() {
        return list.size();
    }

    public RouteView getRouteView(int index) {
        return list.get(index);
    }

    public int getId() {
        return id;
    }

    @Override
    public Iterator<RouteView> iterator() {
        return list.iterator();
    }
}
