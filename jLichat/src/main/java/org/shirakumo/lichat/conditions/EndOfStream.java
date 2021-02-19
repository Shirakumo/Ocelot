package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class EndOfStream extends Condition{
    public EndOfStream(){
        
    }

    public String report(){
        return "The stream ended prematurely.";
    }
}
