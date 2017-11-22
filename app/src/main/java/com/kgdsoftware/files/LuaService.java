package com.kgdsoftware.files;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.jse.Helper;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LuaService extends Service implements ResourceFinder {
    public static String TAG = "LS";
    private ExecutorService executor;
    private Worker worker;
    private boolean running;

    public LuaService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
            running = false;
        } else {
            Log.v(TAG, "ServerService: executor already exists. This should not be.");
        }
        worker = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v(TAG, "LuaService onStartCommand:");

        boolean start = intent.getBooleanExtra("start", false);
        if (start) {
            String script = intent.getStringExtra("script");
            worker = new Worker(script);
            executor.execute(worker);
            running = true;
        } else {
            if (worker != null) {
                worker.stop();
            }
            running = false;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    public class Worker implements Runnable {
        private LuaValue stop;
        private LuaValue main;
        private String script;

        Worker(String script) {
            this.script = script;
        }

        @Override
        public void run() {
            try {
                Globals globals = JsePlatform.standardGlobals();
                LuaBridge bridge = new LuaBridge();
                globals.finder = LuaService.this;

                File file = new File(getFilesDir(), script);
                Log.v(TAG, "script file: " + script);
                Log.v(TAG, "script file: " + file.getName() + ": " + file.getCanonicalFile());
                if (file.exists()) {
                    Log.v(TAG, script + " exists +++++++++++++++++++++++++");

                    LuaValue luaScript = globals.loadfile(script);
                    Log.v(TAG, "script: " + luaScript);
                    luaScript.call();

                    // now get the Main entry point and call it.
                    main = globals.get("Main");
                    stop = globals.get("stop");

                    // If there is no stop, the script must not start.
                    if (stop.isnil()) throw new Exception(script + " has no stop.");

                    Log.v(TAG, "main: " + main + "  stop: " + stop);
                    // Is there anything else to do before starting? Do it here.
                    // Somewhere we need to get the event lists. I think there are 2 inputList and channelList.
                    // These (Lua) tables have the registered event handlers for 'input' and 'channel'


                    // Now start this mess...
                    main.call();
                    Log.v(TAG, "Worker Main returned");
                } else {
                    Log.v(TAG, script + " does not exist +++++++++++++++++++++++++++++++");
                }
                main = null;
                stop = null;
                globals = null;

                Log.v(TAG, "Worker thread is exiting properly");
            } catch (Exception e) {
                Log.v(TAG, "Script thread exiting because the script failed: " + e.getMessage());
            }
        }

        // This requires the main.lua script to have a 'stop' function.
        //
        //      function stop()
        //          runLumaScript = false
        //      end
        //
        // is the required implementation.
        public void stop() {
            try {
                stop.call();
            } catch (Exception e) {
                Log.v(TAG, "Call to stop failed: " + e.getMessage());
            }
        }
    }

    public static void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            InputStream in = assetManager.open("levitonAPI.lua", 0);
            File outFile = new File(context.getFilesDir(), "levitonAPI.lua");
            OutputStream out = new FileOutputStream(outFile);

            copyFile(in, out);

            in.close();
            out.close();
        } catch (IOException e) {
            Log.e("tag", "Asset copy failed", e);
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    @Override
    public InputStream findResource(String name) {
        try {
            Log.v(TAG, "findResource: " + getFilesDir() + "/" + name);
            File tmpFile = new File(getFilesDir(), name);
            InputStream tmpInputStream = new FileInputStream(tmpFile);
            Log.v(TAG, "after FileInputStream");
            return (tmpInputStream);

            //return getAssets().open(name);
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class LuaBridge {
        public Varargs getClass(String clazzName) {
            try {
                Class clazz = Class.forName(clazzName);
                return Helper.forClass(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
