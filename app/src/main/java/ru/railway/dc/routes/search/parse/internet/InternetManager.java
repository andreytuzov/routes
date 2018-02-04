package ru.railway.dc.routes.search.parse.internet;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.request.data.RequestData;
import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.data.Data;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.parse.cash.QueryCash;

public class InternetManager {

	private final static Logger logger = Logger.getLogger(InternetManager.class);

	private String bStation;
	private String eStation;
	private Calendar bDateTime;
	private Calendar eDateTime;
	private String date;
	private List<String> stations;
	private ListRoute saveToCash;

	@SuppressWarnings("unchecked")
	public InternetManager() {
		saveToCash = new ListRoute();

		Data data = Data.getInstance();
		this.bStation = (String) data.getParam(ComposeData.B_STATION);
		this.eStation = (String) data.getParam(ComposeData.E_STATION);
		this.stations = (List<String>) data.getParam(ComposeData.LIST_PARSE_STATION);

		bDateTime = (Calendar) data.getParam(ComposeData.B_DATETIME);
		eDateTime = (Calendar) data.getParam(ComposeData.E_DATETIME);
		date = new SimpleDateFormat(RequestData.FORMAT_DATE).format(bDateTime.getTime());
	}

	// ============================= ПОДГОТОВКА ЗАПРОСОВ ===========================================

	// Подготовка запросов для загрузки данных на один день (без кэша)
	private List<QueryInternet> getListQuery(List<QueryCash.PairStation> list) {
		logger.debug("START");
		List<QueryInternet> listQuery = new ArrayList<>();
		// Добавляем все маршруты в список
		for (int i = 0; i < list.size(); i++) {
			listQuery.add(new QueryInternet(list.get(i).getBStation(),
					list.get(i).getEStation(), date, false));
		}
		logger.debug("Запрос подготовлен");
		return listQuery;
	}

	// Подготовка запросов для загрузки данных на несколько дней (с кэшем)
	private List<QueryInternet> getListQuery(boolean isCash) {
		logger.debug("START");
		List<QueryInternet> listQuery = new ArrayList<>();

		SimpleDateFormat format = new SimpleDateFormat(RequestData.FORMAT_DATE);
		String date = null;

		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(bDateTime.getTimeInMillis());
		String sEDateTime = format.format(eDateTime.getTime());
		do {
			date = format.format(calendar.getTime());

			// Добавляем все маршруты в список
			// Маршруты отправления с первой станции
			for (int i = 0; i < stations.size(); i++) {
				listQuery.add(new QueryInternet(bStation, stations.get(i), date, isCash));
			}
			// Маршруты прибытия на конечную станцию
			for (int i = 0; i < stations.size(); i++) {
				listQuery.add(new QueryInternet(stations.get(i), eStation, date, isCash));
			}
			// Маршруты между промежуточными станциями
			for (int i = 0; i < stations.size(); i++) {
				for (int j = 0; j < stations.size(); j++) {
					if (i != j) {
						String iStation = stations.get(i), jStation = stations.get(j);
						listQuery.add(new QueryInternet(iStation, jStation, date, isCash));
					}
				}
			}
			if (stations.size() == 0) {
				listQuery.add(new QueryInternet(bStation, eStation, date, isCash));
			}

			// Условие выхода из цикла
			if (date.equals(sEDateTime)) {
				break;
			}

			calendar.add(Calendar.DAY_OF_MONTH, 1);
		} while (true);
		logger.debug("Запрос подготовлен");
		return listQuery;
	}

	// ============================ ПОСЫЛКА ЗАПРОСОВ ===============================================

	private ListRoute getRoutes(List<QueryInternet> listQuery) {
		logger.debug("START");
		CountDownLatch barrier = new CountDownLatch(listQuery.size());
		// Выполняем запросы
		for (QueryInternet query : listQuery) {
			query.setBarrier(barrier);
			query.start();
		}
		logger.info("Потоки запущены");
		// Ждем окончания всех запросов
		try {
			barrier.await();
		} catch (InterruptedException e) {
			System.err.println("Ошибка работы с барьером: " + e);
		}
		logger.info("Все потоки завершили работу");
		// Заполняем все данными
		ListRoute listRoute = new ListRoute();
		for (QueryInternet query : listQuery) {
			ListRoute routes = query.getListRoute();
			if (routes != null && routes.size() != 0) {
				saveToCash.addAll(routes);
				// Добавляем для сохранения в кэш
				if (!query.isEmpty()) {
					listRoute.addAll(routes);
				}
			}
			query.interrupt();
		}
		logger.info("Информация со всем потоков получена, size = " + listRoute.size());
		return FilterUtils.filterByDateTime(listRoute, bDateTime, eDateTime);
	}

	// Загрузка данных без кэша (указаны маршруты, которых нет в кэше)
	public ListRoute getListRoute(List<QueryCash.PairStation> list) {
		return getRoutes(getListQuery(list));
	}

	// Загрузка данных с кэшем
	public ListRoute getListRoute(boolean isCash) {
		return getRoutes(getListQuery(isCash));
	}

	public ListRoute getSaveToCash() {
		return saveToCash;
	}
}
