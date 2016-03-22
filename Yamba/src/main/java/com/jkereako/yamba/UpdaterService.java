package com.jkereako.yamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by jkereakoglow on 8/9/13.
 */
public class UpdaterService extends Service {
    private static final String TAG = "UpaterService";
    private static final int DELAY = 30;
    private boolean threadIsRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int delay = Integer.parseInt(((YambaApplication) getApplication()).preferences.getString("refresh_interval", "30"));
        threadIsRunning = true;
        // Why not subclass AsyncTask? Because we don't need to synchronize the background thread
        // with the main (UI) thread. Android services do not use UI updates, hence, there is no
        // need to use AsyncTask.
        //
        // A service does not have access to the UI except for a Toast.

        // This is an Anonymous inner class
        new Thread() {
            public void run() {
                // We wrap this in a try/catch block in case the thread fails for whatever reason.
                // we want to report this error back to the user.
                try {
                    // The boolean threadIsRunning is a way to control the this loop, which is in
                    // a background thread, from the main thread. If we didn't have this boolean,
                    // then the background thread would run perpetually, even when we're not using
                    // it.
                    while (threadIsRunning) {
                        ((YambaApplication) getApplication()).pullAndInsert();

                        Thread.sleep(delay * 1000);
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "Updater interrupted.");
                }
            }
        }.start();

        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        threadIsRunning = false;
        Log.d(TAG, "onDestroy() executed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
