package org.shirakumo.ocelot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class DynamicListPreference extends DialogPreference {
    private Set<String> entries = new TreeSet<>();

    public DynamicListPreference(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public DynamicListPreference(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DynamicListPreference);
        int entriesId = ta.getResourceId(R.styleable.DynamicListPreference_entries, -1);
        if(entriesId != -1){
            entries.addAll(Arrays.asList(context.getResources().getStringArray(entriesId)));
        }
        ta.recycle();

        setDialogLayoutResource(R.layout.dynamic_list_preference);
    }

    private void addEntry(LinearLayout view, String entry){
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View entryView = li.inflate(R.layout.dynamic_list_preference_entry, null);

        ((TextView)entryView.findViewById(R.id.entry)).setText(entry);
        entryView.findViewById(R.id.remove_entry).setOnClickListener((vw)->{
            entries.remove(entry);
            view.removeView(entryView);
        });
        view.addView(entryView);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Button add_entry = view.findViewById(R.id.add_entry);
        EditText entry_name = view.findViewById(R.id.new_entry);
        LinearLayout entries = view.findViewById(R.id.entries);

        entry_name.setOnEditorActionListener((TextView vw, int actionId, KeyEvent event)->{
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && !event.isShiftPressed() && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                add_entry.callOnClick();
                return true;
            }
            return false;
        });

        add_entry.setOnClickListener((vw)->{
            String channel = entry_name.getText().toString();
            if(!channel.isEmpty()){
                entry_name.setText("");
                if(this.entries.add(channel))
                    addEntry(entries, channel);
            }
        });

        for(String entry : this.entries){
            addEntry(entries, entry);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putStringSet(getKey(), entries);
        edit.commit();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        entries.clear();
        if (restorePersistedValue) {
            entries.addAll(getSharedPreferences().getStringSet(getKey(), new TreeSet<>()));
        } else {
            entries.addAll((Set<String>)defaultValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getTextArray(index);
    }
}
