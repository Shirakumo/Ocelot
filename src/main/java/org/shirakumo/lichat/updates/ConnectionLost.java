package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ConnectionLost extends Failure{
    public static final Symbol className;
    static{
        className = CL.intern("CONNECTION-LOST");
        CL.registerClass(className, ConnectionLost.class);
    }

    public final Exception exception;

    public static ConnectionLost create(Exception exception){
        Map<String, Object> initargs = new HashMap<String, Object>();
        initargs.put("text", "Connection lost.");
        initargs.put("clock", CL.getUniversalTime());
        initargs.put("id", null);
        initargs.put("from", "System");
        initargs.put("exception", exception);
        return new ConnectionLost(initargs);
    }

    public ConnectionLost(Map<String, Object> initargs){
        super(initargs);
        exception = (Exception)CL.requiredArg(initargs, "exception");
    }
}
