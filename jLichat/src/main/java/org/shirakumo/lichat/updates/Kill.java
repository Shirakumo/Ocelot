package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Kill extends TargetUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("KILL");
        CL.registerClass(className, Kill.class);
    }

    public Kill(Map<String, Object> initargs){
        super(initargs);
    }
}
