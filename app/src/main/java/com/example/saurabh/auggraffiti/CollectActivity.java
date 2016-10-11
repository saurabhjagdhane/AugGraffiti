/*
    This is the sample collect activity which would be explained in upcoming phase.
    This may be changed to placeTag or placing and collecting can be combined within an activity.
    Design decision pending...
    Description to be updated by 5th October. Stay tuned...!!
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
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.google.android.gms.vision.text.Text;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CollectActivity extends AppCompatActivity {
    private Tag tag;
    private static Camera mCamera;
    private CameraPreview mPreview;
    public static final int MY_CAMERA_REQUEST_CODE = 1015;
    private static ImageLoader imageLoader;
    private FrameLayout preview;
    private ImageView niv;
    private ImageView image;
    private Bitmap cameraBitmap;
    private byte bitmapBytes[];
    private Bitmap b;
    private Canvas c;
    private static boolean isRunning = false;
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
    private static boolean isBound = false;
    private ServiceConnection serviceConnection;


    public ServiceConnection getServiceConnection() {
        return serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                //Toast.makeText(CameraActivity.this, "Service Connected!", Toast.LENGTH_LONG).show();
                UserLocationService.TagBinder tagBinder = (UserLocationService.TagBinder) iBinder;
                myService = tagBinder.getService();
                isBound = true;
                startThread();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isBound = false;
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        tag = getIntent().getParcelableExtra("CollectTag");
        emailID = getIntent().getExtras().getString("EmailID");

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
        mCamera = getCameraInstance();
        Intent i = new Intent(this, UserLocationService.class);
        bindService(i, getServiceConnection(), Context.BIND_AUTO_CREATE);
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

        isRunning = true;
        Toast.makeText(CollectActivity.this, "Stay within 5m radius of this tag and rotate the device to collect tag: ", Toast.LENGTH_SHORT).show();
    }

    Handler handlerTextViews = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            azimuthView.setText(String.valueOf(azimuth));
            distanceView.setText(String.valueOf(distance[0]));
        }
    };

    public void startThread(){
        Thread t =new Thread(new Runnable() {
            @Override
           public void run() {
                while(isRunning) {
                    final double parameters[] = myService.getParameters();
                    azimuth = (float) parameters[2];
                    distance = new float[1];
                    Location.distanceBetween(parameters[0], parameters[1], tag.getLatitude(), tag.getLongitude(), distance);
                    handlerTextViews.sendEmptyMessage(0);
                    boolean check = distance[0] <= 5;
                    //check = true;
                    if (azimuth == tag.getAzimuth() && check) {
                        isRunning = false;
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlCollectTag, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(CollectActivity.this, "response: " + response, Toast.LENGTH_SHORT).show();
                                Log.d("response:", "CollectTagResponse:" + response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> param_map = new HashMap<String, String>();
                                param_map.put("email", emailID);
                                param_map.put("tag_id", tag.getTagID());
                                param_map.put("collect_img", base64EncodedImage);
                                return param_map;
                            }
                        };
                        // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
                        stringRequest.setTag(TAG);

                        // Adding the request to the RequestQueue
                        RequestQueueSingleton.getInstance(CollectActivity.this).addToRequestQueue(stringRequest);
                        Toast.makeText(CollectActivity.this, "Tag collected successfully!!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
        t.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        b = Bitmap.createBitmap(mPreview.getWidth(), mPreview.getHeight(), Bitmap.Config.ARGB_8888);
        c = new Canvas(b);

        bitmapBytes = mPreview.getCameraBitmap();

        Log.d("response:", "getCameraBitmap:" + bitmapBytes.length);
        image =(ImageView)findViewById(R.id.cameraImageView1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        cameraBitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length, options));
        Matrix matrix = new Matrix();

        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(cameraBitmap , 0, 0, cameraBitmap.getWidth(), cameraBitmap.getHeight(), matrix, true);
        image.setImageBitmap(rotatedBitmap);
        image.draw(c);
        image.setImageDrawable(null);

        Bitmap b1 = Bitmap.createBitmap(niv.getWidth(), niv.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c1 = new Canvas(b1);
        niv.draw(c1);
        Matrix m = new Matrix();

        m.postTranslate(b1.getWidth() / 4, b1.getHeight() / 3); // Centers imag
        c.drawBitmap(b1, m, null);

        ByteArrayOutputStream stream =  new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 30, stream);

        File f = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "collectImage.jpg");
        base64EncodedImage = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(f));
            outputStream.write(stream.toByteArray());
            Toast.makeText(CollectActivity.this, f.getAbsolutePath() + ", size: " + f.getTotalSpace(), Toast.LENGTH_SHORT).show();
            if (outputStream != null) {
                outputStream.close();
            }
       }catch(Exception e){
            e.printStackTrace();
        }
        return super.onTouchEvent(event);
    }


    public void renderTagOnCameraView(){
        imageLoader = RequestQueueSingleton.getInstance(this).getImageLoader();
        //NetworkImageView niv = new NetworkImageView(this);
//            niv.getLayoutParams().height = 50;
        //          niv.getLayoutParams().width = 50;
        niv = (ImageView)findViewById(R.id.collect_tagView);
        imageLoader.get("http://www.freedigitalphotos.net/images/img/homepage/87357.jpg",ImageLoader.getImageListener(niv, R.mipmap.default_image_gallery, R.mipmap.error_image_gallery));
        /*
        Log.d("response:", "Image width & height:" + niv.getWidth()+ " ," + niv.getHeight());
        Matrix m = new Matrix();
        niv.setScaleType(ImageView.ScaleType.MATRIX);
        m.postRotate(45f, niv.getWidth()/2, niv.getHeight()/2);
        niv.setImageMatrix(m);
        Log.d("response:", "Image width & height:" + niv.getWidth()+ " ," + niv.getHeight());
        */
        //preview.addView(niv);
    }


    public Camera getCameraInstance() {
        Camera c = null;
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_CONTACTS},
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
        isRunning = false;
        if(isBound){
            unbindService(serviceConnection);
            isBound = false;
            serviceConnection = null;
        }
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
}
