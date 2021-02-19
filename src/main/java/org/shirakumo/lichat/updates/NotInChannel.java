package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class NotInChannel extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("NOT-IN-CHANNEL");
        CL.registerClass(className, NotInChannel.class);
    }

    public NotInChannel(Map<String, Object> initargs){
        super(initargs);
        
    }
}
