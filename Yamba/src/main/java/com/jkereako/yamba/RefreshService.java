package com.jkereako.yamba;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jkereakoglow on 8/10/13.
 */
public class RefreshService extends IntentService {
    private static final String TAG = "RefreshService";

    // This must be an "empty constructor" which means "no parameters allowed".
    public RefreshService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate() executed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ((YambaApplication) getApplication()).pullAndInsert();

        Log.d(TAG, "onHandleIntent() executed.");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() executed");
        super.onDestroy();
    }
}
