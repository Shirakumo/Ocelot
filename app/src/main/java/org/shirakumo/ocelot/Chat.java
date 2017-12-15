package org.shirakumo.ocelot;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shirakumo.lichat.CL;
import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.Leave;
import org.shirakumo.lichat.updates.NoSuchChannel;
import org.shirakumo.lichat.updates.Update;

public class Chat extends Activity implements Channel.ChannelListener, EmoteList.EmoteListListener, Handler{
    public static final String SYSTEM_CHANNEL = "@System";
    private static final int SETTINGS_REQUEST = 1;

    private Intent serviceIntent;
    private UpdateHandler handler;
    private HashMap<String, Channel> channels;
    private HashMap<String, Command> commands;
    private Channel channel;
    private long lastSeen = 0;
    private Service.Binder binder;
    private List<Runnable> onBindRunnables = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serviceIntent = new Intent(this, Service.class);
        handler = new UpdateHandler(this);
        channels = new HashMap<>();
        commands = new HashMap<>();

        ((TextView)findViewById(R.id.input)).setOnEditorActionListener((TextView v, int actionId, KeyEvent event)->{
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && !event.isShiftPressed() && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                onInput(v.getText().toString());
                v.setText("");
                return true;
            }
            return false;
        });

        findViewById(R.id.send_file).setOnClickListener((vw)->{
            requestSendFile(channel);
        });

        findViewById(R.id.select_emote).setOnClickListener((vw)->{
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            EmoteList.newInstance().show(ft, "emotes");
        });

        addCommand("join", (Channel c, String[] args)->{
            String name = Toolkit.join(args, " ", 1);
            binder.getClient().s("JOIN",
                    "channel", name);
            showChannel(ensureChannel(name));
        });

        addCommand("leave", (Channel c, String[] args)->{
            binder.getClient().s("LEAVE",
                    "channel", (args.length == 1)? c.getName() : Toolkit.join(args, " ", 1));
        });

        addCommand("close", (Channel c, String[] args)->{
            Object id = ((Update)binder.getClient().s("LEAVE","channel", c.getName())).id;
            binder.getClient().addCallback((Integer)id, (Update u)->{
                if(u instanceof Leave || u instanceof NoSuchChannel) removeChannel(c);
            });
        });

        addCommand("create", (Channel c, String[] args)->{
            binder.getClient().s("CREATE",
                    "channel", (args.length == 1)? null : Toolkit.join(args, " ", 1));
        });

        addCommand("channels", (Channel c, String[] args)->{
            binder.getClient().s("CHANNELS");
        });

        addCommand("users", (Channel c, String[] args)->{
            binder.getClient().s("USERS",
                    "channel", c.getName());
        });

        addCommand("connect", (Channel c, String[] args)->{
            binder.getService().connect();
        });

        addCommand("disconnect", (Channel c, String[] args)->{
            binder.getService().disconnect();
        });

        addCommand("help", (Channel c, String[] args)->{
            String[] commandNames = new String[commands.keySet().size()];
            commandNames = (String[])commands.keySet().toArray(commandNames);
            Arrays.sort(commandNames);
            c.showHTML("Available commands:<br>"+Toolkit.join(commandNames, "<br>"));
        });

        addCommand("settings", (Channel c, String[] args)->{
            Intent i = new Intent(this, Settings.class);
            startActivityForResult(i, SETTINGS_REQUEST);
        });

        addCommand("clear", (Channel c, String[] args)->{
            c.clear();
            binder.getService().clearChannelUpdates(c.getName());
        });

        final SharedPreferences hasDefaults = getSharedPreferences(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        if(!hasDefaults.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(this, R.xml.settings_connection, true);
            PreferenceManager.setDefaultValues(this, R.xml.settings_notification, true);
            PreferenceManager.setDefaultValues(this, R.xml.settings_looks, true);
            generateStyleSheet();
        }

        commands.get("help").execute(ensureChannel(SYSTEM_CHANNEL), null);
        String channelToShow = getIntent().getStringExtra("channel");
        if(channelToShow != null){
            showChannel(ensureChannel(channelToShow));
        }else{
            showChannel(SYSTEM_CHANNEL);
        }
    }

    @Override
    public void registerChannel(Channel channel){
        channels.put(channel.getName(), channel);
        // Create tab button
        ToggleButton tab = (ToggleButton)getLayoutInflater().inflate(R.layout.channel_button, null);
        tab.setTextOff(channel.getName());
        tab.setTextOn(channel.getName());
        tab.setText(channel.getName());
        tab.setTag(channel);
        tab.setOnClickListener((View v)->showChannel(channel));
        ((LinearLayout)findViewById(R.id.tabs)).addView(tab);
        Log.d("ocelot.chat", "Registered channel "+channel);
    }

    public Channel ensureChannel(String name){
        if(!channels.containsKey(name)){
            // Create channel fragment
            Channel channel = Channel.newInstance(name);
            getFragmentManager().beginTransaction()
                    .add(R.id.channel, channel)
                    .addToBackStack(null)
                    .commit();
            getFragmentManager().beginTransaction()
                    .hide(channel)
                    .addToBackStack(null)
                    .commit();
            getFragmentManager().executePendingTransactions();
            Log.d("ocelot.chat", "Created channel "+name);
        }
        return getChannel(name);
    }

    public void removeChannel(Channel channel){
        channels.remove(channel.getName());
        getFragmentManager().beginTransaction()
                .remove(channel)
                .addToBackStack(null)
                .commit();
        LinearLayout tabs = (LinearLayout)findViewById(R.id.tabs);
        tabs.removeView(tabs.findViewWithTag(channel));
        Log.d("ocelot.chat", "Removed channel "+channel.getName());
    }

    public void removeChannel(String name){
        removeChannel(getChannel(name));
    }

    public Channel getChannel(){
        return channel;
    }

    public Channel getChannel(String name){
        return channels.get(name);
    }

    public Channel showChannel(Channel toShow){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LinearLayout tabs = (LinearLayout)findViewById(R.id.tabs);
        for(Channel c : channels.values()){
            if(c != toShow){
                ft.hide(c);
                ((ToggleButton)tabs.findViewWithTag(c)).setChecked(false);
            }
        }
        ft.show(toShow);
        ft.addToBackStack(null);
        ft.commit();
        ((ToggleButton)tabs.findViewWithTag(toShow)).setChecked(true);
        channel = toShow;
        return channel;
    }

    public Channel showChannel(String name){
        return showChannel(getChannel(name));
    }

    public Command addCommand(String name, Command command){
        commands.put(name, command);
        Log.d("ocelot.chat", "Added command "+name);
        return command;
    }

    public Command removeCommand(String name){
        Command command = commands.get(name);
        commands.remove(name);
        Log.d("ocelot.chat", "Removed command "+name);
        return command;
    }

    public String getUsername(){
        return (binder == null)? null : binder.getClient().username;
    }

    public File getEmotePath(String name){
        if(binder.getClient() == null) return null;
        return binder.getService().getEmotePath(name);
    }

    public void showUrl(Uri url){
        if(url.getScheme().equals("file")) {
            url = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider", new File(url.getPath()));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    public void generateStyleSheet(){
        SharedPreferences pref = getPreferences();
        Resources.Theme res = getTheme();
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        String style = "html{"
                +"background:"+ Toolkit.getColorHex(pref, res, "color_background", android.R.attr.colorBackground)+";"
                +"color:"+Toolkit.getColorHex(pref, res, "color_foreground", android.R.attr.colorForeground)+";"
                +"font-size:"+pref.getInt("fontsize", 11)+"pt;"
                +"}"
                +"mark{"
                +"background:"+Toolkit.getColorHex(pref, res, "color_mention", android.R.attr.colorActivatedHighlight)+";"
                +"}"
                +"#channel .update .from{"
                +"min-width:"+(6*pref.getInt("fontsize", 11))+"pt;"
                +"max-width:"+(6*pref.getInt("fontsize", 11))+"pt;"
                +"}"
                +"#channel .update.self .from{"
                +"color:"+Toolkit.getColorHex(pref, res, "color_self", android.R.attr.colorForeground)+";"
                +"}"
                +"#channel .update .text a{"
                +"color:"+Toolkit.getColorHex(pref, res, "color_link", android.R.attr.colorActivatedHighlight)+";"
                +"}"
                +"#channel .update .text .payload{"
                +"max-height:"+(size.y*1/3)+"px"
                +"}";
        File file = new File(getFilesDir(), "style.css");
        try {
            Toolkit.writeStringToFile(style, file);
        }catch(Exception ex){
            Log.e("ocelot.chat", "Failed to generate style sheet "+file, ex);
        }
    }

    public void updateStyleSheet(){
        generateStyleSheet();
        for(Channel c : channels.values()){
            c.runScript("reloadCSS();");
        }
    }

    public void bind(){
        if(binder == null) {
            bindService(serviceIntent, serviceConnection, Context.BIND_IMPORTANT);
        }
    }

    public void unbind(){
        if(binder != null){
            unbindService(serviceConnection);
            lastSeen = CL.getUniversalTime();
            binder.unbind();
            for(Channel c : channels.values()) c.clear();
            binder = null;
        }
    }

    @Override
    public Map<String, Payload> getEmotes() {
        return (binder == null)? null : binder.getClient().emotes;
    }

    private Map<Integer, Channel> sendFileRequestMap = new HashMap<>();
    private int sendFileRequestId = 100;
    public void requestSendFile(Channel channel){
        int id = sendFileRequestId++;
        sendFileRequestMap.put(id, channel);
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ocelot.chat", "Activity result "+requestCode);
        if(requestCode == SETTINGS_REQUEST){
            updateStyleSheet();
        }else {
            Channel channel = sendFileRequestMap.get(requestCode);
            if (channel != null) {
                sendFileRequestMap.remove(requestCode);
                if (resultCode == RESULT_OK) {
                    Log.d("ocelot.chat", "Selected file " + data.getData());
                    if(binder != null)
                        binder.getService().sendFile(channel.getName(), data.getData());
                    else
                        onBindRunnables.add(()->binder.getService().sendFile(channel.getName(), data.getData()));
                }
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d("ocelot.chat", "Starting");
        super.onStart();
        if(binder == null) startService(serviceIntent);
        bind();
    }

    @Override
    protected void onPause() {
        Log.d("ocelot.chat", "Pausing");
        super.onPause();
        unbind();
    }

    @Override
    protected void onResume() {
        Log.d("ocelot.chat", "Resuming");
        super.onResume();
        bind();
    }

    @Override
    protected void onStop() {
        Log.d("ocelot.chat", "Stopping");
        super.onStop();
        unbind();
    }

    @Override
    protected void onDestroy() {
        Log.d("ocelot.chat", "Destroying");
        super.onDestroy();
    }

    private boolean backPressed = false;
    @Override
    public void onBackPressed() {
        if(backPressed || (binder != null && !binder.getService().client.isConnected())) {
            if(binder != null) {
                binder.getService().disconnect();
                unbind();
            }
            stopService(serviceIntent);
            finish();
        }else{
            backPressed = true;
            Toast.makeText(this, "Press BACK again to disconnect.", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(()->{
                backPressed = false;
            }, 3000);
        }
    }

    @Override
    public void onEmoteChosen(String emote) {
        channel.setInput(channel.getInput()+":"+emote+":");
    }

    public void onInput(String input){
        if(input.isEmpty()) return;

        if(input.startsWith("/")){
            String[] args = input.substring(1).split(" +");
            Command command = commands.get(args[0].toLowerCase());
            if(command != null){
                command.execute(channel, args);
            }else{
                channel.showText("No such command "+args[0]);
            }
        }else{
            binder.getClient().s("MESSAGE",
                    "channel", channel.getName(),
                    "text", input);
        }
    }

    public void handle(Update update){
        runOnUiThread(()->{
            handler.handle(update);
        });
    }

    public SharedPreferences getPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder ibinder) {
            binder = (Service.Binder)ibinder;
            binder.bind(Chat.this);
            if(getPreferences().getBoolean("autoconnect", false))
                binder.getService().connect();
            binder.getService().replayUpdates(lastSeen, Chat.this);
            for(Runnable r : onBindRunnables) r.run();
            onBindRunnables.clear();
            Log.d("ocelot.chat", "Connected to "+binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
            Log.w("ocelot.chat", "Disconnected from service.");
        }
    };
}
