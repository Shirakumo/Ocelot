package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class EncodingUnsupported extends Condition{
    public final String encoding;
    
    public EncodingUnsupported(String encoding){
        this.encoding = encoding;
    }

    public String report(){
        return "The encoding "+encoding+" is required but not supported on your system.";
    }
}
