package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Unban extends TargetUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("UNBAN");
        CL.registerClass(className, Unban.class);
    }

    public Unban(Map<String, Object> initargs){
        super(initargs);
    }
}
