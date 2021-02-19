package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class UpdateFailure extends Failure{
    public static final Symbol className;
    static{
        className = CL.intern("UPDATE-FAILURE");
        CL.registerClass(className, UpdateFailure.class);
    }

    public final Object updateId;

    public UpdateFailure(Map<String, Object> initargs){
        super(initargs);
        updateId = CL.requiredArg(initargs, "update-id");
    }
}
