package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class NoSuchProfile extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("NO-SUCH-PROFILE");
        CL.registerClass(className, NoSuchProfile.class);
    }

    public NoSuchProfile(Map<String, Object> initargs){
        super(initargs);
        
    }
}
