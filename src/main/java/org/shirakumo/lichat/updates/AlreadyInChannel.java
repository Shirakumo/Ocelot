package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class AlreadyInChannel extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("ALREADY-IN-CHANNEL");
        CL.registerClass(className, AlreadyInChannel.class);
    }

    public AlreadyInChannel(Map<String, Object> initargs){
        super(initargs);
    }
}
