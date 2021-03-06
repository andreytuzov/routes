package ru.railway.dc.routes.display.sort.comparator;

import java.util.Comparator;

import ru.railway.dc.routes.display.model.ListRouteView;

public class StationTimeComparator implements Comparator<ListRouteView> {

	@Override
	public int compare(ListRouteView o1, ListRouteView o2) {
		int stationTime1 = o1.getExternRoute().getStationTime(),
				stationTime2 = o2.getExternRoute().getStationTime();
		return stationTime1 - stationTime2;
	}

}
