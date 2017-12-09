package org.shirakumo.ocelot;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.Leave;
import org.shirakumo.lichat.updates.Update;

public class Chat extends Activity implements Channel.ChannelListener, EmoteList.EmoteListListener{

    private Intent serviceIntent;
    private UpdateHandler handler;
    private boolean bound = false;
    private Service service;
    private HashMap<String, Channel> channels;
    private HashMap<String, Command> commands;
    private Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serviceIntent = new Intent(this, Service.class);
        handler = new UpdateHandler(this);
        channels = new HashMap<>();
        commands = new HashMap<>();

        addCommand("join", (Channel c, String[] args)->{
            service.client.s("JOIN",
                    "channel", Toolkit.join(args, " ", 1));
        });

        addCommand("leave", (Channel c, String[] args)->{
            service.client.s("LEAVE",
                    "channel", (args.length == 1)? c.getName() : Toolkit.join(args, " ", 1));
        });

        addCommand("close", (Channel c, String[] args)->{
            Object id = ((Update)service.client.s("LEAVE","channel", c.getName())).id;
            service.client.addCallback((Integer)id, (Update u)->{
                if(u instanceof Leave) removeChannel(c);
            });
        });

        addCommand("create", (Channel c, String[] args)->{
            service.client.s("CREATE",
                    "channel", (args.length == 1)? null : Toolkit.join(args, " ", 1));
        });

        addCommand("help", (Channel c, String[] args)->{
            String[] commandNames = new String[commands.keySet().size()];
            c.showText("Available commands: "+Toolkit.join((String[])commands.keySet().toArray(commandNames), ", "));
        });
    }

    @Override
    public void onInput(Channel c, String input){
        if(service == null) return;
        if(input.isEmpty()) return;

        if(input.startsWith("/")){
            String[] args = input.substring(1).split(" +");
            Command command = commands.get(args[0].toLowerCase());
            if(command != null){
                command.execute(c, args);
            }else{
                c.showText("No such command "+args[0]);
            }
        }else{
            service.client.s("MESSAGE",
                    "channel", c.getName(),
                    "text", input);
        }
    }

    @Override
    public void registerChannel(Channel c){
        channels.put(c.getName(), c);
    }

    public Channel ensureChannel(String name){
        if(!channels.containsKey(name)){
            // Create channel fragment
            Channel channel = Channel.newInstance(name);
            channels.put(name, channel);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.channel, channel);
            ft.commit();
            getFragmentManager().executePendingTransactions();
            // Create tab button
            Button tab = (Button)getLayoutInflater().inflate(R.layout.channel_button, null);
            tab.setText(name);
            tab.setTag(channel);
            tab.setOnClickListener((View v)->showChannel(channel));
            ((LinearLayout)findViewById(R.id.tabs)).addView(tab);
        }
        return getChannel(name);
    }

    public void removeChannel(Channel channel){
        ((ViewGroup)channel.getView().getParent()).removeView(channel.getView());
        LinearLayout tabs = (LinearLayout)findViewById(R.id.tabs);
        tabs.removeView(tabs.findViewWithTag(channel));
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
        for(Channel c : channels.values()){
            c.getView().setVisibility(View.GONE);
        }
        toShow.getView().setVisibility(View.VISIBLE);
        channel = toShow;
        return channel;
    }

    public Channel showChannel(String name){
        return showChannel(getChannel(name));
    }

    public Command addCommand(String name, Command command){
        commands.put(name, command);
        return command;
    }

    public Command removeCommand(String name){
        Command command = commands.get(name);
        commands.remove(name);
        return command;
    }

    public Payload getEmote(String name){
        return service.client.emotes.get(name);
    }

    public void bind(){
        if(!bound) {
            bound = bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbind(){
        if(bound){
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(serviceIntent);
        bind();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(service != null) service.disconnect();
        unbind();
        stopService(serviceIntent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        ServiceBinder binder;
        Handler handlerWrapper = (Update update)->{
            runOnUiThread(()->{
                handler.handle(update);
            });
        };

        @Override
        public void onServiceConnected(ComponentName className, IBinder ibinder) {
            binder = (ServiceBinder)ibinder;
            binder.service.addHandler(handlerWrapper);
            service = binder.service;
            service.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder.service.removeHandler(handlerWrapper);
            service = null;
        }
    };

    @Override
    public void emoteChosen(String emote) {
        channel.setInput(channel.getInput()+":"+emote+":");
    }

    @Override
    public Map<String, Payload> getEmotes() {
        return service.client.emotes;
    }
}
