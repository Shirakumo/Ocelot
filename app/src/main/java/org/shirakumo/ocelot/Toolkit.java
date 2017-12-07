package org.shirakumo.ocelot;

public class Toolkit {
    public static String join(String[] strings, String delim, int from, int to){
        if(strings.length < to) return "";
        StringBuilder build = new StringBuilder();
        for(int i=from; i<to-1; i++){
            build.append(strings[i]);
            build.append(delim);
        }
        build.append(strings[to-1]);
        return build.toString();
    }

    public static String join(String[] strings, String delim, int from){
        return join(strings, delim, from, strings.length);
    }

    public static String join(String[] strings, String delim){
        return join(strings, delim, 0, strings.length);
    }
}
