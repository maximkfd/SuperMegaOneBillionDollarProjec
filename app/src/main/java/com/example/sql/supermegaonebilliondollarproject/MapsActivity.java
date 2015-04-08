package com.example.sql.supermegaonebilliondollarproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.example.sql.database.Base;
import com.example.sql.parser.JSONParser;

import com.example.sql.supermegaonebilliondollarproject.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;
    final String TAG = "myLogs";
    public Context context = this;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MARKS = "marks";
    private static final String TAG_AUTHOR = "authorName";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_REWARD = "reward";
    private static final String TAG_SHORT_DESCRIPTION = "short_description";
    private static final String TAG_FULL_DESCRIPTION = "full_description";


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

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getApplicationContext(), NewMarkActivity.class);
                intent.putExtra("markerID", marker.getId());
                startActivity(intent);
            }
        });

        try {
            Base sqh = new Base(getApplicationContext());
            SQLiteDatabase mDBRead = sqh.getReadableDatabase();
            SQLiteDatabase mDBWrite= sqh.getWritableDatabase();

            new ReqireMarks().execute();

            Cursor cursor = mDBRead.query(Base.TABLE_NAME,
                    new String[]{
                            Base._ID, Base.FULL_DESCRIPTION, Base.SHORT_DESCRIPTION,
                            Base.REWARD, Base.LATITUDE, Base.LONGITUDE, Base.AUTHOR_NAME
                    },
                    null,
                    null,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()){
                LatLng latLng = new LatLng(
                        cursor.getDouble(cursor.getColumnIndex(Base.LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(Base.LONGITUDE)));
                String sd = cursor.getString(cursor.getColumnIndex(TAG_SHORT_DESCRIPTION));
//                String author = cursor.getString(cursor.getColumnIndex(TAG_AUTHOR));
                map.addMarker(new MarkerOptions().position(latLng).title(sd));

            }
        } catch (Exception e){
            e.printStackTrace();
        }
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

    public void OnMarkerClick(Marker marker){

    }

    class ReqireMarks extends AsyncTask<String, String, String> {

        private ProgressDialog pDialog;

        JSONParser jsonParser = new JSONParser();


        private String url_get_marks = "http://" + JSONParser.IP + "/db_get_marks.php";

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
                    JSONArray marks = json.getJSONArray(TAG_MARKS);
                    for (int i = 0; i < marks.length(); i++){
                        JSONObject m = marks.getJSONObject(i);

                        String author = m.getString(TAG_AUTHOR);
                        String short_description = m.getString(TAG_SHORT_DESCRIPTION);
                        String full_description = m.getString(TAG_FULL_DESCRIPTION);
                        Integer reward = m.getInt(TAG_REWARD);
                        Double latitude = m.getDouble(TAG_LATITUDE);
                        Double longitude = m.getDouble(TAG_LONGITUDE);
                        try {
                            Base sqh = new Base(getApplicationContext());
                            SQLiteDatabase mDBWrite = sqh.getWritableDatabase();

                            ContentValues cv = new ContentValues();
                            cv.put(Base.LATITUDE, latitude);
                            cv.put(Base.LONGITUDE, longitude);
                            cv.put(Base.AUTHOR_NAME, author);
                            cv.put(Base.SHORT_DESCRIPTION, short_description);
                            cv.put(Base.FULL_DESCRIPTION, full_description);
                            cv.put(Base.REWARD, reward);

                            mDBWrite.insert(Base.TABLE_NAME, Base._ID, cv);

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
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