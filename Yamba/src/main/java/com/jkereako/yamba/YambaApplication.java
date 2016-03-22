package com.jkereako.yamba;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

/**
 * Created by jkereakoglow on 8/10/13.
 *
 * This is an application class meaning, it is visible to all classes within the com.jkereako.yamba
 * package. It is ideal for centralizing code and data. You access the class thusly:
 *
 *  ((YambaApplication) getApplication())
 */
public class YambaApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "YambaApplication";
    public static final String ACTION_NEW_STATUS = "com.jkereako.yamba.action.NEW_STATUS";
    public static final String ACTION_REFRESH_TIMELINE = "com.jkereako.yamba.action.REFRESH_TIMELINE";
    public static final String ACTION_REFRESH_ALARM = "com.jkereako.yamba.action.REFRESH_ALARM";
    public static Twitter twitter;
    public static SharedPreferences preferences;
    public static StatusData statusData;
    public static StatusProvider statusProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        statusData = new StatusData();

        // This must be instantiated AFTER StatusData.
        statusProvider = new StatusProvider();

        twitter = new Twitter(preferences.getString("username", ""),  preferences.getString("password", ""));
        twitter.setAPIRootUrl(preferences.getString("api_url", ""));

        Log.d(TAG, "onCreate() executed");
    }

    static final Intent refreshAlarm = new Intent(ACTION_REFRESH_ALARM);
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("username") || key.equals("password") ) {
            twitter = null;
            twitter = new Twitter(sharedPreferences.getString("username", ""),  sharedPreferences.getString("password", ""));
        }

        twitter.setAPIRootUrl(sharedPreferences.getString("api_url", ""));

        sendBroadcast(refreshAlarm);
        Log.d(TAG, "onSharedPreferenceChanged() executed for key " + key);
    }

    long lastTimestampSeen = -1;

    public int pullAndInsert() {
        int count = 0;
        try {
            List<Twitter.Status> statusList = twitter.getPublicTimeline();

            // This is Java's foreach loop
            for (Twitter.Status status: statusList) {
                getContentResolver().insert(StatusProvider.CONTENT_URI, StatusData.statusToContentValues(status));

                // This logic determines how many new Tweets exist based on created_at
                if (status.createdAt.getTime() > lastTimestampSeen){
                    ++ count;
                    lastTimestampSeen = status.createdAt.getTime();
                }
                Log.d(TAG, String.format("%s -\"%s\"", status.user.name, status.text));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        if (count > 0) {

            // The value passed to the constructor Intent() can be anything. The only thing to make
            // sure of is the broadcast sender's intent must match the broadcast receiver's intent.

            // Think of an Intent object as a URL GET, for example,
            // http://nesn.com?category=boston-red-sox&page=7
            // The URL above tells the server which resource the browser requested and also pass
            // parameters to that resource so we can filter the results.

            // putExtra() attaches an argument to the Intent object which is to be received by the
            // broadcast receiver.
            sendBroadcast(new Intent(ACTION_NEW_STATUS).putExtra("count", count));
        }

        return count;
    }
}
