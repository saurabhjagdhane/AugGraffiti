package com.example.saurabh.auggraffiti;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.vision.text.Text;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static GoogleMap mMap = null;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    //private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private boolean isBoundToCollectTags = false;
    private List<Tag> tagList;
    private List<Circle> circles;
    private List<Polyline> polylines;
    private GoogleApiClient client;
    private static String emailID;
    private  ServiceConnection serviceConnection;
    private final static String urlNearByTags = "http://roblkw.com/msa/neartags.php";
    private final static String urlGetScore = "http://roblkw.com/msa/getscore.php";
    private String postResponse;
    private String score;
    private static RequestQueue rq;

    private static Double lat;
    private static Double lng;
    private static boolean isRunning = false;
    private static boolean isResumed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagList = new ArrayList<Tag>();
        circles = new ArrayList<Circle>();
        polylines = new ArrayList<Polyline>();

        Bundle extraData = getIntent().getExtras();
        emailID = extraData.getString("EmailID");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(googleServicesAvailable()) {
            //Toast.makeText(this, "Working!", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_maps);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        // client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //findViewById(R.id.sign_out_button).setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.

        final Button sign_out = (Button)findViewById(R.id.signout_button);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                finish();
            }
        });
        // [END customize_button]
        /*
        if(mMap != null){
            getScore();
            startDisplayTags();
        }
        */
    }

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i2 = new Intent(MapsActivity.this, MainActivity.class);
                        startActivity(i2);
                    }
                });
    }
    // [END signOut]

    public boolean googleServicesAvailable(){
        GoogleApiAvailability g = GoogleApiAvailability.getInstance();
        int available = g.isGooglePlayServicesAvailable(this);
        if(available == ConnectionResult.SUCCESS){
            return true;
        }else if(g.isUserResolvableError(available)){
            Dialog d = g.getErrorDialog(this, available, 0);
            d.show();
        }else{
            Toast.makeText(this, "Cant connect to play services!", Toast.LENGTH_LONG).show();
        }
        return  false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
       // Toast.makeText(this, "Inside Map ready!", Toast.LENGTH_SHORT).show();

        mMap = googleMap;
        Log.d(TAG, "handleSignInResult:" + emailID);
        rq = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();


        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if(circle.getFillColor() == Color.BLUE){
                    Intent i = new Intent(MapsActivity.this, CameraActivity.class);
                    i.putExtra("EmailID", emailID);
                    startActivity(i);
                    finish();
                }else{
                    float[] distance = new float[1];
                    Location.distanceBetween(lat, lng, circle.getCenter().latitude, circle.getCenter().longitude, distance);
                    boolean check = distance[0] <= 5;

                    if(check){
                        Toast.makeText(MapsActivity.this, "Distance:"+distance[0]+", Within 5m!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MapsActivity.this, CollectActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(MapsActivity.this, "Distance:"+distance[0]+", Not within 5m to collect!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();

        if(!isRunning){
            isRunning = true;
            getScore();
            startDisplayTags();
        }
    }




    Handler handlerPlace = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            addOverlays();
            //addLines(latitude, longitude, lat, lng);
        }
    };

    Handler handlerRemove = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            removeTags();
        }
    };


    Double latitude;
    Double longitude;
    public void startDisplayTags() {
        //Toast.makeText(MapsActivity.this, "Tags Thread!, isRunning = "+isRunning, Toast.LENGTH_SHORT).show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (this) {
                        try {
                            wait(1500);
                            //getNearbyTags("ssshett3@asu.edu", lat, lng);
                            getNearbyTags();
                            if(circles.size() > 0) {
                                handlerRemove.sendEmptyMessage(0);
                            }
                            handlerPlace.sendEmptyMessage(0);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    TextView scoreText;
    Handler handlerScore = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            scoreText = (TextView)findViewById(R.id.scoreNumber);
            if(score != null){
                if(score.matches("[0-9]+")) {
                    scoreText.setText(score);
                }
            }
        }
    };

    public void getScore(){
        //Toast.makeText(MapsActivity.this, "Score Thread!, isRunning = "+isRunning, Toast.LENGTH_SHORT).show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (this) {
                        try {
                            wait(1000);
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, urlGetScore, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    score = response;
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }){
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> param_map = new HashMap<String, String>();
                                    param_map.put("email", "ssshett3@asu.edu");
                                    return param_map;
                                }
                            };
                            stringRequest.setTag(TAG);
                            RequestQueueSingleton.getInstance(MapsActivity.this).addToRequestQueue(stringRequest);
                            handlerScore.sendEmptyMessage(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    public void getNearbyTags(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlNearByTags, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //postResponse = "tag1, 33.423935, -111.927484, tag2, 33.420713, -111.922923, tag3, 33.420561, -111.920069";
                postResponse = response;
                //Log.d(TAG, "response:" + postResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param_map = new HashMap<String, String>();
                param_map.put("email", emailID);
                param_map.put("loc_long", String.valueOf(lng));
                param_map.put("loc_lat", String.valueOf(lat));
                return param_map;
            }
        };
        stringRequest.setTag(TAG);
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }



    LocationRequest lr;

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "inside onConnected!", Toast.LENGTH_SHORT).show();
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
        LocationServices.FusedLocationApi.requestLocationUpdates(client, lr, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to google api failed!!", Toast.LENGTH_SHORT).show();
    }

    Marker marker;
    Circle circle_p;
    Circle circle_c;
    Polygon overlay;
    Polyline line;

    @Override
    public void onLocationChanged(Location location){
        if(location == null)
            Toast.makeText(this, "Cant get current location!", Toast.LENGTH_LONG).show();
        else{
            lng = location.getLongitude();
            lat = location.getLatitude();
            lat = 33.419351;
            lng = -111.938083;
            LatLng loc = new LatLng(lat, lng);
            //ong=-111.938083&loc_lat=33.419351

            List<Address> l;
            Address a;

            // Adding place tag
            if(circle_p != null)
                remove();

            CircleOptions copt = new CircleOptions()
                    .center(loc)
                    .radius(50)
                    .fillColor(Color.BLUE)
                    .clickable(true)
                    .strokeWidth(0);
            circle_p = mMap.addCircle(copt);

            // Overlay 50m * 50m
            /*
            overlay = mMap.addPolygon(new PolygonOptions()
                    .fillColor(0x15FF0000)
                    .strokeWidth(3)
                    .add(new LatLng(lat-0.0025, lng+0.0025))
                    .add(new LatLng(lat-0.0025, lng-0.0025))
                    .add(new LatLng(lat+0.0025, lng-0.0025))
                    .add(new LatLng(lat+0.0025, lng+0.0025)));
            */

            CameraUpdate c = CameraUpdateFactory.newLatLngZoom(loc, 15);
            mMap.animateCamera(c);
        }

    }

    public void addOverlays(){
        if(postResponse != null){
            String parameters[] = postResponse.split(",");
            int i = 0;
            if(parameters.length%3 == 0){
                while(i < parameters.length){
                    String id = parameters[i++];
                    longitude = Double.parseDouble(parameters[i++]);
                    latitude = Double.parseDouble(parameters[i++]);
                    Tag t = new Tag(id ,latitude, longitude);

                    //tagList.add(t);

                    LatLng loc = new LatLng(latitude, longitude);
                     CircleOptions copt = new CircleOptions()
                        .center(loc)
                        .radius(40)
                        .fillColor(Color.GREEN)
                        .clickable(true)
                        .strokeWidth(0);
                    circle_c = mMap.addCircle(copt);
                    circles.add(circle_c);
                    addLines(latitude, longitude, lat, lng);
                }
            }
        }
    }

    public void addLines(Double cLatitude, Double cLongitude, Double pLatitude, Double pLongitude){
        LatLng loc_c = new LatLng(cLatitude, cLongitude);
        LatLng loc_p = new LatLng(pLatitude, pLongitude);
        line = mMap.addPolyline(new PolylineOptions()
                .add(loc_p)
                .add(loc_c)
                .color(Color.BLACK));
        polylines.add(line);
    }

    public void remove(){
        circle_p.remove();
        circle_p = null;
        //overlay.remove();
        //overlay = null;
    }

    public void removeTags(){
        int i = 0;
        while(i < circles.size()){
            circles.get(i).remove();
            polylines.get(i).remove();
            i++;
        }
        circle_c = null;
        line = null;
    }

    @Override
    public void onBackPressed() {
        // Do nothing...can go back to sign-in page only using sign out button
        //super.onBackPressed();
        //finish();
        //return;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(MapsActivity.this, "On pause!"+isRunning, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(rq != null){
            rq.cancelAll(TAG);
        }
        /*
        if(!isResumed) {
            isRunning = false;
        }else{
            isResumed = false;
        }
        */
        //Toast.makeText(MapsActivity.this, "On stop!"+isRunning, Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Toast.makeText(MapsActivity.this, "On start!"+isRunning, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        //Toast.makeText(MapsActivity.this, "On resume!"+isRunning, Toast.LENGTH_SHORT).show();
    }

}