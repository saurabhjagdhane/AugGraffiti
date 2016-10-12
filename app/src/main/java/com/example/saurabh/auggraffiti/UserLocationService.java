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
    float accels[]=new float[3];
    float mags[]=new float[3];
    float[] values = new float[3];

    float azimuth;
    float pitch;
    float roll;
    double altitude;

    static int ACCE_FILTER_DATA_MIN_TIME = 100; // 1000ms
    long lastSaved = System.currentTimeMillis();

    public UserLocationService() {
    }

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

    public void setParameters(String emailID, Double Latitude, Double Longitude){
        this.emailID = emailID;
        this.lat = Latitude;
        this.lng = Longitude;
    }

    public synchronized double[] getParameters(){
        double location[] = {lat, lng, azimuth, altitude};
        return location;
    }

    public synchronized Location getLocation(){
        return location;
    }

    LocationRequest lr;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(800);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Toast.makeText(this, "onSensorChanged", Toast.LENGTH_SHORT).show();
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = sensorEvent.values.clone();
                break;
        }

        if (mags != null && accels != null) {
            Rot = new float[9];
            I = new float[9];
            SensorManager.getRotationMatrix(Rot, I, accels, mags);
            // Correct if screen is in Landscape

            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);

            //Sensor values displayed at a sample of 1000 msec
            if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME) {
                lastSaved = System.currentTimeMillis();
                azimuth = values[0] * 57.2957795f; //looks like we don't need this one
                //azm.setText((int) azimuth);
                //azm.setText("Azimuth: " + azimuth);
                //Log.d("response:", "Azimuth: "+ azimuth);
                pitch = values[1] * 57.2957795f;
                //Log.d("Sensor", "Pitch: "+ pitch);
                //ptch.setText("Pitch: " + pitch);
                roll = values[2] * 57.2957795f;
                //Log.d("Sensor", "Roll: "+ roll);
                //rol.setText("Roll: " + roll);
                mags = null; //retrigger the loop when things are repopulated
                accels = null; ////retrigger the loop when things are repopulated
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "Unbinded!", Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    public class TagBinder extends Binder{
        UserLocationService getService(){
            return UserLocationService.this;
        }
    }
}