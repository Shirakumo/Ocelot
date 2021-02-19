package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Emote extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("EMOTE");
        CL.registerClass(className, Emote.class);
    }

    public final String contentType;
    public final String name;
    public final String payload;

    public Emote(Map<String, Object> initargs){
        super(initargs);
        contentType = (String)CL.requiredArg(initargs, "content-type");
        name = (String)CL.requiredArg(initargs, "name");
        payload = (String)CL.requiredArg(initargs, "payload");
    }
}
