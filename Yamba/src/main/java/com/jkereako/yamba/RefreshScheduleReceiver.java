package com.jkereako.yamba;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The purpose of this class is to schedule stuff with the Android service AlarmManager
 */
public class RefreshScheduleReceiver extends BroadcastReceiver {
    private static final String TAG = "RefreshScheduleReceiver";
    private static PendingIntent lastOperation;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long interval = Long.parseLong(
                prefs.getString(
                        // Get the actual value from strings.xml
                        context.getString(R.string.pref_title_refresh_interval),
                        "900000"
                )
        );

        // Get the Alarm Manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Log.d(TAG, "onReceive() executed");
            // This is the intent which we define now, but will execute sometime later in the future.
            PendingIntent operation  = PendingIntent.getService(
                    context,
                    -1,
                    new Intent(YambaApplication.ACTION_REFRESH_TIMELINE), // This is the intent
                    PendingIntent.FLAG_UPDATE_CURRENT
            );



        if (interval > 0) {
            alarmManager.cancel(lastOperation);

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC,
                    System.currentTimeMillis(),
                    interval,
                    operation
            );
        }

        lastOperation = operation;

//            context.startService( new Intent(context, UpdaterService.class) );

    }
}
