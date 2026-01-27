package com.mad.pogoenhancer.ui.favouritePlaces;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.gpx.GpxManager;
import com.mad.shared.gpx.LatLon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GpxManagerListingAdapter extends RecyclerView.Adapter<GpxManagerListingAdapter.GpxViewHolder> {
    private ArrayList<String> mDataset;
    private Context _context;

    public GpxManagerListingAdapter(Context context) {
        this._context = context;
        Logger.pdebug("ProtoHookJ", "Setting up GpxManagerListing");
        this.updateDataset();
    }


    @Override
    public GpxManagerListingAdapter.GpxViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gpx_manager_listing_item, parent, false);

        return new GpxManagerListingAdapter.GpxViewHolder(v);
    }

    public void updateDataset() {
        Logger.pdebug("ProtoHookJ", "Updating GpxManagerListing");
        GpxManager gpxManager = GpxManager.getInstance(this._context);
        if (gpxManager != null) {
            mDataset = new ArrayList<>(gpxManager.getCurrentlyLoadedRoutes().getRoutes().keySet());
            Collections.sort(mDataset);
        }
        Logger.pdebug("ProtoHookJ", "Updating GpxManagerListing calling set change");
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull GpxViewHolder holder, int position) {
        GpxManager gpxManager = GpxManager.getInstance(this._context);
        String gpxName = mDataset.get(position);

        HashMap<String, List<LatLon>> routes = gpxManager.get_loadedGpxRoutesOneDimension();
        List<LatLon> latLons = routes.get(gpxName);
        int wayCount = latLons.size();

        holder.gpxName.setText(gpxName);
        if (wayCount == 1) {
            holder.gpxType.setImageResource(R.drawable.ic_location);
        } else {
            holder.gpxType.setImageResource(R.drawable.ic_route);
        }


        // add a delete dialog
        holder.deleteGpxIcon.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this._context);
            builder.setTitle("Delete route");
            builder.setMessage("Do you really want to delete " + gpxName + "?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                gpxManager.removeRoute(gpxName);
                this.updateDataset();
                dialog.dismiss();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog alertDialog = builder.create();
            /*int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
            }
            alertDialog.getWindow().setType(LAYOUT_FLAG);*/
            alertDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return this.mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class GpxViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout gpxViewItem;

        public TextView gpxName;
        public ImageView deleteGpxIcon;
        public ImageView gpxType;

        public GpxViewHolder(LinearLayout v) {
            super(v);
            gpxViewItem = v;

            gpxName = v.findViewById(R.id.gpx_name);
            deleteGpxIcon = v.findViewById(R.id.gpx_delete);
            gpxType = v.findViewById(R.id.gpx_type);
        }
    }
}
