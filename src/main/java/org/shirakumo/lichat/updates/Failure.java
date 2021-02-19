package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Failure extends TextUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("FAILURE");
        CL.registerClass(className, Failure.class);
    }

    public Failure(Map<String, Object> initargs){
        super(initargs);
        
    }
}
