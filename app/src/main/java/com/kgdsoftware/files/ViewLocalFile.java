package com.kgdsoftware.files;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.zip.ZipInputStream;

public class ViewLocalFile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_local_file);

        Intent intent = getIntent();
        Toast.makeText(getApplicationContext(), "You selected " + intent.getStringExtra("filename"),
                Toast.LENGTH_SHORT).show();
        String filename = intent.getStringExtra("filename");
        EditText console = (EditText) findViewById(R.id.console);

        BufferedReader br = null;

        try {
            if (filename.endsWith("luas")) {
                copyPrivateKey();
                File encryptedFile = new File(getFilesDir(), filename);
                ZipInputStream zip = new ZipInputStream(new FileInputStream(encryptedFile));

                File privateKeyFile = new File(getFilesDir(), "private.der");
                Decryptor decryptor = new Decryptor();
                decryptor.loadKey(zip, privateKeyFile);
                filename = decryptor.decrypt(zip, getFilesDir());
            }

            File file = new File(getFilesDir(), filename);
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            Log.v("AA", sb.toString());
            console.setText(sb.toString());
            hideKeyboard(console);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void copyPrivateKey() {
        try {
            AssetManager assetManager = getAssets();
            InputStream in = assetManager.open("private.der");
            File outputFile = new File(getFilesDir(), "private.der");
            OutputStream out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
