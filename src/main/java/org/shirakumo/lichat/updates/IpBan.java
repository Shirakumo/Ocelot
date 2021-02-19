package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class IpBan extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("IP-BAN");
        CL.registerClass(className, IpBan.class);
    }

    public final String ip;
    public final String mask;

    public IpBan(Map<String, Object> initargs){
        super(initargs);
        ip = (String)CL.requiredArg(initargs, "ip");
        mask = (String)CL.requiredArg(initargs, "mask");
    }
}
