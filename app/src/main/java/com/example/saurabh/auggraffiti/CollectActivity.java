/**
 * CollectActivity is used for collecting tags placed in real world.
 * CollectActivity is allowed only when user is within 5 meter distance from a tag.
 * This activity also continuously monitors user's distance from the tag being collected.
 * And this keeps tracks of Azimuth angle of the device as well as mentions target Azimuth angle at which tag can be collected.
 * We're having +5 or -5 as a threshold of azimuth matching to collect tag. Enjoy Capturing....!!!
 */
package com.example.saurabh.auggraffiti;

import android.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
// Implementing SensorEventListener for Azimuth angle data
public class CollectActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SensorEventListener {
    private Tag tag;
    private static Camera mCamera;
    private CameraPreview mPreview;
    public static final int MY_CAMERA_REQUEST_CODE = 110394;
    private static ImageLoader imageLoader;
    private FrameLayout preview;
    private ImageView niv;
    private ImageView image;
    private Bitmap cameraBitmap;
    private byte bitmapBytes[];
    private Bitmap b;
    private Canvas c;
    private String emailID;
    private static final String TAG = "CollectActivity";
    private final static String urlCollectTag = "http://roblkw.com/msa/collecttag.php";
    private static RequestQueue rq;
    private float azimuth = 0f;
    private float[] distance;
    private String base64EncodedImage;

    private TextView azimuthView;
    private TextView distanceView;
    private TextView tagOrientationView;

    private UserLocationService myService;
    private OutputStream outputStream = null;
    SensorManager sManager;
    float gRot[]=null; //for gravity rotational data
    float mRot[]=null; //for magnetic rotational data
    float accels[]=new float[3];
    float mags[]=new float[3];
    float[] values = new float[3];
    static int MIN_TIME = 100; // 100ms
    long lastSaved = System.currentTimeMillis(); // This is used to set sampling time for sensor data

    private GoogleApiClient client;  // Google play services integration
    private Location location;
    private double lat = 0.0;
    private double lng = 0.0;
    private double altitude;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        tag = getIntent().getParcelableExtra("CollectTag");  // Get tag ID from Intent
        emailID = getIntent().getExtras().getString("EmailID");  // Get email-ID from Intent

        // GoogleApiClient connection
        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();

        // Sensor Manager
        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Registering Listener for Accelerometer data
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);

        // Registering Listener for Magnetometer data
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        azimuthView = (TextView)findViewById(R.id.text_azimuth);
        distanceView = (TextView)findViewById(R.id.text_distance);
        tagOrientationView = (TextView)findViewById(R.id.text_tag_azimuth);
        tagOrientationView.setText(String.valueOf(tag.getAzimuth()));

        // Get an instance of RequestQueue
        rq = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Create an instance of Camera
        flag = true;
        mCamera = getCameraInstance();
        renderTagOnCameraView();
        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);

            preview = (FrameLayout) findViewById(R.id.collect_layout);

            if(preview.getChildCount()>0){
                preview.removeAllViews();
            }
            preview.addView(mPreview);
            preview.addView(niv);

        }
        Toast.makeText(CollectActivity.this, "Stay within 5m radius of this tag and rotate the device to collect tag: ", Toast.LENGTH_SHORT).show();
    }


    // This method is the core of this activity as it evaluates azimuth angle and also Distance from tag.
    // flag variable is used to make sure that every tag is collected once in an activity cycle.
    public void evaluateOrientation(){
        boolean check = distance[0] <= 5;  // Check if distance is less than 5 meters
        //check = true;
        if (((azimuth >= tag.getAzimuth()-5) && (azimuth <= tag.getAzimuth()+5)) && check && flag) {
            getBase64EncodedString(); // get the tag
            StringRequest stringRequest = new StringRequest(Request.Method.POST, urlCollectTag, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response.equals("0"))
                        Toast.makeText(CollectActivity.this, "Tag collected successfully!!", Toast.LENGTH_SHORT).show();
                    Log.d("response:", "CollectTagResponse:" + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {  // Puts up the CapturedImage along with tag as a request in HashMap
                    Map<String, String> param_map = new HashMap<String, String>();
                    param_map.put("email", emailID);
                    param_map.put("tag_id", tag.getTagID());
                    param_map.put("collect_img", base64EncodedImage);
                    flag = false;
                    return param_map;
                }
            };
            // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
            stringRequest.setTag(TAG);

            // Adding the request to the RequestQueue
            RequestQueueSingleton.getInstance(CollectActivity.this).addToRequestQueue(stringRequest);
            //Toast.makeText(CollectActivity.this, "Tag collected successfully!!", Toast.LENGTH_SHORT).show();
            flag = false;
        }
    }


    // When image is to be collected first Byte array is decoded and then while sending a successfull capturing request to PHP server
    // it again needs to be converted back to the encoded format.
    public void getBase64EncodedString() {
        b = Bitmap.createBitmap(mPreview.getWidth(), mPreview.getHeight(), Bitmap.Config.ARGB_8888);
        c = new Canvas(b);

        bitmapBytes = mPreview.getCameraBitmap();
        if(bitmapBytes != null) {
            Log.d("response:", "getCameraBitmap:" + bitmapBytes.length);
            image = (ImageView) findViewById(R.id.cameraImageView1);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            cameraBitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options));
            Matrix matrix = new Matrix();

            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(), cameraBitmap.getHeight(), matrix, true);
            image.setImageBitmap(rotatedBitmap);
            //image.setScaleType(ImageView.ScaleType.FIT_XY);
            image.draw(c);
            image.setImageDrawable(null);

            Bitmap b1 = Bitmap.createBitmap(niv.getWidth(), niv.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c1 = new Canvas(b1);
            niv.draw(c1);
            Matrix m = new Matrix();
            //RectF drawableRect = new RectF(0, 0, b1.getWidth(), b1.getHeight());
            // RectF viewRect = new RectF(0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            //m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            m.postTranslate(b1.getWidth() - 50, b1.getHeight()); // to center the image
            c.drawBitmap(b1, m, null);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 30, stream);

            base64EncodedImage = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
        }
    }


    // Image Loader will load in the image from URL and rendder it on CameraView while collecting
    public void renderTagOnCameraView(){
        imageLoader = RequestQueueSingleton.getInstance(this).getImageLoader();

        niv = (ImageView)findViewById(R.id.collect_tagView);
        imageLoader.get(tag.getImageURL(),ImageLoader.getImageListener(niv, R.mipmap.default_image_gallery, R.mipmap.error_image_gallery));
    }


    public Camera getCameraInstance() {
        Camera c = null;

        // Permission for Camera
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {},MY_CAMERA_REQUEST_CODE);

            } else {
                c = Camera.open(); // attempt to get a Camera instance
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    // onStop() for activity stopping preview of a camera
    @Override
    protected void onStop() {
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if(rq != null){
            rq.cancelAll(TAG);
        }
        super.onStop();
    }

    LocationRequest lr;
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(1000); // Location updates for every 1000 ms
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // request location updates continuously in an interval of 1 sec
        LocationServices.FusedLocationApi.requestLocationUpdates(client, lr, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // After every location update (1000 msec) onLocationChanged is called.
    @Override
    public void onLocationChanged(Location location) {
        if(location == null) {
            //Toast.makeText(this, "Can't get current location!", Toast.LENGTH_LONG).show();
        }
        else {
            lng = location.getLongitude();
            lat = location.getLatitude();
            distance = new float[1];

            // Continuously monitoring the distance of the tag
            Location.distanceBetween(lat, lng, tag.getLatitude(), tag.getLongitude(), distance);
            distanceView.setText(String.valueOf(distance[0]));
            altitude = location.getAltitude();

            evaluateOrientation();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to LocationServices failed!!", Toast.LENGTH_LONG).show();
    }


    // This method is called when sensor values are changed
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
            gRot = new float[9];
            mRot = new float[9];

            // Computing inclination matrix mRot and rotation matrix gRot. Used for transformation into actual co-ordinate systems.
            SensorManager.getRotationMatrix(gRot, mRot, accels, mags);

            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(gRot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, values);

            //Sensor values displayed at a sample of 1000 msec
            if ((System.currentTimeMillis() - lastSaved) > MIN_TIME) {
                lastSaved = System.currentTimeMillis();
                azimuth = values[0] * 57.2957795f; //Range -180 to +180
                azimuthView.setText(String.valueOf(azimuth));
                //Log.d("response:", "Azimuth: "+ azimuth);
                mags = null; // Again start the loop
                accels = null;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // We didn't find any use but has to be overriden because of implementing SensorEventListener
    }
}