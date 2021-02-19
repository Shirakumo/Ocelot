package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class InvalidUpdate extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("INVALID-UPDATE");
        CL.registerClass(className, InvalidUpdate.class);
    }

    public InvalidUpdate(Map<String, Object> initargs){
        super(initargs);
        
    }
}
