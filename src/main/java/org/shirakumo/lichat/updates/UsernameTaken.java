package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class UsernameTaken extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("USERNAME-TAKEN");
        CL.registerClass(className, UsernameTaken.class);
    }

    public UsernameTaken(Map<String, Object> initargs){
        super(initargs);
        
    }
}
