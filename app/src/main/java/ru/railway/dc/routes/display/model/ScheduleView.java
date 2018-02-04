package ru.railway.dc.routes.display.model;

/**
 * Created by SQL on 29.12.2016.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.railway.dc.routes.search.model.Schedule;


public class ScheduleView implements Iterable<ListRouteView> {
    private List<ListRouteView> schedule;

    public ScheduleView() {
        this.schedule = new ArrayList<ListRouteView>();
    }

    public ScheduleView(Schedule schedule)  {
        this();
        for (int i = 0; i < schedule.size(); i++) {
            this.schedule.add(new ListRouteView(schedule.get(i), i));
        }
    }

    public int size() {
        return schedule.size();
    }

    public List<ListRouteView> getList() {
        return schedule;
    }

    public ListRouteView getListRouteView(int index) {
        return schedule.get(index);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < schedule.size(); i++) {
            s.append("#" + (i + 1) + "\n");
            s.append(schedule.get(i) + "\n");
        }
        return s.toString();
    }

    @Override
    public Iterator<ListRouteView> iterator() {
        return schedule.iterator();
    }
}
