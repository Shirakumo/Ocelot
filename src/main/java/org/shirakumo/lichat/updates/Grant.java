package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Grant extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("GRANT");
        CL.registerClass(className, Grant.class);
    }

    public final String target;
    public final Symbol update;

    public Grant(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
        update = (Symbol)CL.requiredArg(initargs, "update");
    }
}
