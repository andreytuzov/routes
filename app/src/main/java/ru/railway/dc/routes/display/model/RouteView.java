package ru.railway.dc.routes.display.model;

import ru.railway.dc.routes.search.model.Route;

/**
 * Created by SQL on 29.12.2016.
 */

public class RouteView extends Route {

    private int trainTime;
    private int stationTime;
    private int totalTime;

    public RouteView(Route route) {
        super(route);
    }

    public RouteView(Route currentRoute, Route nextRoute) {
        super(currentRoute);
        // Получаем дополнительные данные
        // Время в поезде
        trainTime = (int) (eDateTime.getTimeInMillis() - bDateTime.getTimeInMillis()) / 60000;
        // Время на станции
        if (nextRoute == null) {
            stationTime = 0;
        } else {
            stationTime = (int) (nextRoute.getBTime().getTimeInMillis() - eDateTime.getTimeInMillis()) / 60000;
        }
        // Общее время
        totalTime = stationTime + trainTime;
    }

    public int getTrainTime() {
        return trainTime;
    }

    public void setTrainTime(int trainTime) {
        this.trainTime = trainTime;
    }

    public int getStationTime() {
        return stationTime;
    }

    public void setStationTime(int stationTime) {
        this.stationTime = stationTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return super.toString() + " trainTime = " + trainTime + " stationTime " + stationTime + " totalTime " + totalTime;
    }
}
