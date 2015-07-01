package com.example.sql.supermegaonebilliondollarproject;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.sql.database.MarksBase;
import com.example.sql.parser.JSONParser;

import com.example.sql.preferences.PreferencesMain;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActionBarActivity {

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
    Marker tmpMarker;
    Marker[] markers = new Marker[1000];
    int countMarkers = 0;
    MarksBase sqh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActionBar actionBar = getActionBar();
        try {
            actionBar.show();
            actionBar.setTitle(R.string.action_bar_title);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        JSONParser.IP = sh.getString("ip", "192.168.43.185");


        sqh = new MarksBase(getApplicationContext());
        sqh.onUpgrade(sqh.getWritableDatabase(), 1, 1);

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
                Intent intent = new Intent(getApplicationContext(), MarkViewActivity.class);
                intent.putExtra("markerID", marker.getId());
                intent.putExtra("latLng", marker.getPosition());
                SQLiteDatabase DBRead = sqh.getReadableDatabase();
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), PreferencesMain.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_refresh){
            update();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void update(){
        try {
            new RequireMarks().execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init() {
        //Perm: 58, 56,3\
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(58.0222, 56.308), 10));
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
                Intent intent = new Intent(getApplicationContext(), NewMarkActivity.class);
                intent.putExtra("longitude", latLng.longitude);
                intent.putExtra("latitude", latLng.latitude);

                startActivity(intent);
                Cursor cursor = (new MarksBase(getApplicationContext())).getWritableDatabase().query(
                        MarksBase.TABLE_NAME,
                        new String[]{
                                MarksBase._ID, MarksBase.FULL_DESCRIPTION, MarksBase.SHORT_DESCRIPTION,
                                MarksBase.REWARD, MarksBase.LATITUDE, MarksBase.LONGITUDE, MarksBase.AUTHOR_NAME
                        },
                        null,
                        null,
                        null,
                        null,
                        null
                );
                cursor.moveToLast();
                String sd = cursor.getString(cursor.getColumnIndex(MarksBase.SHORT_DESCRIPTION));

                Marker marker = map.addMarker(new MarkerOptions().position(latLng).title("Click me"));
                markers[countMarkers++] = marker;
                marker.setTitle(sd);
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition camera) {
                Log.d(TAG, "onCameraChange: " + camera.target.latitude + "," + camera.target.longitude);
            }
        });
    }

    class RequireMarks extends AsyncTask<String, String, String> {

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
            //С сервера придёт только те, что в нужном городе. => нужно отправить город.
            List<NameValuePair> params = new ArrayList<>();
            JSONObject json = jsonParser.makeHttpRequest(url_get_marks, "POST", params);

            Log.d("Create Response", json.toString());
            try {
                countMarkers = 0;
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    JSONArray marks = json.getJSONArray(TAG_MARKS);
                    MarksBase sqh = new MarksBase(getApplicationContext());
                    for (int i = 0; i < marks.length(); i++){
                        JSONObject m = marks.getJSONObject(i);

                        String author = m.getString(TAG_AUTHOR);
                        String short_description = m.getString(TAG_SHORT_DESCRIPTION);
                        String full_description = m.getString(TAG_FULL_DESCRIPTION);
                        Integer reward = m.getInt(TAG_REWARD);
                        Double latitude = m.getDouble(TAG_LATITUDE);
                        Double longitude = m.getDouble(TAG_LONGITUDE);
                        try {
                            //TODO: check for memory leaks on multiple base creation
                            //MarksBase sqh = new MarksBase(getApplicationContext());
                            SQLiteDatabase mDBWrite = sqh.getWritableDatabase();

                            ContentValues cv = new ContentValues();
                            cv.put(MarksBase.LATITUDE, latitude);
                            cv.put(MarksBase.LONGITUDE, longitude);
                            cv.put(MarksBase.AUTHOR_NAME, author);
                            cv.put(MarksBase.SHORT_DESCRIPTION, short_description);
                            cv.put(MarksBase.FULL_DESCRIPTION, full_description);
                            cv.put(MarksBase.REWARD, reward);
                            try {
                                markers[countMarkers++].remove();
                            } catch (Exception e){
                                Log.e("Cannot delete marker", countMarkers + "");
                            }
                            mDBWrite.insert(MarksBase.TABLE_NAME, MarksBase._ID, cv);

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

            try {
                countMarkers = 0;
                SQLiteDatabase mDBRead = sqh.getReadableDatabase();
                SQLiteDatabase mDBWrite = sqh.getWritableDatabase();
                Cursor cursor = mDBRead.query(MarksBase.TABLE_NAME,
                        new String[]{
                                MarksBase._ID, MarksBase.FULL_DESCRIPTION, MarksBase.SHORT_DESCRIPTION,
                                MarksBase.REWARD, MarksBase.LATITUDE, MarksBase.LONGITUDE, MarksBase.AUTHOR_NAME
                        },
                        null,
                        null,
                        null,
                        null,
                        null
                );
                while (cursor.moveToNext()) {
                    LatLng latLng = new LatLng(
                            cursor.getDouble(cursor.getColumnIndex(MarksBase.LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(MarksBase.LONGITUDE)));
                    String sd = cursor.getString(cursor.getColumnIndex(TAG_SHORT_DESCRIPTION));
//                String author = cursor.getString(cursor.getColumnIndex(TAG_AUTHOR));
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(sd));
                    markers[countMarkers++] = marker;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
