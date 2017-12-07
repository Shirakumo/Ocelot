package org.shirakumo.ocelot;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.FragmentTransaction;
import org.shirakumo.lichat.CL;


public class Channel extends Fragment {
    private static final String ARG_NAME = "name";

    private String name;

    private OnFragmentInteractionListener mListener;

    public Channel() {
        // Required empty public constructor
    }

    public void showText(String text){
        showText(CL.getUniversalTime(), "System", text);
    }

    public void showText(long clock, String from, String text){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        int id = getView().findViewById(R.id.output).getId();
        ft.add(id, Output.newInstance(CL.universalToUnix(clock), from, text));
        ft.commit();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
