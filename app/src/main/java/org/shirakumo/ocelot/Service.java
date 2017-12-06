package org.shirakumo.ocelot;

import android.content.Intent;
import android.os.IBinder;

public class Service extends android.app.Service {
    public Service() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return android.app.Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
