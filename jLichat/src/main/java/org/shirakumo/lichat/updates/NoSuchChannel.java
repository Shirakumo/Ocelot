package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class NoSuchChannel extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("NO-SUCH-CHANNEL");
        CL.registerClass(className, NoSuchChannel.class);
    }

    public NoSuchChannel(Map<String, Object> initargs){
        super(initargs);
        
    }
}
