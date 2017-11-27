package com.kgdsoftware.files;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoapService extends Service {
    CoapServer server;

    @Override
    public void onCreate() {
        server = new Server();

        server.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String button = intent.getStringExtra("sensor");
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


    public class Server extends CoapServer {
        public Server() {
            add(new ButtonResource());
        }

    }

    class ButtonResource extends CoapResource {

        public ButtonResource() {

            // set resource identifier
            super("hello");

            // set display name
            getAttributes().setTitle("Button Resource");
            setObserveType(CoAP.Type.CON);
            getAttributes().setObservable();
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
