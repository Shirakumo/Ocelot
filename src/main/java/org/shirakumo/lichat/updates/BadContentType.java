package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class BadContentType extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("BAD-CONTENT-TYPE");
        CL.registerClass(className, BadContentType.class);
    }

    public final List<String> allowedContentTypes = new ArrayList<String>();

    public BadContentType(Map<String, Object> initargs){
        super(initargs);
        allowedContentTypes.addAll((List<String>)CL.requiredArg(initargs, "allowed-content-types"));
    }
}
