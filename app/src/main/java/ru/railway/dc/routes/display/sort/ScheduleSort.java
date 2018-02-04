package ru.railway.dc.routes.display.sort;

import java.util.Collections;
import java.util.Comparator;

import ru.railway.dc.routes.display.model.ListRouteView;
import ru.railway.dc.routes.display.model.ScheduleView;
import ru.railway.dc.routes.display.sort.comparator.ArrivalTimeComparator;
import ru.railway.dc.routes.display.sort.comparator.CountTrainComparator;
import ru.railway.dc.routes.display.sort.comparator.DepartureTimeComparator;
import ru.railway.dc.routes.display.sort.comparator.StationTimeComparator;
import ru.railway.dc.routes.display.sort.comparator.TotalTimeComparator;
import ru.railway.dc.routes.display.sort.comparator.TrainTimeComparator;

public class ScheduleSort {

	public static ScheduleView sort(ScheduleView schedule, TypeSortEnum typeSort) {
		
		Comparator<ListRouteView> comparator = null;
		// Тип сортировки
		switch (typeSort) {
		case COUNT_TRAIN:
			comparator = new CountTrainComparator();
			break;
		case DEPARTURE_TIME:
			comparator = new DepartureTimeComparator();
			break;
		case ARRIVAL_TIME:
			comparator = new ArrivalTimeComparator();
			break;
		case STATION_TIME:
			comparator = new StationTimeComparator();
			break;
		case TRAIN_TIME:
			comparator = new TrainTimeComparator();
			break;
		case TOTAL_TIME:
			comparator = new TotalTimeComparator();
			break;
		}
		// Сортировка
		if (comparator != null) {
			Collections.sort(schedule.getList(), comparator);
		}
		return schedule;
	}
}
