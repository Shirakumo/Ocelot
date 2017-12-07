package org.shirakumo.ocelot;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LocalActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import java.util.HashMap;
import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.updates.Update;

public class Chat extends FragmentActivity implements Channel.ChannelListener{

    private Intent serviceIntent;
    private UpdateHandler handler;
    private boolean bound = false;
    private Service service;
    private HashMap<String, Channel> channels;
    private HashMap<String, Command> commands;
    private FragmentTabHost tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serviceIntent = new Intent(this, Service.class);
        handler = new UpdateHandler(this);
        channels = new HashMap<>();
        commands = new HashMap<>();
        tabs = findViewById(R.id.channels);
        tabs.setup(this, getSupportFragmentManager(), R.id.channels);

        addCommand("join", (Channel c, String[] args)->{
            service.client.s("JOIN",
                    "channel", Toolkit.join(args, " ", 1));
        });

        addCommand("leave", (Channel c, String[] args)->{
            service.client.s("LEAVE",
                    "channel", (args.length == 1)? c.getName() : Toolkit.join(args, " ", 1));
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
        if(input.indexOf("/") == 0){
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

    public Channel ensureChannel(String name){
        if(!channels.containsKey(name)){
            Channel channel = Channel.newInstance(name);
            channels.put(name, channel);
            tabs.addTab(tabs.newTabSpec(name).setIndicator(name), Placeholder.class, null);
            getSupportFragmentManager().beginTransaction().replace(R.id.placeholder, channel).commit();
        }
        return getChannel(name);
    }

    public Channel getChannel(){
        return getChannel(tabs.getCurrentTabTag());
    }

    public Channel getChannel(String name){
        return channels.get(name);
    }

    public Channel showChannel(Channel channel){
        tabs.setCurrentTabByTag(channel.getName());
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
}
