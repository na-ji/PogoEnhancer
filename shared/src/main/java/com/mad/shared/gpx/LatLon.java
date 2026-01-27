package com.mad.shared.gpx;

import android.annotation.Nullable;

import com.mad.shared.Constants;

public class LatLon {
    private double lat = 0.0f;
    private double lon = 0.0f;
    private float speed = 0.0f; //TODO
    private Integer satellites = null;
    private Double altitude = null;

    public LatLon(double lat, double lon) {
        lat = lat % 90; // java returns negative remainders :)
        lon = lon % 180;

        this.setLat(lat);
        this.setLon(lon);
    }

    public synchronized void setLocation(LatLon newLoc) {
        this.setLat(newLoc.getLat());
        this.setLon(newLoc.getLon());
    }

    public float getSpeed() {
        return speed;
    }

    public synchronized void setSpeed(float speed) {
        if (speed < 0) {
            this.speed = 0;
            return;
        }
        this.speed = speed;
    }

    public synchronized void setAltitude(Double alt) {
        this.altitude = alt;
    }

    public Double getAltitude() {
        return this.altitude;
    }

    public synchronized void setSatellites(Integer satellites) {
        this.satellites = satellites;
    }

    public Integer getSatellites() {
        return this.satellites;
    }

    public double getLat() {
        return lat;
    }

    public synchronized void setLat(double lat) {
        if (lat < -90.0f || lat > 90.0f) {
            return;
        }
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public synchronized void setLon(double lon) {
        // normalize longitude to the interval ]-180,180]
        this.lon = lon % 360.0f;
        if (this.lon > 180.0f) {
            this.lon -= 360.0f;
        }
    }

    /**
     *
     * @param endpoint
     * @return Distance in meters to endpoint provided
     */
    public synchronized double distance(LatLon endpoint) {
        double dLat = Math.toRadians(endpoint.getLat() - this.getLat());
        double dLng = Math.toRadians(endpoint.getLon() - this.getLon());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.getLat())) * Math.cos(Math.toRadians(endpoint.getLat())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Constants.GEO.EARTH_RADIUS * c;
    }

    public synchronized double distanceetweenpoints(LatLon startpoint, LatLon endpoint) {
        double dLat = Math.toRadians(endpoint.getLat() - startpoint.getLat());
        double dLng = Math.toRadians(endpoint.getLon() - startpoint.getLon());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startpoint.getLat())) * Math.cos(Math.toRadians(endpoint.getLat())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Constants.GEO.EARTH_RADIUS * c;
    }

    /*
    Degrees to reach the endpoint
     */
    public synchronized double bearing(LatLon endpoint) {
        if (this.getLat() > 90.0f) {
            return 180.0f; // starting from north pole -> the only direction is south
        } else if (this.getLat() < -90.0f) {
            return 0.0f; // starting from south pole -> can only move north
        }
        double latStartRad = Math.toRadians(this.getLat());
        double latEndRad = Math.toRadians(endpoint.getLat());
        double lonDiff = Math.toRadians(endpoint.getLon() - this.getLon());

        double bearing = Math.atan2(
                Math.sin(lonDiff) * Math.cos(latEndRad),
                Math.cos(latStartRad) * Math.sin(latEndRad)
                        - Math.sin(latStartRad) * Math.cos(latEndRad) * Math.cos(lonDiff)
        );
        return Math.toDegrees(bearing);
//        double bearingDegrees = bearing * (180.0f / Math.PI); // convert to degrees
//        bearingDegrees = (bearingDegrees > 0.0f ? bearingDegrees : (360.0f + bearingDegrees)); // correct discontinuity
//        return bearingDegrees;
    }

    public synchronized void add(CourseDistance courseDistance) {
        double latStartRad = Math.toRadians(this.getLat());
//        double lonStartRad = Math.toRadians(this.getLon());
        double dRad = courseDistance.getDistance() / Constants.GEO.EARTH_RADIUS;
        double cRad = Math.toRadians(courseDistance.getCourse());
        double sinStartLat = Math.sin(latStartRad);
        double cosStartLat = Math.cos(latStartRad);
        double sinD = Math.sin(dRad);
        double cosD = Math.cos(dRad);

        double sinLat = sinStartLat * cosD + cosStartLat * sinD * Math.cos(cRad);
        double lonDiff = Math.atan2(
                Math.sin(cRad) * sinD * cosStartLat,
                cosD - sinStartLat * sinLat
        );

        this.lat = Math.toDegrees(Math.asin(sinLat));
        this.lon = (this.getLon() + Math.toDegrees(lonDiff)) % 360;
        if (this.lon > 180) {
            lon -= 360;
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof LatLon)) {
            return false;
        }

        LatLon toCompareAgainst = (LatLon) obj;

        return toCompareAgainst.getLat() == this.lat && toCompareAgainst.getLon() == this.lon;
    }
}
