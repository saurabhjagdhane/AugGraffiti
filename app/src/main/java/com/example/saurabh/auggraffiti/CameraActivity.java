/*
    This is CameraActivity as a sample when user will try to place a tag at his current location.
    This may be combined with the CollectActvity. To be decided....
    Description to be updated by 5th October. Stay tuned...!!
 */

package com.example.saurabh.auggraffiti;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.saurabh.auggraffiti.UserLocationService.TagBinder;

public class CameraActivity extends AppCompatActivity {

    private static Camera mCamera;
    private CameraPreview mPreview;
    private Button capture;
    private static String emailID;
    public static final int MY_CAMERA_REQUEST_CODE = 1015;
    private static Bitmap bitmap;

    private DrawingScreen dv;
    private Paint mPaint;
    private OutputStream outputStream = null;

    private static final String urlPlaceTag = "http://roblkw.com/msa/placetag.php";
    private static final String TAG = "PlaceTag";

    private UserLocationService myService;
    private static boolean isBound = false;

    private double lat;
    private double lng;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Toast.makeText(CameraActivity.this, "Service Connected!", Toast.LENGTH_LONG).show();
            TagBinder tagBinder = (TagBinder)iBinder;
            myService = tagBinder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Drawing //

        //setContentView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        dv = new DrawingScreen(this, mPaint);
        ////////////////////////////////////

        Bundle extraData = getIntent().getExtras();
        emailID = extraData.getString("EmailID");


        Intent i = new Intent(this, UserLocationService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        capture = (Button) findViewById(R.id.gallery_button);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final double location[] = myService.getParameters();
                lat = location[0];
                lng = location[1];

                bitmap = dv.getmBitmap();
                ByteArrayOutputStream stream =  new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
                //final File f = new File(CameraActivity.this.getFilesDir(), "image.jpg");
                File f = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "tagImage.jpg");

                try {
                    final String base64EncodedImage = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    Log.d("response:", "Azimuth: "+location[2]+", Altitude: "+location[3]+", base64: "+base64EncodedImage);
                    outputStream = new BufferedOutputStream(new FileOutputStream(f));
                    outputStream.write(stream.toByteArray());
                    /*
                    outputStream = openFileOutput("image.jpg", Context.MODE_PRIVATE);
                    outputStream.write(stream.toByteArray());
                    */
                    Toast.makeText(CameraActivity.this,f.getAbsolutePath()+", size: "+f.getTotalSpace(),Toast.LENGTH_SHORT).show();
                    if(outputStream != null){
                        outputStream.close();
                    }
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, urlPlaceTag, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(CameraActivity.this, "response: "+response, Toast.LENGTH_SHORT).show();
                            Log.d("response:", "PlaceTagResponse:" + response);
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
                            param_map.put("tag_img", base64EncodedImage);
                            param_map.put("loc_long", String.valueOf(lng));
                            param_map.put("loc_lat", String.valueOf(lat));
                            param_map.put("orient_azimuth", String.valueOf(location[2]));
                            param_map.put("orient_altitude", String.valueOf(location[3]));
                            return param_map;
                        }
                    };
                    // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
                    stringRequest.setTag(TAG);

                    // Adding the request to the RequestQueue
                    RequestQueueSingleton.getInstance(CameraActivity.this).addToRequestQueue(stringRequest);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            if(preview.getChildCount()>0){
                preview.removeAllViews();
            }
            preview.addView(mPreview);
            preview.addView(dv);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Camera c = null;
        Toast.makeText(this, "onRequestPermissionsResult, requestCode: " + requestCode, Toast.LENGTH_SHORT).show();
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                c = Camera.open();
                mCamera = c;
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
            }
        }
    }

    public Camera getCameraInstance() {
        Camera c = null;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_CAMERA_REQUEST_CODE);

            } else {
                c = Camera.open(); // attempt to get a Camera instance
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onStop() {
        if(isBound){
            unbindService(serviceConnection);
            isBound = false;
            serviceConnection = null;
        }
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        super.onStop();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("EmailID", emailID);
        startActivity(i);
        mCamera.release();
        mCamera = null;
    }

}




