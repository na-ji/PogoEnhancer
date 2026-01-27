package com.mad.pogoenhancer.overlay.elements.gyms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import POGOProtos.Rpc.ClientMapCellProto;
import POGOProtos.Rpc.FortType;
import POGOProtos.Rpc.GetMapObjectsOutProto;
import POGOProtos.Rpc.GymDisplayProto;
import POGOProtos.Rpc.PokemonFortProto;
import POGOProtos.Rpc.RaidInfoProto;


public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {
    public Context _serviceContext;
    private final ArrayList<GymListItem> mDataset;
    private final LatLon _SharedLatLonHandle;
    private final OverlayManager _SupervisingManager;
    private final SharedPreferences _SharedPreferences;

    public GymAdapter(LatLon sharedLatLon, Context context, OverlayManager supervisingManager) {
        this.mDataset = new ArrayList<>();
        this._SharedLatLonHandle = sharedLatLon;
        this._serviceContext = context;
        this._SupervisingManager = supervisingManager;
        this._SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Create new views (invoked by the layout_old manager)
    @NotNull
    @Override
    public GymAdapter.GymViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gym_list_item, parent, false);

        return new GymViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout_old manager)
    @Override
    public void onBindViewHolder(@NotNull GymAdapter.GymViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//            holder.textView.setText(mDataset.get(position).getMonName());
        holder.resetIcons();
        GymListItem gymListItem = mDataset.get(position);
        PokemonFortProto fortData = gymListItem.getRepresentedElement();

        switch (fortData.getTeam()) {
            case TEAM_BLUE:
                holder.setGymColour(GameFractionHoldingGym.BLUE);
                break;
            case TEAM_RED:
                holder.setGymColour(GameFractionHoldingGym.RED);
                break;
            case TEAM_YELLOW:
                holder.setGymColour(GameFractionHoldingGym.YELLOW);
                break;
            case TEAM_UNSET:
                holder.setGymColour(GameFractionHoldingGym.NONE);
                break;
            case UNRECOGNIZED:
            default:
                break;
        }

        GymDisplayProto gymDisplay = fortData.getGymDisplay();
        int freeSlots = gymDisplay.getSlotsAvailable();
        holder.setSlots(freeSlots);

        // If a raid is present, we want to display the level and mon icon
        if (fortData.hasRaidInfo()) {
            RaidInfoProto raidInfo = fortData.getRaidInfo();
            int raidLevel = raidInfo.getRaidLevelValue();
            int displayPokemonId = raidInfo.getRaidPokemon().getPokemonIdValue();
            long hatchAtMs = 0;
            if (displayPokemonId == 0) {
                hatchAtMs = raidInfo.getRaidBattleMs();
            }
            holder.setRaidInfo(this._serviceContext, raidLevel, displayPokemonId, hatchAtMs);
        }


        double distance = gymListItem.getDistance(this._SharedLatLonHandle);
        if (distance < 0.001) {
            holder.distance.setText("0m");
        } else {
            holder.distance.setText(String.format(Locale.ENGLISH, "%.0f", distance) + "m");
        }
        holder.itemView.setOnClickListener(v -> {
            if (!_SharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED,
                    Constants.DEFAULT_VALUES.SPOOFING_ENABLED)) {
                DecimalFormat df = new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(8);
                LatLon location = gymListItem.get_Location();


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
                    alertDialogBuilder.setTitle("Open GMaps");

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
                try {
                    LocationDialogHandler.showTeleportOrWalkDialog(_serviceContext,
                            _SupervisingManager,
                            _SharedLatLonHandle,
                            gymListItem.get_Location(),
                            "the gym",
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    // Return the size of your dataset (invoked by the layout_old manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected void updateDataset(GetMapObjectsOutProto gmoReceived) {
        // TODO: Get current location and calc cell IDs surrounding, remove any gyms that are not in those cells?
        if (gmoReceived == null) {
            return;
        }
        List<ClientMapCellProto> mapCellsList = gmoReceived.getMapCellList();
        if (mapCellsList.size() == 0) {
            return;
        }
        ArrayList<GymListItem> toBeDeletedFromDataset = new ArrayList<>();
        Map<String, PokemonFortProto> allGymsOfGmo = new HashMap<>();

        boolean showHeldByNone = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_NONE,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_NONE);
        boolean showHeldByRed = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_RED,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_RED);
        boolean showHeldByBlue = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_BLUE,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_BLUE);
        boolean showHeldByYellow = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_FRACTION_YELLOW,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_FRACTION_YELLOW);

        for (ClientMapCellProto cell : mapCellsList) {
            List<PokemonFortProto> fortsList = cell.getFortList();
            for (PokemonFortProto fort : fortsList) {
                if (fort.getFortType() != FortType.GYM) {
                    continue;
                }
                // TODO: Only put if level fits settings
                switch (fort.getTeam()) {
                    case TEAM_UNSET:
                        if (showHeldByNone && isRaidLevelAllowed(fort)) {
                            allGymsOfGmo.put(fort.getFortId(), fort);
                        }
                        break;
                    case TEAM_BLUE:
                        if (showHeldByBlue && isRaidLevelAllowed(fort)) {
                            allGymsOfGmo.put(fort.getFortId(), fort);
                        }
                        break;
                    case TEAM_RED:
                        if (showHeldByRed && isRaidLevelAllowed(fort)) {
                            allGymsOfGmo.put(fort.getFortId(), fort);
                        }
                        break;
                    case TEAM_YELLOW:
                        if (showHeldByYellow && isRaidLevelAllowed(fort)) {
                            allGymsOfGmo.put(fort.getFortId(), fort);
                        }
                        break;
                    case UNRECOGNIZED:
                    default:
                        break;
                }
            }
        }

        // We now have all forts of the GMO
        // Iterate through the current dataset and filter all that we already have, update them
        // Delete any we cannot find in allFortsOfGmo
        for (GymListItem item : this.mDataset) {
            if (allGymsOfGmo.containsKey(item.getRepresentedElement().getFortId())) {
                // We already have that stop in our dataset, update it
                item.setRepresentedElement(allGymsOfGmo.get(item.getRepresentedElement().getFortId()));
                // Now remove it from the hashset to lateron know which one we need to freshly add
                allGymsOfGmo.remove(item.getRepresentedElement().getFortId());
            } else {
                // We can safely delete the fort from our current dataset, it's not relevant anymore
                toBeDeletedFromDataset.add(item);
            }
        }

        // Now we just need to delete all from dataset we do not need anymore and add those that are new
        this.mDataset.removeAll(toBeDeletedFromDataset);
        for (PokemonFortProto fort : allGymsOfGmo.values()) {
            this.mDataset.add(new GymListItem(fort));
        }

        Collections.sort(this.mDataset, GymListItem.getCompByDistance(this._SharedLatLonHandle));

        this.notifyDataSetChanged();

    }

    private boolean isRaidLevelAllowed(PokemonFortProto fort) {
        boolean showRaidLevelNone = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_NONE);
        if (!fort.hasRaidInfo()) {
            return showRaidLevelNone;
        }
        boolean showRaidLevel1 = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_1);
        boolean showRaidLevel2 = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_2);
        boolean showRaidLevel3 = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_3);
        boolean showRaidLevel4 = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_4);
        boolean showRaidLevel5AndAbove = _SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5,
                Constants.DEFAULT_VALUES.SHOW_NEARBY_GYM_WITH_RAID_LEVEL_5);
        int raidLevelValue = fort.getRaidInfo().getRaidLevelValue();
        switch (raidLevelValue) {
            case 1:
                return showRaidLevel1;
            case 2:
                return showRaidLevel2;
            case 3:
                return showRaidLevel3;
            case 4:
                return showRaidLevel4;
            case 5:
            default:
                return showRaidLevel5AndAbove;
        }


    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class GymViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout gymViewItem;

        /**
         * Linear Layout to be populated with ImageViews of the characters of TeamRocket
         **/
        public ImageView gymColour;
        public TextView freeSlots;
        public TextView raidLevel;
        public ImageView raidMonIcon;
        public TextView hatchAt;
        public TextView distance;

        public GymViewHolder(LinearLayout v) {
            super(v);
            gymViewItem = v;

            gymColour = v.findViewById(R.id.gym_color);
            freeSlots = v.findViewById(R.id.free_slots);
            raidLevel = v.findViewById(R.id.raid_level);
            raidMonIcon = v.findViewById(R.id.raid_mon_icon);
            hatchAt = v.findViewById(R.id.hatch_at);
            distance = v.findViewById(R.id.distance);
        }

        private int getMonIconId(Context context, int pokemonId) {
            String picaddon = "";
            return context.getResources().getIdentifier(
                    "mon_" + picaddon + String.format(Locale.ENGLISH, "%03d", pokemonId),
                    "drawable", "com.mad.pogoenhancer");
        }

        @SuppressLint("SetTextI18n")
        public void setSlots(int freeSlotsToSet) {
            freeSlots.setText(Integer.toString(freeSlotsToSet));
        }

        public void resetIcons() {
            gymColour.setImageResource(R.color.white);
            raidMonIcon.setImageResource(0);
            freeSlots.setText("");
            raidLevel.setText("");
            distance.setText("");
            hatchAt.setText("");
        }

        private void setRaidMonIcon(Context ctx, int pokemonId) {
            int iconId = getMonIconId(ctx, pokemonId);
            if (iconId == 0) return;
            raidMonIcon.setImageResource(getMonIconId(ctx, pokemonId));
        }

        public void setGymColour(GameFractionHoldingGym fractionHoldingGym) {
            switch (fractionHoldingGym) {
                case RED:
                    gymColour.setImageResource(R.color.colorRedVersionMismatch);
                    break;
                case BLUE:
                    gymColour.setImageResource(R.color.blue);
                    break;
                case YELLOW:
                    gymColour.setImageResource(R.color.yellow);
                    break;
                case NONE:
                default:
                    gymColour.setImageResource(R.color.white);
            }
        }

        @SuppressLint("SetTextI18n")
        public void setRaidInfo(Context ctx, int raidLevelToBeSet, int displayPokemonId, long hatchAtTimestamp) {
            if (displayPokemonId != 0) {
                this.raidMonIcon.setVisibility(View.VISIBLE);
                this.hatchAt.setVisibility(View.GONE);
                this.setRaidMonIcon(ctx, displayPokemonId);
                this.raidLevel.setText(Integer.toString(raidLevelToBeSet));

            } else {
                this.raidMonIcon.setVisibility(View.GONE);
                this.hatchAt.setVisibility(View.VISIBLE);
                this.raidLevel.setText("(" + raidLevelToBeSet + ")");

                Date date = new Date(hatchAtTimestamp);
                DateFormat formatter = new SimpleDateFormat("HH:mm");
                //formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateFormatted = formatter.format(date);
                this.hatchAt.setText(dateFormatted);
            }
        }
    }

}
