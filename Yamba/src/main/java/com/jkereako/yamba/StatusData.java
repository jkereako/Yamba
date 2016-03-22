package com.jkereako.yamba;

import android.content.ContentValues;
import android.provider.BaseColumns;

import winterwell.jtwitter.Twitter.Status;

/**
 * This file just describes the database schema. Left here to make shit easier to maintain.
 */
public class StatusData {
    private static final String TAG = "StatusData";

    // Always make the database schema information public static final so it can easily be accessed
    // throughout the application
    public static final String DB_NAME = "timeline.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "status";

    //  Database Schema
    public static final String COL_ID = BaseColumns._ID;
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_USER = "user";
    public static final String COL_STATUS_TEXT = "status_text";

    // Leave the newlines in so it's easily read in logcat
    public static final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE [%s] (\n" +
            "    [%s] INTEGER PRIMARY KEY NOT NULL,\n" +
            "    [%s] INTEGER,\n" +
            "    [%s] TEXT,\n" +
            "    [%s] TEXT\n" +
            ")",
            TABLE_NAME, COL_ID, COL_CREATED_AT, COL_USER, COL_STATUS_TEXT);

    public static ContentValues statusToContentValues(Status status) {
        // We can call getWritableDatabase() as many times as we want because the database is only
        // opened once and a cached version of the database is referenced from then on.

        ContentValues values = new ContentValues();

        values.put(COL_ID, status.id);
        values.put(COL_CREATED_AT, status.createdAt.getTime());
        values.put(COL_USER, status.user.name);
        values.put(COL_STATUS_TEXT, status.text);

        return values;
    }

}
