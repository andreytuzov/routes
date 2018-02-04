package ru.railway.dc.routes.database.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.database.DB;
import ru.railway.dc.routes.database.struct.CashTable;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

public class CashTableUtils {

    public static Logger logger = Logger.getLogger(CashTableUtils.class);

    // =============================== УДАЛЕНИЕ ДАННЫХ =============================================

    public static void clearData() {
        logger.info("Производится очистка кэша");
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            int count = db.delete(CashTable.TABLE_CASH, null, null);
            logger.debug("Очистка кэша закончена. Удалено " + count + " строк");
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Проверка есть ли данные
    public static boolean checkData(long bDateTime, long eDateTime) {
        String sqlSelection = CashTable.COLUMN_B_DATETIME + " >= '" + bDateTime + "' and " +
                CashTable.COLUMN_E_DATETIME + " <= '" + eDateTime + "'";
        SQLiteDatabase db = null;
        Cursor c = null;
        boolean isEmpty = false;
        try {
            db = DB.getSqlLiteDatabase();
            c = db.query(CashTable.TABLE_CASH, null, sqlSelection, null, null, null, null);
            if (c.getCount() == 0) {
                isEmpty = true;
            }
            logger.debug("Check, size = " + c.getCount());
        } finally {
            if (db != null) {
                db.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return isEmpty;
    }

//
//    // TODO удалять только прошедший кэш
//    public static void clearPrevData() {
//        logger.info("Производится очистка старого кэша");
//
//        // Получаем текущую дату
//        String date = new SimpleDateFormat(Route.DATE_FORMAT)
//                .format(Calendar.getInstance().getTime());
//        SQLiteDatabase db = null;
//        try {
//            db = DB.getSqlLiteDatabase();
////            int count = db.delete(CashTable.TABLE_CASH, CashTable.COLUMN_DATE + " != '" + date + "'",
////                    null);
//            logger.debug("Очистка кэша закончена. Удалено " + count + " строк");
//        } finally {
//            if (db != null){
//                db.close();
//            }
//        }
//    }

    // =============================== ЗАГРУЗКА ДАННЫХ =============================================

    private static ListRoute loadData(String sqlSelection) {
        SQLiteDatabase db = null;
        ListRoute listRoute = null;
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            // Получаем данные
            c = db.query(CashTable.TABLE_CASH, null, sqlSelection, null, null, null, null);
            // Выолняем перебор
            if (c.moveToFirst()) {
                listRoute = new ListRoute();
                int bStationIndex = c.getColumnIndex(CashTable.COLUMN_B_STATION);
                int eStationIndex = c.getColumnIndex(CashTable.COLUMN_E_STATION);
                int bDatetimeIndex = c.getColumnIndex(CashTable.COLUMN_B_DATETIME);
                int eDatetimeIndex = c.getColumnIndex(CashTable.COLUMN_E_DATETIME);
                int numberTrainIndex = c.getColumnIndex(CashTable.COLUMN_NUMBER_TRAIN);
                int typeTrainIndex = c.getColumnIndex(CashTable.COLUMN_TYPE_TRAIN);
                int detailURIIndex = c.getColumnIndex(CashTable.COLUMN_DETAIL_URI);
                int bEnterStationIndex = c.getColumnIndex(CashTable.COLUMN_B_ENTER_STATION);
                int eEnterStationIndex = c.getColumnIndex(CashTable.COLUMN_E_ENTER_STATION);

                do {
                    Route route = new Route();

                    route.setBEnterStation(c.getString(bEnterStationIndex));
                    route.setEEnterStation(c.getString(eEnterStationIndex));
                    route.setBStation(c.getString(bStationIndex));
                    route.setEStation(c.getString(eStationIndex));
                    route.setNumberTrain(c.getString(numberTrainIndex));
                    route.setTypeTrain(c.getString(typeTrainIndex));
                    route.setDetailURI(c.getString(detailURIIndex));
                    route.setBDateTime(c.getLong(bDatetimeIndex));
                    route.setEDateTime(c.getLong(eDatetimeIndex));

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

    public static ListRoute loadData(String bStation, String eStation, long bDatetime, long eDatetime) {
        String sqlSelection = CashTable.COLUMN_B_ENTER_STATION + " = '" + bStation + "' and " +
                CashTable.COLUMN_E_ENTER_STATION + " = '" + eStation + "' and " +
                CashTable.COLUMN_B_DATETIME + " >= '" + bDatetime + "' and " +
                CashTable.COLUMN_E_DATETIME + " <= '" + eDatetime + "'";
        return loadData(sqlSelection);
    }

    public static ListRoute loadData(String bStation, String eStation, String bDate) {
        String sqlSelection = CashTable.COLUMN_B_ENTER_STATION + " = '" + bStation + "' and " +
                CashTable.COLUMN_E_ENTER_STATION + " = '" + eStation + "' and " +
                CashTable.COLUMN_B_DATE + " = '" + bDate + "'";
        return loadData(sqlSelection);
    }

    // ================================== СОХРАНЕНИЕ ДАННЫХ ========================================

    public static void saveData(ListRoute listRoute) {
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            ContentValues cv = new ContentValues();
            for (Route route : listRoute) {
                cv.put(CashTable.COLUMN_B_STATION, route.getBStation());
                cv.put(CashTable.COLUMN_E_STATION, route.getEStation());
                cv.put(CashTable.COLUMN_B_ENTER_STATION, route.getBEnterStation());
                cv.put(CashTable.COLUMN_E_ENTER_STATION, route.getEEnterStation());
                cv.put(CashTable.COLUMN_B_DATETIME, route.getBTime().getTimeInMillis());
                cv.put(CashTable.COLUMN_E_DATETIME, route.getETime().getTimeInMillis());
                cv.put(CashTable.COLUMN_B_DATE, route.getBTimeString(Route.DATE_FORMAT));
                cv.put(CashTable.COLUMN_NUMBER_TRAIN, route.getNumberTrain());
                cv.put(CashTable.COLUMN_TYPE_TRAIN, route.getTypeTrain());
                cv.put(CashTable.COLUMN_DETAIL_URI, route.getDetailURI());
                db.insert(CashTable.TABLE_CASH, null, cv);
            }
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
