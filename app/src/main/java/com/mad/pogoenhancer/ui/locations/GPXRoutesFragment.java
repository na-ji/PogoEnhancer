package com.mad.pogoenhancer.ui.locations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mad.pogoenhancer.R;


public class GPXRoutesFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1424;
    private View rootFragment = null;

    public static GPXRoutesFragment newInstance() {
        GPXRoutesFragment fragment = new GPXRoutesFragment();
        Bundle bundle = new Bundle();
        //bundle.putInt(ARG_SECTION_NUMBER, 0);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST) {
            // Got a file URI...
            if (rootFragment == null || data == null || data.getData() == null) {
                return;
            }

            Uri file = data.getData();

            EditText viewById = rootFragment.findViewById(R.id.overlay_gpx_uri_input);
            String src = file.getPath();
            viewById.setText(src);

        }
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
        rootFragment = inflater.inflate(R.layout.fragment_favourite_places, container, false);

        final Button browseFilesButton = rootFragment.findViewById(R.id.overlay_gpx_file_browse);
        browseFilesButton.setOnClickListener(v -> {
            Context context = this.getContext();
            if (context == null) {
                return;
            }
            /*Intent fileChooserIntent = new Intent(context, FileChooser.class);
            fileChooserIntent.putExtra(com.aditya.filebrowser.Constants.ALLOWED_FILE_EXTENSIONS, "gpx");
            fileChooserIntent.putExtra(com.aditya.filebrowser.Constants.SELECTION_MODE, com.aditya.filebrowser.Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
            startActivityForResult(fileChooserIntent, PICK_FILE_REQUEST);*/
            // TODO...
        });

        return rootFragment;
    }
}
