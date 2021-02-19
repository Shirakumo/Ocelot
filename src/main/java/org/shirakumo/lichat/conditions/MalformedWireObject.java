package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;
import java.util.*;

public class MalformedWireObject extends Condition{
    public final List<Object> initargs;
    
    public MalformedWireObject(List<Object> initargs){
        this.initargs = initargs;
    }

    public String report(){
        return "The list of initargs received does not construct a valid update.";
    }
}
