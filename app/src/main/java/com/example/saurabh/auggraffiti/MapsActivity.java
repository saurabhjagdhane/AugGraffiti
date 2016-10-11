/**
 * MapsActivity deals with all things related to Map screen.
 * Buttons: Sign Out, Gallery
 * TextView: Score
 * Blue Circle: User location (onClick -> CameraActivity)
 * Green circle: Near-by Tags in 50m*50m (onClick -> CollectActivity)
 * Black Polylines: Lines connecting user and ner-by tags
 */

package com.example.saurabh.auggraffiti;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

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
    private final static String urlNearByTags = "http://roblkw.com/msa/neartags.php";
    private final static String urlGetScore = "http://roblkw.com/msa/getscore.php";
    private final static String urlFindTag = "http://roblkw.com/msa/findtag.php";
    private String postResponse;
    private String score;
    private static RequestQueue rq;

    private static Double lat;
    private static Double lng;
    private static volatile boolean isRunning = false;
    private Button galleryButton;

    private UserLocationService myService;
    private static volatile boolean isBound = false;
    private ServiceConnection serviceConnection;


    public ServiceConnection getServiceConnection(){
        return serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                //Toast.makeText(MapsActivity.this, "Service Connected!", Toast.LENGTH_LONG).show();
                UserLocationService.TagBinder tagBinder = (UserLocationService.TagBinder)iBinder;
                myService = tagBinder.getService();
                isBound = true;
                getScore();
                startDisplayTags();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isBound = false;
                myService = null;
                isRunning = false;
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        //if(!isBound) {
        Intent i = new Intent(this, UserLocationService.class);
        bindService(i, getServiceConnection(), Context.BIND_AUTO_CREATE);
        isRunning = true;
        Toast.makeText(this, "onResume!", Toast.LENGTH_SHORT).show();
        //}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(this, "onCreate!", Toast.LENGTH_SHORT).show();
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

        //Intent i = new Intent(this, UserLocationService.class);
        //bindService(i, getServiceConnection(), Context.BIND_AUTO_CREATE);

        galleryButton = (Button)findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MapsActivity.this, GalleryScreen.class);
                i.putExtra("EmailID", emailID);
                startActivity(i);
            }
        });


        final Button sign_out = (Button)findViewById(R.id.signout_button);
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                //finish();
            }
        });
        // [END customize_button]

    }


    /**
     * Handles signing out after presing sign out button on Maps Activity and takes the user back to sign-in page.
     */
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


    /**
     * Checks if google service is available. Returns true or false.
     */
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


    /**
     * Handles screen orientation change.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where the threads are started for getting score and placing the nerbyTags in 50m*50m on Map
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Toast.makeText(this, "Inside Map ready!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "onMapReady!", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        // Get an instance of RequestQueue
        rq = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        // On user location click, moves to CameraActivity where one can place their own tag.
        // On collect tags click, moves to CollectActivity where one can collect tags placed by other users of this app.
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if(circle.getFillColor() == Color.BLUE){
                    Intent i = new Intent(MapsActivity.this, CameraActivity.class);
                    i.putExtra("EmailID", emailID);
                    startActivity(i);
                    //finish();
                }else{
                    float[] distance = new float[1];
                    double cLat = circle.getCenter().latitude;
                    double cLong = circle.getCenter().longitude;
                    Location.distanceBetween(lat, lng, cLat, cLong, distance);
                    boolean check = distance[0] <= 5;

                    if(true){
                        Toast.makeText(MapsActivity.this, "Distance:"+distance[0]+", Within 5m!", Toast.LENGTH_SHORT).show();
                        int j = 0;
                        Tag t = null;
                        while(j < tagList.size()){
                            if(tagList.get(j).getLatitude() == cLat && tagList.get(j).getLongitude() == cLong){
                                t = tagList.get(j);
                                break;
                            }
                            j++;
                        }
                        getTagDetails(t);
                        //finish();
                    }else{
                        Toast.makeText(MapsActivity.this, "Distance:"+distance[0]+", Not within 5m to collect!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        /*
        // enabling user location tracking
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
        */
        try {
            //wait(1000);
            // initiating the threads which continuously update the score and place nearbyTags in 50m*50m
            //isRunning = true;
            //getScore();
            //startDisplayTags();
        }catch(Exception e){

        }
    }


    public void getTagDetails(Tag t){
        final Tag tag = t;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlFindTag, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response != null) {
                    String parameters[] = response.split(",");
                    tag.setImageURL(parameters[0]);
                    tag.setAzimuth(Float.parseFloat(parameters[1]));
                    tag.setAltitude(Float.parseFloat(parameters[2]));
                    Intent i = new Intent(MapsActivity.this, CollectActivity.class);
                    i.putExtra("CollectTag", tag);
                    i.putExtra("EmailID", emailID);
                    startActivity(i);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param_map = new HashMap<String, String>();
                param_map.put("tag_id", tag.getTagID());
                return param_map;
            }
        };
        // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
        stringRequest.setTag(TAG);

        // Adding the request to the RequestQueue
        RequestQueueSingleton.getInstance(MapsActivity.this).addToRequestQueue(stringRequest);
    }

    /**
     * Handler for tracking current user location.
     */
    Handler handlerCurrentLocation = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            setUserLocation(myService.getLocation());
            //addLines(latitude, longitude, lat, lng);
        }
    };

    /**
     * Handler for placing tags in 50m*50m.
     */
    Handler handlerPlace = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            addOverlays();
            //addLines(latitude, longitude, lat, lng);
        }
    };


    /**
     * Handler for removing duplicate tags in 50m*50m.
     */
    Handler handlerRemove = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            removeTags();
        }
    };


    Double latitude;
    Double longitude;
    /**
     * Function which starts a thread which places nearByTags in 50m*50m continuously.
     */
    public void startDisplayTags() {
        Toast.makeText(MapsActivity.this, "Tags Thread!, isRunning = "+isRunning, Toast.LENGTH_SHORT).show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (this) {
                        try {
                            wait(1200);
                            //getNearbyTags("ssshett3@asu.edu", lat, lng);
                            handlerCurrentLocation.sendEmptyMessage(0);
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
    /**
     * Handler which updates the textview for score on Map Activity.
     */
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


    /**
     * Function which starts a thread which updates the score continuously.
     * Sends a post request to getscore.php with field: email id & its value passed in the body of the HTTP Request.
     */
    public void getScore(){
        Toast.makeText(MapsActivity.this, "Score Thread!, isRunning = "+isRunning, Toast.LENGTH_SHORT).show();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    synchronized (this) {
                        try {
                            // Runs the loop continuously in an interval of 1 sec.
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
                                    param_map.put("email", emailID);
                                    return param_map;
                                }
                            };
                            // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
                            stringRequest.setTag(TAG);

                            // Adding the request to the RequestQueue
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


    /**
     * Called by thread instantiated in startDisplayTags() function.
     * Sends a post request to nearTags.php with fields: email id, longitude and latitude & its values passed in the body of the HTTP Request.
     */
    public void getNearbyTags(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlNearByTags, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //postResponse = "tag1, 33.423935, -111.927484, tag2, 33.420713, -111.922923, tag3, 33.420561, -111.920069";
                postResponse = response;
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








    Marker marker;
    Circle circle_p;
    Circle circle_c;
    Polygon overlay;
    Polyline line;
    /**
     * Called once in every interval of 1 second.
     */

    public void setUserLocation(Location location){
        if(location == null) {
            //Toast.makeText(this, "Cant get current location!", Toast.LENGTH_SHORT).show();
        }
        else{
            lng = location.getLongitude();
            lat = location.getLatitude();
            //lat = 33.419351;
            //lng = -111.938083;
            LatLng loc = new LatLng(lat, lng);

            // Adding place tag and removing previously placed marker to avoid overlappig and duplication.
            if(circle_p != null)
                remove();
            // User's location marked as a clickable circle on Map with radius 5m and color BLUE.
            CircleOptions copt = new CircleOptions()
                    .center(loc)
                    .radius(5)
                    .fillColor(Color.BLUE)
                    .clickable(true)
                    .strokeWidth(0);
            circle_p = mMap.addCircle(copt);

            // Sets the amount of zoom on the Map screen
            CameraUpdate c = CameraUpdateFactory.newLatLngZoom(loc, 19);
            mMap.animateCamera(c);
        }

    }


    /**
     * Adds overlays (circles) representing nearByTags in 50m*50m of radius 5m and color GREEN which are clickable.
     * Called by Handler handlerPlace invoked by the thread as the thread should not update the UI by itself.
     */
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
                    tagList.add(t);
                    LatLng loc = new LatLng(latitude, longitude);
                    CircleOptions copt = new CircleOptions()
                            .center(loc)
                            .radius(5)
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


    /**
     * Adds overlays (lines) representing lines between nearByTags in 50m*50m and user's circle of color BLACK.
     * Called by addOverlays() function after it has placed the circles representing nearByTags.
     */
    public void addLines(Double cLatitude, Double cLongitude, Double pLatitude, Double pLongitude){
        LatLng loc_c = new LatLng(cLatitude, cLongitude);
        LatLng loc_p = new LatLng(pLatitude, pLongitude);
        line = mMap.addPolyline(new PolylineOptions()
                .add(loc_p)
                .add(loc_c)
                .width(3)
                .color(Color.BLACK));
        polylines.add(line);
    }


    /**
     * Removes circle representing user to avoid duplicate overlays.
     * Called by onLocationChanged() function.
     */
    public void remove(){
        circle_p.remove();
        circle_p = null;
    }


    /**
     * Removes circle representing nearByTags to avoid duplicate overlays.
     * Called by Handler handlerRemove invoked by the thread as the thread should not update the UI by itself.
     */
    public void removeTags(){
        int i = 0;
        while(i < circles.size()){
            circles.get(i).remove();
            polylines.get(i).remove();
            tagList.clear();
            i++;
        }
        circle_c = null;
        line = null;
    }


    /**
     * Disabling back button feature such that user can go back to sign-in page only by clicking on 'Sign Out' button.
     * User can press home button if he/she wants to temporarily stop the app.
     */
    @Override
    public void onBackPressed() {
        // Do nothing...can go back to sign-in page only using sign out button
    }


    /**
     * Called when activity is finished or when home button pressed.
     * Cancels all pending requests in the request queue placed by the application to getScore and place nearByTags in 50m*50m
     */
    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        if(isBound){
            unbindService(serviceConnection);
            isBound = false;
            serviceConnection = null;
        }
        if(rq != null){
            rq.cancelAll(TAG);
        }
        Toast.makeText(this, "onStop!", Toast.LENGTH_SHORT).show();
    }




    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
