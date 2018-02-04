package ru.railway.dc.routes.display.sort.comparator;

import java.util.Comparator;

import ru.railway.dc.routes.display.model.ListRouteView;

public class ArrivalTimeComparator implements Comparator<ListRouteView> {

	@Override
	public int compare(ListRouteView o1, ListRouteView o2) {
		long bTime1 = o1.getExternRoute().getETime().getTimeInMillis(),
			bTime2 = o2.getExternRoute().getETime().getTimeInMillis();
		return (int) (bTime1 - bTime2);
	}

}
