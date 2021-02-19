package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Message extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("MESSAGE");
        CL.registerClass(className, Message.class);
    }

    public final String text;

    public Message(Map<String, Object> initargs){
        super(initargs);
        text = (String)CL.requiredArg(initargs, "text");
    }
}
