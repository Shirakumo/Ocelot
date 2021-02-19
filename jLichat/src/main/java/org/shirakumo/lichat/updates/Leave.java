package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Leave extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("LEAVE");
        CL.registerClass(className, Leave.class);
    }

    public Leave(Map<String, Object> initargs){
        super(initargs);
        
    }
}
