package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Join extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("JOIN");
        CL.registerClass(className, Join.class);
    }

    public Join(Map<String, Object> initargs){
        super(initargs);
        
    }
}
