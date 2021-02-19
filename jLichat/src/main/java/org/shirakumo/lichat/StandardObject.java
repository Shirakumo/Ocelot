package org.shirakumo.lichat;
import java.util.*;

public class StandardObject{
    public static final Symbol className;
    static{
        className = CL.intern("STANDARD-OBJECT");
        CL.registerClass(className, StandardObject.class);
    }

    public StandardObject(Map<String, Object> initargs){
    }

    public String toString(){
        return "#<"+className+" {"+System.identityHashCode(this)+"}>";
    }
}
