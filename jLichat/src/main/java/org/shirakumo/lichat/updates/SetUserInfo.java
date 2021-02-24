package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class SetUserInfo extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("SET-USER-INFO");
        CL.registerClass(className, SetUserInfo.class);
    }

    public final Symbol key;
    public final String text;

    public SetUserInfo(Map<String, Object> initargs){
        super(initargs);
        key = (Symbol)CL.requiredArg(initargs, "key");
        text = (String)CL.requiredArg(initargs, "text");
    }
}
