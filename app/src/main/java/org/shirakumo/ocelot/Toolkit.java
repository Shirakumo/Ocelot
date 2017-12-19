package org.shirakumo.ocelot;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Toolkit {
    public static String join(Iterable strings, String delim, int from, int to){
        StringBuilder build = new StringBuilder();
        Iterator e = strings.iterator();
        for(int i=0; i<from; i++){
            if(!e.hasNext()) return "";
            e.next();
        }
        for(int i=from; i<to; i++){
            build.append(e.next());
            if(e.hasNext()) build.append(delim);
            else break;
        }
        return build.toString();
    }

    public static String join(Iterable strings, String delim, int from){
        return join(strings, delim, from, Integer.MAX_VALUE);
    }

    public static String join(Iterable strings, String delim){
        return join(strings, delim, 0, Integer.MAX_VALUE);
    }

    public static String join(Object[] strings, String delim, int from, int to){
        return join(Arrays.asList(strings), delim, from, to);
    }

    public static String join(Object[] strings, String delim, int from){
        return join(Arrays.asList(strings), delim, from, Integer.MAX_VALUE);
    }

    public static String join(Object[] strings, String delim){
        return join(Arrays.asList(strings), delim, 0, Integer.MAX_VALUE);
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
        try{
            InputStream is = ctx.getAssets().open(sourceHtmlLocation);
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

    public static String readStringFromFile(File file) throws IOException{
        InputStream is = new FileInputStream(file);
        int size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        return new String(buffer, "UTF-8");
    }

    public static void writeStringToFile(String string, File file) throws IOException{
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(string);
        out.close();
    }

    public static void deleteDirectoryTree(File root){
        List<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(root.listFiles()));
        while(files.size() != 0){
            File file = files.remove(files.size()-1);
            if(file.isDirectory()){
                files.addAll(Arrays.asList(file.listFiles()));
            }else{
                file.delete();
            }
        }
    }

    public static <T> T find(T object, T[] items){
        for(T item : items){
            if(object.equals(item)) return item;
        }
        return null;
    }

    private static final String[] imageMimes = new String[]{
            "image/gif", "image/png", "image/svg+xml", "image/jpeg"};
    public static boolean isImageMime(String type){ return find(type, imageMimes) != null; }

    private static final String[] audioMimes = new String[]{
            "audio/wave", "audio/wav", "audio/x-wav", "audio/x-pn-wav", "audio/vnd", "audio/webm", "audio/ogg", "audio/mpeg", "audio/mp3", "audio/mp4", "audio/flac"};
    public static boolean isAudioMime(String type){ return find(type, audioMimes) != null; }

    private static final String[] videoMimes = new String[]{
            "video/webm", "video/mp4", "video/mp4", "application/ogg"};
    public static boolean isVideoMime(String type){ return find(type, videoMimes) != null; }

    public static String fileExtensionForMime(String mime){
        // Images
        if(mime.equals("image/gif")) return "gif";
        if(mime.equals("image/png")) return "png";
        if(mime.equals("image/svg+xml")) return "svg";
        if(mime.equals("image/jpeg")) return "jpg";
        // Audio
        if(mime.equals("audio/wave")) return "wav";
        if(mime.equals("audio/wav")) return "wav";
        if(mime.equals("audio/x-wav")) return "wav";
        if(mime.equals("audio/x-pn-wav")) return "wav";
        if(mime.equals("audio/vnd.wave")) return "wav";
        if(mime.equals("audio/webm")) return "webm";
        if(mime.equals("audio/ogg")) return "oga";
        if(mime.equals("audio/mpeg")) return "mpg";
        if(mime.equals("audio/mp3")) return "mp3";
        if(mime.equals("audio/mp4")) return "mp4";
        if(mime.equals("audio/flac")) return "flac";
        // Video
        if(mime.equals("video/webm")) return "webm";
        if(mime.equals("video/mp4")) return "mp4";
        if(mime.equals("video/ogg")) return "ogv";
        if(mime.equals("application/ogg")) return "ogg";
        return "dat";
    }

    public static String mimeForFileExtension(String ext){
        // Images
        if(ext.equals("gif")) return "image/gif";
        if(ext.equals("png")) return "image/png";
        if(ext.equals("svg")) return "image/svg+xml";
        if(ext.equals("jpg")) return "image/jpeg";
        // Audio
        if(ext.equals("wav")) return "audio/wav";
        if(ext.equals("webm")) return "audio/webm";
        if(ext.equals("oga")) return "audio/ogg";
        if(ext.equals("mpg")) return "audio/mpeg";
        if(ext.equals("mp3")) return "audio/mp3";
        if(ext.equals("mp4")) return "audio/mp4";
        if(ext.equals("flac")) return "audio/flac";
        // Video
        if(ext.equals("webm")) return "video/webm";
        if(ext.equals("mp4")) return "video/mp4";
        if(ext.equals("ogv")) return "video/ogg";
        if(ext.equals("ogg")) return "application/ogg";
        return "application/octet-stream";
    }

    public static String getFileName(File file){
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if(0 < pos) return name.substring(0, pos);
        return name;
    }

    public static String getFileType(File file){
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if(0 < pos) return name.substring(1+pos);
        return "";
    }

    public static String getUriFileName(ContentResolver resolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = resolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String prin1(Object object){
        if(object instanceof String){
            StringBuilder out = new StringBuilder();
            String str = (String)object;
            out.append('"');
            for(int i=0; i<str.length(); i++){
                char c = str.charAt(i);
                if(c == '"' || c == '\\') out.append('\\');
                out.append(c);
            }
            out.append('"');
            return out.toString();
        }else if(object instanceof Number){
            return ""+object;
        }else{
            return object.toString();
        }
    }

    public static int getColor(SharedPreferences prefs, Resources.Theme theme, String key, int defAttr){
        int c = prefs.getInt(key, -1);
        if(c == -1){
            TypedValue tv = new TypedValue();
            theme.resolveAttribute(defAttr, tv, true);
            if(tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT){
                c = tv.data;
            }else{
                c = 0;
            }
        }
        return c;
    }

    public static String getColorHex(SharedPreferences prefs, Resources.Theme theme, String key, int defAttr){
        return String.format("#%06X", (0xFFFFFF & getColor(prefs, theme, key, defAttr)));
    }

    public static boolean mentionsUser(String text, String user){
        return Pattern.compile("\\b"+Pattern.quote(user)+"\\b").matcher(text).find();
    }
}
