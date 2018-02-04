package ru.railway.dc.routes.database.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.railway.dc.routes.database.DB;
import ru.railway.dc.routes.database.struct.EventTable;
import ru.railway.dc.routes.database.struct.RouteDetailTable;
import ru.railway.dc.routes.database.struct.RouteTable;
import ru.railway.dc.routes.event.parse.ManagerParseDetail;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;
import ru.railway.dc.routes.search.model.Schedule;

/**
 * Created by SQL on 04.01.2017.
 */

public class EventTableUtils {

    // ======================== РАБОТА СО СПИСКОМ СОБЫТИЙ ==========================================
//
    // Загрузка списка событий
    public static List<EventID> loadEventIDs(long dateTime) {
        List<EventID> list = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            c = db.query(EventTable.TABLE_EVENT, null,
                    EventTable.COLUMN_E_DATETIME + " > '" + dateTime + "'", null, null, null,
                    EventTable.COLUMN_E_DATETIME);
            if (c.moveToFirst()) {
                list = new ArrayList<>();
                // Получаем индексы
                int idIndex = c.getColumnIndex(EventTable.COLUMN_ID);
                int isNotificationIndex = c.getColumnIndex(EventTable.COLUMN_IS_NOTIFICATION);
                int isWidgetIndex = c.getColumnIndex(EventTable.COLUMN_IS_WIDGET);
                int eDateTimeIndex = c.getColumnIndex(EventTable.COLUMN_E_DATETIME);
                // Обходим данные
                do {
                    boolean isNotification = (c.getInt(isNotificationIndex) == 1) ? true : false;
                    boolean isWidget = (c.getInt(isWidgetIndex) == 1) ? true : false;
                    long eDateTime = c.getLong(eDateTimeIndex);
                    list.add(new EventID(c.getInt(idIndex), isNotification, isWidget, eDateTime));
                } while (c.moveToNext());
            }
        } finally {
            if (db != null) {
                db.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    public static EventID loadEventID(int id) {
        EventID eventID = null;
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            c = db.query(EventTable.TABLE_EVENT, null,
                    EventTable.COLUMN_ID + " = '" + id + "'", null, null, null, null);
            if (c.moveToFirst()) {
                // Получаем индексы
                int idIndex = c.getColumnIndex(EventTable.COLUMN_ID);
                int isNotificationIndex = c.getColumnIndex(EventTable.COLUMN_IS_NOTIFICATION);
                int isWidgetIndex = c.getColumnIndex(EventTable.COLUMN_IS_WIDGET);
                int eDateTimeIndex = c.getColumnIndex(EventTable.COLUMN_E_DATETIME);
                // Записываем данные
                boolean isNotification = (c.getInt(isNotificationIndex) == 1) ? true : false;
                boolean isWidget = (c.getInt(isWidgetIndex) == 1) ? true : false;
                long eDateTime = c.getLong(eDateTimeIndex);
                eventID = new EventID(c.getInt(idIndex), isNotification, isWidget, eDateTime);
            }
        } finally {
            if (db != null) {
                db.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return eventID;
    }

    // Содержимое событий
    public static class EventID {
        private int id;
        private long eDateTime;
        private boolean isNotification;
        private boolean isWidget;

        public EventID(int id, boolean isNotification, boolean isWidget, long eDateTime) {
            this.id = id;
            this.isNotification = isNotification;
            this.isWidget = isWidget;
            this.eDateTime = eDateTime;
        }

        public int getId() {
            return id;
        }

        public boolean isNotification() {
            return isNotification;
        }

        public boolean isWidget() {
            return isWidget;
        }

        public long geteDateTime() {
            return eDateTime;
        }
    }

    // ======================== ЗАГРУЗКА ДАННЫХ ====================================================

    // Загрузка данных для каждого события
    public static ListRoute loadData(int id) {
        SQLiteDatabase db = null;
        ListRoute listRoute = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            // Получаем данные
            c = db.query(RouteTable.TABLE_ROUTE, null,
                    RouteTable.COLUMN_EVENT_TABLE_ID + " = '" + id + "'", null, null, null, null);

            // Выолняем перебор
            if (c.moveToFirst()) {
                listRoute = new ListRoute();
                // Получаем индексы
                int bStationIndex = c.getColumnIndex(RouteTable.COLUMN_B_STATION);
                int eStationIndex = c.getColumnIndex(RouteTable.COLUMN_E_STATION);
                int bEnterStationIndex = c.getColumnIndex(RouteTable.COLUMN_B_ENTER_STATION);
                int eEnterStationIndex = c.getColumnIndex(RouteTable.COLUMN_E_ENTER_STATION);
                int bDateTimeIndex = c.getColumnIndex(RouteTable.COLUMN_B_DATETIME);
                int eDateTimeIndex = c.getColumnIndex(RouteTable.COLUMN_E_DATETIME);
                int numberTrainIndex = c.getColumnIndex(RouteTable.COLUMN_NUMBER_TRAIN);
                int typeTrainIndex = c.getColumnIndex(RouteTable.COLUMN_TYPE_TRAIN);
                int detailURIIndex = c.getColumnIndex(RouteTable.COLUMN_DETAIL_URI);
                // Выполняем обход данных
                do {
                    Route route = new Route();

                    route.setBEnterStation(c.getString(bEnterStationIndex));
                    route.setEEnterStation(c.getString(eEnterStationIndex));
                    route.setBStation(c.getString(bStationIndex));
                    route.setEStation(c.getString(eStationIndex));
                    route.setNumberTrain(c.getString(numberTrainIndex));
                    route.setTypeTrain(c.getString(typeTrainIndex));
                    route.setDetailURI(c.getString(detailURIIndex));
                    route.setBDateTime(c.getLong(bDateTimeIndex));
                    route.setEDateTime(c.getLong(eDateTimeIndex));

                    listRoute.add(route);
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return listRoute;
    }

    private static List<Integer> loadDataIDs(int id) {
        SQLiteDatabase db = null;
        List<Integer> list = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            // Получаем данные
            c = db.query(RouteTable.TABLE_ROUTE, null,
                    RouteTable.COLUMN_EVENT_TABLE_ID + " = '" + id + "'", null, null, null, null);
            // Выолняем перебор
            if (c.moveToFirst()) {
                list = new ArrayList<>();
                // Получаем индексы
                int IDIndex = c.getColumnIndex(RouteTable.COLUMN_ID);
                // Выполняем обход данных
                do {
                    list.add(c.getInt(IDIndex));
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    private static ListRoute loadDetailData(int routeID) {
        SQLiteDatabase db = null;
        ListRoute listRoute = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            // Получаем данные
            c = db.query(RouteDetailTable.TABLE_ROUTE_DETAIL, null,
                    RouteDetailTable.COLUMN_ROUTE_TABLE_ID + " = '" + routeID + "'", null, null, null, null);
            // Выолняем перебор
            if (c.moveToFirst()) {
                listRoute = new ListRoute();
                // Получаем индексы
                int bStationIndex = c.getColumnIndex(RouteDetailTable.COLUMN_B_STATION);
                int eStationIndex = c.getColumnIndex(RouteDetailTable.COLUMN_E_STATION);
                int bEnterStationIndex = c.getColumnIndex(RouteDetailTable.COLUMN_B_ENTER_STATION);
                int eEnterStationIndex = c.getColumnIndex(RouteDetailTable.COLUMN_E_ENTER_STATION);
                int bDateTimeIndex = c.getColumnIndex(RouteDetailTable.COLUMN_B_DATETIME);
                int eDateTimeIndex = c.getColumnIndex(RouteDetailTable.COLUMN_E_DATETIME);
                int numberTrainIndex = c.getColumnIndex(RouteDetailTable.COLUMN_NUMBER_TRAIN);
                int typeTrainIndex = c.getColumnIndex(RouteDetailTable.COLUMN_TYPE_TRAIN);
                // Выполняем обход данных
                do {
                    Route route = new Route();

                    route.setBEnterStation(c.getString(bEnterStationIndex));
                    route.setEEnterStation(c.getString(eEnterStationIndex));
                    route.setBStation(c.getString(bStationIndex));
                    route.setEStation(c.getString(eStationIndex));
                    route.setNumberTrain(c.getString(numberTrainIndex));
                    route.setTypeTrain(c.getString(typeTrainIndex));
                    route.setBDateTime(c.getLong(bDateTimeIndex));
                    route.setEDateTime(c.getLong(eDateTimeIndex));

                    listRoute.add(route);
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return listRoute;
    }

    public static Schedule loadAllDetailData(int id) {
        Schedule schedule = new Schedule();
        // Получаем список
        List<Integer> routeIDs = loadDataIDs(id);
        Log.d("EventTableUtils", "routeIDs:" + routeIDs);
        for (int routeID : routeIDs) {
            schedule.add(loadDetailData(routeID));
        }
        Log.d("EventTableUtils", "schedule:" + schedule);
        return schedule;
    }

    // ==================== СОХРАНЕНИЕ ДАННЫХ ======================================================

    private static int saveEvent(long eDateTime, boolean isNotification, boolean isWidget) {
        int notification = (isNotification ? 1 : 0);
        int widget = (isWidget ? 1 : 0);
        long id = 0;
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                // Записываем данные в таблицу EventTable
                ContentValues cv = new ContentValues();
                cv.put(EventTable.COLUMN_E_DATETIME, eDateTime);
                cv.put(EventTable.COLUMN_IS_NOTIFICATION, notification);
                cv.put(EventTable.COLUMN_IS_WIDGET, widget);
                id = db.insert(EventTable.TABLE_EVENT, null, cv);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return (int) id;
    }

    private static List<Long> saveData(ListRoute listRoute, int eventID) {
        List<Long> list = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                for (Route route : listRoute) {
                    cv.clear();
                    cv.put(RouteTable.COLUMN_B_STATION, route.getBStation());
                    cv.put(RouteTable.COLUMN_E_STATION, route.getEStation());
                    cv.put(RouteTable.COLUMN_B_ENTER_STATION, route.getBEnterStation());
                    cv.put(RouteTable.COLUMN_E_ENTER_STATION, route.getEEnterStation());
                    cv.put(RouteTable.COLUMN_B_DATETIME, route.getBTime().getTimeInMillis());
                    cv.put(RouteTable.COLUMN_E_DATETIME, route.getETime().getTimeInMillis());
                    cv.put(RouteTable.COLUMN_NUMBER_TRAIN, route.getNumberTrain());
                    cv.put(RouteTable.COLUMN_TYPE_TRAIN, route.getTypeTrain());
                    cv.put(RouteTable.COLUMN_DETAIL_URI, route.getDetailURI());
                    cv.put(RouteTable.COLUMN_EVENT_TABLE_ID, eventID);
                    list.add(db.insert(RouteTable.TABLE_ROUTE, null, cv));
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    private static List<Long> saveDetailData(ListRoute listRoute, int routeID) {
        List<Long> list = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                for (Route route : listRoute) {
                    cv.clear();
                    cv.put(RouteDetailTable.COLUMN_B_STATION, route.getBStation());
                    cv.put(RouteDetailTable.COLUMN_E_STATION, route.getEStation());
                    cv.put(RouteDetailTable.COLUMN_B_ENTER_STATION, route.getBEnterStation());
                    cv.put(RouteDetailTable.COLUMN_E_ENTER_STATION, route.getEEnterStation());
                    cv.put(RouteDetailTable.COLUMN_B_DATETIME, route.getBTime().getTimeInMillis());
                    cv.put(RouteDetailTable.COLUMN_E_DATETIME, route.getETime().getTimeInMillis());
                    cv.put(RouteDetailTable.COLUMN_NUMBER_TRAIN, route.getNumberTrain());
                    cv.put(RouteDetailTable.COLUMN_TYPE_TRAIN, route.getTypeTrain());
                    cv.put(RouteDetailTable.COLUMN_ROUTE_TABLE_ID, routeID);
                    list.add(db.insert(RouteDetailTable.TABLE_ROUTE_DETAIL, null, cv));
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return list;
    }


    // Сохранение данных с деталями
    public static boolean saveDataWithLoadDetail(ListRoute listRoute, boolean isNotification,
                                boolean isWidget, long date) {
        // Записываем событие
        int eventID = saveEvent(date, isNotification, isWidget);
        // Записываем маршруты
        List<Long> routeIDs = saveData(listRoute, eventID);
        // Записываем детальные маршруты
        Schedule schedule = ManagerParseDetail.getScheduleDetail(listRoute);

        // Проверяем на наличие данных
        if (schedule != null) {
            for (int i = 0; i < routeIDs.size(); i++) {
                saveDetailData(schedule.get(i), routeIDs.get(i).intValue());
            }
            return true;
        } else {
            removeData(eventID);
            return false;
        }
    }

    // Сохранение данных без деталей
    public static void saveDataWithoutLoadDetail(ListRoute listRoute, boolean isNotification,
                                                 boolean isWidget, long date) {
        int eventID = saveEvent(date, isNotification, isWidget);
        List<Long> routeIDs = saveData(listRoute, eventID);
    }

    // ============================== УДАЛЕНИЕ ДАННЫХ ==============================================

    public static void removeData(int id) {
        SQLiteDatabase db = null;
        try {
            List<Integer> routeIDs = loadDataIDs(id);
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                if (routeIDs != null) {
                    for (int routeID : routeIDs) {
                        db.delete(RouteDetailTable.TABLE_ROUTE_DETAIL,
                                RouteDetailTable.COLUMN_ROUTE_TABLE_ID + " = " + id, null);
                    }
                }
                db.delete(RouteTable.TABLE_ROUTE,
                        RouteTable.COLUMN_EVENT_TABLE_ID + " = " + id, null);
                db.delete(EventTable.TABLE_EVENT,
                        EventTable.COLUMN_ID + " = " + id, null);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void removeAll() {
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();

            db.delete(EventTable.TABLE_EVENT, null, null);
            db.delete(RouteTable.TABLE_ROUTE, null, null);
            db.delete(RouteDetailTable.TABLE_ROUTE_DETAIL, null, null);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (db != null) {
                db.close();
            }
        }
    }

    // ================================ ОБНОВЛЕНИЕ =================================================
    public static void update(int eventID, Boolean isNotification, Boolean isWidget) {
        int notification = 0;
        if (isNotification != null && isNotification) {
            notification = 1;
        }
        int widget = 0;
        if (isWidget != null && isWidget) {
            widget = 1;
        }
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                // Записываем данные в таблицу EventTable
                ContentValues cv = new ContentValues();
                if (isNotification != null) {
                    cv.put(EventTable.COLUMN_IS_NOTIFICATION, notification);
                }
                if (isWidget != null) {
                    cv.put(EventTable.COLUMN_IS_WIDGET, widget);
                }
                db.update(EventTable.TABLE_EVENT, cv, EventTable.COLUMN_ID + " = " + eventID, null);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
