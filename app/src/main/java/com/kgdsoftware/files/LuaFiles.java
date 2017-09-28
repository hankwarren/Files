package com.kgdsoftware.files;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Spinner;

import java.io.File;


public class LuaFiles extends AppCompatActivity {
    private static final String TAG = "LF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lua_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner luaSpinner = (Spinner)findViewById(R.id.lua_spinner);
        File[] fileList = getFilesDir().listFiles();

        FileArrayAdapter adapter = new FileArrayAdapter(this, R.layout.file_list_row, fileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        luaSpinner.setAdapter(adapter);

    }

    public void startLuaScriptClick(View view) {
        Spinner spinner = (Spinner)findViewById(R.id.lua_spinner);
        File file = (File)spinner.getSelectedItem();
        String script = file.getName();

        Log.v(TAG, "startLuaScript: " + script);
        Intent intent = new Intent(this, LuaService.class);
        intent.putExtra("script", script);
        intent.putExtra("start", true);
        startService(intent);
    }

    public void stopLuaScriptClick(View view) {
        Intent intent = new Intent(this, LuaService.class);
        intent.putExtra("start", false);
        startService(intent);
    }

    public class FileArrayAdapter extends ArrayAdapter<File> implements SpinnerAdapter {
        public FileArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public FileArrayAdapter(Context context, int resource, File[] items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.file_list_row, null);
            }

            File p = getItem(position);

            if (p != null) {
                TextView fileName = (TextView) v.findViewById(R.id.file_name);
                TextView fileSize = (TextView) v.findViewById(R.id.file_size);

                if (fileName != null) {
                    fileName.setText(p.getName());
                }

                if (fileSize != null) {
                    fileSize.setText(String.valueOf(p.length()) + " bytes");
                }
            }
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView text = new TextView(getContext());
            text.setTextSize(30.0f);
            File p = getItem(position);
            text.setText(p.getName());
            return text;
        }
    }
}
