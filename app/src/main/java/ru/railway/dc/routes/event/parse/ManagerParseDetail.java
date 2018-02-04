package ru.railway.dc.routes.event.parse;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.railway.dc.routes.database.utils.CashDetailTableUtils;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;
import ru.railway.dc.routes.tools.AppUtils;

public class ManagerParseDetail {

	public static Logger logger = Logger.getLogger(ManagerParseDetail.class);

	public static Schedule getScheduleDetail(ListRoute listRoute) {

		List<QueryParseDetail> listQuery = new ArrayList<>();

		boolean isNetwork = AppUtils.hasConnection();
		for (Route route : listRoute) {
			listQuery.add(new QueryParseDetail(route, isNetwork));
		}
		CountDownLatch barrier = new CountDownLatch(listQuery.size());
		// Выполняем запросы
		for (QueryParseDetail query : listQuery) {
			query.setBarrier(barrier);
			query.start();
		}
		// Ждем окончания всех запросов
		try {
			barrier.await();
		} catch (InterruptedException e) {
			System.err.println("Ошибка работы с барьером: " + e);
		}

		// =========================== ПОЛУЧЕНИЕ РЕЗУЛЬТАТА ========================================
		// Хранение кэша
		ListRoute saveToCash = new ListRoute();
		// Хранение расписания
		Schedule schedule = new Schedule();
		boolean isError = false;
		for (QueryParseDetail query : listQuery) {
			ListRoute routes = query.getListRoute();
			// Если данных нет
			if (routes == null || routes.isEmpty()) {
				isError = true;
			} else {
				schedule.add(routes);
				// Добавляем в кэш
				if (query.isDownload()) {
					saveToCash.addAll(routes);
				}
			}
			query.interrupt();
		}
		// Запускаем сохранение данных в кэш
		if (!saveToCash.isEmpty()) {
			new Thread(new SaveDetailHash(saveToCash)).start();
		}
		// Если данные не получены, хотя бы по одному пункту
		if (isError) {
			return null;
		}

		// ============================= ФИЛЬТРАЦИЯ ================================================
		Schedule result = new Schedule();
		for (int i = 0; i < listRoute.size(); i++) {
			Route route = listRoute.get(i);
			result.add(filter(schedule.get(i), route.getBTimeString(Route.DATE_TIME_FORMAT),
					route.getETimeString(Route.DATE_TIME_FORMAT)));
		}

		return result;
	}

	// Фильтрация
	private static ListRoute filter(ListRoute listRoute, String bTime, String eTime) {
		ListRoute list = new ListRoute();
		boolean isStart = false;
		for (Route route : listRoute) {
			if (route.getBTimeString(Route.DATE_TIME_FORMAT).equals(bTime)) {
				isStart = true;
			}
			if (!isStart) {
				continue;
			}
			list.add(route);
			if (route.getETimeString(Route.DATE_TIME_FORMAT).equals(eTime)) {
				break;
          	}
		}
		return list;
	}


	static class SaveDetailHash implements Runnable {

		private ListRoute listRoute;

		public SaveDetailHash(ListRoute listRoute) {
			this.listRoute = listRoute;
		}

		@Override
		public void run() {
			CashDetailTableUtils.saveData(listRoute);
			logger.debug("Расписание сохранено в кэш " + listRoute.size());
		}
	}


}
