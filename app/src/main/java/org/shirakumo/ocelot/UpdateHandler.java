package org.shirakumo.ocelot;

import android.app.FragmentTransaction;
import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.updates.*;
import org.shirakumo.lichat.CL;

public class UpdateHandler extends HandlerAdapter {

    private Chat chat;

    public UpdateHandler(Chat chat){
        this.chat = chat;
    }

    public void showText(long clock, String from, String text){
        FragmentTransaction ft = chat.getFragmentManager().beginTransaction();
        ft.add(R.id.output, Output.newInstance(CL.universalToUnix(clock), from, text), "a");
        ft.commit();
    }

    public void handle(Join update){
        showText(update.clock, update.from, " ** Joined "+update.channel);
    }

    public void handle(Leave update){
        showText(update.clock, update.from, " ** Left "+update.channel);
    }

    public void handle(Message update){
        showText(update.clock, update.from, update.text);
    }

    public void onConnectionLost(Exception ex){
        showText(0, "System", " ** Connection lost");
        ex.printStackTrace();
    }
}
