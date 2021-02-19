package org.shirakumo.lichat;

public class Symbol{
    public final String name;
    public final Package pkg;

    Symbol(Package pkg, String name){
        this.pkg = pkg;
        this.name = name;
    }

    public String toString(){
        if(pkg == null){
            return "#:"+name;
        }else if(pkg == CL.findPackage("KEYWORD")){
            return ":"+name;
        }else{
            return pkg.name+":"+name;
        }
    }
}
