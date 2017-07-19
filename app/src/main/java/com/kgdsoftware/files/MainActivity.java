package com.kgdsoftware.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity implements ResourceFinder {
    private static final String TAG = "FS";

    private Globals globals;
    private LuaValue setLight;
    private LuaValue onOccSensorChange;

    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String fileAddress = preferences.getString("address", "http://192.168.1.106:8080/README.txt");
        EditText urlText = (EditText)findViewById(R.id.url_text);

        urlText.setText(fileAddress);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        receiver = new Receiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(FilesService.CONTENT_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        StringBuilder stringBuilder;

        switch (item.getItemId()) {
            case R.id.list_local_files:
                stringBuilder = new StringBuilder();
                for (File file : getFilesDir().listFiles()) {
                    stringBuilder.append(file.getName());
                    stringBuilder.append("\n");
                }
                EditText console = (EditText)findViewById(R.id.console);
                console.setText(stringBuilder.toString());
                return true;

            case R.id.list_remote_files:
                startActivity(new Intent(this, ListRemoteFiles.class));
                return true;

            case R.id.view_local_file:
                startActivity(new Intent(this, ListLocalFile.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getFileClick(View view) {
        Log.v(TAG, "getFileClick");

        EditText urlText = (EditText)findViewById(R.id.url_text);
        Intent intent = new Intent(this, FilesService.class);
        intent.putExtra("url", urlText.getText().toString());
        startService(intent);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("address", urlText.getText().toString());
        editor.commit();

        // hide the keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void exitClick(View view) {
        finish();
    }

    public void startServerClick(View view) {
        Log.v(TAG, "startServerClick");

        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra("port", 8080);
        startService(intent);
    }

    public void stopServerClick(View view) {
        Log.v(TAG, "stopServerClick");

        Intent intent = new Intent(this, ServerService.class);
        stopService(intent);
    }

    public void refreshClick(View view) {
        Log.v(TAG, "refreshClick");
        EditText console = (EditText)findViewById(R.id.console);
        console.setText("");

        File[] files = getFilesDir().listFiles();

        // Sort the array, just for grins...
        Arrays.sort(files, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        for (File file : files) {
            console.append(file.getName() + "\n");
        }
    }

    public void executeClick(View view) {
        Log.v(TAG, "executeClick");

        globals = JsePlatform.standardGlobals();
        globals.finder = this;

        LuaValue init = globals.loadfile("init.lua");
        init.call();

//        LuaValue fun1 = globals.get("fun1");
//        fun1.call(LuaValue.valueOf("This is a test\n"), LuaValue.valueOf(42));
//
        EditText console = (EditText)findViewById(R.id.console);

        LuaValue setConsole = globals.get("setConsole");
        setConsole.call(CoerceJavaToLua.coerce(console));

        onOccSensorChange = globals.get("onOccSensorChange");
        onOccSensorChange.call(LuaValue.valueOf(0));
        onOccSensorChange.call(LuaValue.valueOf(100));

        setLight = globals.get("setLight");
    }

    public void lightClick(View view) {
        Log.v(TAG, "lightClick");
        if (setLight != null) {
            EditText console = (EditText)findViewById(R.id.console);

            setLight.call(CoerceJavaToLua.coerce(console));
        }
    }

    // Implement a finder that loads from the assets directory.
    public InputStream findResource(String name) {
        try {
            Log.v(TAG, "findResource: " + name);
            return(new FileInputStream(new File(getFilesDir(), name)));

            //return getAssets().open(name);
        } catch (java.io.IOException ioe) {
            return null;
        }
    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "got a message from the service");

            EditText console = (EditText)findViewById(R.id.console);
            console.setText(intent.getStringExtra("content"));
        }
    }
}
