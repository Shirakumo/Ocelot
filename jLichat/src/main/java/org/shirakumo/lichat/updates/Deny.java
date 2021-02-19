package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Deny extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("DENY");
        CL.registerClass(className, Deny.class);
    }

    public final String target;
    public final Symbol update;

    public Deny(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
        update = (Symbol)CL.requiredArg(initargs, "update");
    }
}
