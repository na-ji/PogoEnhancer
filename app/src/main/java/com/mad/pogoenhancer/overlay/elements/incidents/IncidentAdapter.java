package com.mad.pogoenhancer.overlay.elements.incidents;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.LocationDialogHandler;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import POGOProtos.Rpc.ClientMapCellProto;
import POGOProtos.Rpc.EnumWrapper;
import POGOProtos.Rpc.GetMapObjectsOutProto;
import POGOProtos.Rpc.IncidentDisplayType;
import POGOProtos.Rpc.PokemonFortProto;
import POGOProtos.Rpc.PokestopIncidentDisplayProto;


public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {
    public Context _serviceContext;
    private final ArrayList<IncidentListItem> mDataset;
    private final LatLon _SharedLatLonHandle;
    private final OverlayManager _SupervisingManager;
    private final SharedPreferences _SharedPreferences;

    public IncidentAdapter(LatLon sharedLatLon, Context context, OverlayManager supervisingManager) {
        this.mDataset = new ArrayList<>();
        this._SharedLatLonHandle = sharedLatLon;
        this._serviceContext = context;
        this._SupervisingManager = supervisingManager;
        this._SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Create new views (invoked by the layout_old manager)
    @NotNull
    @Override
    public IncidentViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.incident_list_item, parent, false);

        return new IncidentViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout_old manager)
    @Override
    public void onBindViewHolder(@NotNull IncidentAdapter.IncidentViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//            holder.textView.setText(mDataset.get(position).getMonName());
        holder.resetIcons();
        IncidentListItem incidentListItem = mDataset.get(position);

        IncidentViewType incidentViewType = IncidentViewType.NONE;
        List<PokestopIncidentDisplayProto> pokestopDisplaysList = incidentListItem.getRepresentedElement().getPokestopDisplaysList();
        for (PokestopIncidentDisplayProto display : pokestopDisplaysList) {
            holder.setTimeRemaining(display.getIncidentExpirationMs());

            if (display.getIncidentCompleted() || display.hasInvasionFinished()
                    && display.getInvasionFinished().getStyle()
                    == EnumWrapper.PokestopStyle.POKESTOP_ROCKET_VICTORY) {
                incidentViewType = IncidentViewType.FINISHED;
                break;
            } else if (display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GIOVANNI) {
                incidentViewType = IncidentViewType.GIOVANNI;
                // TODO: Display display.getIncidentExpirationMs()
                holder.setIcon(this._serviceContext, display.getCharacterDisplay().getCharacter());
                break;
            } else if (display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_LEADER) {
                incidentViewType = IncidentViewType.LEADER;
                holder.setIcon(this._serviceContext, display.getCharacterDisplay().getCharacter());
            } else if ((display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GRUNT
                || display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GRUNTB)
                    && incidentViewType != IncidentViewType.LEADER) {
                incidentViewType = IncidentViewType.GRUNT;
                holder.setIcon(this._serviceContext, display.getCharacterDisplay().getCharacter());
            }
        }

        // TODO: Populate with all grunt icons...
        if (incidentViewType == IncidentViewType.FINISHED) {
            holder.incidentViewItem.setBackgroundColor(Color.LTGRAY);
        } else if (incidentViewType == IncidentViewType.GRUNT) {
            holder.incidentViewItem.setBackgroundColor(Color.WHITE);
        } else if (incidentViewType == IncidentViewType.LEADER) {
            holder.incidentViewItem.setBackgroundColor(Color.YELLOW);
        } else if (incidentViewType == IncidentViewType.GIOVANNI) {
            holder.incidentViewItem.setBackgroundColor(Color.RED);
        }


        double distance = incidentListItem.getDistance(this._SharedLatLonHandle);
        if (distance < 0.001) {
            holder.distance.setText("0m");
        } else {
            //holder.distance.setText(String.format(Locale.ENGLISH, "%.0f", distance) + "m");

            holder.distance.setText(String.format(Locale.ENGLISH, "%.0fm", distance));
        }
        holder.itemView.setOnClickListener(v -> {
            if (!_SharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED,
                    Constants.DEFAULT_VALUES.SPOOFING_ENABLED)) {
                DecimalFormat df = new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(8);
                LatLon location = incidentListItem.get_Location();


                if (_SharedPreferences.getBoolean(
                        Constants.SHAREDPERFERENCES_KEYS.OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY,
                        Constants.DEFAULT_VALUES.OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY
                )) {
                    ClipboardManager clipboard = (ClipboardManager) _serviceContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    String clipboardContent = String.format("%s, %s", df.format(location.getLat()), df.format(location.getLon()));
                    ClipData clip = ClipData.newPlainText("Nearby coords", clipboardContent);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(_serviceContext,
                            "Copied coords of incident to clipboard.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(_serviceContext);
                    alertDialogBuilder.setTitle("Move to...");

                    // TODO: view approx. travel time
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Do you want to open GMaps with the location of the incident?")
                            .setCancelable(false)
                            .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, id) -> {
                                dialog.dismiss();
                                //_SharedLatLonHandle.setLat(monLoc.getLat());
                                //_SharedLatLonHandle.setLon(monLoc.getLon());

                                String urlString = String.format("http://maps.google.com/maps?daddr=%s,%s", df.format(location.getLat()), df.format(location.getLon()));

                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(urlString));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                                _serviceContext.startActivity(intent);
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    int LAYOUT_FLAG;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                    }

                    alertDialog.getWindow().setType(LAYOUT_FLAG);
                    // show it
                    alertDialog.show();
                }
            } else {
                LocationDialogHandler.showTeleportOrWalkDialog(_serviceContext,
                        _SupervisingManager,
                        _SharedLatLonHandle,
                        incidentListItem.get_Location(),
                        "the incident",
                        null);
            }

        });
    }

    // Return the size of your dataset (invoked by the layout_old manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected void updateDataset(GetMapObjectsOutProto gmoReceived) {
        if (gmoReceived == null) {
            return;
        }
        List<ClientMapCellProto> mapCellsList = gmoReceived.getMapCellList();

        ArrayList<IncidentListItem> toBeDeletedFromDataset = new ArrayList<>();
        Map<String, PokemonFortProto> allFortsOfGmoWithIncidents = new HashMap<>();

        boolean gruntDisplayEnabled = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_GRUNTS,
                Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_GRUNTS);
        boolean leaderDisplayEnabled = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_LEADERS,
                Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_LEADERS);
        boolean giovanniDisplayEnabled = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.INCIDENT_DISPLAY_GIOVANNI,
                Constants.DEFAULT_VALUES.INCIDENT_DISPLAY_GIOVANNI);

        for (ClientMapCellProto cell : mapCellsList) {
            List<PokemonFortProto> fortsList = cell.getFortList();
            for (PokemonFortProto fort : fortsList) {
                List<PokestopIncidentDisplayProto> pokestopDisplaysList = fort.getPokestopDisplaysList();
                boolean notToBeDisplayed = false;
                for (PokestopIncidentDisplayProto display : pokestopDisplaysList) {
                    if (display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_NONE
                            || display.getIncidentCompleted()) {
                        notToBeDisplayed = true;
                        break;
                    } else if (display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GIOVANNI
                            && !giovanniDisplayEnabled) {
                        notToBeDisplayed = true;
                        break;
                    } else if (display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_LEADER
                            && !leaderDisplayEnabled) {
                        notToBeDisplayed = true;
                        break;
                    } else if ((display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GRUNT
                            || display.getIncidentDisplayType() == IncidentDisplayType.INCIDENT_DISPLAY_TYPE_INVASION_GRUNTB)
                            && !gruntDisplayEnabled) {
                        notToBeDisplayed = true;
                        break;
                    }

                }
                if (pokestopDisplaysList.size() > 0 && !notToBeDisplayed) {
                    allFortsOfGmoWithIncidents.put(fort.getFortId(), fort);
                }
            }
        }

        // We now have all forts of the GMO
        // Iterate through the current dataset and filter all that we already have, update them
        // Delete any we cannot find in allFortsOfGmo
        for (IncidentListItem item : this.mDataset) {
            if (allFortsOfGmoWithIncidents.containsKey(item.getRepresentedElement().getFortId())) {
                // We already have that stop in our dataset, update it
                item.setRepresentedElement(allFortsOfGmoWithIncidents.get(item.getRepresentedElement().getFortId()));
                // Now remove it from the hashset to lateron know which one we need to freshly add
                allFortsOfGmoWithIncidents.remove(item.getRepresentedElement().getFortId());
            } else {
                // We can safely delete the fort from our current dataset, it's not relevant anymore
                toBeDeletedFromDataset.add(item);
            }
        }

        // Now we just need to delete all from dataset we do not need anymore and add those that are new
        this.mDataset.removeAll(toBeDeletedFromDataset);
        for (PokemonFortProto fort : allFortsOfGmoWithIncidents.values()) {
            this.mDataset.add(new IncidentListItem(fort));
        }

        Collections.sort(this.mDataset, IncidentListItem.getCompByDistance(this._SharedLatLonHandle));

        this.notifyDataSetChanged();

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class IncidentViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout incidentViewItem;

        /**
         * Linear Layout to be populated with ImageViews of the characters of TeamRocket
         **/
        public LinearLayout characterPictures;
        public TextView distance;
        public TextView timeRemaining;

        public IncidentViewHolder(LinearLayout v) {
            super(v);
            incidentViewItem = v;

            characterPictures = v.findViewById(R.id.incident_item_pictures);
            distance = v.findViewById(R.id.distance);
            timeRemaining = v.findViewById(R.id.incident_time_remaining);
        }

        private int getIconId(Context context, EnumWrapper.InvasionCharacter invasionCharacter) {
            return context.getResources().getIdentifier(
                    "grunt_" + invasionCharacter.getNumber(),
                    "drawable", "com.mad.pogoenhancer");
        }

        public void resetIcons() {
            characterPictures.removeAllViews();
            this.timeRemaining.setText("");
        }

        public void setIcon(Context ctx, EnumWrapper.InvasionCharacter invasionCharacter) {
            int iconId = getIconId(ctx, invasionCharacter);
            if (iconId == 0) return;

            //ImageView Setup
            ImageView imageView = new ImageView(ctx);
            //setting image resource
            imageView.setImageResource(iconId);

            characterPictures.addView(imageView);
        }

        public void setTimeRemaining(long timeEndingMs) {
            long currentTimeMs = System.currentTimeMillis();
            long diffSeconds = timeEndingMs / 1000 - currentTimeMs / 1000;
            double minutesLeftTotal = diffSeconds / 60.0;
            double minutesLeftFloor = Math.floor(minutesLeftTotal);

            int hours = (int) (minutesLeftFloor / 60);
            int minutes = (int) (minutesLeftFloor % 60);
            this.timeRemaining.setText(String.format("%02d:%02d", hours, minutes));
        }

    }

}
