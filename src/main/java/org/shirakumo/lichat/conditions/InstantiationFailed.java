package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;
import java.util.*;

public class InstantiationFailed extends Condition{
    public final Class clas;
    public final Map<String, Object> initargs;
    
    public InstantiationFailed(Class clas, Map<String, Object> initargs){
        this.clas = clas;
        this.initargs = initargs;
    }

    public String report(){
        return "Failed to create an instance of "+clas+".";
    }
}
