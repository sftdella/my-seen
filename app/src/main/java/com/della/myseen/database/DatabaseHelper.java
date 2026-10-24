package com.della.myseen.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "myseen.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_SHOWS = "shows";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_GENRE = "genre";
    public static final String COL_STATUS = "status";
    public static final String COL_RATING = "rating";
    public static final String COL_ADDED_DATE = "added_date";
    public static final String COL_ADDED_TIME = "added_time";
    public static final String COL_NOTE = "note";
    public static final String COL_ADDED_TIMESTAMP = "added_timestamp";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_SHOWS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE + " TEXT NOT NULL, " +
            COL_GENRE + " TEXT NOT NULL, " +
            COL_STATUS + " TEXT NOT NULL, " +
            COL_RATING + " INTEGER, " +
            COL_ADDED_DATE + " TEXT NOT NULL, " +
            COL_ADDED_TIME + " TEXT NOT NULL, " +
            COL_NOTE + " TEXT, " +
            COL_ADDED_TIMESTAMP + " INTEGER NOT NULL DEFAULT 0)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
        onCreate(db);
    }
}
