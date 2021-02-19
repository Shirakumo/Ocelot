package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class SetChannelInfo extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("SET-CHANNEL-INFO");
        CL.registerClass(className, SetChannelInfo.class);
    }

    public final Symbol key;
    public final String text;

    public SetChannelInfo(Map<String, Object> initargs){
        super(initargs);
        key = (Symbol)CL.requiredArg(initargs, "key");
        text = (String)CL.requiredArg(initargs, "text");
    }
}
