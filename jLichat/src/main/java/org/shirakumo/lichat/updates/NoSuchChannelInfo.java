package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class NoSuchChannelInfo extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("NO-SUCH-CHANNEL-INFO");
        CL.registerClass(className, NoSuchChannelInfo.class);
    }

    public final Symbol key;

    public NoSuchChannelInfo(Map<String, Object> initargs){
        super(initargs);
        key = (Symbol)initargs.get("key");
    }
}
