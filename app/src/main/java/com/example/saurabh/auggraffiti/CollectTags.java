package com.example.saurabh.auggraffiti;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class CollectTags extends Service {

    private final IBinder collectTag = new TagBinder();
    private final static String url = "http://roblkw.com/msa/neartags.php";
    private String postResponse;
    private static RequestQueue rq;
    public CollectTags() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        rq = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        return collectTag;
    }

    public void getNearbyTags(final String emailID, final double Latitude, final double Longitude){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                 postResponse = response;
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
                param_map.put("loc_long", String.valueOf(Longitude));
                param_map.put("loc_long", String.valueOf(Latitude));
                return param_map;
            }
        };
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public String getPostResponse(){
        return "tag1, 33.423935, -111.927484, tag2, 33.420713, -111.922923, tag3, 33.420561, -111.920069";
        //return postResponse;
    }

    public class TagBinder extends Binder{
        CollectTags getService(){
            return CollectTags.this;
        }
    }
}
