package com.example.saurabh.auggraffiti;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GalleryScreen extends AppCompatActivity {
    private GridView gridView;
    private GalleryViewAdapter adapter;
    private final static String urlGallery = "http://roblkw.com/msa//getgallery.php";
    private final static String TAG = "gallery";
    private String emailID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_screen);

        Bundle extraData = getIntent().getExtras();
        emailID = extraData.getString("EmailID");

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>=0) {
                    Intent intent = new Intent(GalleryScreen.this, SingleTagView.class);
                    intent.putExtra("position", i);
                    startActivity(intent);
                }
            }
        });
        getImages();
    }


    public void getImages(){
        //ArrayList images = new ArrayList();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlGallery, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //if(response!=null) {
                    //String[] imageUrls = response.split(",");
                //Toast.makeText(GalleryScreen.this, "getgallery.php response: "+response, Toast.LENGTH_SHORT).show();
                    String[] imageUrls = {"http://www.freedigitalphotos.net/images/img/homepage/87357.jpg",
                    "http://www.planwallpaper.com/static/images/Winter-Tiger-Wild-Cat-Images.jpg",
                    "https://cdn.spacetelescope.org/archives/images/large/heic1509a.jpg"};
                    adapter = new GalleryViewAdapter(GalleryScreen.this, R.layout.grid_view_item, imageUrls);
                    gridView.setAdapter(adapter);
                //}
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
                return param_map;
            }
        };
        // Setting all the string requests sent with a tag so that they can be tracked and removed in onStop() method (claen-up).
        stringRequest.setTag(TAG);

        // Adding the request to the RequestQueue
        RequestQueueSingleton.getInstance(GalleryScreen.this).addToRequestQueue(stringRequest);
        //return images;
    }
}
