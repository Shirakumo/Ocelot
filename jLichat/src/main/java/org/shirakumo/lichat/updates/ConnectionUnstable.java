package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ConnectionUnstable extends Failure{
    public static final Symbol className;
    static{
        className = CL.intern("CONNECTION-UNSTABLE");
        CL.registerClass(className, ConnectionUnstable.class);
    }

    public ConnectionUnstable(Map<String, Object> initargs){
        super(initargs);
        
    }
}
