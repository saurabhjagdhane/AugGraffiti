package com.example.saurabh.auggraffiti;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.animation.Transformation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.vision.text.Text;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                float[] distance = new float[1];
                Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);
                boolean check = distance[0]<circle.getRadius();
                if(check){
                    Toast.makeText(MapsActivity.this, "My Location!", Toast.LENGTH_SHORT).show();
                    Intent cameraIntent = new Intent(MapsActivity.this, CameraActivity.class);
                    startActivity(cameraIntent);
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(MapsActivity.this, "Inside Map ready!", Toast.LENGTH_SHORT).show();
                return true;
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
    Circle circle, overlay;
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



            if(circle != null)
                remove();

            CircleOptions copt = new CircleOptions()
                    .center(loc)
                    .radius(80)
                    .fillColor(Color.BLUE)
                    .strokeWidth(0);
            circle = mMap.addCircle(copt);
            if(overlay != null){
                overlay.remove();
            }
            overlay = mMap.addCircle(new CircleOptions()
                    .center(loc)
                    .radius(1000)
                    .fillColor(0x15FF0000)
                    .strokeWidth(3));

            /*
            MarkerOptions moptions = new MarkerOptions()
                    .title(locality)
                    .position(loc)
                    .snippet("current location!");
            marker = mMap.addMarker(moptions);
            */
            CameraUpdate c = CameraUpdateFactory.newLatLngZoom(loc, 15);
            mMap.animateCamera(c);
        }

    }


    public void remove(){
        circle.remove();
        circle = null;
        overlay.remove();
        overlay = null;
    }


}
