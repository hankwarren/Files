package com.kgdsoftware.files;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ListRemoteFiles extends AppCompatActivity {
    private static final String TAG = "FS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_remote_files);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String fileAddress = preferences.getString("filesurl", "http://192.168.1.106:8080/files");
        EditText urlText = (EditText) findViewById(R.id.url_text);
        urlText.setText(fileAddress);

        urlText.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    EditText editText = (EditText) v;
                    Toast.makeText(v.getContext(), editText.getText(), Toast.LENGTH_SHORT).show();

                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("filesurl", editText.getText().toString());
                    editor.commit();

                    // hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ListView listView = (ListView) findViewById(R.id.file_list);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = (String)parent.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), ViewRemoteFile.class);
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                String filesurl = preferences.getString("filesurl", "http://192.168.1.106:8080/files");
                intent.putExtra("filesurl", filesurl);
                intent.putExtra("filename", fileName);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "You selected " + fileName,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void clickGetList(View view) {
        EditText editText = (EditText)findViewById(R.id.url_text);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("filesurl", editText.getText().toString());
        editor.commit();

        // hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        new Worker().execute(editText.getText().toString());
    }

    public class Worker extends AsyncTask<String, Integer, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                Log.v(TAG, "get: " + params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                Log.v(TAG, "got: " + stringBuilder.toString());

                return(new JSONArray(stringBuilder.toString()));

            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONArray result) {
            ListView listView = (ListView)findViewById(R.id.file_list);
            JSONArrayAdapter adapter = new JSONArrayAdapter(result);
            listView.setAdapter(adapter);
        }
    }

    public class JSONArrayAdapter extends BaseAdapter {
        private JSONArray jsonArray;
        public JSONArrayAdapter(JSONArray array) {
            jsonArray = array;
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public String getItem(int position) {
            try {
                return jsonArray.getString(position);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(ListRemoteFiles.this);
                v = vi.inflate(R.layout.remote_file_row, null);
            }

            String p = getItem(position);

            if (p != null) {
                TextView fileName = (TextView) v.findViewById(R.id.file_name);

                if (fileName != null) {
                    fileName.setText(p);
                }
            }
            return v;
        }

    }
}
