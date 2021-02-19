package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class TargetUpdate extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("TARGET-UPDATE");
        CL.registerClass(className, TargetUpdate.class);
    }

    public final String target;

    public TargetUpdate(Map<String, Object> initargs){
        super(initargs);
        target = (String)CL.requiredArg(initargs, "target");
    }
}
