/*
    This is CameraActivity as a sample when user will try to place a tag at his current location.
    This may be combined with the CollectActvity. To be decided....
    Description to be updated by 5th October. Stay tuned...!!
 */

package com.example.saurabh.auggraffiti;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {

    private static Camera mCamera;
    private CameraPreview mPreview;
    private Button capture;
    private static String emailID;
    public static final int MY_CAMERA_REQUEST_CODE = 1015;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Bundle extraData = getIntent().getExtras();
        emailID = extraData.getString("EmailID");

        capture = (Button)findViewById(R.id.gallery_button);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CameraActivity.this,DrawingScreen.class );
                startActivity(i);
            }
        });

        // Create an instance of Camera
        mCamera = getCameraInstance();
        if(mCamera != null){
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Camera c = null;
        Toast.makeText(this, "onRequestPermissionsResult, requestCode: "+requestCode, Toast.LENGTH_SHORT).show();
        if(requestCode == MY_CAMERA_REQUEST_CODE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                c = Camera.open();
                mCamera = c;
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
            }
        }
    }

    public Camera getCameraInstance(){
        Camera c = null;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
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

            }else {
                c = Camera.open(); // attempt to get a Camera instance
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onStop() {
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