package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class MalformedUpdate extends Failure{
    public static final Symbol className;
    static{
        className = CL.intern("MALFORMED-UPDATE");
        CL.registerClass(className, MalformedUpdate.class);
    }

    public MalformedUpdate(Map<String, Object> initargs){
        super(initargs);
        
    }
}
