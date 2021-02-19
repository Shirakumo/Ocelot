package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Data extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("DATA");
        CL.registerClass(className, Data.class);
    }

    public final String contentType;
    public final String filename;
    public String payload;

    public Data(Map<String, Object> initargs){
        super(initargs);
        contentType = (String)CL.requiredArg(initargs, "content-type");
        filename = (String)CL.requiredArg(initargs, "filename");
        payload = (String)CL.requiredArg(initargs, "payload");
    }
}
