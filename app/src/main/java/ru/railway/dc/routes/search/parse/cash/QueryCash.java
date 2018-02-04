package ru.railway.dc.routes.search.parse.cash;


import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.database.utils.CashTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;

/**
 * Класс для парсинга данных с различных источников
 * 
 * @author SQL
 *
 */
public class QueryCash extends Thread {

	public static Logger logger = Logger.getLogger(QueryCash.class);

	// Входные данные
	private PairStation pairStation;
	private Calendar bDatetime;
	private Calendar eDatetime;
	// Для снятие барьера
	private CountDownLatch barrier;
	// Выходные данные
	private ListRoute listRoute;
	private boolean isDownload;

	public QueryCash(String bStation, String eStation, Calendar bDatetime,
					 Calendar eDatetime) {
		pairStation = new PairStation(bStation, eStation);
		this.bDatetime = bDatetime;
		this.eDatetime = eDatetime;
	}

	public boolean isDownload() {
		return isDownload;
	}

	public ListRoute getListRoute() {
		return listRoute;
	}

	public void setBarrier(CountDownLatch barrier) {
		this.barrier = barrier;
	}

	private void checkEmpty() {
		if (listRoute.size() == 1 && (listRoute.get(0).getBTime().getTimeInMillis() ==
				listRoute.get(0).getETime().getTimeInMillis()) ) {
			listRoute = new ListRoute();
			logger.debug("Пустой маршрут: "
					+ "bStation = " + pairStation.bStation
					+ ", eStation = " + pairStation.eStation
					+ ", bDatetime = " + bDatetime
					+ ", eDatetime = " + eDatetime);
		}
	}

	@Override
	public void run() {
		listRoute = CashTableUtils.loadData(pairStation.bStation, pairStation.eStation,
				bDatetime.getTimeInMillis(), eDatetime.getTimeInMillis());
		if (listRoute == null || listRoute.isEmpty()) {
			logger.debug("Указанный маршрут не был найден: "
					+ "bStation = " + pairStation.bStation
					+ ", eStation = " + pairStation.eStation
					+ ", bDatetime = " + bDatetime
					+ ", eDatetime = " + eDatetime);
		} else {
			isDownload = true;
			logger.debug("Маршрут был загружен из кэша: "
					+ "bStation = " + pairStation.bStation
					+ ", eStation = " + pairStation.eStation
					+ ", bDatetime = " + bDatetime
					+ ", eDatetime = " + eDatetime);
			// Проверяем на пустоту
			checkEmpty();
		}
		// Если есть барьер, то ослабляем его
		if (barrier != null) {
			barrier.countDown();
		}
	}

	public PairStation getPairStation() {
		return pairStation;
	}

	public static class PairStation {
		String bStation;
		String eStation;

		public PairStation(String bStation, String eStation) {
			this.bStation = bStation;
			this.eStation = eStation;
		}

		public String getBStation() {
			return bStation;
		}

		public String getEStation() {
			return eStation;
		}
	}
}
