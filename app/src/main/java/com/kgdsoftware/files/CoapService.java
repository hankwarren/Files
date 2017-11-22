package com.kgdsoftware.files;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoapService extends Service {
    CoapServer server;

    @Override
    public void onCreate() {
        this.server = new CoapServer();
        server.add(new ButtonResource());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        server.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        server.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class ButtonResource extends CoapResource {

        public ButtonResource() {

            // set resource identifier
            super("hello");

            // set display name
            getAttributes().setTitle("Button Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            // respond to the request
            exchange.respond("Hello Android!");
        }

        @Override
        public void handlePUT(CoapExchange exchange) {

        }
    }

}
