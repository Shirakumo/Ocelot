package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class SlotValueFailed extends Condition{
    public final StandardObject object;
    public final String slot;
    
    public SlotValueFailed(StandardObject object, String slot){
        this.object = object;
        this.slot = slot;
    }

    public String report(){
        return "The slot "+slot+" could not be retrieved from the object "+object+".";
    }
}
