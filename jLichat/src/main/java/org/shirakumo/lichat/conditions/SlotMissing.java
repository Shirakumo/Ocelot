package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class SlotMissing extends Condition{
    public final StandardObject object;
    public final String slot;
    
    public SlotMissing(StandardObject object, String slot){
        this.object = object;
        this.slot = slot;
    }

    public String report(){
        return "The slot "+slot+" is missing from the object "+object+".";
    }
}
