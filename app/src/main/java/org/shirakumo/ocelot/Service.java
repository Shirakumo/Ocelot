package org.shirakumo.ocelot;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import org.shirakumo.lichat.Base64;
import org.shirakumo.lichat.CL;
import org.shirakumo.lichat.Client;
import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Service extends android.app.Service implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String CHANNEL_GROUP = "ocelot-notifications";
    public static final String UPDATES_CHANNEL = "ocelot-notification-channel";
    public static final String SERVICE_CHANNEL = "ocelot-service-channel";
    public static final int SERVICE_NOTIFICATION = 1;
    public static final int UPDATE_NOTIFICATION = 2;
    public static final int ACTION_DISMISS_NOTIFICATION = 1;
    public static final int ACTION_ACCEPT_NOTIFICATION = 2;
    public static final int ACTION_START_SERVICE = 3;
    public static final int ACTION_STOP_SERVICE = 4;
    public static final int ACTION_CONNECT = 5;
    public static final int ACTION_DISCONNECT = 6;
    public static final int MAX_RECONNECT_ATTEMPTS = 10;
    // FIXME: If we could encode updates on the fly this would not be necessary.
    public static final int MAX_UPDATE_SIZE = 10 * 1024 * 1024;

    public final Client client = new Client();
    public int reconnectTimeout = 30;
    public int reconnectCounter = 0;
    private int notificationCounter = 0;
    private boolean foregrounded = false;
    private boolean connecting = false;
    private Chat chat;
    private android.os.Handler reconnecter;
    private final List<Update> updates = new ArrayList<>();

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            NotificationChannelGroup group = new NotificationChannelGroup(CHANNEL_GROUP, getString(R.string.notification_group));
            manager.createNotificationChannelGroup(group);
            {
                NotificationChannel service = new NotificationChannel(SERVICE_CHANNEL, getString(R.string.notification_channel_service), NotificationManager.IMPORTANCE_LOW);
                service.setGroup(CHANNEL_GROUP);
                service.setDescription(getString(R.string.notification_channel_service_description));
                manager.createNotificationChannel(service);
            }{
                NotificationChannel updates = new NotificationChannel(UPDATES_CHANNEL, getString(R.string.notification_channel_updates), NotificationManager.IMPORTANCE_DEFAULT);
                updates.setGroup(CHANNEL_GROUP);
                updates.setDescription(getString(R.string.notification_channel_updates_description));
                updates.setShowBadge(true);
                manager.createNotificationChannel(updates);
            }
        }

        reconnecter = new android.os.Handler(getMainLooper());

        getPreferences().registerOnSharedPreferenceChangeListener(this);
        for(String key : new String[]{"notify_sound","notify_vibrate","notify_light","notify_light_color"})
            onSharedPreferenceChanged(getPreferences(), key);
        Log.d("ocelot.service", "Created");
    }

    private void startForeground() {
        if(foregrounded) return;
        Intent intent = new Intent(this, Service.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = new Notification.Builder(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(SERVICE_CHANNEL);

        builder.setContentTitle(getText(R.string.service_title))
                .setContentText(getText(R.string.service_message))
                .setSmallIcon(R.drawable.ic_ocelot)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_LOW)
                .setOngoing(true);

        // Button to disconnect
        Intent disconnectIntent = new Intent(this, Service.class);
        disconnectIntent.putExtra("action", ACTION_STOP_SERVICE);
        PendingIntent pendingDisconnect = PendingIntent.getService(this, 0, disconnectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Action.Builder abuilder = new Notification.Action.Builder(R.drawable.ic_close_black_24dp,
                getApplicationContext().getString(R.string.drawer_disconnect),
                pendingDisconnect);
        builder.addAction(abuilder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(SERVICE_NOTIFICATION, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING);
        }else{
            startForeground(SERVICE_NOTIFICATION, builder.build());
        }
        foregrounded = true;
        Log.d("ocelot.service", "Started foreground");
    }

    private void stopForeground(){
        if(!foregrounded) return;
        stopForeground(true);
        foregrounded = false;
        Log.d("ocelot.service", "Stopped foreground");
    }

    public static Intent startForeground(Context ctx){
        Intent serviceIntent = new Intent(ctx, Service.class);
        serviceIntent.putExtra("action", ACTION_START_SERVICE);
        ContextCompat.startForegroundService(ctx, serviceIntent);
        return serviceIntent;
    }

    public static Intent stopForeground(Context ctx){
        Intent serviceIntent = new Intent(ctx, Service.class);
        serviceIntent.putExtra("action", ACTION_STOP_SERVICE);
        ContextCompat.startForegroundService(ctx, serviceIntent);
        return serviceIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ocelot.service", "Got start command: "+intent.getIntExtra("action", -1));
        switch(intent.getIntExtra("action", -1)) {
            case ACTION_START_SERVICE:
                startForeground();
                break;
            case ACTION_STOP_SERVICE:
                disconnect();
                stopForeground();
                break;
            case ACTION_CONNECT:
                connect();
                break;
            case ACTION_DISCONNECT:
                disconnect();
                break;
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
        getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        try{((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();}
        catch(Exception ex){Log.d("ocelot.service","Failed to cancel service: "+ex);}
        disconnect();
        clearCache();
    }

    public void showUpdateNotification(String channel, String from, String content){
        notificationCounter++;

        Intent deleteIntent = new Intent(this, Service.class);
        deleteIntent.putExtra("action", ACTION_DISMISS_NOTIFICATION);
        PendingIntent pendingDelete = PendingIntent.getService(this, 0, deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent acceptIntent = new Intent(this, Service.class);
        acceptIntent.putExtra("action", ACTION_ACCEPT_NOTIFICATION);
        acceptIntent.putExtra("channel", channel);
        PendingIntent pendingAccept = PendingIntent.getService(this, 0, acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(UPDATES_CHANNEL);
        }else{
            if(!getPreferences().getString("notify_sound", "").isEmpty())
                builder.setSound(Uri.parse(getPreferences().getString("notify_sound", "")));
            if(getPreferences().getBoolean("notify_vibrate", false))
                builder.setVibrate(new long[]{0, 100});
            if(getPreferences().getBoolean("notify_light", false))
                builder.setLights(Toolkit.getColor(getPreferences(), getTheme(), "notify_light_color", android.R.attr.colorActivatedHighlight), 1000, 1000);
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

    public SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void connect(){
        if(!client.isConnected() && !connecting) {
            boolean successful = false;
            try {
                connecting = true;
                SharedPreferences prefs = getPreferences();
                client.username = prefs.getString("username", "Ocelot");
                client.password = prefs.getString("password", "");
                if (client.password.isEmpty()) client.password = null;
                client.hostname = prefs.getString("hostname", "chat.tymoon.eu");
                client.port = Integer.parseInt(prefs.getString("port", "1111"));
                client.connect();
                Log.d("ocelot.service", "Connecting to " + client.username + "/" + client.password + "@" + client.hostname + ":" + client.port);
                successful = true;
            }finally{
                if(!successful) connecting = false;
            }
        }
    }

    public void disconnect(){
        if(client.isConnected()){
            client.disconnect();
        }
        reconnecter.removeCallbacksAndMessages(null);
        connecting = false;
    }

    public File[] getEmotePaths(){
        File[] files = new File(getFilesDir(), "emotes/").listFiles();
        return (files == null)? new File[0] : files;
    }

    public File getEmotePath(String name){
        Payload payload = client.emotes.get(name.toLowerCase());
        if(payload != null){
            return new File(new File(getFilesDir(), "emotes/"),
                    payload.name+"."+Toolkit.fileExtensionForMime(payload.contentType));
        }
        return null;
    }

    public File getTempFile(String name, String type){
        try {
            return File.createTempFile(name, "." + type, getExternalCacheDir());
        } catch(Exception ex){
            Log.e("ocelot.service", "Failed to create temporary file.", ex);
            return null;
        }
    }

    public void clearCache() {
        Log.d("ocelot.service", "Clearing caches.");
        Toolkit.deleteDirectoryTree(getExternalCacheDir());
        Toolkit.deleteDirectoryTree(getCacheDir());
    }

    public void sendFile(String channel, Uri file){
        Log.d("ocelot.service", "Sending file "+file+"...");
        try {
            ContentResolver cr = getContentResolver();

            android.database.Cursor cursor = cr.query(file,null, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
            if(index < 0){
                Log.e("ocelot.service", "No file selected.");
                if(chat != null) chat.getChannel().showText("No file selected.");
                return;
            }

            long size = cursor.getLong(index);
            cursor.close();

            if(MAX_UPDATE_SIZE < size){
                Log.e("ocelot.service", "File is too large, refusing to send.");
                if(chat != null) chat.getChannel().showText("Failed to send the file as it is bigger than "+(MAX_UPDATE_SIZE/1024/1024)+"MB.");
                return;
            }

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

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).getNotificationChannel(UPDATES_CHANNEL);
            if(key.equals("notify_sound")){
                String sound = sharedPreferences.getString(key, null);
                Log.d("ocelot.service", "Updating notification sound to "+sound);
                if(sound != null){
                    channel.setSound(Uri.parse(sound),
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                                    .build());
                }else{
                    channel.setSound(null, null);
                }
            }else if(key.equals("notify_vibrate")){
                channel.enableVibration(sharedPreferences.getBoolean(key, false));
            }else if(key.equals("notify_light")){
                channel.enableLights(sharedPreferences.getBoolean(key, false));
            }else if(key.equals("notify_light_color")){
                channel.setLightColor(Toolkit.getColor(sharedPreferences, getTheme(), key, android.R.attr.colorActivatedHighlight));
            }
        }
    }

    class Binder extends android.os.Binder{
        public Binder(){}

        public Service bind(Chat toBind){
            Log.d("ocelot.service", "Bound to "+toBind);
            chat = toBind;
            client.addHandler(chat);
            // Replay unseen updates
            for(Update update : updates) chat.handle(update);
            updates.clear();
            return Service.this;
        }

        public Service unbind(){
            if(chat == null) return null;
            Log.d("ocelot.service", "Unbound from "+chat);
            client.removeHandler(chat);
            chat = null;
            return null;
        }

        public Service getService(){
            return Service.this;
        }

        public Client getClient(){
            return client;
        }
    }

    private class UpdateHandler extends HandlerAdapter{

        public void handle(Update update){
            if(chat == null)
                updates.add(update);
            super.handle(update);
        }

        public void handle(Emote emote){
            String name = emote.name.toLowerCase();
            Payload payload = client.emotes.get(name);
            if(payload != null){
                File dest = getEmotePath(name);
                try {
                    dest.getParentFile().mkdirs();
                    payload.save(dest.getAbsolutePath());
                    Log.d("ocelot.service", "Saved emote to "+dest);
                }catch(Exception ex){
                    Log.e("ocelot.service", "Failed to save emote to "+dest, ex);
                }
            }
        }

        public void handle(Data update){
            Payload payload = new Payload(update);
            File temp = getTempFile(payload.name, Toolkit.fileExtensionForMime(payload.contentType));
            Log.d("ocelot.service", "Saving data "+update+" to "+temp+"...");
            if(temp != null){
                try {
                    payload.save(temp);
                    update.payload = temp.getAbsolutePath();
                    return;
                }catch(Exception ex){
                    Log.e("ocelot.service", "Failed to save payload to "+temp, ex);
                }
            }
            update.payload = "";
        }

        public void handle(Message update){
            SharedPreferences prefs = getPreferences();
            if(!client.isSelf(update)
                    && (chat == null || !update.channel.equalsIgnoreCase(chat.getChannel().getName()))
                    && (prefs.getBoolean("notifications", true)
                     && prefs.getBoolean("notify_message", false)
                     || (prefs.getBoolean("notify_mention", true)
                      && Toolkit.mentionsUser(update.text, client.username)))){
                showUpdateNotification(update.channel, update.from, update.text);
            }
        }

        public void handle(Connect update){
            Log.d("ocelot.service", "Connection established.");
            reconnectCounter = 0;
            SharedPreferences prefs = getPreferences();
            Set<String> toJoin = new HashSet<String>();
            toJoin.addAll(chat.listChannels());
            toJoin.addAll(prefs.getStringSet("channels", new HashSet<String>()));
            for(String channel : toJoin){
                client.s("JOIN", "channel", channel);
            }
            connecting = false;
        }

        public void handle(Disconnect update){
            Log.d("ocelot.service", "Closed connection.");
        }

        public void handle(ConnectionLost update){
            Log.i("ocelot.service", "Lost connection", update.exception);
            int timeout = reconnectCounter*reconnectTimeout;
            reconnectCounter++;
            Log.i("ocelot.service", "Reconnecting in "+timeout);
            if(MAX_RECONNECT_ATTEMPTS <= reconnectCounter) {
                Log.w("ocelot.service", "Reconnection attempts stopped, maximum reached. Need manual reconnect.");
            } else if(0 < reconnectCounter) {
                reconnecter.postDelayed(()->{
                    Log.i("ocelot.service", "Attempting reconnect...");
                    try{client.connect();}catch(Exception ex){
                        Log.d("ocelot.service", "Reconnect failed: "+ex);
                    }
                }, 1000*timeout);
                CL.sleep(timeout);
            }
        }
    }
}
