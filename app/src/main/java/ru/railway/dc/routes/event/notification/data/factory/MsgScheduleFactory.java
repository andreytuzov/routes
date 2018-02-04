package ru.railway.dc.routes.event.notification.data.factory;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.notification.data.MsgSchedule;
import ru.railway.dc.routes.event.notification.data.item.DetailMessage;
import ru.railway.dc.routes.event.notification.data.item.EffectMessage;
import ru.railway.dc.routes.event.notification.data.item.TitleMessage;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;

/**
 * Created by SQL on 31.12.2016.
 */


// TODO оптимизировать (убрать лишнее)
public class MsgScheduleFactory {

    // Парсинг эффектов
    private static List<MsgSchedule.MsgTime> parseEffect(ListRoute listRoute, Context context) {
        List<MsgSchedule.MsgTime> list = new ArrayList<>();
        Route route;
        long time;
        for (int i = 0; i < listRoute.size(); i++) {
            route = listRoute.get(i);

            time = route.getBTime().getTimeInMillis();
            list.add(getMsgTimeEffect(time - 600000, EffectMessage.EffectType.RED_LED_VIBRA));

            time = route.getETime().getTimeInMillis();
            list.add(getMsgTimeEffect(time - 60000, EffectMessage.EffectType.RED_LED_VIBRA));
        }
        return list;
    }

    // Парсинг основных данных
    private static List<MsgSchedule.MsgTime> parseMain(ListRoute listRoute, Context context) {
        List<MsgSchedule.MsgTime> list = new ArrayList<>();
        Route route;
        long sCode = 0;
        long eCode;
        for (int i = 0; i < listRoute.size(); i++) {
            route = listRoute.get(i);

            eCode = route.getBTime().getTimeInMillis();
            list.add(getMsgTimeTitle(sCode, eCode, "Поезд", route.getBStation() +
                    " - " + route.getEStation()));

            sCode = eCode;
            eCode = route.getETime().getTimeInMillis();
            list.add(getMsgTimeTitle(sCode, eCode, "Прибытие " + route.getEStation(), null));

            sCode = eCode;
        }
        return list;
    }

    // Парсинг детальных данных
    private static List<MsgSchedule.MsgTime> parseDetail(Schedule schedule, Context context) {
        List<MsgSchedule.MsgTime> list = new ArrayList<>();
        long sCode;
        long eCode;
        ListRoute listRoute;
        for (int i = 0; i < schedule.size(); i++) {
            listRoute = schedule.get(i);
            for (int j = 0; j < listRoute.size() - 1; j++) {
                sCode = listRoute.get(j).getBTime().getTimeInMillis();
                eCode = listRoute.get(j).getETime().getTimeInMillis();
                list.add(getMsgTimeDetail(sCode, eCode, listRoute.get(j).getEStation() +
                    " через "));

                sCode = eCode;
                eCode = listRoute.get(j + 1).getBTime().getTimeInMillis();
                list.add(getMsgTimeDetail(sCode, eCode, listRoute.get(j).getEStation()
                        + " "));
            }
        }
        return list;
    }


    public static MsgSchedule parse(Context context, int eventID, boolean isEffect, boolean isDetail) {
        // Добавляем основные данные
        ListRoute listRoute = EventTableUtils.loadData(eventID);
        List<MsgSchedule.MsgTime> list = parseMain(listRoute, context);
        // Если нужны эффекты
        if (isEffect) {
            list.addAll(parseEffect(listRoute, context));
        }
        // Если нужно выводить детальную информацию
        if (isDetail) {
            Schedule schedule = EventTableUtils.loadAllDetailData(eventID);
            list.addAll(parseDetail(schedule, context));
        }
        // Если данных для оповещений нет
        if (list.isEmpty()) {
            return null;
        }
        // Выполняем сортировку
        Collections.sort(list, new Comparator<MsgSchedule.MsgTime>() {
            @Override
            public int compare(MsgSchedule.MsgTime lhs, MsgSchedule.MsgTime rhs) {
                if (lhs.getSTime() > rhs.getSTime()) {
                    return 1;
                } else if (lhs.getSTime() < rhs.getSTime()) {
                    return -1;
                }
                return 0;
            }
        });
        return new MsgSchedule(list, context);
    }



    // ================================= ФУНКЦИИ ДЛЯ СОЗДАНИЯ СООБЩЕНИЙ ============================

    private static MsgSchedule.MsgTime getMsgTimeDetail(long sCode, long eCode, String detail) {
        return new MsgSchedule.MsgTime(sCode, eCode, new DetailMessage(detail, eCode));
    }

    private static MsgSchedule.MsgTime getMsgTimeEffect(long sCode, EffectMessage.EffectType effectType) {
        return new MsgSchedule.MsgTime(sCode, new EffectMessage(effectType));
    }

    private static MsgSchedule.MsgTime getMsgTimeTitle(long sCode, long eCode, String title, String subTitle) {
        return new MsgSchedule.MsgTime(sCode, eCode, new TitleMessage(title, subTitle, eCode));
    }

}
