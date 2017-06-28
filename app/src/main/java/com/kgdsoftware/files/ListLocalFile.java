package com.kgdsoftware.files;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class ListLocalFile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView)findViewById(R.id.file_list);
        File[] fileList = getFilesDir().listFiles();

        FileArrayAdapter fileArrayAdapter = new FileArrayAdapter(this, R.layout.file_list_row, fileList);
        listView.setAdapter(fileArrayAdapter);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File)parent.getItemAtPosition(position);

                Intent intent = new Intent(view.getContext(), ViewLocalFile.class);
                intent.putExtra("filename", file.getName());
                startActivity(intent);
//                Toast.makeText(getApplicationContext(), "You selected " + file.getName(),
//                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class FileArrayAdapter extends ArrayAdapter<File> {
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

    }
}
