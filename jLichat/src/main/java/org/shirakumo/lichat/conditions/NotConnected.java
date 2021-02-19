package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class NotConnected extends Condition{
    public NotConnected(){
        
    }

    public String report(){
        return "The client is not yet connected.";
    }
}
