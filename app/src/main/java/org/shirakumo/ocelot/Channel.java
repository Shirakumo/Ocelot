package org.shirakumo.ocelot;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.FragmentTransaction;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.util.Log;
import org.shirakumo.lichat.CL;

public class Channel extends Fragment  implements EditText.OnEditorActionListener{
    public static final String ARG_NAME = "name";
    private static int idCounter = 1000;

    private String name;
    private View view;
    private ChannelListener listener;
    private int outputId = idCounter++;

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
        if(view != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(outputId, Output.newInstance(CL.universalToUnix(clock), from, text));
            ft.commit();
        }
    }

    public String getName(){
        return name;
    }
    public View getView() { return view; }

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
        v.findViewById(R.id.output).setId(outputId);

        EditText input = (EditText) v.findViewById(R.id.input);
        input.setOnEditorActionListener(this);

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
    }
}
