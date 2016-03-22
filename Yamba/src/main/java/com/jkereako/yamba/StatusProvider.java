package com.jkereako.yamba;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by jkereakoglow on 8/20/13.
 *
 * A content provider is an API to an app's data. The only supported methods are CRUD methods. The
 * content provider is used by other apps but often used by desktop widgets
 *
 * This class is an interface to the data store, which, in this case, is a SQLite database
 */
public class StatusProvider extends ContentProvider {
    public static final String AUTHORITY = "content://com.jkereako.yamba.provider";
    public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);
    SQLiteDatabase db;
    DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = dbHelper.getReadableDatabase();

        // SELECT * FROM status ORDER BY created_at DESC
        // This returns a Cursor, which is really just a set.
        Cursor resultSet =  db.query(YambaApplication.statusData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        // DO NOT close the database. Once it's closed, the data (cursor object) is released and
        // suddenly, resultSet will be set to null. Let Android manage the lifecycle
        //db.close();
        return resultSet;
    }

    // Returns a MIME Type
    @Override
    public String getType(Uri uri) {
        if (null == uri.getLastPathSegment()){
            return "vnd.android.cursor.dir/vnd.jkereako.yamba.status"; // for when you expect the Cursor to contain 0..x items
        }
        else {
            return "vnd.android.cursor.item/vnd.jkereako.yamba.status"; // for when you expect the Cursor to contain 1 item
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        long id = db.insertWithOnConflict(YambaApplication.statusData.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        // Build a fully qualified URI if the insertion was successful.
        if (-1 != id) {
            uri = Uri.withAppendedPath(uri, Long.toString(id));
        }

        return uri;
    }

    /**
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return int number of records deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Inner class DbHelper. This class is not static because we want it to always retain its
     * association with the outer class, StatusProvider. DbHelper should not exist outside of
     * StatusProvider.
     */
    class DbHelper extends SQLiteOpenHelper {
        private String TAG = "DbHelper";

        DbHelper(Context context) {
            // Here, we pass the parent our database name and version. This is how it builds the
            // database file
            super(context, StatusData.DB_NAME, null, StatusData.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(StatusData.SQL_CREATE_TABLE);
            Log.d(TAG, "onCreate() executed with SQL: " + StatusData.SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
