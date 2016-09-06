package com.example.saurabh.auggraffiti;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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

import com.example.saurabh.auggraffiti.CollectTags.TagBinder;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private CollectTags collectTags;
    private boolean isBoundToCollectTags = false;
    private List<Tag> tagList;
    private List<Circle> overlays;
    private List<Polyline> polylines;
    private GoogleApiClient client;
    private String emailID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

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
        Toast.makeText(this, "Inside Map ready!", Toast.LENGTH_SHORT).show();

        mMap = googleMap;
        /*
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        */

        // Bind to service that collects tags
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                TagBinder tagBinder = (TagBinder) iBinder;
                collectTags = tagBinder.getService();
                isBoundToCollectTags = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isBoundToCollectTags = false;
            }
        };

        Intent collect = new Intent(this, CollectTags.class);
        bindService(collect, serviceConnection, Context.BIND_AUTO_CREATE);

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if(circle.getFillColor() == Color.BLUE){
                    Intent i = new Intent(MapsActivity.this, CollectActivity.class);
                }else{

                }
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                float[] distance = new float[1];
                Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);
                boolean check = distance[0]<circle.getRadius();
                if(check){
                    Toast.makeText(MapsActivity.this, "My Location!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();


        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
        /*
        LatLng NEWARK = new LatLng(40.714086, -74.228697);
        CameraUpdate c = CameraUpdateFactory.newLatLngZoom(NEWARK, 15);
        mMap.moveCamera(c);
        */
        /*
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.image))
                .position(NEWARK, 8600f, 6500f);

// Add an overlay to the map, retaining a handle to the GroundOverlay object.
        GroundOverlay imageOverlay = mMap.addGroundOverlay(newarkMap);
        */
    }



    LocationRequest lr;

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "inside onConnected!", Toast.LENGTH_SHORT).show();
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
    Circle circle;
    Polygon overlay;
    Polyline line;

    @Override
    public void onLocationChanged(Location location){
        if(location == null)
            Toast.makeText(this, "Cant get current location!", Toast.LENGTH_LONG).show();
        else{
            Geocoder g = new Geocoder(this);

            double lng = location.getLongitude();
            double lat = location.getLatitude();
            LatLng loc = new LatLng(lat, lng);

            List<Address> l;
            Address a;
            String locality;
            try {
                l = g.getFromLocation(lat, lng, 1);
                a = l.get(0);
                locality = a.getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Adding place tag
            if(circle != null)
                remove();

            CircleOptions copt = new CircleOptions()
                    .center(loc)
                    .radius(50)
                    .fillColor(Color.BLUE)
                    .clickable(true)
                    .strokeWidth(0);
            circle = mMap.addCircle(copt);

            // Overlay 50m * 50m
            /*
            double latA = lat + (180/Math.PI)*((-25)/6378137);
            double lngA = lng + (180/Math.PI)*(25/6378137)/Math.cos(Math.PI/180.0 * lat);
            double latB = lat + (180/Math.PI)*((-25)/6378137);
            double lngB = lng + (180/Math.PI)*((-25)/6378137)/Math.cos(Math.PI/180.0 * lat);
            double latC = lat + (180/Math.PI)*(25/6378137);
            double lngC = lng + (180/Math.PI)*((-25)/6378137)/Math.cos(Math.PI/180.0 * lat);
            double latD = lat + (180/Math.PI)*(25/6378137);
            double lngD = lng + (180/Math.PI)*(25/6378137)/Math.cos(Math.PI/180.0 * lat);
            */
            overlay = mMap.addPolygon(new PolygonOptions()
                    .fillColor(0x15FF0000)
                    .strokeWidth(3)
                    .add(new LatLng(lat-0.0025, lng+0.0025))
                    .add(new LatLng(lat-0.0025, lng-0.0025))
                    .add(new LatLng(lat+0.0025, lng-0.0025))
                    .add(new LatLng(lat+0.0025, lng+0.0025)));


            // Adding collect tags
            collectTags.getNearbyTags("ssshett3@asu.edu", lat, lng);
            String response = collectTags.getPostResponse();
            if(response != null){
                String parameters[] = response.split(",");
                int i = 0;
                if(parameters.length/3 == 0){
                    while(i < parameters.length){
                        String id = parameters[i++];
                        double latitude = Double.parseDouble(parameters[i++]);
                        double longitude = Double.parseDouble(parameters[i++]);
                        Tag t = new Tag(id ,latitude, longitude);
                        tagList.add(t);
                        if(overlays.size() > 0) {
                            removeTags();
                        }
                        addOverlays(latitude, longitude);
                        addLines(latitude, longitude, lat, lng);
                    }
                }
            }

            CameraUpdate c = CameraUpdateFactory.newLatLngZoom(loc, 15);
            mMap.animateCamera(c);
        }

    }

    public void addOverlays(Double latitude, Double longitude){
        LatLng loc = new LatLng(latitude, longitude);
        CircleOptions copt = new CircleOptions()
                .center(loc)
                .radius(80)
                .fillColor(0xFFA500)
                .clickable(true)
                .strokeWidth(0);
        circle = mMap.addCircle(copt);
        overlays.add(circle);
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
        circle.remove();
        circle = null;
        overlay.remove();
        overlay = null;
    }

    public void removeTags(){
        int i = 0;
        while(i < overlays.size()){
            overlays.get(i).remove();
            polylines.get(i).remove();
            i++;
        }
        overlay = null;
        line = null;
    }


}
