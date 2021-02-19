package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class TextUpdate extends Update{
    public static final Symbol className;
    static{
        className = CL.intern("TEXT-UPDATE");
        CL.registerClass(className, TextUpdate.class);
    }

    public final String text;

    public TextUpdate(Map<String, Object> initargs){
        super(initargs);
        text = (String)CL.requiredArg(initargs, "text");
    }
}
