package ru.railway.dc.routes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.log4j.Logger;

import ru.railway.dc.routes.database.struct.CashDetailTable;
import ru.railway.dc.routes.database.struct.CashTable;
import ru.railway.dc.routes.database.struct.EventTable;
import ru.railway.dc.routes.database.struct.RouteDetailTable;
import ru.railway.dc.routes.database.struct.RouteTable;

/**
 * Created by SQL on 03.01.2017.
 */


// Создание таблиц и подключение к базе данных
public class DB {

    public static final Logger logger = Logger.getLogger(DB.class);

    // Параметры базы данных
    private static final String DB_NAME = "db";
    private static final int DB_VERSION = 13;

    private static Context context;

    public static void configure(Context cntx) {
        context = cntx;
    }

    public static SQLiteDatabase getSqlLiteDatabase() {
        if (context == null) {
            logger.error("Ошибка конфигурации класса DB");
            return null;
        }
        return new DBHelper(context, DB_NAME, null, DB_VERSION).getWritableDatabase();
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory,
                        int version) {
            super(context, name, cursorFactory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();

            try {
                createTable(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL(CashTable.SQL_DB_TABLE_CASH_CREATE);
            db.execSQL(RouteTable.SQL_DB_TABLE_ROUTE_CREATE);
            db.execSQL(RouteDetailTable.SQL_DB_TABLE_ROUTE_DETAIL_CREATE);
            db.execSQL(EventTable.SQL_DB_TABLE_EVENT_CREATE);
            db.execSQL(CashDetailTable.SQL_DB_TABLE_CASH_DETAIL_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.beginTransaction();
            try {
                db.execSQL("drop table " + CashTable.TABLE_CASH);
                db.execSQL("drop table " + RouteTable.TABLE_ROUTE);
                db.execSQL("drop table " + EventTable.TABLE_EVENT);
                db.execSQL("drop table " + RouteDetailTable.TABLE_ROUTE_DETAIL);
                db.execSQL("drop table " + CashDetailTable.TABLE_CASH_DETAIL);
                createTable(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

}
