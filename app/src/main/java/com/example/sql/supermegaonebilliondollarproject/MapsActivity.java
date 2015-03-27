package com.example.sql.supermegaonebilliondollarproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.example.sql.parser.JSONParser;

import com.example.sql.supermegaonebilliondollarproject.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;
    final String TAG = "myLogs";
    public Context context = this;

    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }
        init();
    }

    private void init() {
        //Perm: 58, 56,3\

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick " + latLng.latitude + ", " + latLng.longitude);
            }

        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d(TAG, "onMapLongClick: " + latLng.latitude + "," + latLng.longitude);
                map.addMarker(new MarkerOptions().position(latLng).title("SomeShit"));
                Intent intent = new Intent(getApplicationContext(), NewMarkActivity.class);
                intent.putExtra("longitude", latLng.longitude);
                intent.putExtra("latitude", latLng.latitude);
                startActivity(intent);
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition camera) {
                Log.d(TAG, "onCameraChange: " + camera.target.latitude + "," + camera.target.longitude);
            }
        });
    }


    public void onClickTest(View view) {
        //Perm: 58.0222 56.308; 58.004, 56.301

//        HttpClient client = new DefaultHttpClient();
//        HttpGet get = new HttpGet("127.0.0.1");

    }

    class ReqireMarks extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        JSONParser jsonParser = new JSONParser();


        private String url_get_marks = "http://" + JSONParser.IP + "/het_marks.php";

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Загрузка меток...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<>();
            JSONObject json = jsonParser.makeHttpRequest(url_get_marks, "POST", params);

            Log.d("Create Response", json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // продукт удачно создан
                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
//                    startActivity(i);

                    // закрываем это окно
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }

    }
}