package com.mad.pogoenhancer.overlay;

import androidx.annotation.Nullable;

import com.mad.shared.gpx.LatLon;

import java.util.Comparator;

public abstract class RadarItem<T> {
    protected T _representedElement;
    protected LatLon _location;

    public RadarItem(T representedElement, LatLon location) {
        this._representedElement = representedElement;
        this._location = location;
    }

    public LatLon get_Location() {
        return _location;
    }

    public void set_Location(LatLon _Location) {
        this._location = _Location;
    }

    public T getRepresentedElement() {
        return this._representedElement;
    }

    public void setRepresentedElement(T representedElement) {
        this._representedElement = representedElement;
    }

    public double getDistance(LatLon currentLocation) {
        return currentLocation.distance(this._location);
    }

    @Override
    public abstract boolean equals(@Nullable Object obj);

    public static Comparator<RadarItem<?>> getCompByDistance(LatLon currentLocation) {
        return (s1, s2) -> {
            double distanceToS1 = s1.getDistance(currentLocation);
            double distanceToS2 = s2.getDistance(currentLocation);

            return Double.compare(distanceToS1, distanceToS2);
        };
    }
}
