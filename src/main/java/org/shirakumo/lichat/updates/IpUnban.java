package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class IpUnban extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("IP-UNBAN");
        CL.registerClass(className, IpUnban.class);
    }

    public final String ip;
    public final String mask;

    public IpUnban(Map<String, Object> initargs){
        super(initargs);
        ip = (String)CL.requiredArg(initargs, "ip");
        mask = (String)CL.requiredArg(initargs, "mask");
    }
}
