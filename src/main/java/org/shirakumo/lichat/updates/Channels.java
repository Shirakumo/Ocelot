package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Channels extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("CHANNELS");
        CL.registerClass(className, Channels.class);
    }

    public final List<String> channels = new ArrayList<String>();

    public Channels(Map<String, Object> initargs){
        super(initargs);
        if(initargs.get("channels") != null)
            channels.addAll((List<String>)initargs.get("channels"));
    }
}
