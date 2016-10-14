/**
 * UserLocation service which generates Location details and Sensor data
 */

package com.example.saurabh.auggraffiti;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class UserLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SensorEventListener{
    private final IBinder service = new TagBinder();
    private static RequestQueue rq;
    private String emailID = "";
    private Location location;
    private double lat = 0.0;
    private double lng = 0.0;
    private GoogleApiClient client;


    private static boolean isRunning = false;
    SensorManager sManager;
    float Rot[]=null; //for gravity rotational data
    float I[]=null; //for magnetic rotational data
    float accel[]=new float[3];
    float mags[]=new float[3];
    float[] values = new float[3];

    float azimuth;
    double altitude;

    static int SENSOR_INTERVAL = 100; // 1000ms
    long lastSaved = System.currentTimeMillis();

    public UserLocationService() {
    }


    /**
     * Invoked when bindservice called by any activity.
     * Creates a GoogleApiClient object to add LocationServices API.
     * Create SensorManager object and registers listeners to retrieve data from Accelerometer & Magnetometer.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "service binded!", Toast.LENGTH_LONG).show();
        rq = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        //Bundle extraData = intent.getExtras();
        //Object a =  extraData.getString("MapObject");
        //googleMap = (GoogleMap)a;client = new GoogleApiClient.Builder(this)
        if(!isRunning) {
            client = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            client.connect();
            sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
            sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
        }
        isRunning = true;
        return service;
    }

    /**
     * Function which is called by any activity which is bounded to this service
     * to retrieve values for latitude, longitude, azimuth and altitude.
     */
    public synchronized double[] getParameters(){
        double location[] = {lat, lng, azimuth, altitude};
        return location;
    }

    /**
     * Callback method invoked when connected to LocationServices API
     * Creates a Location Request to get location updates in an interval of 1000ms (1 sec).
     */
    LocationRequest lr;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // request location updates continuously in an interval of 1 sec
        LocationServices.FusedLocationApi.requestLocationUpdates(client, lr, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Callback method invoked continuously in an interval of 1 sec.
     * Params: Location object
     * Retrieves latitude, longitude and altitude details
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location == null) {
            //Toast.makeText(this, "Cant get current location!", Toast.LENGTH_LONG).show();
        }
        else {
            this.location = location;
            lng = location.getLongitude();
            lat = location.getLatitude();
            altitude = location.getAltitude();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to LocationServices failed!!", Toast.LENGTH_LONG).show();
    }

    /**
     * Callback method invoked which retrieves data from Accelerometer and Magnetometer sensors
     * in an interval of 100 ms.
     * Params: SensorEvent object
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Toast.makeText(this, "onSensorChanged", Toast.LENGTH_SHORT).show();
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accel = sensorEvent.values.clone();
                break;
        }

        if (mags != null && accel != null) {
            Rot = new float[9];
            I = new float[9];
            SensorManager.getRotationMatrix(Rot, I, accel, mags);
            // Correct if screen is in Landscape

            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);

            //Sensor values displayed at a sample of 1000 msec
            if ((System.currentTimeMillis() - lastSaved) > SENSOR_INTERVAL) {
                lastSaved = System.currentTimeMillis();
                azimuth = values[0] * 57.2957795f; //looks like we don't need this one
                //azm.setText((int) azimuth);
                //azm.setText("Azimuth: " + azimuth);
                //Log.d("response:", "Azimuth: "+ azimuth);
                mags = null; //retrigger the loop when things are repopulated
                accel = null; ////retrigger the loop when things are repopulated
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Callback method invoked when any activity
     * unbinds from the service.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        //Toast.makeText(this, "Unbinded!", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    public class TagBinder extends Binder{
        UserLocationService getService(){
            return UserLocationService.this;
        }
    }
}