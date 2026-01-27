package com.mad.pogoenhancer;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonIdPreferenceListPicker extends MultiSelectListPreference {
    String[] selectionArgs = new String[]{"1"};
    private Context ctx;

    void selectAll() {
        Set<String> newVals = new HashSet<>();
        for (CharSequence entryValue : getEntryValues()) {
            newVals.add(entryValue.toString());
        }

        setValues(newVals);
    }

    void deselectAll() {
        Set<String> newVals = new HashSet<>();
        setValues(newVals);
    }

    public MonIdPreferenceListPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;


        List<CharSequence> entries = new ArrayList<CharSequence>();
        List<CharSequence> entriesValues = new ArrayList<CharSequence>();

        for (int i = 1; i <= 1010; i++) {
            String monName = (String) context.getResources().getText(
                    context.getResources().getIdentifier(
                            "pokemon_" + i,
                            "string", "com.mad.pogoenhancer"
                    ));
            String display = i + " " + monName;
            String value = Integer.toString(i);

            entries.add(display);
            entriesValues.add(value);
        }

        setEntries(entries.toArray(new CharSequence[]{}));
        setEntryValues(entriesValues.toArray(new CharSequence[]{}));

    }
}
