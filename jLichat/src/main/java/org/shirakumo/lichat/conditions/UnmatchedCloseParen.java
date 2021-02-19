package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class UnmatchedCloseParen extends Condition{
    public UnmatchedCloseParen(){
        
    }

    public String report(){
        return "An unmatched closing paren was found on the stream.";
    }
}
