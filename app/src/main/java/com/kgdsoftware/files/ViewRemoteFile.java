package com.kgdsoftware.files;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ViewRemoteFile extends AppCompatActivity {
    private static final String TAG = "VR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_remote_file);

        Intent intent = getIntent();
        Toast.makeText(getApplicationContext(), "You selected " + intent.getStringExtra("filename"),
                Toast.LENGTH_SHORT).show();
        String fileName = intent.getStringExtra("filename");
        String filesurl = intent.getStringExtra("filesurl");
        filesurl = filesurl.replace("files", "static/" + fileName);
        new Worker().execute(filesurl);
    }


    public class Worker extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                Log.v(TAG, "get: " + params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                reader.close();
                Log.v(TAG, "got: " + stringBuilder.toString());

                return(stringBuilder.toString());

            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onPostExecute(String result) {
            EditText content = (EditText)findViewById(R.id.content);
            Log.v(TAG, "result: " + result);
            content.setText(result);

            // hide the keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
        }
    }

}
