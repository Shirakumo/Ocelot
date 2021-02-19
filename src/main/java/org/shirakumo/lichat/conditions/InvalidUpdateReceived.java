package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class InvalidUpdateReceived extends Condition{
    public final Object object;
    
    public InvalidUpdateReceived(Object object){
        this.object = object;
    }

    public String report(){
        return "Received unexpected object: "+object;
    }
}
