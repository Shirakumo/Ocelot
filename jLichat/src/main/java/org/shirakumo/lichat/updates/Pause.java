package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Pause extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("MESSAGE");
        CL.registerClass(className, Pause.class);
    }

    public final int by;

    public Pause(Map<String, Object> initargs){
        super(initargs);
        by = (Integer)CL.requiredArg(initargs, "by");
    }
}
