package ru.railway.dc.routes.search.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.search.core.model.IndexSchedule;
import ru.railway.dc.routes.search.core.model.ListIndexRoute;
import ru.railway.dc.routes.search.core.search.UpIndexSearch;
import ru.railway.dc.routes.search.data.ComposeData;
import ru.railway.dc.routes.search.data.Data;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Schedule;

public class IndexSearch {

	private static final int COUNT_MILLIS_IN_MINUTE = 60000;

	private final long time;
	private IndexDecoder iDecoder;
	private Map<Integer, Long> durationIDs;
	private int[] iStations;


	public IndexSearch(ListRoute listRoute) {
		// Получаем данные
		Data data = Data.getInstance();
		Map<String, Integer> durations = (Map<String, Integer>) data
				.getParam(ComposeData.MAP_STATION_DURATION);
		Calendar dateTime = (Calendar) data.getParam(ComposeData.B_DATETIME);

		time = dateTime.getTimeInMillis();
		iDecoder = new IndexDecoder(listRoute);
		convertDurationWithID(durations);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		// Время
		str.append("\n\ntime = " + time);
		
		// Карта длительностей
		str.append("\nDurationIDs");
		for (Integer i : durationIDs.keySet()) {
			str.append("\n key = " + i + ", value = " + durationIDs.get(i));
		}
		str.append("\niDecoder: " + iDecoder);
		return str.toString();
	}

	@SuppressWarnings("unchecked")
	private void loadIStations() {
		// Станции, которые используются в маршруте
		Data data = Data.getInstance();
		String bStation = (String) data.getParam(ComposeData.B_STATION);
		String eStation = (String) data.getParam(ComposeData.E_STATION);
		List<String> stations = (List<String>) data.getParam(ComposeData.LIST_STATION);
		int count = stations.size();

		iStations = new int[count + 2];
		// Начальная и конечная станции
		iStations[0] = iDecoder.getIdByStation(bStation);
		iStations[count + 1] = iDecoder.getIdByStation(eStation);

		// Промежуточные станции
		for (int i = 0; i < count; i++) {
			iStations[i + 1] = iDecoder.getIdByStation(stations.get(i));
		}

		Arrays.sort(iStations, 1, count + 1);
	}

	// Преобразуем названия станции в их ID
	private void convertDurationWithID(Map<String, Integer> durations) {
		// Создаем или очищаем карту
		if (durationIDs != null) {
			durationIDs.clear();
		} else {
			durationIDs = new HashMap<>();
		}
		for (String station : durations.keySet()) {
			int id = iDecoder.getIdByStation(station);
			durationIDs.put(id, (long) durations.get(station) * COUNT_MILLIS_IN_MINUTE);
		}
	}

	// Основной алгоритм поиска
	private List<ListIndexRoute> getListIRoute(UpIndexSearch aISearch) {
		aISearch.loadStations(iStations);

		List<ListIndexRoute> list = new ArrayList<>();
		ListIndexRoute listIRoute = null;
		do {
			listIRoute = aISearch.getListIRoute();
			if (listIRoute != null) {
				list.add(listIRoute);
			}
		} while (listIRoute != null);
		
		if (list == null || list.size() == 0) {
			return null;
		}
		
		return list;
	}

	private void print() {
		StringBuilder str = new StringBuilder();
		// Станции
		str.append("\niStations = ");
		if (iStations != null) {
			for (int i : iStations) {
				str.append(" " + i);
			}
		}		
		System.out.println(str.toString());
	}
	
	// Алгоритм перебора
	public boolean next() {
		int N = iStations.length - 1;
		int S = 1;
		int i = N - 2, temp, index;
		while (i >= S && iStations[i] >= iStations[i + 1]) {
			i--;
		}
		// Последняя перестановка
		if (i < S) {
			return false;
		}
		// Обмен с минимальным, большим данного
		index = i + 1;
		for (int x = i + 1; x < N; x++) {
			if (iStations[i] < iStations[x] && 
					iStations[index] > iStations[x]) {
				index = x;
			}
		}
		
		temp = iStations[i];
		iStations[i] = iStations[index];
		iStations[index] = temp;
		
		// Сортировка остальных
		Arrays.sort(iStations, i + 1, N);
		return true;
	}

	public Schedule getSchedule() {
		loadIStations();

		UpIndexSearch aISearch = new UpIndexSearch(iDecoder, durationIDs, time);
		IndexSchedule iSchedule = new IndexSchedule();

		do {
			List<ListIndexRoute> list = getListIRoute(aISearch);
			if (list != null) {
				iSchedule.addAll(list);
			}
		} while (next());		
		
		return iDecoder.decode(iSchedule);
	}

}
