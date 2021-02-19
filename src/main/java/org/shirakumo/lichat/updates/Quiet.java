package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Quiet extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("QUIET");
        CL.registerClass(className, Quiet.class);
    }

    public final String target;

    public Quiet(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
    }
}
