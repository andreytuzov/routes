package ru.railway.dc.routes.event.activity;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;

/**
 * Created by SQL on 24.01.2017.
 */

public class AsyncListItemSchedule extends AsyncTaskLoader<Map<EventTableUtils.EventID,
        View>> {

    private List<EventTableUtils.EventID> eventIDs;

    public AsyncListItemSchedule(Context context, List<EventTableUtils.EventID> eventIDs) {
        super(context);
        this.eventIDs = eventIDs;
    }

    @Override
    public Map<EventTableUtils.EventID, View> loadInBackground() {
        Map<EventTableUtils.EventID, View> map = new HashMap<>();
        for (EventTableUtils.EventID eventID : eventIDs) {
            ListRoute listRoute = EventTableUtils.loadData(eventID.getId());
            RecyclerItemViewHelper viewHelper = new RecyclerItemViewHelper(getContext());
            View layout = viewHelper.getView(listRoute, eventID);
            map.put(eventID, layout);
        }
        return map;
    }

}
