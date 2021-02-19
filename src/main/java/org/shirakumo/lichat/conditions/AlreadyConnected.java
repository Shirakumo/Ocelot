package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class AlreadyConnected extends Condition{
    public AlreadyConnected(){
        
    }

    public String report(){
        return "The client is already connected.";
    }
}
