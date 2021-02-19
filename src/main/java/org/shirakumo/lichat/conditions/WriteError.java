package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class WriteError extends Condition{
    public final Exception exception;
    
    public WriteError(Exception exception){
        this.exception = exception;
    }

    public String report(){
        return "Error while writing: "+exception;
    }
}
