package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Disconnect extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("DISCONNECT");
        CL.registerClass(className, Disconnect.class);
    }

    public Disconnect(Map<String, Object> initargs){
        super(initargs);
        
    }
}
