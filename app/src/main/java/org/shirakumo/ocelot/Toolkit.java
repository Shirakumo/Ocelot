package org.shirakumo.ocelot;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
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
}
