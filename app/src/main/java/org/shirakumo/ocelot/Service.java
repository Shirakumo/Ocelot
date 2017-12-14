package org.shirakumo.ocelot;

import android.app.NotificationChannel;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import org.shirakumo.lichat.Base64;
import org.shirakumo.lichat.CL;
import org.shirakumo.lichat.Client;
import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;

public class Service extends android.app.Service {
    public static final String NOTIFICATION_CHANNEL = "ocelot-service-channel";
    public static final int SERVICE_NOTIFICATION = 1;
    public static final int UPDATE_NOTIFICATION = 2;
    public static final int ACTION_DISMISS_NOTIFICATION = 1;
    public static final int ACTION_ACCEPT_NOTIFICATION = 2;

    public final Client client = new Client();
    public int reconnectTimeout = 30;
    public int reconnectCounter = 0;
    private int notificationCounter = 0;
    private Chat chat;

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
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, NotificationManager.IMPORTANCE_LOW));
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_ocelot)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        startForeground(SERVICE_NOTIFICATION, builder.build());
        Log.d("ocelot.service", "Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getIntExtra("action", -1)) {
            case ACTION_DISMISS_NOTIFICATION:
                notificationCounter = 0;
                break;
            case ACTION_ACCEPT_NOTIFICATION:
                notificationCounter = 0;
                if(chat != null){
                    chat.showChannel(intent.getStringExtra("channel"));
                }else{
                    Intent showChat = new Intent(this, Chat.class);
                    showChat.putExtra("channel", intent.getStringExtra("channel"));
                    startActivity(showChat);
                }
                break;
        }
        return android.app.Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        disconnect();
    }

    public void showUpdateNotification(String channel, String from, String content){
        notificationCounter++;

        Intent deleteIntent = new Intent(this, Service.class);
        deleteIntent.putExtra("action", ACTION_DISMISS_NOTIFICATION);
        PendingIntent pendingDelete = PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent acceptIntent = new Intent(this, Service.class);
        acceptIntent.putExtra("action", ACTION_ACCEPT_NOTIFICATION);
        acceptIntent.putExtra("channel", channel);
        PendingIntent pendingAccept = PendingIntent.getService(this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL);
        }

        builder.setContentTitle(from+" says:")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_ocelot)
                .setNumber(notificationCounter)
                .setDeleteIntent(pendingDelete)
                .setContentIntent(pendingAccept)
                .setAutoCancel(true);

        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(UPDATE_NOTIFICATION, builder.build());
    }

    public void connect(){
        if(!client.isConnected()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            client.username = prefs.getString("username", "Ocelot");
            client.password = prefs.getString("password", "");
            if(client.password.isEmpty()) client.password = null;
            client.hostname = prefs.getString("hostname", "chat.tymoon.eu");
            client.port = Integer.parseInt(prefs.getString("port", "1111"));
            client.connect();
            Log.d("ocelot.service", "Connecting to "+client.username+"/"+client.password+"@"+client.hostname+":"+client.port);
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
        Log.d("ocelot.service", "Sending file "+file+"...");
        try {
            ContentResolver cr = getContentResolver();
            Payload payload = new Payload(Toolkit.getUriFileName(cr, file), cr.getType(file), cr.openInputStream(file));
            client.s("DATA",
                    "channel", channel,
                    "content-type", payload.contentType,
                    "filename", payload.name,
                    "payload", new String(Base64.encode(payload.data)));
            Log.d("ocelot.service", "File queued for sending.");
        }catch (Exception ex){
            Log.e("ocelot.service", "Failed to read file for sending "+file, ex);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    class Binder extends android.os.Binder{
        public Binder(){}

        public Service bind(Chat chat){
            Service.this.chat = chat;
            return Service.this;
        }

        public Service unbind(){
            Service.this.chat = null;
            return null;
        }
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

        public void handle(Message update){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Service.this);
            if(!update.from.equals(client.username)
                    && (chat == null || !update.channel.equals(chat.getChannel().getName()))
                    && (prefs.getBoolean("notifications", false)
                     && prefs.getBoolean("notify_message", false)
                     || (prefs.getBoolean("notify_mention", false)
                      && Toolkit.mentionsUser(update.text, client.username)))){
                showUpdateNotification(update.channel, update.from, update.text);
            }
        }

        public void handle(Connect update){
            reconnectCounter = 0;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Service.this);
            for(String channel : prefs.getStringSet("channels", new HashSet<>())){
                client.s("JOIN", "channel", channel);
            }
        }

        public void handle(ConnectionLost update){
            Log.i("ocelot.service", "Lost connection", update.exception);
            int timeout = reconnectCounter*reconnectTimeout;
            reconnectCounter++;
            Log.i("ocelot.service", "Reconnecting in "+timeout);
            CL.sleep(timeout);
            if(0 < reconnectCounter) {
                Log.i("ocelot.service", "Attempting reconnect...");
                try{client.connect();}catch(Exception ex){}
            }
        }
    }
}
