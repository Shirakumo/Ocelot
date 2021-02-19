package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class BadName extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("BAD-NAME");
        CL.registerClass(className, BadName.class);
    }

    public BadName(Map<String, Object> initargs){
        super(initargs);
        
    }
}
