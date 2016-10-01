/*
This is basic CameraPreview class and and not an activity.
 A camera preview class is a SurfaceView that can display the live image data coming from a camera,
 so users can frame and capture a picture or video.

This will be a part of the CameraActivity which expects an update in upcoming phase.
Description to be updated by 5th October. Stay tuned...!!
 */
package com.example.saurabh.auggraffiti;

/**
 * Created by Saurabh on 05-09-2016.
 */

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    //private static final String[] MY_CAMERA_REQUEST_CODE = {"34"};
    private SurfaceHolder mHolder;
    private static Camera mCamera = null;
    public String TAG = "Debugging";
    private static Context ctx;
    private static boolean isOpened = false;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        ctx = context;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        // The Surface has been created, acquire the camera and tell it where to draw.
        try
        {
            /*
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);


        if(mCamera == null){
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            isOpened =true;
        } */
        Camera.Parameters params = mCamera.getParameters();


        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
        }


            mCamera.setPreviewDisplay(holder);
        }
        catch (IOException exception)
        {
            mCamera.release();
            mCamera = null;
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        //if(isOpened) {
        //    mCamera.release();
        //    mCamera = null;
        //}
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // make any resize, rotate or reformatting changes here
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {

            mCamera.setDisplayOrientation(90);

        } else {

            mCamera.setDisplayOrientation(0);

        }
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
