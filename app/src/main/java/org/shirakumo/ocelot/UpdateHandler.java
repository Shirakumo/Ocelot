package org.shirakumo.ocelot;

import org.shirakumo.lichat.HandlerAdapter;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.conditions.InvalidUpdateReceived;
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
        chat.ensureChannel(update.channel);
        chat.getChannel(update.channel).showText(update.clock, update.from, " ** Joined "+update.channel);
    }

    public void handle(Leave update){
        chat.getChannel(update.channel).showText(update.clock, update.from, " ** Left "+update.channel);
    }

    public void handle(Message update){
        chat.getChannel(update.channel).showText(update.clock, update.from, update.text);
    }

    public void handle(Data update){
        chat.getChannel(update.channel).showData(update.clock, update.from, update);
    }

    public void handle(Channels update){
        chat.getChannel(Chat.SYSTEM_CHANNEL).showText(update.clock, update.from, "Channels: "+
                Toolkit.join(update.channels, ", "));
    }

    public void handle(Users update){
        chat.getChannel(update.from).showText(update.clock, update.from, "Users: "+
                Toolkit.join(update.users, ", "));
    }

    public void handle(Connect update){
        chat.getChannel(Chat.SYSTEM_CHANNEL).showText(" ** Connection established");
    }

    public void handle(ConnectionLost update){
        if(update.exception instanceof InvalidUpdateReceived){
            Object real = ((InvalidUpdateReceived)update.exception).object;
            if(real instanceof Failure){
                chat.getChannel(Chat.SYSTEM_CHANNEL).showText(" ** "+((Failure)real).text);
            }else{
                chat.getChannel(Chat.SYSTEM_CHANNEL).showText(" ** Received unexpected response of type "+real.getClass().getSimpleName());
            }
        }else{
            chat.getChannel(Chat.SYSTEM_CHANNEL).showText(" ** Connection lost");
        }
    }
}
