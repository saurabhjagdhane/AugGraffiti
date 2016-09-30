package com.example.saurabh.auggraffiti;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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


public class UserLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    private static  final String TAG = "nearbyTags";
    private final IBinder service = new TagBinder();
    private final static String url = "http://roblkw.com/msa/neartags.php";
    private String postResponse;
    private static RequestQueue rq;
    private String emailID = "";
    private double lat = 0.0;
    private double lng = 0.0;
    private GoogleApiClient client;


    private static boolean isRunning = false;

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
        }
        isRunning = true;
        return service;
    }

    public void setParameters(String emailID, Double Latitude, Double Longitude){
        this.emailID = emailID;
        this.lat = Latitude;
        this.lng = Longitude;
    }

    public double[] getParameters(){
        double location[] = {lat, lng};
        return location;
    }

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

    @Override
    public void onLocationChanged(Location location) {
        if(location == null)
            Toast.makeText(this, "Cant get current location!", Toast.LENGTH_LONG).show();
        else {
            lng = location.getLongitude();
            lat = location.getLatitude();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to LocationServices failed!!", Toast.LENGTH_LONG).show();
    }


    public class TagBinder extends Binder{
        UserLocationService getService(){
            return UserLocationService.this;
        }
    }
}
