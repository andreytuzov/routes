package ru.railway.dc.routes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.log4j.Logger;

import java.util.List;

import ru.railway.dc.routes.database.assets.AssetsDBUtils;
import ru.railway.dc.routes.database.assets.struct.CountryView;
import ru.railway.dc.routes.database.assets.struct.RegionCountryTable;
import ru.railway.dc.routes.database.assets.struct.StationTable;

/**
 * Created by SQL on 14.01.2017.
 */

public class AssetsDB {

    public static final Logger logger = Logger.getLogger(AssetsDB.class);

    // Параметры базы данных
    public static String DB_FOLDER;
    private static final String DB_NAME = "stations.db";
    public static String DB_PATH;

    public static final String DB_ASSETS_PATH = "db/" + DB_NAME;

    public static final int DB_VERSION = 1;
    public static final int DB_FILES_COPY_BUFFER_SIZE = 8192;

    private static Context context;
    private SQLiteDatabase db;


    public static void configure(Context cnxt) {
        context = cnxt;
        if (context == null) {
            return;
        }
        DB_FOLDER = "/data/data/" +
                context.getPackageName() + "/databases/";
        DB_PATH = DB_FOLDER + DB_NAME;
        // Копируем таблицу если нужно
        if (!AssetsDBUtils.isInitialized()) {
            logger.debug("Выполняется копирование");
            AssetsDBUtils.copyDBFromAssets(context);
        } else {
            logger.debug("База уже сконфигурирована");
        }
    }

    public Cursor getStationCursor(String pattern) {
        String selection;
        String ch = "";
        if (pattern != null) {
            if (pattern.length() > 1) {
                pattern = pattern.substring(0, 1).toUpperCase() + pattern.substring(1);
            } else {
                pattern = pattern.toUpperCase();
            }
            selection = CountryView.COLUMN_NAME + " like '" + pattern + "%'";
        } else {
            selection = CountryView.COLUMN_FAVOURITE + " = '1'";
        }
        return db.query(CountryView.VIEW_COUNTRY, null, selection, null, null, null,
                CountryView.COLUMN_FAVOURITE + " desc ," + CountryView.COLUMN_NAME, null);
    }

    public Cursor getRegionCursor(String pattern) {
        String selection;
        if (pattern == null) {
            selection = "tableRegionCountry.isChoice = '1'";
        } else {
            selection = " tableRegion.name like '" + pattern + "%'";
        }
        String sql = " " +
            " select tableRegionCountry._id as _id, " +
                " tableRegion.name as region, " +
                " tableCountry.name as country " +
            " from tableRegionCountry, tableRegion, tableCountry " +
            " where tableRegionCountry.countryTableID = tableCountry._id " +
                " and tableRegionCountry.regionTableID = tableRegion._id " +
                " and " + selection +
            " order by tableRegionCountry.isChoice desc, country, region";
        return db.rawQuery(sql, null);
    }

    public void updateTableRegion(List<Long> list) {
        db.beginTransaction();
        try {
            // Обновление таблицы с выбранными регионами
            int size = list.size();
            StringBuilder listID = new StringBuilder();
            for (int i = 0; i < size - 1; i++) {
                listID.append(list.get(i) + ", ");
            }
            listID.append(list.get(size - 1));
            db.execSQL("update " + RegionCountryTable.TABLE_REGION_COUNTRY
                    + " set " + RegionCountryTable.COLUMN_IS_CHOICE + " = '1'"
                    + " where " + RegionCountryTable.COLUMN_ID + " in (" + listID + ")");
            db.execSQL("update " + RegionCountryTable.TABLE_REGION_COUNTRY
                    + " set " + RegionCountryTable.COLUMN_IS_CHOICE + " = '0'"
                    + " where " + RegionCountryTable.COLUMN_ID + " not in (" + listID + ")");
            // Обновляем представление
            db.execSQL("drop view " + CountryView.VIEW_COUNTRY);
            String sql = "create view " + CountryView.VIEW_COUNTRY + " as" +
                    " select tableStation._id," +
                        " tableStation.name as name," +
                        " tableRegion.name as region," +
                        " tableStation.isFavourite as favourite" +
                    " from tableStation, tableRegionCountry, tableRegion" +
                    " where tableStation.countryTableID = tableRegionCountry.countryTableID" +
                        " and tableStation.regionTableID = tableRegionCountry.regionTableID" +
                        " and tableRegionCountry.regionTableID = tableRegion._id" +
                        " and tableRegionCountry._id in (" + listID + ")";
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void setFavourite(int _id, int favourite) {
        ContentValues cv = new ContentValues();
        cv.put(StationTable.COLUMN_IS_FAVOURITE, favourite);
        int count = db.update(StationTable.TABLE_STATION, cv, StationTable.COLUMN_ID + " = '" + _id + "'", null);
        logger.debug("_id = " + _id + ", favourite = " + favourite);
    }

    public void open() {
        DBHelper dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
//            throw new SQLiteException(
//                    "Initialize first. The method should never be called.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            throw new SQLiteException(
//                    "Initialize first. The method should never be called.");
        }
    }
}
