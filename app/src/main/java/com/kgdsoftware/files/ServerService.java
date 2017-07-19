package com.kgdsoftware.files;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;


public class ServerService extends Service {
    private static final String TAG = "SS";

    private NotificationManager notificationManager;
    private int port;
    private ExecutorService executor;
    private WebServer server;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        displayNotificationMessage("Server Service running");

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            Log.v(TAG, "ServerService: executor already exists. This should not be.");
        }
    }

    private void displayNotificationMessage(String message) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(message)
                .setSmallIcon(R.drawable.emo_im_winking)
                .setContentIntent(contentIntent)
                .build();

        notificationManager.notify(0, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        port = intent.getIntExtra("port", 8080);
        Log.v(TAG, "ServerService onStartCommand: " + port);

        executor.execute(new Worker());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        if (server != null) server.stop();
        executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // local service
    }

    public class Worker implements Runnable {
        @Override
        public void run() {
            server = new WebServer();
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class WebServer extends NanoHTTPD {

        public WebServer() {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Map<String, List<String>> decodedQueryParameters =
                    decodeParameters(session.getQueryParameterString());
            Map<String, String> files = new HashMap<>();

            StringBuilder sb = new StringBuilder();

            if (session.getMethod() == Method.POST) {
                try {
                    session.parseBody(files);

                    // I don't like the pattern matching stuff.
                    // The files sent have gotten their names changed such that
                    //  the name is <file name>.lua<some number>. The <some number> part is
                    //  wrong and annoying. I don't know a better way to fix this than
                    //  to remove the extra number. The result is a bunch of .lua files.
                    String pattern = "(^.+?)(\\d+$)";    // match number at end of string

                    Set<String> keys = files.keySet();
                    for (String key : keys) {
                        String filename = key;
                        String location = files.get(key);

                        Pattern r = Pattern.compile(pattern);
                        Matcher m = r.matcher(filename);
                        if (m.find()) {
                            Log.v(TAG, "Found value: " + m.group(1));
                            filename = m.group(1);
                        } else {
                            Log.v(TAG, "NO MATCH");
                        }

                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            File tempfile = new File(location);
                            File outFile = new File(getFilesDir(), filename);
                            in = new FileInputStream(tempfile);
                            out = new FileOutputStream(outFile);
                            copyFile(in, out);
                            in.close();
                            in = null;
                            out.flush();
                            out.close();
                            out = null;

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    sb.append("POST ok I am");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    sb.append("POST not so ok I am");

                } catch (ResponseException e1) {
                    e1.printStackTrace();
                    sb.append("POST not so ok I am (2)");
                }

            } else {

                sb.append("GET lost");
            }

            return NanoHTTPD.newFixedLengthResponse(sb.toString());
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
