package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Connect extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("CONNECT");
        CL.registerClass(className, Connect.class);
    }

    public final String password;
    public final String version;
    public final List<String> extensions = new ArrayList<String>();

    public Connect(Map<String, Object> initargs){
        super(initargs);
        password = (String)CL.arg(initargs, "password");
        version = (String)CL.requiredArg(initargs, "version");
        extensions.addAll((List<String>)CL.requiredArg(initargs, "extensions"));
    }
}
