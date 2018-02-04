package ru.railway.dc.routes.search.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.search.core.model.IndexRoute;
import ru.railway.dc.routes.search.core.model.IndexSchedule;
import ru.railway.dc.routes.search.core.model.ListIndexRoute;
import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.data.Data;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;

public class IndexDecoder {

	private Map<String, Integer> stationIDs;
	private ListIndexRoute[][] listIRoute;
	private ListRoute listRoute;

	private String bStation;
	private String eStation;
	private List<String> stations;

	@SuppressWarnings("unchecked")
	public IndexDecoder(ListRoute listRoute) {
		// Сохраняем для кодирования и декодирования
		this.listRoute = listRoute;

		// Станции, которые используются в маршруте
		Data data = Data.getInstance();
		bStation = (String) data.getParam(ComposeData.B_STATION);
		eStation = (String) data.getParam(ComposeData.E_STATION);
		stations = (List<String>) data.getParam(ComposeData.LIST_STATION);

		// Определяем количество разных станций
		List<String> distinct = new ArrayList<>();
		distinct.add(bStation);
		if (!distinct.contains(eStation)) {
			distinct.add(eStation);
		}
		for (String station : stations) {
			if (!distinct.contains(station)) {
				distinct.add(station);
			}
		}
		int count = distinct.size();

		// Выделяем память под данные
		listIRoute = new ListIndexRoute[count][count];
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				listIRoute[i][j] = new ListIndexRoute();
			}
		}

		// Заполнение карты id станций
		convertStationToIDs();
		// Декодирование
		encode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("\nstationIDs");
		for (String station : stationIDs.keySet()) {
			str.append("\n key = " + station + ", value = " + stationIDs.get(station));
		}

		str.append("\nlistIRoute");
		for (int i = 0; i < listIRoute.length; i++) {
			for (int j = 0; j < listIRoute[i].length; j++) {
				str.append("\ni = " + i + ", j = " + j + " : " + listIRoute[i][j]);
			}
		}

		str.append("\nlistRoute: " + listRoute);
		return str.toString();
	}

	// Получаем маршрут
	public ListIndexRoute getListIndexRoute(int iBStation, int iEStation) {
		return listIRoute[iBStation][iEStation];
	}

	// =============== ДЕКОДИРОВАНИЕ ========================================

	// Декодирование всего расписания
	public Schedule decode(IndexSchedule iSchedule) {
		if (iSchedule == null) {
			return null;
		}
		Schedule schedule = new Schedule();
		for (ListIndexRoute listIRoute : iSchedule) {
			schedule.add(decode(listIRoute));
		}
		return schedule;
	}

	// Декодирование маршрутов одной группы
	public ListRoute decode(ListIndexRoute listIRoute) {
		ListRoute listRoute = new ListRoute();
		for (IndexRoute iRoute : listIRoute) {
			listRoute.add(decode(iRoute));
		}
		return listRoute;
	}

	// Декодирование маршрута
	public Route decode(IndexRoute iRoute) {
		return listRoute.get(iRoute.getiLRoute());
	}

	// ================ КОДИРОВАНИЕ =========================================

	// Кодирование всех маршрутов
	private void encode() {
		for (int i = 0; i < listRoute.size(); i++) {
			encode(listRoute.get(i), i);
		}
	}

	// Кодирование одного маршрута
	private void encode(Route route, int iLRoute) {
		// Кодируем информацию
		// Получаем ID станций
		int iBStation = getIdByStation(route.getBEnterStation()),
				iEStation = getIdByStation(route.getEEnterStation());
		long iBTime = route.getBTime().getTimeInMillis(),
				iETime = route.getETime().getTimeInMillis();
		// Добавляем закодированный маршрут
		IndexRoute iRoute = new IndexRoute(iBTime, iETime, iLRoute);
		listIRoute[iBStation][iEStation].add(iRoute);
	}

	// Создание карты id станций
	private void convertStationToIDs() {
		// Создаем или очищаем карту id станций
		if (stationIDs != null) {
			stationIDs.clear();
		} else {
			stationIDs = new HashMap<>();
		}
		int index = 0;
		// Добавляем начальную станцию
		stationIDs.put(bStation, index);
		// Добавляем промежуточные станции
		for (String station : stations) {
			if (!stationIDs.containsKey(station)) {
				stationIDs.put(station, ++index);
			}
		}
		// Добавляем конечную станцию
		if (!stationIDs.containsKey(eStation)) {
			stationIDs.put(eStation, ++index);
		}
	}

	public int getIdByStation(String station) {
		return stationIDs.get(station);
	}

}
