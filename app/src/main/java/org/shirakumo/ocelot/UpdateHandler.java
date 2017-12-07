package org.shirakumo.ocelot;

import org.shirakumo.lichat.HandlerAdapter;
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

    public void handle(ConnectionLost update){
        chat.getChannel().showText(" ** Connection lost");
        update.exception.printStackTrace();
    }
}
