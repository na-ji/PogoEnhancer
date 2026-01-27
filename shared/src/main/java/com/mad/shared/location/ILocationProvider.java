package com.mad.shared.location;

import android.location.Location;

public interface ILocationProvider {
    void destruct();

    void sendLocation(Location location);
}
