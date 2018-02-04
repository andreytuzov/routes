package ru.railway.dc.routes.event.activity.data.factory;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.railway.dc.routes.event.activity.data.ItemSchedule;
import ru.railway.dc.routes.event.activity.data.item.DataItem;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;
import ru.railway.dc.routes.utils.Utils;


// TODO оптимизировать (убрать лишнее)
public class ItemScheduleFactory {
    public static ItemSchedule parse (Schedule schedule) {
        List<List<ItemSchedule.ItemTime>> list = new ArrayList<>();
        // Первый маршрут и последний
        Route routeFirst = null;
        Route routeLast = null;
        for (int i = 0; i < schedule.size(); i++) {
            List<ItemSchedule.ItemTime> listChild = new ArrayList<>();
            ListRoute listRoute = schedule.get(i);
            for (int j = 0; j < listRoute.size(); j++) {
                Route route = listRoute.get(j);
                // Для всех кроме первого и последнего элементов
                String station = route.getBStation();
                String eTime = route.getBTimeString(Route.TIME_FORMAT);
                long eT = route.getBTime().getTimeInMillis();
                // Для всех кроме самого первого элемента
                String bTime = "";
                long sT = 0;
                int trainTime = -1;
                int stationTime = -1;
                int trainTimeGroup = -1;
                // Самый первый элемент
                if (!(i == 0 && j == 0)) {
                    bTime = routeLast.getETimeString(Route.TIME_FORMAT);
                    sT = routeLast.getETime().getTimeInMillis();
                    trainTime = (int) ((routeLast.getETime().getTimeInMillis() -
                            routeLast.getBTime().getTimeInMillis()) / 60000);
                    stationTime = (int) ((route.getBTime().getTimeInMillis() -
                            routeLast.getETime().getTimeInMillis()) / 60000);
                    if (j == 0) {
                        trainTimeGroup = (int) ((routeLast.getETime().getTimeInMillis() -
                                routeFirst.getBTime().getTimeInMillis()) / 60000);
                    }
                }
                // Для элемента группы
                if (j == 0) {
                    listChild.add(createItemTimeGroup(sT, eT, station, bTime, eTime, trainTime, stationTime,
                            trainTimeGroup, true, route.getTypeTrain()));
                } else {
                    listChild.add(createItemTimeChild(sT, eT, station, bTime, eTime, trainTime, stationTime));
                }
                routeLast = route;
            }
            list.add(listChild);
            routeFirst = listRoute.get(0);
        }

        // Добавляем последний элемент
        List<ItemSchedule.ItemTime> listChild = new ArrayList<>();
        String station = routeLast.getEStation();
        String bTime = routeLast.getETimeString(Route.TIME_FORMAT);
        long sT = routeLast.getETime().getTimeInMillis();
        int trainTime = (int) ((routeLast.getETime().getTimeInMillis() -
                routeLast.getBTime().getTimeInMillis()) / 60000);
        int trainTimeGroup = (int) ((routeLast.getETime().getTimeInMillis() -
                routeFirst.getBTime().getTimeInMillis()) / 60000);
        listChild.add(createItemTimeGroup(sT, sT, station, bTime, "", trainTime, -1,
                trainTimeGroup, true, null));
        list.add(listChild);
        // Формируем объект с данными
        ItemSchedule itemSchedule = new ItemSchedule(list);
        Log.d("ItemSchedule: ",  "itemSchedule:" + itemSchedule);

        return itemSchedule;
    }

    // Для Child
    private static ItemSchedule.ItemTime createItemTimeChild(long sT, long eT,
                                                      String station, String bTime, String eTime,
                                                      int trainTime, int stationTime) {
        return createItemTimeGroup(sT, eT, station, bTime, eTime, trainTime, stationTime,
                trainTime, false, null);
    }

    // Для Group
    private static ItemSchedule.ItemTime createItemTimeGroup(long sT, long eT,
                                                      String station, String bTime, String eTime,
                                                      int trainTime, int stationTime,
                                                      int trainTimeGroup, boolean isGroup, String typeTrainGroup) {
        String sTrainTime = "";
        if (trainTime >= 0) {
            sTrainTime = Utils.getTextTime(trainTime);
        }
        String sStationTime = "";
        if (stationTime >= 0) {
            sStationTime = Utils.getTextTime(stationTime);
        }
        String sTrainTimeGroup = "";
        if (trainTimeGroup >= 0) {
            sTrainTimeGroup = Utils.getTextTime(trainTimeGroup);
        }
        DataItem dataItem = new DataItem(station, bTime, eTime, sTrainTime, sTrainTimeGroup,
                sStationTime, isGroup, typeTrainGroup);
        return new ItemSchedule.ItemTime(sT, eT, dataItem);
    }




}
