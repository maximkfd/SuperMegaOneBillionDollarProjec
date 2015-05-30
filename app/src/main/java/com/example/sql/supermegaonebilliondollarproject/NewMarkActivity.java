package com.example.sql.supermegaonebilliondollarproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sql.database.MarksBase;
import com.example.sql.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewMarkActivity extends Activity {

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    EditText inputName;
    EditText inputShortDescription;
    EditText inputFullDescription;
    EditText inputReward;
    Intent intent;

    private static String url_create_product = "http://"+JSONParser.IP+"/db_create_mark.php";

    private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);
        intent = getIntent();
//        Intent intent = getIntent();
//        final double longitude = intent.getDoubleExtra("longitude", 0.00);
//        final double latitude  = intent.getDoubleExtra("latitude", 0.00);

        inputName = (EditText) findViewById(R.id.inputName);
        inputShortDescription = (EditText) findViewById(R.id.inputShortDescription);
        inputFullDescription = (EditText) findViewById(R.id.inputFullDescription);
        inputReward = (EditText) findViewById(R.id.inputReward);

        Button btnCreateProduct = (Button) findViewById(R.id.btnCreateProduct);

        btnCreateProduct.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new CreateNewProduct().execute();

                MarksBase sqh = new MarksBase(getApplicationContext());
                SQLiteDatabase mDBWrite = sqh.getWritableDatabase();

                ContentValues cv = new ContentValues();
                cv.put(MarksBase.LATITUDE, intent.getDoubleExtra("longitude", 0));
                cv.put(MarksBase.LONGITUDE, intent.getDoubleExtra("latitude", 0));
                cv.put(MarksBase.AUTHOR_NAME, inputName.getText().toString());
                cv.put(MarksBase.SHORT_DESCRIPTION, inputShortDescription.getText().toString());
                cv.put(MarksBase.FULL_DESCRIPTION, inputFullDescription.getText().toString());
                cv.put(MarksBase.REWARD, inputReward.getText().toString());
                mDBWrite.insert(MarksBase.TABLE_NAME, MarksBase._ID, cv);
            }
        });
    }

    /**
     * Фоновый Async Task создания нового продукта
     **/
    class CreateNewProduct extends AsyncTask<String, String, String> {

        /**
         * Перед согданием в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewMarkActivity.this);
            pDialog.setMessage("Создание продукта...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Создание продукта
         **/
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String shortDescription = inputShortDescription.getText().toString();
            String fullDescription = inputFullDescription.getText().toString();
            String reward = inputReward.getText().toString();
            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("author", name));
            params.add(new BasicNameValuePair("reward", reward));
            params.add(new BasicNameValuePair("short_description", shortDescription));
            params.add(new BasicNameValuePair("full_description", fullDescription));
            Intent intent = getIntent();
            Double longitude = intent.getDoubleExtra("longitude", 0.00);
            Double latitude  = intent.getDoubleExtra("latitude", 0.00);
            params.add(new BasicNameValuePair("longitude", longitude.toString()));
            params.add(new BasicNameValuePair("latitude", latitude.toString()));
//            params.add(new BasicNameValuePair("full_description", fullDescription));

            // получаем JSON объект
            JSONObject json = jsonParser.makeHttpRequest(url_create_product, "POST", params);

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

        /**
         * После оконачния скрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }

    }

}
