package org.shirakumo.ocelot;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class About extends DialogFragment {
    public About() {
        // Required empty public constructor
    }

    public static About newInstance() {
        return new About();
    }

    public void onResume(){
        super.onResume();
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((TextView)view.findViewById(R.id.about_version)).setText(BuildConfig.VERSION_NAME);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ((TextView) view.findViewById(R.id.about_text)).setText(Html.fromHtml(getString(R.string.app_description), Html.FROM_HTML_MODE_COMPACT));
        }else{
            ((TextView) view.findViewById(R.id.about_text)).setText(Html.fromHtml(getString(R.string.app_description)));
        }
        return view;
    }
}
