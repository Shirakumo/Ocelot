package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Users extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("USERS");
        CL.registerClass(className, Users.class);
    }

    public final List<String> users = new ArrayList<String>();

    public Users(Map<String, Object> initargs){
        super(initargs);
        if(initargs.get("users") != null)
            users.addAll((List<String>)initargs.get("users"));
    }
}
