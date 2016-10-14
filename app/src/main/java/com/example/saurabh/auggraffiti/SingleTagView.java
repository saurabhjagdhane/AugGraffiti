/**
 * SingleTagView representing the single tag view image whenever the user
 * clicks on any of the tag images on the GridView of gallery.
 */

package com.example.saurabh.auggraffiti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.android.volley.toolbox.ImageLoader;


public class SingleTagView extends AppCompatActivity {
    //private NetworkImageView networkImageView;
    private static ImageLoader imageLoader;
    private int position;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tag_view);

        Bundle extraData = getIntent().getExtras();
        position = extraData.getInt("position");
        imageLoader = RequestQueueSingleton.getInstance(this).getImageLoader();
        imageView = (ImageView)findViewById(R.id.tagView);
        imageLoader.get(GalleryViewAdapter.imagesList[position],ImageLoader.getImageListener(imageView, R.mipmap.default_image_gallery, R.mipmap.error_image_gallery));
       // networkImageView.setImageUrl(GalleryViewAdapter.imagesList[position], imageLoader);
    }
}
