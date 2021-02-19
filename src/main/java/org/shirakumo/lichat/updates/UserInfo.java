package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class UserInfo extends TargetUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("USER-INFO");
        CL.registerClass(className, UserInfo.class);
    }

    public final boolean registered;
    public final int connections;

    public UserInfo(Map<String, Object> initargs){
        super(initargs);
        registered = ((Symbol)CL.requiredArg(initargs, "registered")) == CL.findSymbol("T");
        connections = (Integer)CL.requiredArg(initargs, "connections");
    }
}
