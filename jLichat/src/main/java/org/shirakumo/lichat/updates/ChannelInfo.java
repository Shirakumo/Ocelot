package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ChannelInfo extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("CHANNEL-INFO");
        CL.registerClass(className, ChannelInfo.class);
    }

    public final List<Symbol> keys = new ArrayList<Symbol>();

    public ChannelInfo(Map<String, Object> initargs){
        super(initargs);
        if(initargs.get("keys") instanceof Symbol)
            keys.add(CL.intern("T"));
        else
            keys.addAll((List<Symbol>)initargs.get("keys"));
    }
}
