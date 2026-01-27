package com.mad.shared;

import java.util.Set;

public class Constants {

    public interface SHAREDPERFERENCES_KEYS {
        String LAST_LOCATION_LATITUDE = "last_location_latitude";
        String LAST_LOCATION_LONGITUDE = "last_location_longitude";
        String LAST_LOCATION_ALTITUDE = "last_location_altitude";
    }

    public interface DEFAULT_VALUES {
        Long LAST_LOCATION_LATITUDE = 49L;
        Long LAST_LOCATION_LONGITUDE = 9L;
        Long LAST_LOCATION_ALTITUDE = 150L;
    }

    public interface GEO {
        int EARTH_RADIUS = 6371000;
    }
}
