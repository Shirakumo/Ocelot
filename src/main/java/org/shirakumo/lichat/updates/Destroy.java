package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Destroy extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("DESTROY");
        CL.registerClass(className, Destroy.class);
    }

    public Destroy(Map<String, Object> initargs){
        super(initargs);
    }
}
