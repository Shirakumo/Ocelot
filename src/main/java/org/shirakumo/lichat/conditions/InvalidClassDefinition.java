package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class InvalidClassDefinition extends Condition{
    public final Class clas;
    
    public InvalidClassDefinition(Class clas){
        this.clas = clas;
    }

    public String report(){
        return "The class "+clas+" is not a valid StandardObject class.";
    }
}
