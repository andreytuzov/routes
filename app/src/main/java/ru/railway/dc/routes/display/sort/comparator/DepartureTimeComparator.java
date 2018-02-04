package ru.railway.dc.routes.display.sort.comparator;


import java.util.Comparator;

import ru.railway.dc.routes.display.model.ListRouteView;

public class DepartureTimeComparator implements Comparator<ListRouteView> {

	@Override
	public int compare(ListRouteView o1, ListRouteView o2) {
		long eTime1 = o1.getExternRoute().getBTime().getTimeInMillis(),
				eTime2 = o2.getExternRoute().getBTime().getTimeInMillis();
		return (int) (eTime1 - eTime2);
	}

}
