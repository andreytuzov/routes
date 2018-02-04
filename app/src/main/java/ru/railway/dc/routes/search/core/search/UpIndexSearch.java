package ru.railway.dc.routes.search.core.search;

import java.util.Map;

import ru.railway.dc.routes.search.core.IndexDecoder;
import ru.railway.dc.routes.search.core.IndexFilter;
import ru.railway.dc.routes.search.core.model.IndexRoute;
import ru.railway.dc.routes.search.core.model.ListIndexRoute;

public class UpIndexSearch {

	private IndexDecoder iDecoder;
	private Map<Integer, Long> durationIDs;
	private int[] iStations;
	private IndexRoute iRoute;
	private Long time;
	
	public UpIndexSearch(IndexDecoder iDecoder, Map<Integer, Long> durationIDs, Long time) {
		this.iDecoder = iDecoder;
		this.durationIDs = durationIDs;
		this.time = time;
	}

	public void loadStations(int[] iStations) {
		this.iStations = iStations;
		iRoute = IndexFilter.filter(iDecoder.getListIndexRoute(iStations[0], iStations[1]),
				null, time, IndexFilter.Direction.FILTER_UP);
		// Хотя бы 2 станции должно быть
		if (iStations == null || iStations.length < 2) {
			throw new IllegalArgumentException("iStations is null");
		}
	}
	
	// Передвигаемся по первым станциям
	private void nextIRoute() {
		iRoute = IndexFilter.filter(iDecoder.getListIndexRoute(iStations[0], iStations[1]),
				iRoute.getiBTime() + 1, IndexFilter.Direction.FILTER_UP);
	}

	public int getCountRoute() {
		return iStations.length - 1;
	}
	
	public ListIndexRoute getListIRoute() {
		// Первая станция
		if (iRoute == null) {
			return null;
		}
		ListIndexRoute listIRoute = new ListIndexRoute();
		listIRoute.add(iRoute);
		// Остальные станции
		IndexRoute iRoute = this.iRoute;
		for (int i = 1; i < getCountRoute(); i++) {
			iRoute = IndexFilter.filter(iDecoder.getListIndexRoute(iStations[i], iStations[i + 1]),
					iRoute, durationIDs.get(iStations[i]), IndexFilter.Direction.FILTER_UP);
			if (iRoute == null) {
				return null;
			}
			listIRoute.add(iRoute);
		}
		// Ищем заново первую станцию
		nextIRoute();
		return listIRoute;
	}

}
