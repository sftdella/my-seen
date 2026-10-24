package com.della.myseen.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.della.myseen.model.Show;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShowDAO {

    private final DatabaseHelper dbHelper;

    public ShowDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private ContentValues toValues(Show show) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_TITLE, show.getTitle());
        cv.put(DatabaseHelper.COL_GENRE, show.getGenre());
        cv.put(DatabaseHelper.COL_STATUS, show.getStatus());
        if (show.getRating() != null) {
            cv.put(DatabaseHelper.COL_RATING, show.getRating());
        } else {
            cv.putNull(DatabaseHelper.COL_RATING);
        }
        cv.put(DatabaseHelper.COL_ADDED_DATE, show.getAddedDate());
        cv.put(DatabaseHelper.COL_ADDED_TIME, show.getAddedTime());
        cv.put(DatabaseHelper.COL_NOTE, show.getNote());
        cv.put(DatabaseHelper.COL_ADDED_TIMESTAMP, show.getAddedTimestamp());
        return cv;
    }

    private Show fromCursor(Cursor c) {
        Show show = new Show();
        show.setId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        show.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TITLE)));
        show.setGenre(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_GENRE)));
        show.setStatus(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_STATUS)));
        int ratingIdx = c.getColumnIndexOrThrow(DatabaseHelper.COL_RATING);
        show.setRating(c.isNull(ratingIdx) ? null : c.getInt(ratingIdx));
        show.setAddedDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ADDED_DATE)));
        show.setAddedTime(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ADDED_TIME)));
        show.setNote(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE)));
        show.setAddedTimestamp(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ADDED_TIMESTAMP)));
        return show;
    }

    public long insert(Show show) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DatabaseHelper.TABLE_SHOWS, null, toValues(show));
        db.close();
        return id;
    }

    public boolean update(Show show) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(DatabaseHelper.TABLE_SHOWS, toValues(show),
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(show.getId())});
        db.close();
        return rows > 0;
    }

    public boolean delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_SHOWS,
                DatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<Show> getAll() {
        return query(null, null);
    }

    public List<Show> getByStatus(String status) {
        return query(DatabaseHelper.COL_STATUS + "=?", new String[]{status});
    }

    public List<Show> search(String keyword) {
        return query(DatabaseHelper.COL_TITLE + " LIKE ?", new String[]{"%" + keyword + "%"});
    }

    public List<Show> searchByStatus(String keyword, String status) {
        return query(
                DatabaseHelper.COL_TITLE + " LIKE ? AND " + DatabaseHelper.COL_STATUS + "=?",
                new String[]{"%" + keyword + "%", status});
    }

    public List<Show> getByTimeFilter(String filter) {
        long now = System.currentTimeMillis() / 1000L;
        long from;
        switch (filter) {
            case "Hari Ini":
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                from = cal.getTimeInMillis() / 1000L;
                break;
            case "Minggu Ini":
                from = now - (7L * 24 * 60 * 60);
                break;
            case "Bulan Ini":
                from = now - (30L * 24 * 60 * 60);
                break;
            default:
                return getAll();
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Show> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_SHOWS, null,
                DatabaseHelper.COL_ADDED_TIMESTAMP + " >= ?",
                new String[]{String.valueOf(from)}, null, null,
                DatabaseHelper.COL_ID + " DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        db.close();
        return list;
    }

    private List<Show> query(String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Show> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_SHOWS, null, selection, selectionArgs,
                null, null, DatabaseHelper.COL_ID + " DESC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        db.close();
        return list;
    }

    public Map<String, Integer> getCountByStatus() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_STATUS + ", COUNT(*) as cnt FROM " +
                DatabaseHelper.TABLE_SHOWS + " GROUP BY " + DatabaseHelper.COL_STATUS, null);
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getInt(1));
        }
        c.close();
        db.close();
        return map;
    }

    public Map<String, Integer> getCountByStatusFiltered(long fromTimestamp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, Integer> map = new LinkedHashMap<>();
        String where = fromTimestamp > 0
                ? " WHERE " + DatabaseHelper.COL_ADDED_TIMESTAMP + " >= " + fromTimestamp : "";
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_STATUS + ", COUNT(*) as cnt FROM " +
                DatabaseHelper.TABLE_SHOWS + where +
                " GROUP BY " + DatabaseHelper.COL_STATUS, null);
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getInt(1));
        }
        c.close();
        db.close();
        return map;
    }

    public double getAverageRating() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double avg = 0;
        Cursor c = db.rawQuery(
                "SELECT AVG(" + DatabaseHelper.COL_RATING + ") FROM " +
                DatabaseHelper.TABLE_SHOWS + " WHERE " + DatabaseHelper.COL_STATUS + "='Sudah Ditonton'", null);
        if (c.moveToFirst() && !c.isNull(0)) avg = c.getDouble(0);
        c.close();
        db.close();
        return avg;
    }

    public int getSudahCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SHOWS +
                " WHERE " + DatabaseHelper.COL_STATUS + "='Sudah Ditonton'" +
                " AND " + DatabaseHelper.COL_RATING + " IS NOT NULL", null);
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }

    public List<Show> getTop5ByRating() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Show> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_SHOWS +
                " WHERE " + DatabaseHelper.COL_STATUS + "='Sudah Ditonton'" +
                " AND " + DatabaseHelper.COL_RATING + " IS NOT NULL" +
                " ORDER BY " + DatabaseHelper.COL_RATING + " DESC LIMIT 5", null);
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        db.close();
        return list;
    }

    public List<Show> getRecentActivity(int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Show> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_SHOWS, null, null, null, null, null,
                DatabaseHelper.COL_ID + " DESC", String.valueOf(limit));
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        db.close();
        return list;
    }

    public Map<String, Integer> getCountByGenre() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, Integer> map = new LinkedHashMap<>();
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_GENRE + ", COUNT(*) as cnt FROM " +
                DatabaseHelper.TABLE_SHOWS +
                " GROUP BY " + DatabaseHelper.COL_GENRE +
                " ORDER BY cnt DESC", null);
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getInt(1));
        }
        c.close();
        db.close();
        return map;
    }

    public Map<String, Integer> getCountByGenreFiltered(long fromTimestamp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, Integer> map = new LinkedHashMap<>();
        String where = fromTimestamp > 0
                ? " WHERE " + DatabaseHelper.COL_ADDED_TIMESTAMP + " >= " + fromTimestamp : "";
        Cursor c = db.rawQuery(
                "SELECT " + DatabaseHelper.COL_GENRE + ", COUNT(*) as cnt FROM " +
                DatabaseHelper.TABLE_SHOWS + where +
                " GROUP BY " + DatabaseHelper.COL_GENRE +
                " ORDER BY cnt DESC", null);
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getInt(1));
        }
        c.close();
        db.close();
        return map;
    }
}
