package com.kgdsoftware.files;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilesService extends Service {
    public static final String CONTENT_ACTION = "com.kgdsoftware.files.CONTENT";

    private static final String TAG = "FS";
    private NotificationManager notificationManager;
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        displayNotificationMessage("Files Service running");

        if(executor == null) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            Log.v(TAG, "FilesService: executor already exists. This should not be.");
        }
    }

    private void displayNotificationMessage(String message) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(message)
                .setSmallIcon(R.drawable.emo_im_winking)
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;

        notificationManager.notify(0, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String url = intent.getStringExtra("url");

        Log.v(TAG, "onStartCommand: " + url);

        executor.execute(new Worker(url));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        if(executor != null) executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // local service
    }

    public class Worker implements Runnable {
        private String urlString;

        public Worker(String url) {
            urlString = url;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);

                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                //We create an array of bytes
                byte[] data = new byte[1024];
                int current = 0;

                while((current = bis.read(data,0,data.length)) != -1) {
                    buffer.write(data, 0, current);
                }

                String filename = urlString.substring(urlString.lastIndexOf("/")+1);

                File file = new File(getFilesDir(), filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer.toByteArray());
                fos.close();

                Intent intent = new Intent();
                intent.setAction(FilesService.CONTENT_ACTION);
                intent.putExtra("content", filename + " added");
                sendBroadcast(intent);
            } catch(IOException e) {
                e.printStackTrace();
            }

            stopSelf(); // done
        }
    }
}
