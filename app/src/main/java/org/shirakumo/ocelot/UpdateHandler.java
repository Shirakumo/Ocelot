package org.shirakumo.ocelot;

import android.util.Log;

import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.*;

public class UpdateHandler extends HandlerAdapter {

    private Chat chat;

    public UpdateHandler(Chat chat){
        this.chat = chat;
    }

    public void handle(Failure update){
        chat.getChannel().showText(update.clock, update.from, " ** Failure: "+update.text);
    }

    public void handle(Join update){
        chat.showChannel(chat.ensureChannel(update.channel));
        chat.getChannel(update.channel).showText(update.clock, update.from, " ** Joined "+update.channel);
    }

    public void handle(Leave update){
        chat.getChannel(update.channel).showText(update.clock, update.from, " ** Left "+update.channel);
    }

    public void handle(Message update){
        chat.getChannel(update.channel).showText(update.clock, update.from, update.text);
    }

    public void handle(Data update){
        chat.getChannel(update.channel).showPayload(update.clock, update.from, new Payload(update));
    }

    public void handle(ConnectionLost update){
        chat.getChannel(Chat.SYSTEM_CHANNEL).showText(" ** Connection lost");
    }
}
