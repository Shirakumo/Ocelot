package org.shirakumo.ocelot;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String replaceAll(String input, Pattern regex, ReplaceOp replacer){
        Matcher matcher = regex.matcher(input);
        String[] groups = new String[matcher.groupCount()];
        StringBuffer out = new StringBuffer();
        while(matcher.find()){
            for(int i=0; i<groups.length; i++) groups[i] = matcher.group(i+1);
            matcher.appendReplacement(out, replacer.replace(matcher.group(), groups));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    @FunctionalInterface
    public interface ReplaceOp{
        public String replace(String match, String[] groups);
    }

    public static String readAssetFileAsString(Context ctx, String sourceHtmlLocation){
        InputStream is;
        try{
            is = ctx.getAssets().open(sourceHtmlLocation);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer, "UTF-8");
        }catch(Exception e){
            Log.d("Ocelot", "Failed to read asset", e);
        }
        return "";
    }
}
