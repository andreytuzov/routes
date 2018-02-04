package ru.railway.dc.routes.database.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.database.DB;
import ru.railway.dc.routes.database.struct.CashDetailTable;
import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

public class CashDetailTableUtils {

    public static Logger logger = Logger.getLogger(CashDetailTableUtils.class);

    // =============================== УДАЛЕНИЕ ДАННЫХ =============================================

    public static void clearData() {
        logger.info("Производится очистка кэша");
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            int count = db.delete(CashDetailTable.TABLE_CASH_DETAIL, null, null);
            logger.debug("Очистка кэша закончена. Удалено " + count + " строк");
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

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
//            int count = db.delete(CashDetailTable.TABLE_CASH_DETAIL,
//                    CashDetailTable.COLUMN_DATE + " != '" + date + "'", null);
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
        ListRoute listRoute = new ListRoute();
        Cursor c = null;
        try {
            db = DB.getSqlLiteDatabase();
            // Получаем данные
            c = db.query(CashDetailTable.TABLE_CASH_DETAIL, null, sqlSelection, null, null, null, null);
            // Выолняем перебор
            if (c.moveToFirst()) {
                int bStationIndex = c.getColumnIndex(CashDetailTable.COLUMN_B_STATION);
                int eStationIndex = c.getColumnIndex(CashDetailTable.COLUMN_E_STATION);
                int bDateTimeIndex = c.getColumnIndex(CashDetailTable.COLUMN_B_DATETIME);
                int eDateTimeIndex = c.getColumnIndex(CashDetailTable.COLUMN_E_DATETIME);
                int numberTrainIndex = c.getColumnIndex(CashDetailTable.COLUMN_NUMBER_TRAIN);
                int typeTrainIndex = c.getColumnIndex(CashDetailTable.COLUMN_TYPE_TRAIN);
                int bEnterStationIndex = c.getColumnIndex(CashDetailTable.COLUMN_B_ENTER_STATION);
                int eEnterStationIndex = c.getColumnIndex(CashDetailTable.COLUMN_E_ENTER_STATION);

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


    public static ListRoute loadData(String date, String numberTrain) {
        String sqlSelection = CashDetailTable.COLUMN_NUMBER_TRAIN + " = '" + numberTrain + "' and " +
                CashDetailTable.COLUMN_DATE + " = '" + date + "'";
        return loadData(sqlSelection);
    }

    // ================================== СОХРАНЕНИЕ ДАННЫХ ========================================

    public static void saveData(ListRoute listRoute) {
        SQLiteDatabase db = null;
        try {
            db = DB.getSqlLiteDatabase();
            db.beginTransaction();
            try {
                ContentValues cv = new ContentValues();
                for (Route route : listRoute) {
                    cv.clear();
                    cv.put(CashDetailTable.COLUMN_B_STATION, route.getBStation());
                    cv.put(CashDetailTable.COLUMN_E_STATION, route.getEStation());
                    cv.put(CashDetailTable.COLUMN_B_ENTER_STATION, route.getBEnterStation());
                    cv.put(CashDetailTable.COLUMN_E_ENTER_STATION, route.getEEnterStation());
                    cv.put(CashDetailTable.COLUMN_B_DATETIME, route.getBTime().getTimeInMillis());
                    cv.put(CashDetailTable.COLUMN_E_DATETIME, route.getETime().getTimeInMillis());
                    cv.put(CashDetailTable.COLUMN_DATE, route.getBTimeString(Route.DATE_FORMAT));
                    cv.put(CashDetailTable.COLUMN_NUMBER_TRAIN, route.getNumberTrain());
                    cv.put(CashDetailTable.COLUMN_TYPE_TRAIN, route.getTypeTrain());
                    db.insert(CashDetailTable.TABLE_CASH_DETAIL, null, cv);
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
    }

}
