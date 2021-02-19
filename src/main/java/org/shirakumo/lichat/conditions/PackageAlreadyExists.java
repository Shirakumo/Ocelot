package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class PackageAlreadyExists extends Condition{
    public final String name;
    
    public PackageAlreadyExists(String name){
        this.name = name;
    }

    public String report(){
        return "A package with the name "+name+" already exists.";
    }
}
