/**
 * This CameraPreview class is being used while collection and placing of a tag to display live camera frames.
 * This CameraPreview is user-defined class to handle frames in Camera preview.
 * CameraPreview class implements SurfaceHolder.Callback so that it can display the live image data from a camera,
 * Users can frame and capture a picture or video.
 */
package com.example.saurabh.auggraffiti;

/**
 * Created by Saurabh on 05-09-2016.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@SuppressWarnings("deprecation")
// CameraPreview class extending SurfaceView implementing interface SurfaceHolder.Callback to get the information about changes to surface.
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static Camera myCamera = null;
    private SurfaceHolder surfaceHolder;

    private static Context contextObject;
    private byte bytes[];

    //Contructor for CameraPreview using context and camera
    public CameraPreview(Context context, Camera camera) {
        super(context);
        contextObject = context;
        myCamera = camera;

        //getHolder() provides access and control over SurfaceView's underlying surface.
        surfaceHolder = getHolder();

        //Adding a callback interface and
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    //Returns byte array of CameraFrame (base64EncodedString)
    public byte[] getCameraBitmap(){
        return this.bytes;
    }

    //Once the surface is created this method is called. After creation of surface CameraView is handled and placed.
    public void surfaceCreated(SurfaceHolder holder) {
        try
        {
            Camera.Parameters params = myCamera.getParameters();

            //Referring to Google Android Documentation
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
            {
                params.set("orientation", "portrait");
                myCamera.setDisplayOrientation(90);
            }

            myCamera.setPreviewDisplay(holder);
        }
        catch (IOException exception)
        {
            //Resets View for next capture
            myCamera.stopPreview();
            myCamera.setPreviewCallback(null);

            //Releasing camera object resource
            myCamera.release();
            myCamera = null;
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //If CameraPreview is rotated or changed then those conditions are taken care in surfaceChanged()
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        //Check if preview surface exists
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            myCamera.stopPreview();
        } catch (Exception e) {

        }
        try {
            //Resizing and rotating changes done here
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                //Setting camera display orientation as per condition
                myCamera.setDisplayOrientation(90);
            } else {
                myCamera.setDisplayOrientation(0);
            }

            // Every time settings are changed start preview again with newer settings
            myCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    YuvImage image = new YuvImage(bytes, ImageFormat.NV21,
                            size.width, size.height, null);
                    // Rectangle with co-ordinates
                    Rect rect = new Rect();
                    rect.bottom = size.height;
                    rect.top = 0;
                    rect.left = 0;
                    rect.right = size.width;
                    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                    // Compresees rectangle region in YUV image to JPEG
                    // out2 is outputStream used for writing compressed data
                    image.compressToJpeg(rect, 10, out2);
                    CameraPreview.this.bytes = out2.toByteArray();
                }
            });
            myCamera.setPreviewDisplay(surfaceHolder);
            myCamera.startPreview();

        } catch (Exception e) {

        }
    }
}