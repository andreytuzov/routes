package ru.railway.dc.routes.database.struct;

/**
 * Created by SQL on 04.01.2017.
 */

public class EventTable {

    // Таблица для хранения кэша
    public static final String TABLE_EVENT = "tableEvent";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_E_DATETIME = "eDateTime";
    public static final String COLUMN_IS_NOTIFICATION = "isNotification";
    public static final String COLUMN_IS_WIDGET = "isWidget";

    public static final String SQL_DB_TABLE_EVENT_CREATE =
            "create table " + TABLE_EVENT + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_E_DATETIME + " integer, " +
                    COLUMN_IS_NOTIFICATION + " integer, " +
                    COLUMN_IS_WIDGET + " integer" +
                    ");";
}
