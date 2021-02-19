package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Ban extends TargetUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("BAN");
        CL.registerClass(className, Ban.class);
    }

    public Ban(Map<String, Object> initargs){
        super(initargs);
    }
}
