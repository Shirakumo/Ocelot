package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class InvalidPermissions extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("INVALID-PERMISSIONS");
        CL.registerClass(className, InvalidPermissions.class);
    }

    public InvalidPermissions(Map<String, Object> initargs){
        super(initargs);
        
    }
}
