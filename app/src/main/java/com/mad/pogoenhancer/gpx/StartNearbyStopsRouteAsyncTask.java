package com.mad.pogoenhancer.gpx;

import android.os.AsyncTask;
import android.widget.Toast;

import com.mad.pogoenhancer.overlay.elements.joystick.GpxRouteHandler;
import com.mad.shared.gpx.LatLon;

import java.util.List;

public class StartNearbyStopsRouteAsyncTask  extends AsyncTask<Void, Void, List<LatLon>> {

    private GpxRouteHandler _routeHandler;

    public StartNearbyStopsRouteAsyncTask(GpxRouteHandler routeHandler) {
        this._routeHandler = routeHandler;
    }

    @Override
    protected List<LatLon> doInBackground(Void... params) {
        return GpxManager.getInstance(this._routeHandler.getContext()).getShortPathThroughNearbyStops();
    }

    @Override
    protected void onPostExecute(final List<LatLon> route) {
        if (route.isEmpty()) {
            Toast.makeText(this._routeHandler.getContext(), "Cannot handle empty route.", Toast.LENGTH_LONG).show();
            return;
        }
        this._routeHandler.setRoute(route);
        this._routeHandler.startRouteFromCurrentSpot();
    }

    @Override
    protected void onCancelled() {

    }
}
