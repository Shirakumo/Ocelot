package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class UsernameMismatch extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("USERNAME-MISMATCH");
        CL.registerClass(className, UsernameMismatch.class);
    }

    public UsernameMismatch(Map<String, Object> initargs){
        super(initargs);
        
    }
}
