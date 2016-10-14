/**
 * GalleryViewAdapter used by GalleryScreen Activity to load the collected tags in its grid view layout.
 * getView function overridden and implemented.
 */

package com.example.saurabh.auggraffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavilion on 27-09-2016.
 */
public class GalleryViewAdapter extends BaseAdapter{
    private Context context;
    private int resourecId;
    public static String[] imagesList;
    private static ImageLoader imageLoader;

    public GalleryViewAdapter(Context context, int resource, String[] urls) {
        //super(context, resource, objects);
        this.context = context;
        this.resourecId = resource;
        this.imagesList = urls;
        imageLoader = RequestQueueSingleton.getInstance(context).getImageLoader();
        //Toast.makeText(context, imagesList[1], Toast.LENGTH_SHORT).show();
    }

    /**
     * getView function is invoked by the adapter only if no of images in the list is non-zero.
     */
    @Override
    public int getCount() {
        return imagesList.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    /**
     * Creates ImageViews dynamically, loaded using ImageLoader and returned.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Toast.makeText(context, "getview, position: "+position, Toast.LENGTH_SHORT).show();
        ImageView niv = null;
        if(convertView == null){
            niv = new ImageView(context);
            niv.setLayoutParams(new GridView.LayoutParams(300, 300));
            niv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            niv.setPadding(8, 8, 8, 8);
        }else{
            niv = (ImageView) convertView;
        }
        //niv.setImageUrl(imagesList[position], imageLoader);
        imageLoader.get(GalleryViewAdapter.imagesList[position],ImageLoader.getImageListener(niv, R.mipmap.default_image_gallery, R.mipmap.error_image_gallery));
        return niv;
    }
}
