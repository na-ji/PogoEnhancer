package com.mad.pogoenhancer.overlay.elements.joystick;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.gpx.GpxManager;
import com.mad.pogoenhancer.overlay.LocationDialogHandler;
import com.mad.shared.gpx.LatLon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


class GpxListingAdapter extends RecyclerView.Adapter<GpxListingAdapter.GpxViewHolder> {
    private final ArrayList<GpxListingItem> _dataset;
    private final Context _context;
    private final LatLon _SharedLatLonHandle;
    private final GpxRouteHandler _gpxRouteHandler;
    private final OverlayJoystickListener _overlayJoystickListener;

    GpxListingAdapter(Context context, LatLon sharedLatLon, GpxRouteHandler gpxRouteHandler, OverlayJoystickListener overlayJoystickListener) {
        this._dataset = new ArrayList<>();
        this._gpxRouteHandler = gpxRouteHandler;
        this._context = context;
        this._SharedLatLonHandle = sharedLatLon;
        this._overlayJoystickListener = overlayJoystickListener;
        this.updateDataset();
    }

    void updateDataset() {
        GpxManager gpxManager = GpxManager.getInstance(this._context);
        if (gpxManager != null) {
            HashMap<String, List<LatLon>> routes = gpxManager.get_loadedGpxRoutesOneDimension();
            Map<String, Pair<String, List<LatLon>>> nearbyRoutes = GpxManager.getInstance(this._context).getNearbyRoutes();

            this._dataset.clear();
            if (GpxManager.getInstance(this._context).getLatestGmo() != null) {
                this._dataset.add(new GpxListingItem(Constants.NEARBY_STOPS_GPX, this._SharedLatLonHandle));
            } else {
                Toast.makeText(this._context, "Wait a moment for nearby stops to be available.", Toast.LENGTH_LONG).show();
            }

            for (Map.Entry<String, List<LatLon>> entry : routes.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    Logger.pdebug("PogoEnhancerJ", "Empty gpx file");
                    continue;
                }
                this._dataset.add(
                        new GpxListingItem(entry.getKey(), entry.getValue().get(0))
                );
            }

            for (Pair<String, List<LatLon>> nearbyRoute : nearbyRoutes.values()) {
                if (nearbyRoute.second.isEmpty()) {
                    Logger.pdebug("PogoEnhancerJ", "Empty pogo route");
                    continue;
                }
                this._dataset.add(
                        new GpxListingItem(nearbyRoute.first, nearbyRoute.second.get(0))
                );
            }

            Collections.sort(this._dataset, GpxListingItem.getCompByDistance(this._SharedLatLonHandle));
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GpxListingAdapter.GpxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gpx_selection_listing_item, parent, false);

        return new GpxListingAdapter.GpxViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GpxListingAdapter.GpxViewHolder holder, int position) {
        GpxListingItem gpxListingItem = this._dataset.get(position);

        HashMap<String, List<LatLon>> routes = GpxManager.getInstance(this._context).get_loadedGpxRoutesOneDimension();

        List<LatLon> latLons = routes.get(gpxListingItem.getName());
        if (latLons == null && !gpxListingItem.getName().equals(Constants.NEARBY_STOPS_GPX)) {
            // Not a stored route, could still be a route of pogo nearby...
            Map<String, Pair<String, List<LatLon>>> nearbyRoutes = GpxManager.getInstance(this._context).getNearbyRoutes();
            for (Pair<String, List<LatLon>> nearbyRoute : nearbyRoutes.values()) {
                if (nearbyRoute.first.equals(gpxListingItem.getName())) {
                    latLons = nearbyRoute.second;
                    break;
                }
            }

            if (latLons == null) {
                Toast.makeText(this._context, "Could not load route.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        holder.gpxName.setText(gpxListingItem.getName());
        if (gpxListingItem.getDistance(this._SharedLatLonHandle) >= 1000) {
            holder.distanceText.setText(
                    String.format(Locale.ENGLISH, "%.0fkm",
                            gpxListingItem.getDistance(this._SharedLatLonHandle) / 1000)
            );
        } else {
            holder.distanceText.setText(
                    String.format(Locale.ENGLISH, "%.0fm",
                            gpxListingItem.getDistance(this._SharedLatLonHandle))
            );
        }

        if (latLons != null && latLons.size() == 1) {
            holder.gpxType.setImageResource(R.drawable.ic_location);
        } else {
            holder.gpxType.setImageResource(R.drawable.ic_route);
        }

        long now = System.currentTimeMillis();
        double distance = gpxListingItem.getDistance(this._SharedLatLonHandle) / 1000;
        long cooldownInMinutes = Constants.calculateRemainingCooldown(now / 1000, distance) / 60;

        holder.gpxCooldown.setText(String.format(Locale.ENGLISH, "%d mins", cooldownInMinutes));
        final List<LatLon> finalLatLons = latLons;
        holder.gpxViewItem.setOnClickListener(v -> LocationDialogHandler.showMoveToLocationDialog(
                _context,
                _overlayJoystickListener.getOverlayManager(),
                _SharedLatLonHandle,
                finalLatLons,
                gpxListingItem.getName(),
                _gpxRouteHandler,
                this._overlayJoystickListener::closeGpxSelectionDialog));
    }


    @Override
    public int getItemCount() {
        return this._dataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class GpxViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout gpxViewItem;

        public TextView gpxName;
        public TextView distanceText;
        public ImageView gpxType;
        public TextView gpxCooldown;

        public GpxViewHolder(LinearLayout v) {
            super(v);
            gpxViewItem = v;

            gpxType = v.findViewById(R.id.gpx_type);
            gpxName = v.findViewById(R.id.gpx_name);
            distanceText = v.findViewById(R.id.gpx_distance);
            gpxCooldown = v.findViewById(R.id.gpx_cooldown);
        }
    }
}
