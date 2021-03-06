package com.example.tdvpr_000.camera_shit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tdvpr_000 on 2/14/2017.
 */

public class DBManager extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + DBContract.FeedEntry.TABLE_NAME
            + " (" + DBContract.FeedEntry._ID + " INTEGER PRIMARY KEY,"
            + DBContract.FeedEntry.COLUMN_FILE + " TEXT NOT NULL,"
            + DBContract.FeedEntry.COLUMN_TAGS + " TEXT,"
            + DBContract.FeedEntry.COLUMN_DATES + " LONG NOT NULL,"
            + DBContract.FeedEntry.COLUMN_VALUE + " NUMERIC)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE '" + DBContract.FeedEntry.TABLE_NAME + "'";


    private static final String SQL_NUMERICAL_TAGS = "SELECT DISTINCT " + DBContract.FeedEntry.COLUMN_TAGS + " FROM " + DBContract.FeedEntry.TABLE_NAME
            + " WHERE " + DBContract.FeedEntry.COLUMN_VALUE + " IS NOT NULL";

    private static final String SQL_VALUES_WITH_TAG = "SELECT " + DBContract.FeedEntry.COLUMN_VALUE + ","
            + DBContract.FeedEntry.COLUMN_DATES + " FROM " + DBContract.FeedEntry.TABLE_NAME
            + " WHERE " + DBContract.FeedEntry.COLUMN_VALUE + " IS NOT NULL AND " + DBContract.FeedEntry.COLUMN_TAGS + " = ";

    // get tags from file location
    private static final String SQL_TAGS = "SELECT DISTINCT " + DBContract.FeedEntry.COLUMN_TAGS + ", " + DBContract.FeedEntry.COLUMN_DATES + ", " + DBContract.FeedEntry.COLUMN_VALUE
            + " FROM " + DBContract.FeedEntry.TABLE_NAME
            + " WHERE " + DBContract.FeedEntry.COLUMN_FILE + " = ";

    // get counts for each tag
    private static final String SQL_TAG_COUNT = "SELECT " + DBContract.FeedEntry.COLUMN_TAGS + ", count("
            + DBContract.FeedEntry.COLUMN_TAGS + ") AS TAG_APPEARANCES FROM " + DBContract.FeedEntry.TABLE_NAME + " GROUP BY "
            + DBContract.FeedEntry.COLUMN_TAGS;

    private static final String SQL_WITH_TAGS = "SELECT  min(" + DBContract.FeedEntry._ID + ") as _id, min(" + DBContract.FeedEntry.COLUMN_DATES + ") as " + DBContract.FeedEntry.COLUMN_DATES + ","
            + DBContract.FeedEntry.COLUMN_FILE + " FROM " + DBContract.FeedEntry.TABLE_NAME
            + " WHERE " + DBContract.FeedEntry.COLUMN_TAGS + " IN ";

    private static final String SQL_ALL_TAGS = "SELECT " + DBContract.FeedEntry._ID + ", " + DBContract.FeedEntry.COLUMN_TAGS + " FROM " + DBContract.FeedEntry.TABLE_NAME + " GROUP BY " + DBContract.FeedEntry.COLUMN_TAGS + " ORDER BY " + DBContract.FeedEntry.COLUMN_DATES + " DESC";


    public static int DATABASE_VERSION = 1;
    // if passed -1, returns all
    public Cursor filesFromPastNDays(int n) {
        String queryStart = "SELECT MIN(" + DBContract.FeedEntry._ID + ") as _id," +
                DBContract.FeedEntry.COLUMN_FILE + ", MIN(" + DBContract.FeedEntry.COLUMN_DATES
                + ") as " + DBContract.FeedEntry.COLUMN_DATES + " FROM " + DBContract.FeedEntry.TABLE_NAME;
        String queryEnd = " GROUP BY " + DBContract.FeedEntry.COLUMN_FILE + " ORDER BY " + DBContract.FeedEntry.COLUMN_DATES + " DESC";
        if (n == -1) {
            Cursor c = getReadableDatabase().rawQuery(queryStart + queryEnd, null);
            while (c.moveToNext()) {
                Log.v("TESTINGG", ""+c.getCount());//c.getString(c.getColumnIndex(DBContract.FeedEntry.COLUMN_DATES)));
            }
            return c;
        }
        long millisPerDay = 86400000;
        long currentTime = System.currentTimeMillis();
        long product = n * millisPerDay;
        return getReadableDatabase().rawQuery(queryStart + " WHERE (" + currentTime
                + " - " + DBContract.FeedEntry.COLUMN_DATES + ") < " + product + queryEnd, null);
    }

    public Cursor numericalTags() {
        return getReadableDatabase().rawQuery(SQL_NUMERICAL_TAGS, null);
    }

    // returns values and dates associated with he given tag.
    public Cursor allValuesWithTag(String tag) {
        return getReadableDatabase().rawQuery(SQL_VALUES_WITH_TAG + "'" + tag + "'" + " ORDER BY " + DBContract.FeedEntry.COLUMN_DATES + " ASC", null);

    }

    public List<String> tagsFrom(String previewFilePath) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> result = new ArrayList<String>();
        Cursor cursor = db.rawQuery(SQL_TAGS + "'" + previewFilePath + "'", null);
        while(cursor.moveToNext()) {
            String tag = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBContract.FeedEntry.COLUMN_TAGS));
            String value = cursor.getString(cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_VALUE));
            if (value != null) {
                value += tag;
                result.add(value);
            } else {
                result.add(tag);
            }
        }
        cursor.close();
        return result;
    }

    public DBManager(Context context) {
        super(context, DBContract.FeedEntry.DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.FeedEntry.TABLE_NAME);
        onCreate(db);
    }

    public long getDateFromFile(String previewFilePath) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + DBContract.FeedEntry.COLUMN_DATES + " FROM " + DBContract.FeedEntry.TABLE_NAME + " WHERE " + DBContract.FeedEntry.COLUMN_FILE + "='" + previewFilePath + "'", null);
        String[] s = c.getColumnNames();
        for (int i = 0; i < s.length; i ++) {
            Log.v("DATABAW", s[i]);
        }
        int idx = c.getColumnIndex(DBContract.FeedEntry.COLUMN_DATES);
        if (c.moveToNext()) {
            Log.v("INDEX :", idx + "");
            return c.getLong(idx);
        }
        else
            return -1;
    }

    public void insertTags(String previewFilePath, ArrayAdapter<String> adapter, long date) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (date == -1) {
            date = System.currentTimeMillis();
        }
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBContract.FeedEntry.COLUMN_FILE, previewFilePath);
        values.put(DBContract.FeedEntry.COLUMN_DATES, date);
        if (adapter.getCount() == 0) {
            // no tags. we put default
            values.put(DBContract.FeedEntry.COLUMN_TAGS, DBContract.FeedEntry.DEFAULT_NO_TAGS_INPUTTED);
            db.insert(DBContract.FeedEntry.TABLE_NAME, null, values);
            return;
        }

        Set<String> uniqueTags = new HashSet<String>();

        String[] split = findNumbers(adapter.getItem(0));
        if (split.length == 1 && isNumber(split[0])) {
            // number inputtted
            values.put(DBContract.FeedEntry.COLUMN_VALUE, split[0]);
            values.put(DBContract.FeedEntry.COLUMN_TAGS, DBContract.FeedEntry.DEFAULT_NUMERICAL_TAG);
        } else if (split.length == 1) {
            // not a number
            uniqueTags.add(split[0]);
            values.put(DBContract.FeedEntry.COLUMN_TAGS, split[0]);
        } else {
            // number and tag
            values.put(DBContract.FeedEntry.COLUMN_TAGS, split[1]);
            values.put(DBContract.FeedEntry.COLUMN_VALUE, split[0]);
        }

        db.insert(DBContract.FeedEntry.TABLE_NAME, DBContract.FeedEntry.COLUMN_TAGS, values);

        // puts a row in the database for every tag.
        for (int i = 1; i < adapter.getCount(); i++) {
            values.remove(DBContract.FeedEntry.COLUMN_VALUE);
            String tag = adapter.getItem(i);
            split = findNumbers(tag);
            if (split.length == 1) {
                if (isNumber(split[0])) {
                    values.put(DBContract.FeedEntry.COLUMN_VALUE, split[0]);
                    values.put(DBContract.FeedEntry.COLUMN_TAGS, DBContract.FeedEntry.DEFAULT_NUMERICAL_TAG);
                } else {
                    // not numerical
                    if (!uniqueTags.contains(split[0])) {
                        values.put(DBContract.FeedEntry.COLUMN_TAGS, split[0]);
                        uniqueTags.add(split[0]);
                    } else {
                        // don't add duplicate tags
                        continue;
                    }
                }
            } else {
                values.put(DBContract.FeedEntry.COLUMN_TAGS, split[1]);
                values.put(DBContract.FeedEntry.COLUMN_VALUE, split[0]);
            }

            db.insert(DBContract.FeedEntry.TABLE_NAME, null, values);
        }

        db.close();
    }

    private boolean isNumber(String s) {
        boolean decimal = false;
        if (s.isEmpty()) return false;
        if (s.charAt(0) == '-') s = s.substring(1);
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                if (s.charAt(i) == '.' && !decimal) {
                    decimal = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    // splits string up into number and label
    // returns String[] with one element if no numerical AND label values
    private String[] findNumbers(String s) {
        String[] tmp = s.split("(?<=\\d)\\s*(?=[a-zA-Z])");
        return tmp;
    }

    public Cursor allTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(SQL_ALL_TAGS, null);
    }


    public Cursor totalFilter(Set<String> tags, int days) {
            if (tags.isEmpty()) return null;
            SQLiteDatabase db = this.getReadableDatabase();
//            String query = SQL_WITH_TAGS + "('" + tags.get(0) + "'";
            String query = "";
            boolean first = true;
            for (String s : tags) {
                if (first) {
                    query += SQL_WITH_TAGS + "('" + s + "'";
                    first = false;
                } else {
                    query += ",'" + s + "'";
                }
            }
//
//            for (int i = 1; i < tags.size(); i++) {
//                query += ",'" + tags.get(i) + "'";
//            }
            query += ")";
            if (days > -1) {
                long millisPerDay = 86400000;
                long currentTime = System.currentTimeMillis();
                long product = days * millisPerDay;
                long diff = currentTime - product;
                query += " AND " + DBContract.FeedEntry.COLUMN_DATES + " > " + diff;
            }
            query += " GROUP BY " + DBContract.FeedEntry.COLUMN_FILE + " ORDER BY " + DBContract.FeedEntry.COLUMN_DATES + " DESC";
            return db.rawQuery(query, null);
    }


    public Cursor allFilesWithTags(List<String> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String query = SQL_WITH_TAGS + "('" + tags.get(0) + "'";
        for (int i = 1; i < tags.size(); i++) {
            query += ",'" + tags.get(i) + "'";
        }
        return db.rawQuery(query + ")", null);
    }

    public Cursor countTag() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(SQL_TAG_COUNT, null);
    }

    public Map<String, Integer> countTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_TAG_COUNT, null);
        if (cursor.getCount() == 0)
            return null;
        Map<String, Integer> result = new HashMap<String, Integer>();
        while(cursor.moveToNext()) {
            String tag = cursor.getString(
                    cursor.getColumnIndex(DBContract.FeedEntry.COLUMN_TAGS));
            Integer i = cursor.getInt(cursor.getColumnIndex("c"));
            result.put(tag, i);
        }
        cursor.close();
        return result;
    }
}
