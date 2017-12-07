package org.shirakumo.ocelot;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import java.util.HashMap;
import org.shirakumo.lichat.Handler;
import org.shirakumo.lichat.updates.Update;

public class Chat extends Activity implements EditText.OnEditorActionListener{

    private Intent serviceIntent;
    private UpdateHandler handler;
    private boolean bound = false;
    private Service service;
    private Channel channel;
    private HashMap<String, Channel> channels;
    private HashMap<String, Command> commands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serviceIntent = new Intent(this, Service.class);
        handler = new UpdateHandler(this);
        channels = new HashMap<>();
        commands = new HashMap<>();

        EditText editText = (EditText) findViewById(R.id.input);
        editText.setOnEditorActionListener(this);

        addCommand("join", (String[] args)->{
            service.client.s("JOIN",
                    "channel", Toolkit.join(args, " ", 1));
        });

        addCommand("leave", (String[] args)->{
            service.client.s("LEAVE",
                    "channel", (args.length == 1)? channel.getName() : Toolkit.join(args, " ", 1));
        });

        addCommand("create", (String[] args)->{
            service.client.s("CREATE",
                    "channel", (args.length == 1)? null : Toolkit.join(args, " ", 1));
        });

        addCommand("help", (String[] args)->{
            channel.showText("Available commands: "+Toolkit.join((String[])commands.keySet().toArray(), ", "));
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            handleInput(v.getText().toString());
            v.setText("");
            return true;
        }
        return false;
    }

    public void handleInput(String input){
        if(service == null) return;
        if(input.indexOf("/") == 0){
            String[] args = input.substring(1).split(" +");
            Command command = commands.get(args[0].toLowerCase());
            if(command != null){
                command.execute(args);
            }else{
                channel.showText("No such command "+args[0]);
            }
        }else{
            service.client.s("MESSAGE",
                    "channel", getChannel().getName(),
                    "text", input);
        }
    }

    public Channel ensureChannel(String name){
        if(!channels.containsKey(name)){
            Channel channel = Channel.newInstance(name);
            channels.put(name, channel);
        }
        return getChannel(name);
    }

    public Channel getChannel(){
        return channel;
    }

    public Channel getChannel(String name){
        return channels.get(name);
    }

    public Channel showChannel(Channel channel){
        FrameLayout frame = (FrameLayout) findViewById(R.id.channel);
        frame.removeAllViews();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.channel, channel);
        ft.commit();
        this.channel = channel;
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
