package org.shirakumo.lichat.updates;
import org.shirakumo.lichat.*;
import java.util.*;

public class Backfill extends ChannelUpdate{
    public static final Symbol className;
    static{
        className = CL.intern("BACKFILL");
        CL.registerClass(className, Backfill.class);
    }

    public Backfill(Map<String, Object> initargs){
        super(initargs);
        
    }
}
