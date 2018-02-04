package ru.railway.dc.routes.search;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.search.core.IndexSearch;
import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Schedule;
import ru.railway.dc.routes.search.parse.ManagerParseRailway;

/**
 * Created by SQL on 27.12.2016.
 */

public class ScheduleAsyncLoader extends AsyncTaskLoader<Schedule> {


    public static final Logger logger = Logger.getLogger(ScheduleAsyncLoader.class);

    public ScheduleAsyncLoader(Context context) {
        super(context);
    }

    @Override
    public Schedule loadInBackground() {
        logger.info("START");
        // Загружаем данные в Data для соответствующего потока
        if (!ComposeData.loadToThreadData(getContext())) {
            return null;
        }
        logger.info("Данные для потока загружены");
        // Получаем данные
        ListRoute listRoute = ManagerParseRailway.getListRoute();
        logger.info("Парсинг данных закончен, size = " + listRoute.size());
        // Формируем расписание
        IndexSearch indexSearch = new IndexSearch(listRoute);
        logger.info("Объект indexSearch создан");
        Schedule schedule = indexSearch.getSchedule();
        logger.info("END, расписание получено, size = " + schedule.size());
        return schedule;
    };
}
