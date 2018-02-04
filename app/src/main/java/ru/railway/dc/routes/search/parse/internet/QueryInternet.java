package ru.railway.dc.routes.search.parse.internet;


import org.apache.log4j.Logger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.parse.cash.CashParseRailway;
import ru.railway.dc.routes.search.parse.internet.method.HTMLParseRailway;
import ru.railway.dc.routes.search.parse.internet.method.IParseRailway;

/**
 * Класс для парсинга данных с различных источников
 * 
 * @author SQL
 *
 */
public class QueryInternet extends Thread {

	public static Logger logger = Logger.getLogger(QueryInternet.class);
	
	private List<IParseRailway> listParseRailway;

	// Входные данные
	private String bStation;
	private String eStation;
	private String date;
	private boolean isEmpty;
	// Для снятие барьера
	private CountDownLatch barrier;
	// Выходные данные
	private ListRoute listRoute;

	{
		listParseRailway = new ArrayList<IParseRailway>();
	}

	public QueryInternet(String bStation, String eStation, String date, boolean isCash) {
		this.bStation = bStation;
		this.eStation = eStation;
		this.date = date;
		if (isCash) {
			listParseRailway.add(new CashParseRailway());
		}
		listParseRailway.add(new HTMLParseRailway());
	}

	public ListRoute getListRoute() {
		return listRoute;
	}

	public void setBarrier(CountDownLatch barrier) {
		this.barrier = barrier;
	}

	public List<IParseRailway> getListParseRailway() {
		return listParseRailway;
	}

	// Добавление метода парсинга
	public void register(IParseRailway parse) {
		listParseRailway.add(parse);
	}


	private void addEmpty() {
		if (listRoute == null) {
			listRoute = new ListRoute();
		}
		try {
			listRoute.add(new Route(bStation, eStation, date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		for (IParseRailway parseRailway : listParseRailway) {
			listRoute = parseRailway.get(bStation, eStation, date);
			if (listRoute != null && !listRoute.isEmpty()) {
				break;
			}
		}
		if (listRoute == null) {
			logger.debug("Указанный маршрут не был найден: "
					+ "bStation = " + bStation
					+ ", eStation = " + eStation
					+ ", date = " + date);
		} else if (listRoute.isEmpty()) {
			isEmpty = true;
			addEmpty();
		}

		// Если есть барьер, то ослабляем его
		if (barrier != null) {
			barrier.countDown();
		}
	}

	public boolean isEmpty() {
		return isEmpty;
	}
}
