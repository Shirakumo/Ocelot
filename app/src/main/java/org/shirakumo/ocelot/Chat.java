package org.shirakumo.ocelot;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;

public class Chat extends Activity implements Output.OnFragmentInteractionListener{

    private Intent serviceIntent;
    private UpdateHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serviceIntent = new Intent(this, Service.class);
        handler = new UpdateHandler(this);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(serviceIntent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        ClientBinder binder;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (ClientBinder)service;
            binder.client.addHandler(handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder.client.removeHandler(handler);
        }
    };
}
