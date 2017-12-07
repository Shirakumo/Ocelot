package org.shirakumo.ocelot;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentTransaction;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.util.Log;
import org.shirakumo.lichat.CL;

public class Channel extends Fragment  implements EditText.OnEditorActionListener{
    private static final String ARG_NAME = "name";

    private String name;
    private int outputID = -1;
    private ChannelListener listener;

    public Channel() {
        // Required empty public constructor
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && !event.isShiftPressed() && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
            if(listener != null)
                listener.onInput(this, v.getText().toString());
            v.setText("");
            return true;
        }
        return false;
    }

    public void showText(String text){
        showText(CL.getUniversalTime(), "System", text);
    }

    public void showText(long clock, String from, String text){
        if(outputID != -1) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(outputID, Output.newInstance(CL.universalToUnix(clock), from, text));
            ft.commit();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_channel, container, false);
        outputID = v.findViewById(R.id.output).getId();

        EditText input = (EditText) v.findViewById(R.id.input);
        input.setOnEditorActionListener(this);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChannelListener) context;
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
    }
}
