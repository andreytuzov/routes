package ru.railway.dc.routes.search.parse.cash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.data.Data;
import ru.railway.dc.routes.search.model.ListRoute;

public class CashManager {

	private String bStation;
	private String eStation;
	private Calendar bDateTime;
	private Calendar eDateTime;
	private List<String> stations;
	private List<QueryCash.PairStation> loadFromInternet;

	@SuppressWarnings("unchecked")
	public CashManager() {
		loadFromInternet = new ArrayList<>();
		Data data = Data.getInstance();
		this.bStation = (String) data.getParam(ComposeData.B_STATION);
		this.eStation = (String) data.getParam(ComposeData.E_STATION);
		this.stations = (List<String>) data.getParam(ComposeData.LIST_PARSE_STATION);

		bDateTime = (Calendar) data.getParam(ComposeData.B_DATETIME);
		eDateTime = (Calendar) data.getParam(ComposeData.E_DATETIME);
	}

	public ListRoute getListRoute() {
		List<QueryCash> listQuery = new ArrayList<>();

		// Добавляем все маршруты в список
		// Маршруты отправления с первой станции
		for (int i = 0; i < stations.size(); i++) {
			listQuery.add(new QueryCash(bStation, stations.get(i), bDateTime, eDateTime));
		}
		// Маршруты прибытия на конечную станцию
		for (int i = 0; i < stations.size(); i++) {
			listQuery.add(new QueryCash(stations.get(i), eStation, bDateTime, eDateTime));
		}
		// Маршруты между промежуточными станциями
		for (int i = 0; i < stations.size(); i++) {
			for (int j = 0; j < stations.size(); j++) {
				if (i != j) {
					String iStation = stations.get(i), jStation = stations.get(j);
					listQuery.add(new QueryCash(iStation, jStation, bDateTime, eDateTime));
				}
			}
		}
		if (stations.isEmpty()) {
			listQuery.add(new QueryCash(bStation, eStation, bDateTime, eDateTime));
		}
		CountDownLatch barrier = new CountDownLatch(listQuery.size());
		// Выполняем запросы
		for (QueryCash query : listQuery) {
			query.setBarrier(barrier);
			query.start();
		}
		// Ждем окончания всех запросов
		try {
			barrier.await();
		} catch (InterruptedException e) {
			System.err.println("Ошибка работы с барьером: " + e);
		}
		// Заполняем все данными
		ListRoute listRoute = new ListRoute();
		for (QueryCash query : listQuery) {
			ListRoute routes = query.getListRoute();
			if (routes != null && routes.size() != 0) {
				listRoute.addAll(routes);
				// Не загружено из кэша
			} else if (!query.isDownload()) {
				// Добавляем для загрузки
				loadFromInternet.add(query.getPairStation());
			}
			query.interrupt();
		}
		return listRoute;
	}

	public List<QueryCash.PairStation> getLoadFromInternet() {
		return loadFromInternet;
	}

}
