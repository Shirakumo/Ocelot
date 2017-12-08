package org.shirakumo.ocelot;

import android.content.Intent;
import android.app.PendingIntent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import org.shirakumo.lichat.Client;
import org.shirakumo.lichat.Handler;

import java.io.IOException;

public class Service extends android.app.Service {

    public final Client client = new Client();

    public Service() {
    }

    @Override
    public void onCreate(){
        Intent notificationIntent = new Intent(this, Chat.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            //.setSmallIcon(R.drawable.icon)
            .setContentIntent(pendingIntent)
            .build();

        startForeground(1, notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return android.app.Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        disconnect();
    }

    public void connect(){
        if(!client.isConnected()) {
            client.username = "Ocelot";
            client.hostname = "10.0.2.2";
            client.port = 1111;
            client.connect();
        }
    }

    public void disconnect(){
        if(client.isConnected()){
            client.disconnect();
        }
    }

    public void addHandler(Handler handler){
        client.addHandler(handler);
    }

    public void removeHandler(Handler handler){
        client.removeHandler(handler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }
}
