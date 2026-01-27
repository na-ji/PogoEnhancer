package com.mad.pogoenhancer.gpx;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.gpx.christofides.Christofides;
import com.mad.pogoenhancer.utils.IvToast;
import com.mad.shared.gpx.LatLon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import POGOProtos.Rpc.ClientMapCellProto;
import POGOProtos.Rpc.ClientRouteMapCellProto;
import POGOProtos.Rpc.GetMapObjectsOutProto;
import POGOProtos.Rpc.GetRoutesOutProto;
import POGOProtos.Rpc.PokemonFortProto;
import POGOProtos.Rpc.RouteWaypointProto;
import POGOProtos.Rpc.SharedRouteProto;
import io.ticofab.androidgpxparser.parser.domain.Gpx;


public class GpxManager {
    private SharedPreferences _sharedPreferences = null;
    private Context _context;
    private LoadedGpxRoutes _loadedGpxRoutes = null;

    private final HashMap<String, List<LatLon>> _loadedGpxRoutesOneDimension = new HashMap<>();

    private final Map<String, Pair<String, List<LatLon>>> _nearbyPogoRoutes = new HashMap<>();
    private static GpxManager instance;

    private GetMapObjectsOutProto latestGmo = null;

    private GpxManager (SharedPreferences sharedPreferences, Context ctx) {
        this._sharedPreferences = sharedPreferences;
        this._context = ctx;
        this.loadFromSettings();
    }

    public synchronized void updateGmo(GetMapObjectsOutProto latest) {
        this.latestGmo = latest;
    }

    public synchronized GetMapObjectsOutProto getLatestGmo() {
        return this.latestGmo;
    }

    public synchronized List<LatLon> getShortPathThroughNearbyStops() {
        Logger.debug("ProtoHookJ", "Calculating path through nearby stops");
        List<LatLon> nearbyStops = new ArrayList<>();
        if (this.latestGmo == null) {
            return nearbyStops;
        }
        List<ClientMapCellProto> mapCellsList = this.latestGmo.getMapCellList();
        for (ClientMapCellProto cell : mapCellsList) {
            List<PokemonFortProto> fortsList = cell.getFortList();
            for (PokemonFortProto fort : fortsList) {
                nearbyStops.add(new LatLon(fort.getLatitude(), fort.getLongitude()));
            }
        }
        try {
            Christofides christofides = new Christofides(false);
            return christofides.solve(nearbyStops);
        } catch (Exception e) {
            Logger.fatal("ProtoHookJ", "Failed calculating route: " + e.toString());
            return nearbyStops;
        }
    }

    public static synchronized GpxManager getInstance(@NonNull Context context) {
        if (GpxManager.instance == null) {
            Logger.pdebug("ProtoHookJ", "Starting GPX");
            SharedPreferences preferenceManager = PreferenceManager.
                    getDefaultSharedPreferences(context);
            GpxManager.instance = new GpxManager(preferenceManager, context);
        }
        return GpxManager.instance;
    }

    private synchronized void loadFromSettings() {
        Logger.pdebug("ProtoHookJ", "Loading GPX from settings with: " + Constants.SHAREDPERFERENCES_KEYS.GPX_ROUTES);
        String json = this._sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.GPX_ROUTES,
                Constants.DEFAULT_VALUES.GPX_ROUTES
        );
        Logger.debug("ProtoHookJ", json);
        try {
            this._loadedGpxRoutes = new LoadedGpxRoutes();
            this._loadedGpxRoutes.fromJson(json);
        } catch (Exception e) {
            ContextCompat.getMainExecutor(this._context).execute(()  -> {
                        Toast.makeText(this._context,
                                "Failed loading routes. A route was corrupted, sorry.",
                                Toast.LENGTH_LONG).show();
            });
            Logger.fatal("ProtoHookJ", "Failed loading GPX from settings: " + e.toString());
            e.printStackTrace();
        }
        if (this._loadedGpxRoutes == null) {
            // no gpx routes at all, create one...
            Logger.pdebug("ProtoHookJ", "No GPX routes in settings.");
            this._loadedGpxRoutes = new LoadedGpxRoutes();
        }
        populatedOneDimensionalRoutes();

    }

    private void populatedOneDimensionalRoutes() {
        this._loadedGpxRoutesOneDimension.clear();
        HashMap<String, Gpx> routes = this._loadedGpxRoutes.getRoutes();
        for(Map.Entry<String, Gpx> entry : routes.entrySet()) {
            List<LatLon> latLons = GpxUtil.transformAllSpotsOfGpxToOneDimension(entry.getValue());
            this._loadedGpxRoutesOneDimension.put(entry.getKey(), latLons);
        }
    }

    private synchronized void storeToSettings() {
        SharedPreferences.Editor prefsEditor = _sharedPreferences.edit();
        String json = "";
        try {
            // json = _gson.toJson(this._loadedGpxRoutes);
            json = this._loadedGpxRoutes.toJson();
        } catch (Exception e) {
            IvToast.showToast(this._context, "Failed loading routes. A route was corrupted, sorry.", Gravity.BOTTOM|Gravity.CENTER, 0, 200, R.drawable.ic_frown_solid, 0);
        }
        prefsEditor.putString(
                Constants.SHAREDPERFERENCES_KEYS.GPX_ROUTES, json);
        prefsEditor.apply();
        populatedOneDimensionalRoutes();
    }

    public LoadedGpxRoutes getCurrentlyLoadedRoutes() {
        return this._loadedGpxRoutes;
    }

    public boolean addRoute(String text, Gpx gpx) {
        // add route
        // copy GPX data without datetime since that has issues with gson and we do not need that data anyway
        Gpx clonedGpx = GpxUtil.cloneRelevantGpx(gpx);

        boolean success = this._loadedGpxRoutes.addRoute(text, clonedGpx);
        if (success) {
            Logger.debug("ProtoHook", "Storing settings.");
            this.storeToSettings();
        }

        return success;
    }

    public boolean removeRoute(String name) {
        boolean success = this._loadedGpxRoutes.removeRoute(name);
        if (success) {
            this.storeToSettings();
        }

        return success;
    }

    public Gpx getRoute(String gpxName) {
        return this._loadedGpxRoutes.getRoute(gpxName);
    }

    public HashMap<String, List<LatLon>> get_loadedGpxRoutesOneDimension() {
        return _loadedGpxRoutesOneDimension;
    }

    public Map<String, Pair<String, List<LatLon>>> getNearbyRoutes() {
        return _nearbyPogoRoutes;
    }

    public void updateNearbyRoutes(GetRoutesOutProto getRoutesOutProto, LatLon sharedLatLon) {
        // TODO: Also update the UI listing routes..
        // First, remove all routes not in the current GetRoutesOutProto anymore -> Not nearby
        Set<String> routeIdsInProto = new HashSet<>();
        List<ClientRouteMapCellProto> routeMapCellList = getRoutesOutProto.getRouteMapCellList();
        for(ClientRouteMapCellProto routeMapCell : routeMapCellList) {
            List<SharedRouteProto> routeList = routeMapCell.getRouteList();
            for (SharedRouteProto routeProto : routeList) {
                routeIdsInProto.add(routeProto.getId());
            }
        }

        Iterator<Map.Entry<String, Pair<String, List<LatLon>>>> iterator
                = this._nearbyPogoRoutes.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Pair<String, List<LatLon>>> entry = iterator.next();
            if (!routeIdsInProto.contains(entry.getKey()) && entry.getValue() != null
                    && entry.getValue().second != null
                    && !entry.getValue().second.isEmpty()) {
                // Route is not in current set anymore, check if more than 5km away
                if (entry.getValue().second.get(0).distance(sharedLatLon) > 5000) {
                    iterator.remove();
                }
            }
        }

        // Now add all routes not already on the local list
        for(ClientRouteMapCellProto routeMapCell : routeMapCellList) {
            List<SharedRouteProto> routeList = routeMapCell.getRouteList();
            for (SharedRouteProto routeProto : routeList) {
                if (this._nearbyPogoRoutes.containsKey(routeProto.getId())) {
                    continue;
                }
                List<LatLon> coordsOfRoute = new ArrayList<>();

                for (RouteWaypointProto waypoint : routeProto.getWaypointsList()) {
                    LatLon waypointAsLatLon = new LatLon(waypoint.getLatDegrees(), waypoint.getLngDegrees());
                    coordsOfRoute.add(waypointAsLatLon);
                }

                Pair<String, List<LatLon>> newEntry = new Pair<>(routeProto.getName(), coordsOfRoute);
                this._nearbyPogoRoutes.put(routeProto.getId(), newEntry);
            }
        }
    }
}
