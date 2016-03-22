package com.jkereako.yamba;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import winterwell.jtwitter.TwitterException;

import static android.location.LocationManager.GPS_PROVIDER;

public class StatusActivity extends Activity implements LocationListener {
    private static final String TAG = "StatusActivity";  // for debug purposes
    private EditText statusText;  // Declare a variable which will refer to the UI instance of EditText
    private LocationManager locationManager;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        Debug.startMethodTracing("Yamba.trace");

        setContentView(R.layout.activity_status);

        // getSystemService() is actually this.getSystemService which is actually
        // Context.getSystemService()
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        location = locationManager.getLastKnownLocation(GPS_PROVIDER);

        // Find each view (the button and the text field) in the Java source file R.java. We must
        // cast the return value of findViewById() because that function returns a View object.
        statusText = (EditText) findViewById(R.id.status_editor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(GPS_PROVIDER, 300000, 1000, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.status, menu);
        return true;
    }

    // Return "true" if the menu item selection was handled. Return "false" if it was not.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent updateServiceIntent = new Intent(this, UpdaterService.class);
        Intent refreshServiceIntent = new Intent(this, RefreshService.class);

        switch (item.getItemId()){
            case R.id.menu_start_service:
                startService(updateServiceIntent);
                return true;

            case R.id.menu_stop_service:
                stopService(updateServiceIntent);
                return true;

            case R.id.menu_timeline:
                startActivity( new Intent(this, TimelineActivity.class));
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
    /**
     * This is a callback that is defined in activity_status.xml. This type of callback only works
     * for Button Views
     * @param v
     */
    public void onClick(View v) {

        new PostToTwitter().execute(statusText.getText().toString());
    }

    @Override
    protected void onStop() {
        super.onStop();

//        Debug.stopMethodTracing();
    }


    //--- LocationListener Callbacks

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d(TAG, "onLocationChanged() executed. Location: " + location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // Extend the class AsyncTask, which is an Android wrapper class for Java's Thread class. It
    // makes it much easier to synchronize a background thread with the main thread.

    // This is an example of the class template. We pass in class types to the definition of
    // PostToTwitter.
    class PostToTwitter extends AsyncTask<String, Void, String> {

        // This will run in a background thread.
        @Override
        protected String doInBackground(String... params) {
            try {
                ((YambaApplication) getApplication()).twitter.setStatus(params[0]);
            } catch (TwitterException e) {
                e.printStackTrace();
                return "Failed to Post Status";
            }

            return "Successfully Posted Status";
        }

        // This will run on the UI (main) thread
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(StatusActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }
}
