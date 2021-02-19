package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Permissions extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("PERMISSIONS");
        CL.registerClass(className, Permissions.class);
    }

    public final List<Object> permissions = new ArrayList<Object>();

    public Permissions(Map<String, Object> initargs){
        super(initargs);
        if(CL.arg(initargs, "permissions") != null)
            permissions.addAll((List<Object>)initargs.get("permissions"));
    }
}
