package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class ServerInfo extends TargetUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("USER-INFO");
        CL.registerClass(className, ServerInfo.class);
    }

    public final Map<Symbol,Object> attributes = new HashMap<Symbol,Object>();
    public final List<Map<Symbol,Object>> connections = new ArrayList<Map<Symbol,Object>>();

    public ServerInfo(Map<String, Object> initargs){
        super(initargs);
        if(initargs.get("attributes") != null)
            for(List entry : (List<List>)initargs.get("attributes"))
                attributes.put((Symbol)entry.get(0), entry.get(1));
        if(initargs.get("connections") != null)
            for(List<List> conn : (List<List>)initargs.get("connections")){
                Map<Symbol,Object> map = new HashMap<Symbol,Object>();
                for(List entry : conn)
                    map.put((Symbol)entry.get(0), entry.get(1));
                connections.add(map);
            }
    }
}
