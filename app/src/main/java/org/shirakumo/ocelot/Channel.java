package org.shirakumo.ocelot;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.FragmentTransaction;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import org.shirakumo.lichat.CL;
import org.shirakumo.lichat.Payload;

import java.io.File;
import java.util.regex.Pattern;

public class Channel extends Fragment{
    public static final String ARG_NAME = "name";
    private static int idCounter = 1000;

    private String name;
    private View view;
    private ChannelListener listener;
    private int outputId = idCounter++;

    public Channel() {
        // Required empty public constructor
    }

    public String replaceEmotes(String text){
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

    public String renderText(String text){
        return replaceEmotes(linkifyURLs(escapeHTML(text)));
    }

    public void showText(String text){
        showText(CL.getUniversalTime(), "System", text);
    }

    public void runScript(String text){
        if(view != null){
            Log.d("ocelot.channel", "Running: "+text);
            ((WebView) view.findViewById(outputId)).loadUrl("javascript:(function(){"+text+"})()");
        }
    }

    public void showHTML(long clock, String from, String html){
        runScript("showText("+clock+", "+Toolkit.prin1(from)+", "+Toolkit.prin1(html)+");");
    }

    public void showText(long clock, String from, String text){
        showHTML(clock, from, renderText(text));
    }

    public void showPayload(long clock, String from, Payload payload){
        File temp = listener.getTempFile(payload.name, Toolkit.fileExtensionForMime(payload.contentType));
        if(temp != null){
            try{
                payload.save(temp);
                String path = Toolkit.prin1("file://"+escapeHTML(temp.getAbsolutePath()));
                if(Toolkit.isImageMime(payload.contentType)) {
                    showHTML(clock, from, "<img class=\"payload\" src="+path+">");
                }else if(Toolkit.isAudioMime(payload.contentType)){
                    showHTML(clock, from, "<audio class=\"payload\" controls src="+path+"><a href="+path+">"+escapeHTML(payload.name)+"</a></audio>");
                }else if(Toolkit.isVideoMime(payload.contentType)) {
                    showHTML(clock, from, "<video class=\"payload\" controls src="+path+"><a href="+path+">"+escapeHTML(payload.name)+"</a></video>");
                }else{
                    Log.w("ocelot.channel", "Cannot show payload of unknown content type "+payload.contentType);
                    temp.delete();
                }
            }catch(Exception ex){
                Log.e("ocelot.channel", "Failed to save payload to "+temp, ex);
            }
        }
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

    public void hide(){
        if(view == null) return;
        view.setVisibility(View.GONE);
    }

    public void show(){
        if(view == null) return;
        view.setVisibility(View.VISIBLE);
        view.findViewById(R.id.input).requestFocus();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_channel, container, false);

        String content = Toolkit.readAssetFileAsString((Context)listener, "channel.html");

        WebView web = v.findViewById(R.id.output);
        web.setId(outputId);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "UTF-8", null);

        EditText input = (EditText) v.findViewById(R.id.input);
        input.setOnEditorActionListener((TextView vw, int actionId, KeyEvent event)->{
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && !event.isShiftPressed() && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                if(listener != null)
                    listener.onInput(this, vw.getText().toString());
                vw.setText("");
                return true;
            }
            return false;
        });

        v.findViewById(R.id.send_file).setOnClickListener((vw)->{
            listener.requestSendFile(this);
        });

        v.findViewById(R.id.select_emote).setOnClickListener((vw)->{
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            EmoteList.newInstance().show(ft, "emotes");
        });

        view = v;
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChannelListener) context;
            listener.registerChannel(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChannelListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface ChannelListener{
        public void onInput(Channel c, String input);
        public void registerChannel(Channel c);
        public void requestSendFile(Channel c);
        public File getEmotePath(String name);
        public File getTempFile(String name, String type);
    }
}
