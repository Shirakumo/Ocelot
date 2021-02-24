package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ChannelInfo extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("CHANNEL-INFO");
        CL.registerClass(className, ChannelInfo.class);
    }

    public final Object keys;

    public ChannelInfo(Map<String, Object> initargs){
        super(initargs);
        if(initargs.get("keys") instanceof Symbol)
            keys = initargs.get("keys");
        else {
            keys = new ArrayList<Symbol>();
            ((ArrayList<Symbol>)keys).addAll((List<Symbol>) initargs.get("keys"));
        }
    }
}
