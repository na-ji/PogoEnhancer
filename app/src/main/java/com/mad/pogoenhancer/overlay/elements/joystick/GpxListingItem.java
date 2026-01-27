package com.mad.pogoenhancer.overlay.elements.joystick;

import androidx.annotation.Nullable;

import com.mad.shared.gpx.LatLon;

import java.util.Comparator;

public class GpxListingItem {
    private final String _name;
    private final LatLon _startLocation;

    GpxListingItem(String name, LatLon startLocation) {
        this._name = name;
        this._startLocation = startLocation;
    }

    public String getName() {
        return this._name;
    }

    public LatLon getStartLocation() {
        return this._startLocation;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof GpxListingItem)) {
            return false;
        }

        GpxListingItem toCompareAgainst = (GpxListingItem) obj;

        return this._name.equals(toCompareAgainst.getName())
                && this._startLocation.equals(toCompareAgainst.getStartLocation());
    }

    public double getDistance(LatLon sharedLatLon) {
        return sharedLatLon.distance(this._startLocation);
    }

    public static Comparator<GpxListingItem> getCompByDistance(LatLon currentLocation) {
        return (s1, s2) -> {
            double distanceToS1 = s1.getDistance(currentLocation);
            double distanceToS2 = s2.getDistance(currentLocation);

            return Double.compare(distanceToS1, distanceToS2);
        };
    }
}
