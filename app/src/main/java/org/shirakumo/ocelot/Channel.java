package org.shirakumo.ocelot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.FragmentTransaction;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import org.shirakumo.lichat.CL;
import org.shirakumo.lichat.Payload;
import org.shirakumo.lichat.updates.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class Channel extends Fragment{
    public static final String ARG_NAME = "name";

    private String name;
    private WebView view;
    private ChannelListener listener;
    private List<Runnable> scheduledFuncs = new ArrayList<>();
    private ArrayList<String> runScripts = new ArrayList<>();

    public Channel() {
        // Required empty public constructor
    }

    public String replaceEmotes(String text){
        if(!listener.getPreferences().getBoolean("show_emotes", true)) return text;

        StringBuilder builder = new StringBuilder();
        int start = 0;
        while(start<text.length() && text.charAt(start) != ':') start++;
        if(start < text.length()){
            builder.append(text, 0, start);
            int end = start+1;
            for(; end<text.length(); end++){
                if(text.charAt(end) == ':'){
                    String emoteName = text.substring(start+1, end);
                    File emote = listener.getEmotePath(emoteName);
                    if(emote != null){
                        builder.append("<img class=\"emote\" src="+Toolkit.prin1("file://"+emote.getAbsolutePath())+">");
                        start = end+1;
                    }else{
                        builder.append(text, start, end);
                        start = end;
                    }
                }
            }
            builder.append(text, start, end);
        }else{
            builder.append(text);
        }
        return builder.toString();
    }

    private static Pattern urlPattern = Pattern.compile("((?:[\\w\\-_]+:\\/\\/)([\\w_\\-]+(?:(?:\\.[\\w_\\-]+)+))(?:[\\w.,@?^=%&:/~+#\\-()]*[\\w@?^=%&/~+#\\-])?)");
    public String linkifyURLs(String text){
        return Toolkit.replaceAll(text, urlPattern, (String match, String[] groups)->{
            String url = groups[0];
            return "<a href=\""+unescapeHTML(url)+"\" class=\"userlink\">"+url+"</a>";
        });
    }

    private static Pattern unescapePattern = Pattern.compile("&([\\w]+);");
    public String unescapeHTML(String text){
        return Toolkit.replaceAll(text, unescapePattern, (String match, String[] groups)->{
            String attr = groups[0];
            if(attr.equals("lt")) return "<";
            if(attr.equals("gt")) return ">";
            if(attr.equals("quot")) return "\"";
            if(attr.equals("amp")) return "&";
            return match;
        });
    }

    private static Pattern escapePattern = Pattern.compile("&([<>\"&\\n]);");
    public String escapeHTML(String text){
        return Toolkit.replaceAll(text, escapePattern, (String match, String[] groups)->{
            String attr = groups[0];
            if(attr.equals("<")) return "&lt;";
            if(attr.equals(">")) return "&gt;";
            if(attr.equals("\"")) return "&quot;";
            if(attr.equals("&")) return "&amp;";
            if(attr.equals("\n")) return "<br>";
            return match;
        });
    }

    public String markSelf(String text){
        String username = listener.getUsername();
        if(username == null) return text;
        StringBuilder buf = new StringBuilder();
        boolean inLink = false;
        int i=0;
        for(; i<text.length()-username.length(); i++){
            if(!inLink){
                CharSequence seq = text.subSequence(i, i+username.length());
                if(seq.equals(username)){
                    buf.append("<mark>"+seq+"</mark>");
                    i += username.length()-1;
                    continue;
                }
                if(text.charAt(i) == '<' && i+1<text.length() && text.charAt(i+1) == 'a'){
                    inLink = true;
                }
            }else if(inLink && text.charAt(i) == '>'){
                inLink = false;
            }
            buf.append(text.charAt(i));
        }
        buf.append(text, i, text.length());
        return buf.toString();
    }

    public String renderText(String text){
        return replaceEmotes(markSelf(linkifyURLs(escapeHTML(text))));
    }

    public void runScript(String text){
        if(view != null){
            Log.d("ocelot.channel", "Running: "+text);
            view.loadUrl("javascript:(function(){"+text+"})()");
            runScripts.add(text);
        }else{
            // Will be run when the webview is done loading.
            scheduledFuncs.add(()->{
                runScript(text);
            });
        }
    }

    public void showHTML(long clock, String from, String html){
        // FIXME: Update channel button to indicate more messages.
        runScript("showText({"
                +"source:"+Toolkit.prin1(from.equals(listener.getUsername())?"self":"other")+","
                +"clock:"+Toolkit.prin1(clock)+","
                +"from:"+Toolkit.prin1(from)+","
                +"text:"+Toolkit.prin1(html)+"});");
    }

    public void showHTML(String text){ showHTML(CL.getUniversalTime(), "System", text); }

    public void showText(String text){
        showText(CL.getUniversalTime(), "System", text);
    }

    public void showText(long clock, String from, String text){
        showHTML(clock, from, renderText(text));
    }

    public void showData(long clock, String from, Data payload){
        String path = Toolkit.prin1(escapeHTML("file://"+escapeHTML(payload.payload)));
        if(!listener.getPreferences().getBoolean("show_data", true)) {
            showHTML(clock, from, "File: <a class=\"payload\" href="+path+">"+payload.filename+"</a>");
        }else if(Toolkit.isImageMime(payload.contentType)) {
            showHTML(clock, from, "<img class=\"payload\" src="+path+">");
        }else if(Toolkit.isAudioMime(payload.contentType)){
            showHTML(clock, from, "<audio class=\"payload\" controls src="+path+"><a href="+path+">"+escapeHTML(payload.filename)+"</a></audio>");
        }else if(Toolkit.isVideoMime(payload.contentType)) {
            showHTML(clock, from, "<video class=\"payload\" controls src="+path+"><a href="+path+">"+escapeHTML(payload.filename)+"</a></video>");
        }else{
            Log.w("ocelot.channel", "Cannot show payload of unknown content type "+payload.contentType);
        }
    }

    public void clear(){
        runScript("clear();");
        runScripts.clear();
    }

    public String getInput(){
        if(view != null) {
            return ((EditText) view.findViewById(R.id.input)).getText().toString();
        }else{
            return "";
        }
    }

    public void setInput(String text){
        if(view != null) {
            EditText input = view.findViewById(R.id.input);
            input.setText("");
            input.append(text);
            input.requestFocus();
        }
    }

    public String getName(){
        return name;
    }

    public static Channel newInstance(String name) {
        Channel fragment = new Channel();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
        }
        listener.registerChannel(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            name = savedInstanceState.getString("name");
        }

        WebView web = (WebView)inflater.inflate(R.layout.fragment_channel, container, false);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                listener.showUrl(request.getUrl());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                listener.showUrl(Uri.parse(url));
                return true;
            }

            @Override
            public void onPageFinished(WebView vw, String url) {
                super.onPageFinished(vw, url);
                view = web;
                for(Runnable func : scheduledFuncs){func.run();}
                scheduledFuncs.clear();
            }
        });

        String content = Toolkit.readAssetFileAsString((Context)listener, "channel.html");
        web.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "UTF-8", null);

        return web;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name);
    }

    public void saveState(File file){
        Log.d("ocelot.channel", name+" saving sate to "+file);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(String script : runScripts) out.write(script);
            out.close();
        }catch(Exception ex){
            Log.w("ocelot.channel", name+" failed to save state to "+file, ex);
        }
    }

    public void loadState(File file){
        Log.d("ocelot.channel", name+" loading sate from "+file);
        try{
            String text = Toolkit.readStringFromFile(file);
            runScript("clear();");
            runScripts.clear();
            runScript(text);
        }catch(Exception ex){
            Log.w("ocelot.channel", name+" failed to load state from "+file, ex);
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        try {
            listener = (ChannelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implement ChannelListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChannelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implement ChannelListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public String toString(){
        return "#<CHANNEL "+name+">";
    }

    public interface ChannelListener{
        public void showUrl(Uri url);
        public SharedPreferences getPreferences();
        public void registerChannel(Channel c);
        public String getUsername();
        public File getEmotePath(String name);
    }
}
