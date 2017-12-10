package org.shirakumo.ocelot;

import android.app.NotificationChannel;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;

import org.shirakumo.lichat.Base64;
import org.shirakumo.lichat.Client;
import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Service extends android.app.Service {
    public static final String NOTIFICATION_CHANNEL = "ocelot-service-channel";

    public final Client client = new Client();

    public Service() {
    }

    @Override
    public void onCreate(){
        // Register self
        client.addHandler(new UpdateHandler());

        // Load in emotes
        for(File f : getEmotePaths()){
            try {
                client.emotes.put(Toolkit.getFileName(f), new Payload(
                        Toolkit.getFileName(f),
                        Toolkit.mimeForFileExtension(Toolkit.getFileType(f)),
                        new FileInputStream(f)));
            }catch(Exception ex){
                Log.w("ocelot.service", "Failed to load emote "+f, ex);
                f.delete();
            }
        }

        // Create sticky notification
        Intent notificationIntent = new Intent(this, Chat.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_LOW));
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        startForeground(1, builder.build());
        Log.d("ocelot.service", "Created");
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
            Log.d("ocelot.service", "Started connection.");
        }
    }

    public void disconnect(){
        if(client.isConnected()){
            client.disconnect();
            Log.d("ocelot.service", "Closed connection.");
        }
    }

    public void addHandler(Handler handler){
        client.addHandler(handler);
    }

    public void removeHandler(Handler handler){
        client.removeHandler(handler);
    }

    public File[] getEmotePaths(){
        File[] files = new File(getFilesDir(), "emotes/").listFiles();
        return (files == null)? new File[0] : files;
    }

    public File getEmotePath(String name){
        Payload payload = client.emotes.get(name);
        if(payload != null){
            return new File(new File(getFilesDir(), "emotes/"),
                    payload.name+"."+Toolkit.fileExtensionForMime(payload.contentType));
        }
        return null;
    }

    public void sendFile(String channel, Uri file){
        try {
            ContentResolver cr = getContentResolver();
            Payload payload = new Payload(Toolkit.getUriFileName(cr, file), cr.getType(file), cr.openInputStream(file));
            client.s("DATA",
                    "channel", channel,
                    "content-type", payload.contentType,
                    "filename", payload.name,
                    "payload", new String(Base64.encode(payload.data)));
        }catch (Exception ex){
            Log.e("ocelot.service", "Failed to read file for sending "+file, ex);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }

    private class UpdateHandler extends HandlerAdapter{

        public void handle(Emote emote){
            Payload payload = client.emotes.get(emote.name);
            if(payload != null){
                File dest = getEmotePath(emote.name);
                try {
                    dest.getParentFile().mkdirs();
                    payload.save(dest.getAbsolutePath());
                    Log.d("ocelot.service", "Saved emote to "+dest);
                }catch(Exception ex){
                    Log.e("ocelot.service", "Failed to save emote to "+dest, ex);
                }
            }
        }

        public void handle(ConnectionLost update){
            Log.i("ocelot.service", "Lost connection", update.exception);
            // FIXME: handle reconnect
        }
    }
}
