package com.mad.pogoenhancer.overlay.elements.nearby;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.LocationDialogHandler;
import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.shared.gpx.LatLon;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import POGOProtos.Rpc.ClientMapCellProto;
import POGOProtos.Rpc.GetMapObjectsOutProto;
import POGOProtos.Rpc.NearbyPokemonProto;
import POGOProtos.Rpc.PokemonDisplayProto;
import POGOProtos.Rpc.PokemonFortProto;
import POGOProtos.Rpc.WildPokemonProto;

import static com.mad.pogoenhancer.App.HEADS_UP_CHANNEL_ID;
import static com.mad.pogoenhancer.App.ORDINARY_CHANNEL_ID;

public class NearbyMonAdapter extends RecyclerView.Adapter<NearbyMonAdapter.MonViewHolder> {
    private final ArrayList<NearbyMonListItem> mDataset;
    private final LatLon _SharedLatLonHandle;
    public Context _serviceContext;
    private final OverlayManager _SupervisingManager;
    private final SharedPreferences _SharedPreferences;
    private Set<Integer> _ToBeNotifiedAbout;
    private Set<Integer> _ToBeIgnored;
    private final JSONObject _MonTypes;

    public NearbyMonAdapter(LatLon sharedLatLon, Context context, OverlayManager supervisingManager) {
        this.mDataset = new ArrayList<>();
        this._SharedLatLonHandle = sharedLatLon;
        this._serviceContext = context;
        this._SupervisingManager = supervisingManager;
        this._SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this._MonTypes = loadMonTypes(context);

    }

    // Create new views (invoked by the layout_old manager)
    @Override
    public NearbyMonAdapter.MonViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mon_list_item, parent, false);

        return new MonViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout_old manager)
    @Override
    public void onBindViewHolder(@NotNull MonViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//            holder.textView.setText(mDataset.get(position).getMonName());
        holder.reset();
        NearbyMonListItem nearbyMonListItem = mDataset.get(position);

        int monId = nearbyMonListItem.get_MonId();
        Map<Long, Long> alreadyEncounteredMons = this._SupervisingManager.getAlreadyEncountered();
        long encounterId = nearbyMonListItem.getEncounterId();
        boolean alreadyEncountered = alreadyEncounteredMons.containsKey(encounterId);
        if (alreadyEncountered) {
            holder.monViewItem.setBackgroundColor(Color.LTGRAY);
        } else if (_ToBeNotifiedAbout != null && _ToBeNotifiedAbout.contains(monId)) {
            holder.monViewItem.setBackgroundColor(Color.RED);
        } else {
            holder.monViewItem.setBackgroundColor(Color.WHITE);
        }

        holder.monId.setText(Integer.toString(monId));
        holder.monName.setText(nearbyMonListItem.getMonName(_serviceContext));

        holder.monPic.setImageResource(nearbyMonListItem.getMonIcon(_serviceContext));

        double distance = nearbyMonListItem.getDistance(this._SharedLatLonHandle);
        if (distance < 0.001) {
            holder.distance.setText("0m");
        } else {
            holder.distance.setText(String.format(Locale.ENGLISH, "%.0fm", distance));
        }
        holder.itemView.setOnClickListener(v -> {
            if (!_SharedPreferences.getBoolean(Constants.SHAREDPERFERENCES_KEYS.SPOOFING_ENABLED,
                    Constants.DEFAULT_VALUES.SPOOFING_ENABLED)) {
                DecimalFormat df = new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(8);
                LatLon monLoc = nearbyMonListItem.get_Location();


                if (_SharedPreferences.getBoolean(
                        Constants.SHAREDPERFERENCES_KEYS.OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY,
                        Constants.DEFAULT_VALUES.OVERLAY_NEARBY_NON_SPOOFING_CLICK_COPY
                )) {
                    ClipboardManager clipboard = (ClipboardManager) _serviceContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    String clipboardContent = String.format("%s, %s", df.format(monLoc.getLat()), df.format(monLoc.getLon()));
                    ClipData clip = ClipData.newPlainText("Nearby coords", clipboardContent);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(_serviceContext,
                            "Copied coords of " + nearbyMonListItem.getMonName(_serviceContext) + " to clipboard.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(_serviceContext);
                    alertDialogBuilder.setTitle("Move to...");

                    // TODO: view approx. travel time
                    // set dialog message
                    alertDialogBuilder
                            .setMessage("Do you want to open GMaps with the location of " + nearbyMonListItem.getMonName(_serviceContext)
                                    + "?")
                            .setCancelable(false)
                            .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, id) -> {
                                dialog.dismiss();
                                //_SharedLatLonHandle.setLat(monLoc.getLat());
                                //_SharedLatLonHandle.setLon(monLoc.getLon());

                                String urlString = String.format("http://maps.google.com/maps?daddr=%s,%s", df.format(monLoc.getLat()), df.format(monLoc.getLon()));

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
                        nearbyMonListItem.get_Location(),
                        nearbyMonListItem.getMonName(_serviceContext),
                        null);
            }

        });
    }

    // Return the size of your dataset (invoked by the layout_old manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private Set<Integer> getToBeNotified() {
        Set<String> stringSet = this._SharedPreferences.getStringSet(
                Constants.SHAREDPERFERENCES_KEYS.MON_NOTIFICATION_IDS_NEARBY,
                Constants.DEFAULT_VALUES.MON_NOTIFICATION_IDS_NEARBY);
        Set<Integer> retval = new HashSet<>();
        if (stringSet == null || stringSet.isEmpty()) {
            return retval;
        }
        for (String id : stringSet) {
            retval.add(Integer.valueOf(id));
        }
        return retval;
    }

    private Set<Integer> getToBeIgnored() {
        Set<String> stringSet = this._SharedPreferences.getStringSet(
                Constants.SHAREDPERFERENCES_KEYS.MON_HIDE_IDS_NEARBY,
                Constants.DEFAULT_VALUES.MON_HIDE_IDS_NEARBY);
        Set<Integer> retval = new HashSet<>();
        if (stringSet == null || stringSet.isEmpty()) {
            return retval;
        }
        for (String id : stringSet) {
            retval.add(Integer.valueOf(id));
        }
        return retval;
    }

    protected void updateDataset(GetMapObjectsOutProto gmoReceived) {
        if (gmoReceived == null) {
            return;
        }
        List<ClientMapCellProto> mapCellsList = gmoReceived.getMapCellList();
        if (mapCellsList.size() == 0) {
            return;
        }
        boolean sendNotification = false;
        ArrayList<NearbyMonListItem> toBeDeletedFromDataset = new ArrayList<>();
        Map<Long, NearbyMonListItem> allMonsOfGmo = new HashMap<>();
        _ToBeNotifiedAbout = getToBeNotified();
        _ToBeIgnored = getToBeIgnored();

        for (ClientMapCellProto cell : mapCellsList) {
            List<WildPokemonProto> wildPokemonsList = cell.getWildPokemonList();
            for (WildPokemonProto mon : wildPokemonsList) {
                if (_ToBeIgnored.contains(mon.getPokemon().getPokemonIdValue()) ||
                        !showMonByType(mon.getPokemon().getPokemonIdValue())) {
                    // mon is to be ignored entirely
                    continue;
                }

                allMonsOfGmo.put(mon.getEncounterId(),
                        new NearbyMonListItem(mon.getPokemon().getPokemonDisplay(),
                                new LatLon(mon.getLatitude(), mon.getLongitude()),
                                mon.getPokemon().getPokemonIdValue(),
                                mon.getEncounterId())
                );
                if (!this._SupervisingManager.getAlreadyEncountered().containsKey(mon.getEncounterId())
                        && _ToBeNotifiedAbout.contains(mon.getPokemon().getPokemonIdValue())) {
                    sendNotification = true;
                }

            }

            List<NearbyPokemonProto> nearbyPokemonsList = cell.getNearbyPokemonList();
            List<PokemonFortProto> fortsList = cell.getFortList();
            // add all the nearby mons to the latest List
            for (NearbyPokemonProto mon : nearbyPokemonsList) {
                for (PokemonFortProto fort : fortsList) {
                    if (fort.getFortId().equals(mon.getFortId())) {
                        if (_ToBeIgnored.contains(mon.getPokedexNumber()) ||
                                !showMonByType(mon.getPokedexNumber())) {
                            // mon is to be ignored entirely
                            continue;
                        }

                        if (!allMonsOfGmo.containsKey(mon.getEncounterId())) {
                            NearbyMonListItem nearbyMonListItem = new NearbyMonListItem(
                                    mon.getPokemonDisplay(),
                                    new LatLon(fort.getLatitude(), fort.getLongitude()),
                                    mon.getPokedexNumber(),
                                    mon.getEncounterId());
                            allMonsOfGmo.put(mon.getEncounterId(), nearbyMonListItem);

                            if (!this._SupervisingManager.getAlreadyEncountered().containsKey(mon.getEncounterId())
                                    && _ToBeNotifiedAbout.contains(mon.getPokedexNumber())) {
                                sendNotification = true;
                            }
                        }
                    }
                }
            }
        }

        // we now have the latest nearby and wild mons, onto updating the list we know
        for (NearbyMonListItem item : this.mDataset) {
            if (allMonsOfGmo.containsKey(item.getEncounterId())
                    && allMonsOfGmo.get(item.getEncounterId()) != null) {
                // We already have that stop in our dataset, update it
                item.setRepresentedElement(allMonsOfGmo.get(item.getEncounterId()).getRepresentedElement());
                // Now remove it from the hashset to lateron know which one we need to freshly add
                allMonsOfGmo.remove(item.getEncounterId());
            } else {
                // We can safely delete the fort from our current dataset, it's not relevant anymore
                toBeDeletedFromDataset.add(item);
            }
        }

        if (sendNotification) {
            notifyNearby();
        }

        // Now we just need to delete all from dataset we do not need anymore and add those that are new
        this.mDataset.removeAll(toBeDeletedFromDataset);
        this.mDataset.addAll(allMonsOfGmo.values());

        Collections.sort(this.mDataset, NearbyMonListItem.getCompByDistance(this._SharedLatLonHandle));

        this.notifyDataSetChanged();

    }

    public void notifyNearby() {
        boolean lastSentDisabled = this._SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.DISABLE_NOTIFICATION,
                Constants.DEFAULT_VALUES.DISABLE_NOTIFICATION);


        boolean showAsPopupNotification = this._SharedPreferences.getBoolean(
                Constants.SHAREDPERFERENCES_KEYS.SHOW_HEADSUP,
                Constants.DEFAULT_VALUES.SHOW_HEADSUP);

        Date currentTime = Calendar.getInstance().getTime();

        String notificationTest = "Never";
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, HH:mm:ss a", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        df.setTimeZone(TimeZone.getDefault());
        notificationTest = "At least one mon you specified is nearby (" + df.format(currentTime) + ")";

        if (lastSentDisabled) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) this._serviceContext.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );
        //TODO: config option to not have it pop-up all the time...
        NotificationCompat.Builder notificationBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (showAsPopupNotification) {
                notificationBuilder = new NotificationCompat.Builder(this._serviceContext, HEADS_UP_CHANNEL_ID)
                        .setContentTitle("Mon nearby")
                        .setContentText(notificationTest)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.ic_autorenew_black_24dp);
            } else {
                notificationBuilder = new NotificationCompat.Builder(this._serviceContext, ORDINARY_CHANNEL_ID)
                        .setContentTitle("Mon nearby")
                        .setContentText(notificationTest)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.ic_autorenew_black_24dp);
            }
        } else {
            //noinspection deprecation
            notificationBuilder = new NotificationCompat.Builder(this._serviceContext)
                    .setSmallIcon(R.drawable.ic_autorenew_black_24dp)
                    .setContentTitle("Mon nearby")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(notificationTest);
        }
        if (showAsPopupNotification) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(35432, notificationBuilder.build());
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MonViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout monViewItem;

        public TextView monName;
        public TextView monId;
        public TextView distance;
        public ImageView monPic;

        public MonViewHolder(LinearLayout v) {
            super(v);
            monViewItem = v;

            monName = v.findViewById(R.id.mon_name);
            monId = v.findViewById(R.id.mon_id);
            distance = v.findViewById(R.id.distance);
            monPic = v.findViewById(R.id.mon_pic);
        }

        public void reset() {
            monName.setText("");
            monId.setText("");
            distance.setText("");
            monPic.setImageResource(0);
        }
    }

    private JSONObject loadMonTypes(@NonNull Context context) {

        JSONObject obj = null;
        try {
            InputStream is = context.getResources().openRawResource(R.raw.types);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String moveJson = new String(buffer, StandardCharsets.UTF_8);

            obj = new JSONObject(moveJson);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private boolean showMonByType(int MonID) {
        JSONArray monTypes = null;
        try {
            monTypes = _MonTypes.getJSONObject(String.valueOf(MonID)).getJSONArray("types");
        } catch (JSONException e) {
            Log.e("ProtoHookJ", "Missing types for mon " + MonID);
            e.printStackTrace();
            return true;
        }

        boolean notShowMonByType = false;

        for (int i = 0; i < monTypes.length(); i++) {
            String type = null;
            try {
                type = monTypes.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!notShowMonByType) {
                String wildMonTypeIdentifier = "type_" + type;
                notShowMonByType = this._SharedPreferences.getBoolean(
                        wildMonTypeIdentifier,
                        Constants.DEFAULT_VALUES.SHOW_WILDMON);
            }
        }

        return notShowMonByType;

    }

}
