package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Edit extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("EDIT");
        CL.registerClass(className, Edit.class);
    }

    public final String text;

    public Edit(Map<String, Object> initargs){
        super(initargs);
        text = (String)CL.requiredArg(initargs, "text");
    }
}
