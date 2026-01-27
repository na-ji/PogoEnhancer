package com.mad.pogoenhancer.ui.locations;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mad.pogoenhancer.R;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class LocationsFragment extends Fragment {

    private View rootFragment = null;

    public static LocationsFragment newInstance() {
        LocationsFragment fragment = new LocationsFragment();
        Bundle bundle = new Bundle();
        //bundle.putInt(ARG_SECTION_NUMBER, 0);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);*/
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootFragment = inflater.inflate(R.layout.fragment_favourite_locations, container, false);

        EditText locationInput = rootFragment.findViewById(R.id.favourite_locations_location_input);

        final Button pasteButton = rootFragment.findViewById(R.id.favourite_locations__paste);
        pasteButton.setOnClickListener(v -> {
            Context context = this.getContext();
            if (context == null) {
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.getPrimaryClipDescription() != null
                    && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)
                    && clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String yourText = item.getText().toString();
                locationInput.setText(yourText);
            }
        });

        return rootFragment;
    }
}
