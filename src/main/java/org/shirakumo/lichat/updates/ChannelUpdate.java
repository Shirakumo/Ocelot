package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ChannelUpdate extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("CHANNEL-UPDATE");
        CL.registerClass(className, ChannelUpdate.class);
    }

    public final String channel;

    public ChannelUpdate(Map<String, Object> initargs){
        super(initargs);
        channel = (String)CL.requiredArg(initargs, "channel");
    }
}
