package ru.railway.dc.routes.search.core;

import ru.railway.dc.routes.search.core.model.IndexRoute;
import ru.railway.dc.routes.search.core.model.ListIndexRoute;

public class IndexFilter {
	// Фильтрация (поиск)
	public static IndexRoute filter(ListIndexRoute listIRoute,
									IndexRoute iRoute, long time, Direction direction) {
		if (iRoute != null) {
			if (direction == Direction.FILTER_UP) {
				time = iRoute.getiETime() + time;
			} else {
				time = iRoute.getiBTime() - time;
			}
		}
		return filter(listIRoute, time, direction);
	}
	
	public static IndexRoute filter(ListIndexRoute listIRoute, 
			long time, Direction direction) {
		if (listIRoute.size() == 0) {
			return null;
		}
		int index = -1;
		long t, iT = 0;
		for (int i = 0; i < listIRoute.size(); i++) {
			if (direction == Direction.FILTER_UP) {
				t = listIRoute.get(i).getiBTime();
				// Должно быть больше
				if (t >= time) {
					if (index == -1) {
						index = i;
						iT = t;	
					// Ищу минимальный
					} else if (t < iT) {
						index = i;
						iT = t;
					}
				}
			} else {
				t = listIRoute.get(i).getiETime();
				// Должно быть меньше
				if (t <= time) {
					if (index == -1) {
						index = i;
						iT = t;	
					// Ищу максимальный
					} else if (t >= iT) {
						index = i;
						iT = t;
					}
				}
			}
		}
		// Если нет маршрутов
		if (index == -1) {
			return null;
		}
		return listIRoute.get(index);
	}
	
	// Направление фильтрации
	public enum Direction {
		FILTER_UP,
		FILTER_DOWN
	}
}
