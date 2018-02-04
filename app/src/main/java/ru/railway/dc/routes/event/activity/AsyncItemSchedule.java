package ru.railway.dc.routes.event.activity;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.event.activity.data.ItemSchedule;
import ru.railway.dc.routes.event.activity.data.factory.ItemScheduleFactory;
import ru.railway.dc.routes.search.model.Schedule;

/**
 * Created by SQL on 24.01.2017.
 */

public class AsyncItemSchedule extends AsyncTaskLoader<ItemSchedule> {

    private int eventID;

    public AsyncItemSchedule(Context context, int eventID) {
        super(context);
        this.eventID = eventID;
    }

    @Override
    public ItemSchedule loadInBackground() {
        Schedule schedule = EventTableUtils.loadAllDetailData(eventID);
        // Создаем данные для адаптера
        ItemSchedule itemSchedule = ItemScheduleFactory.parse(schedule);
        return itemSchedule;
    }

}
