package org.shirakumo.lichat;

public class Condition extends RuntimeException{
    private final String message;

    public Condition(){
        this.message = null;
    }
    
    public Condition(String message){
        this.message = message;
    }

    public String toString(){
        return "[Condition of type "+this.getClass().getName()+"] "+report();
    }

    public String report(){
        return (message == null)? "" : message;
    }
}
