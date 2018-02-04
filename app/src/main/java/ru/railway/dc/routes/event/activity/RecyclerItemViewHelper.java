package ru.railway.dc.routes.event.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

/**
 * Created by SQL on 25.01.2017.
 */

public class RecyclerItemViewHelper {

    private static final int resource = R.layout.adapter_favourite_preview_item;
    private static final int resource_header = R.layout.adapter_favourite_preview_header;

    private Context context;
    private LayoutInflater inflater;
    private Calendar now = Calendar.getInstance();
    // Для определения цвета
    private int state;

    public RecyclerItemViewHelper(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public View getView(ListRoute listRoute, EventTableUtils.EventID eventID) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        // 3 - Событие завершилось
        // 4 - Событие не началось
        // 5 - Событие активно

        state = getState(now, listRoute.get(0).getBTime(),
                listRoute.get(listRoute.size() - 1).getETime()) + 3;

        // Заголовок
        layout.addView(getHeader(listRoute, eventID));

        // 0 - Событие-элемент завершилось
        // 1 - Событие-элемент не началось
        // 2 - Событие-элемент активно

        for (Route route : listRoute) {
            layout.addView(getItem(route));
        }
        return layout;
    }

    // Получение заголовка
    private View getHeader(ListRoute listRoute, EventTableUtils.EventID eventID) {
        View header = inflater.inflate(resource_header, null);

        TextView textView = (TextView)header.findViewById(R.id.textView);
        String bDateTime = listRoute.get(0).getBTimeString(Route.DATE_TIME_FORMAT);
        textView.setText(" " + bDateTime);

        View background = header.findViewById(R.id.background);
        if (eventID.isNotification()) {
            background.setActivated(true);
        }

        return header;
    }

    // Получение элемента
    private View getItem(Route route) {
        View item = inflater.inflate(resource, null);
        // Раскрашиваем элементы
        // Событие не началось
        if (state != 1 && state != 4) {
            // Событие
            if (state != 3) {
                state = getState(now, route.getBTime(), route.getETime());
            }
            setColor(item, state);
        }
        ((TextView)item.findViewById(R.id.bStation))
                .setText(route.getBStation());
        ((TextView)item.findViewById(R.id.eStation))
                .setText(" - " + route.getEStation());
        return item;
    }

    // Установка цвета для элемента
    private void setColor(View item, int state) {

        // Событие завершилось
        if (state == 0 || state == 3) {
            item.setSelected(true);
        // Событие активно
        } else if (state == 2) {
            item.setActivated(true);
        }
    }

    // Получить состояние элемента
    private int getState(Calendar now, Calendar bDateTime, Calendar eDateTime) {
        if (now.after(eDateTime)) {
            return 0;
        } else if (bDateTime.after(now)) {
            return 1;
        }
        return 2;
    }

}
