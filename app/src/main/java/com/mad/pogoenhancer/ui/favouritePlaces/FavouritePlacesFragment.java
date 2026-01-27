package com.mad.pogoenhancer.ui.favouritePlaces;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.gpx.GpxManager;
import com.mad.pogoenhancer.gpx.GpxUtil;
import com.mad.shared.gpx.LatLon;

import java.io.File;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;

import static android.app.Activity.RESULT_OK;
import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FavouritePlacesFragment extends Fragment {

    private final static int GPX_FILE_PICKER_RESULT = 1001;

    private FavouritePlacesViewModel favouritePlacesViewModel;
    private LayoutInflater _layoutInflater = null;

    private EditText urlInput = null;
    private GpxManager _gpxManager = null;
    private AlertDialog _newGpxNameDialog = null;

    private EditText singlePointName = null;
    private EditText singlePointLocation = null;

    private RecyclerView _gpxManagerListingView;
    private GpxManagerListingAdapter _gpxManagerListingAdapter;
    private RecyclerView.LayoutManager _layoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favouritePlacesViewModel = new ViewModelProvider(this).get(FavouritePlacesViewModel.class);

        Context context = this.getContext();
        if (context != null) {
            _layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            SharedPreferences sharedPreferences = PreferenceManager.
                    getDefaultSharedPreferences(context);
            this._gpxManager = GpxManager.getInstance(this.getContext());
        }

        View root = inflater.inflate(R.layout.fragment_favourite_places, container, false);

        urlInput = root.findViewById(R.id.overlay_gpx_uri_input);

        singlePointName = root.findViewById(R.id.overlay_single_point_name_input);
        singlePointLocation = root.findViewById(R.id.overlay_single_point_coord_input);

        Button pasteButton = root.findViewById(R.id.overlay_gpx_uri_paste);
        pasteButton.setOnClickListener(v -> pasteUrlToInput());

        Button fileBrowserButton = root.findViewById(R.id.overlay_gpx_file_browse);
        fileBrowserButton.setOnClickListener(v -> openFileBrowser());

        Button addInputToGpx = root.findViewById(R.id.overlay_gpx_add);
        addInputToGpx.setOnClickListener(v -> readTextInputToGpx());

        Button addSinglePoint = root.findViewById(R.id.overlay_single_point_add);
        addSinglePoint.setOnClickListener(v -> addSinglePointToGpx());



        _gpxManagerListingView = root.findViewById(R.id.gpx_imported_recycler_view);
        _gpxManagerListingView.setHasFixedSize(true);
        // use a linear layout manager
        _layoutManager = new LinearLayoutManager(context);
        _gpxManagerListingView.setLayoutManager(_layoutManager);

        // specify an adapter (see also next example)
        _gpxManagerListingAdapter = new GpxManagerListingAdapter(context);
        _gpxManagerListingView.setAdapter(_gpxManagerListingAdapter);

        return root;
    }

    private void pasteUrlToInput() {
        if (this.getContext() == null) return;
        ClipboardManager clipboard = (ClipboardManager) this.getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.getPrimaryClipDescription() != null
                && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)
                && clipboard.getPrimaryClip() != null) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String yourText = item.getText().toString();
            this.urlInput.setText(yourText);
        }
    }

    private void openFileBrowser() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Set your required file type
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS); //optional
        startActivityForResult(Intent.createChooser(intent, "GPX Picker"), GPX_FILE_PICKER_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPX_FILE_PICKER_RESULT && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            Logger.pdebug("PogoEnhancerJ", selectedfile.getPath());

            Context ctx = this.getContext();
            if (ctx == null) {
                return;
            }
            // try dumping some content..
            Cursor returnCursor =
                    this.getContext().getContentResolver().query(selectedfile, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            this.urlInput.setText(returnCursor.getString(nameIndex));
            returnCursor.close();

            Gpx gpxRead = GpxUtil.readToGpx(this.getContext(), selectedfile);
            if (gpxRead == null) {
                Logger.pdebug("PogoEnhancerJ", "Failed parsing GPX: " + selectedfile);
                return;
            }
            addGpxToManager(gpxRead);
        }
    }

    private void readTextInputToGpx() {
        GPXParser gpxParser = new GPXParser();
        String url = urlInput.getText().toString();
        gpxParser.parse(url, gpx -> {
            Context ctx = this.getContext();
            if (ctx == null || _layoutInflater == null) {
                Logger.pdebug("PogoEnhancerJ",
                        "Cannot show further information, context is null");
            } else if (gpx == null) {
                // error parsing track
                Logger.pdebug("PogoEnhancerJ",
                        "Could not parse remote GPX file at " + url);
                Toast.makeText(this.getContext(),
                        "Failed parsing GPX from " + url,
                        Toast.LENGTH_LONG).show();
            } else {
                // do something with the parsed track
                // see included example app and tests
                addGpxToManager(gpx);
            }
        });
    }

    private void addGpxToManager(Gpx gpx) {
        if (gpx == null) {
            return;
        }
        List<LatLon> latLons = GpxUtil.transformAllSpotsOfGpxToOneDimension(gpx);
        if (latLons.isEmpty()) {
            Toast.makeText(this.getContext(),
                    "Failed importing GPX: no coordinates could be read",
                    Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Add route");

        LinearLayout dialogNameInputLayout = (LinearLayout) _layoutInflater.inflate(R.layout.dialog_name_input, null);

        EditText dialogNameInputText = dialogNameInputLayout.findViewById(R.id.dialog_name_input_edit);

        builder.setView(dialogNameInputLayout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = dialogNameInputText.getText().toString();
            if (name.equals(Constants.NEARBY_STOPS_GPX)) {
                Toast.makeText(this.getContext(),
                        name + " could not be added. Not allowed for a name.",
                        Toast.LENGTH_LONG).show();
            } else if (this._gpxManager.addRoute(name, gpx)) {
                Toast.makeText(this.getContext(),
                        "GPX added",
                        Toast.LENGTH_LONG).show();
                this._gpxManagerListingAdapter.updateDataset();
            } else {
                Toast.makeText(this.getContext(),
                        "GPX with name " + name + " could not be added. Already present.",
                        Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        _newGpxNameDialog = builder.create();
        _newGpxNameDialog.show();
    }

    public void addSinglePointToGpx(){
        String locationPoint = singlePointLocation.getText().toString().replaceAll("\\s+","");
        String locationName = singlePointName.getText().toString();
        if(locationName == null || locationName.isEmpty()) {
            Toast.makeText(this.getContext(),
                    "No Location Name is set",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (locationPoint == null || locationPoint.isEmpty() || !locationPoint.matches("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$")) {
            Toast.makeText(this.getContext(),
                    "No Location is set or wrong format (0.0, 0.0)",
                    Toast.LENGTH_LONG).show();
            return;

        }
        String[] latlong =  locationPoint.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);

        LatLon location = new LatLon(latitude, longitude);

        File externalStorage = Environment.getExternalStorageDirectory();
        String tmpFile = externalStorage.getAbsolutePath() + "/tmpgpx.gpx";
        Uri tmpFileUri = Uri.parse("file:///" + tmpFile);
        File tmpGpxFile = new File(tmpFile);

        if (GpxUtil.generateGfx(tmpGpxFile, locationName, location)) {
            Gpx gpxRead = GpxUtil.readToGpx(this.getContext(), tmpFileUri);
            this._gpxManager.addRoute(locationName, gpxRead);
            this._gpxManagerListingAdapter.updateDataset();
            tmpGpxFile.delete();
            singlePointLocation.getText().clear();
            singlePointName.getText().clear();
            Toast.makeText(this.getContext(),
                    "Location added",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.getContext(),
                    "Something went wrong",
                    Toast.LENGTH_LONG).show();
            return;
        };
    }



}