package ru.railway.dc.routes.event.widget;

import ru.railway.dc.routes.event.ITime;

/**
 * Created by SQL on 04.01.2017.
 */

public class WidgetTime implements ITime {
    @Override
    public boolean update(long code) {
        return true;
    }
}
