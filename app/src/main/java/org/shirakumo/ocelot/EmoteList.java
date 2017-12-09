package org.shirakumo.ocelot;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.shirakumo.lichat.Payload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmoteList extends DialogFragment {

    EmoteListListener listener;
    View view;
    List<PayloadView> images = new ArrayList<>();

    public EmoteList() {
        // Required empty public constructor
    }

    public static EmoteList newInstance() {
        EmoteList fragment = new EmoteList();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    public void onResume(){
        super.onResume();
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_emote_list, container, false);
        GridView grid = view.findViewById(R.id.emotes);
        grid.setAdapter(new GridViewAdapter());

        if(listener != null) showEmotes(listener.getEmotes());
        return view;
    }

    public void showEmotes(Map<String, Payload> emotes){
        String[] names = new String[emotes.size()];
        names = emotes.keySet().toArray(names);
        Arrays.sort(names);
        for(String name : names){
            // FIXME: cache views
            PayloadView emote = new PayloadView(view.getContext(), emotes.get(name));
            emote.setOnClickListener((v)->{
                listener.emoteChosen(name);
                dismiss();
            });
            images.add(emote);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EmoteListListener) context;
            if(view != null) showEmotes(listener.getEmotes());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement EmoteListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface EmoteListListener{
        public void emoteChosen(String emote);
        public Map<String,Payload> getEmotes();
    }

    private class GridViewAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return images.get(position);
        }
    }
}
