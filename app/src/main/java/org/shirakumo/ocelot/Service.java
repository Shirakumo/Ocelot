package org.shirakumo.ocelot;

import android.content.Intent;
import android.app.PendingIntent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import org.shirakumo.lichat.Client;

import java.io.IOException;

public class Service extends android.app.Service {

    Client client = null;

    public Service() {
    }

    @Override
    public void onCreate(){
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (client == null) {
            client = new Client();
            client.username = "Ocelot";
            client.hostname = "chat.tymoon.eu";
            client.port = 1111;
            client.connect();

            // Show foreground
            Intent notificationIntent = new Intent(this, Service.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText(getText(R.string.notification_message))
                    //.setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
        }
        return android.app.Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        client.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ClientBinder(client);
    }
}
