package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class NoSuchUserInfo extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("NO-SUCH-USER-INFO");
        CL.registerClass(className, NoSuchUserInfo.class);
    }

    public final Symbol key;

    public NoSuchUserInfo(Map<String, Object> initargs){
        super(initargs);
        key = (Symbol)initargs.get("key");
    }
}
