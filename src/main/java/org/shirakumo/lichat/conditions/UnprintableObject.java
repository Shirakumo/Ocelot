package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class UnprintableObject extends Condition{
    public final Object object;
    
    public UnprintableObject(Object object){
        this.object = object;
    }

    public String report(){
        return "The object "+object+" is not printable to the wire.";
    }
}
