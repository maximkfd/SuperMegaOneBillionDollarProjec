package com.example.sql.supermegaonebilliondollarproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.sql.database.MarksBase;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public class MarkViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_view);
        TextView Name = (TextView) findViewById(R.id.Name);
        TextView mFD = (TextView) findViewById(R.id.inputFullDescription);
        TextView Num = (TextView) findViewById(R.id.inputReward);
        Intent i = getIntent();
        MarksBase sqh = new MarksBase(getApplicationContext());
        SQLiteDatabase DBRead = sqh.getReadableDatabase();
        String markerID = i.getStringExtra("markerID");
        LatLng position = i.getParcelableExtra("latLng");
        boolean b = true;
        Cursor cursor = DBRead.query(MarksBase.TABLE_NAME,
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
        while ((cursor.moveToNext())&&(b)){
            int j = cursor.getInt(cursor.getColumnIndex(MarksBase._ID));
            LatLng latLng = new LatLng(
                    cursor.getDouble(cursor.getColumnIndex(MarksBase.LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(MarksBase.LONGITUDE)));

            if ((Math.abs(position.latitude - latLng.latitude) < 0.00000001)&&(Math.abs(position.longitude - latLng.longitude) < 0.00000001)) {
                Name.setText(cursor.getString(cursor.getColumnIndex(MarksBase.AUTHOR_NAME)));
                mFD.setText(cursor.getString(cursor.getColumnIndex(MarksBase.FULL_DESCRIPTION)));
                Num.setText(cursor.getString(cursor.getColumnIndex(MarksBase.REWARD)));
                b = false;
            }
//                    String sd = cursor.getString(cursor.getColumnIndex(TAG_SHORT_DESCRIPTION));
//                String author = cursor.getString(cursor.getColumnIndex(TAG_AUTHOR));

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mark_view, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
