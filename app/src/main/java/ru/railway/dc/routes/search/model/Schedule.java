package ru.railway.dc.routes.search.model;

/**
 * Created by SQL on 27.12.2016.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Класс для сохранения всех вариантов маршрутов
 * @author SQL
 *
 */
public class Schedule implements Iterable<ListRoute> {
    private List<ListRoute> schedule;

    public Schedule() {
        schedule = new ArrayList<ListRoute>();
    }

    public ListRoute get(int index) {
        return schedule.get(index);
    }

    public int size() {
        return schedule.size();
    }

    public void add(ListRoute listRoute) {
        schedule.add(listRoute);
    }

    public void clearAll() {
        schedule.clear();
    }

    public List<ListRoute> getList() {
        return Collections.unmodifiableList(schedule);
    }

    @Override
    public Iterator<ListRoute> iterator() {
        return schedule.iterator();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            s.append("#" + (i + 1) + "\n");
            s.append(schedule.get(i) + "\n");
        }
        return s.toString();
    }

}
