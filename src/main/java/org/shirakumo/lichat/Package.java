package org.shirakumo.lichat;
import java.util.*;

public class Package{
    public final String name;
    private final Map<String, Symbol> symbols;
    
    Package(String name){
        this.name = name;
        symbols = new HashMap<String, Symbol>();
    }

    public Symbol findSymbol(String name){
        return symbols.get(name);
    }

    public Symbol intern(String name){
        Symbol symbol = symbols.get(name);
        if(symbol == null){
            symbol = new Symbol(this, name);
            symbols.put(name, symbol);
        }
        return symbol;
    }
}
