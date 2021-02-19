package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class IncompatibleVersion extends UpdateFailure{
    public static final Symbol className;
    static{
        className = CL.intern("INCOMPATIBLE-VERSION");
        CL.registerClass(className, IncompatibleVersion.class);
    }

    public final List<String> compatibleVersions = new ArrayList<String>();

    public IncompatibleVersion(Map<String, Object> initargs){
        super(initargs);
        compatibleVersions.addAll((List<String>)CL.requiredArg(initargs, "compatible-versions"));
    }
}
