package com.mad.pogoenhancer.overlay;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.WindowManager;
import android.widget.Toast;

import com.mad.pogoenhancer.gpx.StartNearbyStopsRouteAsyncTask;
import com.mad.pogoenhancer.overlay.elements.joystick.GpxRouteHandler;
import com.mad.shared.gpx.LatLon;

import java.util.List;
import java.util.Locale;

public class LocationDialogHandler {
    public static void showMoveToLocationDialog(Context context, OverlayManager overlayManager,
                                                LatLon sharedLatLonHandle, List<LatLon> latLons,
                                                String locationOrRoutename,
                                                GpxRouteHandler gpxRouteHandler,
                                                UponSuccessCallback uponSuccess) {
        String popupMessage;
        if (latLons == null) {
            popupMessage = "Start route consisting of nearby stops? Starting may take a moment.";
        } else if (latLons.size() == 1) {
            popupMessage = "Teleport to " + locationOrRoutename + "?";
        } else {
            popupMessage = "Start route " + locationOrRoutename + " with " + latLons.size() + " waypoints?";
        }

        if (latLons != null && latLons.size() == 1) {
            LocationDialogHandler.showTeleportOrWalkDialog(context,
                    overlayManager, sharedLatLonHandle,
                    latLons.get(0), locationOrRoutename, uponSuccess);

        } else {
            LocationDialogHandler.showWalkRouteDialog(context, latLons, popupMessage,
                    gpxRouteHandler,
                    uponSuccess);
        }
    }


    public static void showTeleportOrWalkDialog(Context context, OverlayManager overlayManager,
                                                LatLon sharedLatLon, LatLon locationToBeSet,
                                                String locationName,
                                                UponSuccessCallback uponSuccess) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Move to...");
        double distanceInMeters = sharedLatLon.distance(locationToBeSet);
        String message = "Do you really want to move to " +
                locationName +
                " (" +
                String.format(Locale.ENGLISH, "%.3f", locationToBeSet.getLat()) +
                ", " +
                String.format(Locale.ENGLISH, "%.3f", locationToBeSet.getLon()) +
                ")? If so, do you want to walk or teleport? " +
                "Distance: " +
                String.format(Locale.ENGLISH, "%.0f", distanceInMeters) +
                " meters.";
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .setPositiveButton("Walk", (dialog, id) -> {
                    dialog.dismiss();
                    if (uponSuccess != null) {
                        uponSuccess.call();
                    }
                    overlayManager.cancelGpxWalk();
                    overlayManager.walkToDest(locationToBeSet);
                })
                .setNeutralButton("Teleport", (dialog, id) -> {
                    dialog.dismiss();
                    if (uponSuccess != null) {
                        uponSuccess.call();
                    }
                    overlayManager.cancelGpxWalk();
                    sharedLatLon.setLocation(locationToBeSet);
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        alertDialog.getWindow().setType(LAYOUT_FLAG);
        alertDialog.show();
    }

    public static void showWalkRouteDialog(Context context, List<LatLon> latLons,
                                           String finalPopupMessage,
                                           GpxRouteHandler gpxRouteHandler,
                                           UponSuccessCallback uponSuccess) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Walk route....");

        alertDialogBuilder
                .setMessage(finalPopupMessage)
                .setCancelable(false)
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, id) -> {
                    dialog.dismiss();
                    if (uponSuccess != null) {
                        uponSuccess.call();
                    }
                    if (latLons == null) {
                        // Calculate shortest path through GMO stops
                        StartNearbyStopsRouteAsyncTask startRouteTask = new StartNearbyStopsRouteAsyncTask(gpxRouteHandler);
                        startRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        if (latLons.isEmpty()) {
                            Toast.makeText(context, "Cannot handle empty route.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        gpxRouteHandler.setRoute(latLons);
                        gpxRouteHandler.startRouteFromCurrentSpot();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        alertDialog.getWindow().setType(LAYOUT_FLAG);
        alertDialog.show();
    }

    public interface UponSuccessCallback {
        void call();
    }

}
