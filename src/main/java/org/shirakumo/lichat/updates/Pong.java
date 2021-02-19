package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Pong extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("PONG");
        CL.registerClass(className, Pong.class);
    }

    public Pong(Map<String, Object> initargs){
        super(initargs);
        
    }
}
