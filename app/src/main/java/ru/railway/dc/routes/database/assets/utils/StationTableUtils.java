package ru.railway.dc.routes.database.assets.utils;

/**
 * Created by SQL on 04.01.2017.
 */

public class StationTableUtils {

    // ======================== РАБОТА СО СПИСКОМ СОБЫТИЙ ==========================================

//
//    // Загрузка списка станций
//    public static List<Station> loadStation(String pattern) {
//        List<Station> list = null;
//        SQLiteDatabase db = null;
//        Cursor c = null;
//        try {
//            db = AssetsDB.getSqlLiteDatabase();
//            c = db.query(CountryView.VIEW_COUNTRY, null,
//                    CountryView.COLUMN_NAME + " like '" + pattern + "%'", null, null, null, null);
//            if (c.moveToFirst()) {
//                list = new ArrayList<>();
//                // Получаем индексы
//                int idIndex = c.getColumnIndex(CountryView.COLUMN_ID);
//                int idName = c.getColumnIndex(CountryView.COLUMN_NAME);
//                int idRegion = c.getColumnIndex(CountryView.COLUMN_REGION);
//                // Обходим данные
//                do {
//                    list.add(new Station(c.getInt(idIndex),
//                            c.getString(idName), c.getString(idRegion)));
//                } while (c.moveToNext());
//            }
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//            if (c != null) {
//                c.close();
//            }
//        }
//        return list;
//    }
}
