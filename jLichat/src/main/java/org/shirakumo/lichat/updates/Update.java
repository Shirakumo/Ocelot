package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Update extends StandardObject{
    public static final Symbol className;
    static{
        className = CL.intern("UPDATE");
        CL.registerClass(className, Update.class);
    }

    public final long clock;
    public final Object id;
    public String from;
    private Client client;

    public Update(Map<String, Object> initargs){
        super(initargs);
        clock = (Long)CL.arg(initargs, "clock", CL.getUniversalTime());
        id = (Object)CL.requiredArg(initargs, "id");
        from = (String)CL.arg(initargs, "from");
    }

    public String toString(){
        return "#<"+className+" FROM "+from+" ID "+id+">";
    }

    public Update onResponse(Handler handler){
        client.addCallback((Integer)id, handler);
        return this;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
