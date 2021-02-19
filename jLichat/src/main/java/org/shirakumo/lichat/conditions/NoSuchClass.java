package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class NoSuchClass extends Condition{
    public final Symbol className;
    
    public NoSuchClass(Symbol className){
        this.className = className;
    }

    public String report(){
        return "No class named "+className+" known.";
    }
}
