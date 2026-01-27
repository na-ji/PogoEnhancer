package com.mad.pogoenhancer.overlay.elements.joystick;

import android.content.Context;
import android.widget.Toast;

import com.mad.pogoenhancer.Logger;
import com.mad.shared.gpx.LatLon;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.ticofab.androidgpxparser.parser.GPXParser;

public class GpxRouteHandler {
    private static final String TAG = "PogoEnhancerJ";
    private final JoystickManager _joystickManager;

    private Context _context = null;

    private LatLon _sharedLatLon = null;

    private GPXParser _gpxParser = null;

    private List<LatLon> _currentRoute = null;

    private volatile GPXWalkIssuer _gpxWalkIssuer = null;

    private final AtomicBoolean _stopWalker = new AtomicBoolean(false);

    GpxRouteHandler(JoystickManager joystickManager, Context contex, LatLon sharedLatLon) {
        this._context = contex;
        this._joystickManager = joystickManager;
        this._sharedLatLon = sharedLatLon;
        this._gpxParser = new GPXParser();
    }

    public Context getContext() {
        return this._context;
    }

    public void startRouteFromCurrentSpot() {
        if (this._currentRoute == null) {
            Toast.makeText(this._context, "No GPX route set.", Toast.LENGTH_LONG).show();
            return;
        }

        if (_currentRoute.size() == 1) {
            // get just one spot - teleport;
            Toast.makeText(this._context, "Teleport to Location.", Toast.LENGTH_LONG).show();
            _joystickManager.teleportTo(_currentRoute.get(0));
            return;
        }

        if (_currentRoute.size() < 1) {
            Toast.makeText(this._context, "Empty route won't be processed.", Toast.LENGTH_LONG).show();
            return;
        }
        LatLon closestOfRoute = _currentRoute.get(0);
        double distanceToClosest = this._sharedLatLon.distance(closestOfRoute);
        for (LatLon next : _currentRoute) {
            double distanceToNext = this._sharedLatLon.distance(next);
            if (distanceToNext < distanceToClosest) {
                distanceToClosest = distanceToNext;
                closestOfRoute = next;
            }
        }

        // rotate the route to closestOfRoute
        int distanceToRotate = _currentRoute.indexOf(closestOfRoute);
        Collections.rotate(_currentRoute, -(distanceToRotate));
        // pass GPX to a thread handling the GPX route which then issues walking
        // TODO: from current spot
        // TODO: check if walker already running, abort accordingly
        if (this._gpxWalkIssuer != null) {
            this._gpxWalkIssuer.interrupt();
            this._stopWalker.set(true);
            try {
                this._gpxWalkIssuer.join();
            } catch (InterruptedException e) {
                Logger.error("PogoEnhancerJ", "Failed joining old GPX issuer");
            }
        }
        this._stopWalker.set(false);
        _gpxWalkIssuer = new GPXWalkIssuer(this._joystickManager, this._context, _currentRoute, this._stopWalker);
        _gpxWalkIssuer.start();
    }

    void stopRoute() {
        if (this._gpxWalkIssuer != null && this._gpxWalkIssuer.isAlive()) {
            this._gpxWalkIssuer.interrupt();
        }
        this._stopWalker.set(true);
    }

    public void setRoute(List<LatLon> route) {
        this._currentRoute = route;
    }

    private static class GPXWalkIssuer extends Thread {
        private final JoystickManager _joystickManager;
        private Context _context = null;
        private List<LatLon> _route = null;
        private AtomicBoolean _stopRoute = null;

        GPXWalkIssuer(JoystickManager joystickManager, Context context, List<LatLon> route, AtomicBoolean stopRoute) {
            this._context = context;
            this._joystickManager = joystickManager;
            this._route = route;
            this._stopRoute = stopRoute;
        }

        @Override
        public void run() {
            if (this._route == null || _context == null) {
                return;
            }
            // check if there is more than one of the top level entries filled, if so, let's just serialize them all...
            if (_route.isEmpty() || _route.size() == 1) {
                Toast.makeText(this._context, "Empty GPX file.", Toast.LENGTH_LONG).show();
                return;
            }
            _joystickManager.showAutowalkAbortButton();

            // teleport to first stop for now
            LatLon latLon = _route.get(0);
            _joystickManager.teleportTo(latLon);


            while (!this.isInterrupted() && !_stopRoute.get()) {
                for (LatLon stopInRoute : _route) {
                    if (this.isInterrupted() || _stopRoute.get()) {
                        break;
                    }
                    while (_joystickManager.isWalkOngoing()) {
                        try {
                            Thread.sleep(500);
                            if (this.isInterrupted() || _stopRoute.get()) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (this.isInterrupted() || _stopRoute.get()) {
                        break;
                    }
                    _joystickManager.walkToDest(stopInRoute, false);
                }
            }
            _joystickManager.hideAutowalkAbortButton();

        }


    }
}
