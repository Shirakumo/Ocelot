package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Kick extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("KICK");
        CL.registerClass(className, Kick.class);
    }

    public final String target;

    public Kick(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
    }
}
