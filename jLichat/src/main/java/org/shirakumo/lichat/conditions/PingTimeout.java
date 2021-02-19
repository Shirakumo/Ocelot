package org.shirakumo.lichat.conditions;
import org.shirakumo.lichat.*;

public class PingTimeout extends Condition{
    public PingTimeout(){
        
    }

    public String report(){
        return "A ping timeout has been reached and the connection was closed.";
    }
}
