package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class MissingArgument extends Condition{
    public final String argument;
    
    public MissingArgument(String argument){
        this.argument = argument;
    }

    public String report(){
        return "The initialization argument "+argument+" is required but not given.";
    }
}
