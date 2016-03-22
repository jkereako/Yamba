package com.jkereako.yamba;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Created by jkereakoglow on 8/13/13.
 */
public class TimelineActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "TimelineActivity";
    private static final int STATUS_LOADER_ID = 47;
    // From what columns
    private static final String[] FROM = {
            StatusData.COL_USER,
            StatusData.COL_STATUS_TEXT,
            StatusData.COL_CREATED_AT
    };
    private static final int[] TO = {
            R.id.text_user,
            R.id.text_status_text,
            R.id.text_created_at
    };
    //private Cursor cursor;                  // This is the result set from the database
    private SimpleCursorAdapter adapter;    // And adapter is identical to a Delphi data source
    private TimelineReceiver receiver;
    // GENERAL EXPLANATION:
    // In order for the ListView to use the data of a Cursor object, it must be passed through a
    // mediator object of the class Adapter. Think of the adapter as a data massager for a database
    // result set.

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OLD METHOD. This loads data synchronously
       // cursor = getContentResolver().query(StatusProvider.CONTENT_URI, null, null, null, StatusData.COL_CREATED_AT + " DESC");

        // The layout parameter refers to a single row layout in a table. It is identical to iOS's
        // UITableViewCell, except 100 fucking times easier

        // This now loads the cursor object
        getLoaderManager().initLoader(STATUS_LOADER_ID, null, this);


        adapter = new SimpleCursorAdapter(this, R.layout.list_item, null, FROM, TO, CursorAdapter.FLAG_AUTO_REQUERY);

        // Set a view binder to the adapter. This performs special logic between data and view.
        adapter.setViewBinder(VIEW_BINDER);
        // Set the adapter
        setListAdapter(adapter);
    }

    // The methods below turn on and turn off the receiver which updates the timeline activity with
    // new status updates. The reason we register and unregister the receiver pragmatically versus
    // in the manifest file, is because we want to be able to turn it dynamically. Declaring the
    // broadcast receiver in the manifest file will leave the broadcast receiver on indefinitely.
    @Override
    protected void onResume() {
        super.onResume();

        if (receiver == null) {
            receiver = new TimelineReceiver();
        }
        // The bode below registers (turns on) the broadcast receiver so it becomes ready to listen
        // for broadcasted messages.For the given broadcast messages below, think of the intent
        // filter as thusly:
        //
        // Broadcast messages: ACTION_FOO, ACTION_BAR, ACTION_SPAM, ACTION_NEW_STATUS, ACTION_EGGS
        //
        // Of the broadcast messages listed above, we only want ACTION_NEW_STATUS, therefore, we
        // must filter the broadcast messages by telling Android that we specifically want to
        // receive the message ACTION_NEW_STATUS.
        registerReceiver(receiver, new IntentFilter(YambaApplication.ACTION_NEW_STATUS));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister (turn off) the receiver.
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timeline, menu);
        return true;
    }

    // Return "true" if the menu item selection was handled. Return "false" if it was not.
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Intent updateServiceIntent = new Intent(this, UpdaterService.class);
        Intent refreshServiceIntent = new Intent(this, RefreshService.class);

        switch ( item.getItemId()){
            case R.id.menu_start_service:
                startService(updateServiceIntent);
                return true;

            case R.id.menu_stop_service:
                stopService(updateServiceIntent);
                return true;

            case R.id.menu_status:
                startActivity( new Intent(this, StatusActivity.class));
                return true;

            case R.id.menu_refresh_service:
                startService(refreshServiceIntent);
                return true;

            case R.id.menu_settings:
                startActivity( new Intent(this, SettingsActivity.class));
                return true;
        }

        return false;
    }

    // This is a constant which adds business logic between the adapter and the view.
    static final ViewBinder VIEW_BINDER = new ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() != R.id.text_created_at) {
                return false;
            }

//            cursor.getColumnIndex(StatusData.COL_CREATED_AT);
            long time = cursor.getLong(columnIndex);

            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time);

            ((TextView) view).setText(relativeTime);

            return true;
        }
    };

    //-- LoaderManager.Callbacks<Cursor>. This is an asynchronous way of loading data.

    // This method is executed on a background thread
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // The argument "this" is a context. Remember, the class Activity is a subclass of Context.
        // So, we can pass an Activity object as a Context.
        return new CursorLoader(this, StatusProvider.CONTENT_URI, null, null, null, StatusData.COL_CREATED_AT + " DESC");
    }

    // And this method is executed on the UI thread.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the current cursor with the new one above
        adapter.swapCursor(data);

        Log.d(TAG, "onLoadFinished() executed");
    }

    // Also executed on the UI thread
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Set the adapter back to the original cursor
        adapter.swapCursor(null);
    }

    // Inner class
    class TimelineReceiver extends BroadcastReceiver {
        private final String TAG = "TimelineReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            // The cursor is set by onLoadFinished() which is defined above.
            getLoaderManager().restartLoader(STATUS_LOADER_ID, null, TimelineActivity.this);
            //   old way
            //cursor = getContentResolver().query(StatusProvider.CONTENT_URI, null, null, null, StatusData.COL_CREATED_AT + " DESC");

            // Swap the current cursor with the new one above
            //adapter.swapCursor(cursor);

            Log.d(TAG, "onRecieve() executed with count " + intent.getIntExtra("count", 0));
        }
    }
}