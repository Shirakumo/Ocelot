package org.shirakumo.ocelot;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Output extends Fragment {
    private static final String ARG_TIME = "time";
    private static final String ARG_AUTHOR = "author";
    private static final String ARG_TEXT = "text";

    private long time;
    private String author;
    private String text;
    private OnFragmentInteractionListener mListener;

    public Output() {
        // Required empty public constructor
    }

    public static Output newInstance(long time, String author, String text) {
        Output fragment = new Output();
        Bundle args = new Bundle();
        args.putLong(ARG_TIME, time);
        args.putString(ARG_AUTHOR, author);
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    public String renderTimestamp(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(timestamp*1000L));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            time = getArguments().getLong(ARG_TIME);
            author = getArguments().getString(ARG_AUTHOR);
            text = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_output, container, false);
        ((TextView)v.findViewById(R.id.time)).setText(renderTimestamp(time));
        ((TextView)v.findViewById(R.id.author)).setText(author);
        ((TextView)v.findViewById(R.id.content)).setText(text);
        return v;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
