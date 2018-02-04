package ru.railway.dc.routes.event.parse;


import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.event.parse.method.DBParseDetail;
import ru.railway.dc.routes.event.parse.method.HTMLParseDetail;
import ru.railway.dc.routes.event.parse.method.IParseDetail;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

/**
 * Класс для парсинга данных с различных источников
 * 
 * @author SQL
 *
 */
public class QueryParseDetail extends Thread {

	public static Logger logger = Logger.getLogger(QueryParseDetail.class);
	
	private List<IParseDetail> listParseRailway;

	// Входные данные
	private Route route;
	// Для снятие барьера
	private CountDownLatch barrier;
	// Выходные данные
	private ListRoute listRoute;
	private boolean isDownload;
	private boolean isNetwork;


	public QueryParseDetail(Route route, boolean isNetwork) {
		this.route = route;
		listParseRailway = new ArrayList<IParseDetail>();
		listParseRailway.add(new DBParseDetail());
		this.isNetwork = isNetwork;
		if (isNetwork) {
			listParseRailway.add(new HTMLParseDetail());
		}
	}


	public ListRoute getListRoute() {
		return listRoute;
	}

	public void setBarrier(CountDownLatch barrier) {
		this.barrier = barrier;
	}

	public List<IParseDetail> getListParseRailway() {
		return listParseRailway;
	}

	// Добавление метода парсинга
	public void register(IParseDetail parse) {
		listParseRailway.add(parse);
	}

	public boolean isDownload() {
		return isDownload;
	}

	@Override
	public void run() {
		for (IParseDetail parseDetail : listParseRailway) {
			listRoute = parseDetail.get(route);
			if (listRoute != null && !listRoute.isEmpty()) {
				break;
			}
			isDownload = isNetwork;
		}
		if (listRoute == null || listRoute.isEmpty()) {
			logger.debug("Указанный детальный маршрут не был найден: "
					+ "route = " + route);
		}
		// Если есть барьер, то ослабляем его
		if (barrier != null) {
			barrier.countDown();
		}
	}
}
