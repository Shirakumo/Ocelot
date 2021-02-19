package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Unquiet extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("UNQUIET");
        CL.registerClass(className, Unquiet.class);
    }

    public final String target;

    public Unquiet(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
    }
}
