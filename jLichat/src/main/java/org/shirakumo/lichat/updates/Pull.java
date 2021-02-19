package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Pull extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("PULL");
        CL.registerClass(className, Pull.class);
    }

    public final String target;

    public Pull(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
    }
}
