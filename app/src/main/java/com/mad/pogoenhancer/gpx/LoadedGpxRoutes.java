package com.mad.pogoenhancer.gpx;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Point;
import io.ticofab.androidgpxparser.parser.domain.Route;
import io.ticofab.androidgpxparser.parser.domain.RoutePoint;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;

public class LoadedGpxRoutes {
    private HashMap<String, Gpx> _routes = null;

    LoadedGpxRoutes() {
        this._routes = new HashMap<>();
    }

    void setRoutes(HashMap<String, Gpx> routes) {
        this._routes = routes;
    }

    public HashMap<String, Gpx> getRoutes() {
        return new HashMap<>(this._routes);
    }

    synchronized boolean addRoute(String name, Gpx gpx) {
        if (this._routes == null || this._routes.containsKey(name)) {
            return false;
        } else {
            this._routes.put(name, gpx);
            return true;
        }
    }

    synchronized boolean removeRoute(String name) {
        return this._routes != null && this._routes.remove(name) != null;
    }


    synchronized Gpx getRoute(String gpxName) {
        return this._routes.get(gpxName);
    }

    synchronized String toJson() throws JSONException {
        JSONObject routesAsJson = new JSONObject();
        for (Map.Entry<String, Gpx> entry : this._routes.entrySet()) {
            String name = entry.getKey();
            Gpx gpx = entry.getValue();
            JSONObject gpxJson = new JSONObject();

            gpxJson.put("mVersion", gpx.getVersion());
            gpxJson.put("mCreator", gpx.getCreator());
            JSONArray waypointsJson = getJsonArrayPoints(gpx.getWayPoints());
            gpxJson.put("mWayPoints", waypointsJson);
            
            JSONArray routesJson = getJsonArrayRoutes(gpx.getRoutes());
            gpxJson.put("mRoutes", routesJson);
            
            JSONArray tracksJson = getJsonArrayTracks(gpx.getTracks());
            gpxJson.put("mTracks", tracksJson);

            routesAsJson.put(name, gpxJson);
        }

        return routesAsJson.toString();
    }

    synchronized void fromJson(String jsonStringified) throws JSONException {
        JSONObject routesAsJson = new JSONObject(jsonStringified);
        for (Iterator<String> it = routesAsJson.keys(); it.hasNext(); ) {
            String gpxName = it.next();
            JSONObject gpxObj = routesAsJson.getJSONObject(gpxName);

            Gpx.Builder gpxBuilder = new Gpx.Builder();
            if (gpxObj.has("mVersion")) {
                gpxBuilder.setVersion(gpxObj.getString("mVersion"));
            }
            if (gpxObj.has("mCreator")) {
                gpxBuilder.setCreator(gpxObj.getString("mCreator"));
            }

            // Parse waypoints
            List<WayPoint> waypoints = new ArrayList<>();
            if (gpxObj.has("mWayPoints")) {
                JSONArray waypointsJson = gpxObj.getJSONArray("mWayPoints");
                if (waypointsJson != null) {
                    for (int i = 0; i < waypointsJson.length(); i++) {
                        JSONObject point = waypointsJson.getJSONObject(i);
                        WayPoint.Builder pointBuilder = new WayPoint.Builder();
                        fillPointBuilder(point, pointBuilder);
                        waypoints.add(pointBuilder.build());
                    }
                }
            }
            gpxBuilder.setWayPoints(waypoints);

            // Parse routes
            List<Route> routes = new ArrayList<>();
            if (gpxObj.has("mRoutes")) {
                JSONArray routesJson = gpxObj.getJSONArray("mRoutes");
                if (routesJson != null) {
                    for (int i = 0; i < routesJson.length(); i++) {
                        JSONObject routeObj = routesJson.getJSONObject(i);
                        Route.Builder routeBuilder = new Route.Builder();

                        List<RoutePoint> routePoints = new ArrayList<>();
                        if (!routeObj.has("mRoutePoints")) {
                            continue;
                        }
                        JSONArray pointsJson = routeObj.getJSONArray("mRoutePoints");
                        for (int j = 0; j < pointsJson.length(); j++) {
                            JSONObject point = pointsJson.getJSONObject(j);
                            RoutePoint.Builder pointBuilder = new RoutePoint.Builder();
                            fillPointBuilder(point, pointBuilder);
                            routePoints.add(pointBuilder.build());
                        }
                        routeBuilder.setRoutePoints(routePoints);

                        if (routeObj.has("mRouteName")) {
                            routeBuilder.setRouteName(routeObj.getString("mRouteName"));
                        }
                        if (routeObj.has("mRouteDesc")) {
                            routeBuilder.setRouteDesc(routeObj.getString("mRouteDesc"));
                        }
                        if (routeObj.has("mRouteCmt")) {
                            routeBuilder.setRouteCmt(routeObj.getString("mRouteCmt"));
                        }
                        if (routeObj.has("mRouteSrc")) {
                            routeBuilder.setRouteSrc(routeObj.getString("mRouteSrc"));
                        }
                        if (routeObj.has("mRouteNumber")) {
                            routeBuilder.setRouteNumber(routeObj.getInt("mRouteNumber"));
                        }
                        if (routeObj.has("mRouteType")) {
                            routeBuilder.setRouteType(routeObj.getString("mRouteType"));
                        }

                        routes.add(routeBuilder.build());
                    }
                }
            }
            gpxBuilder.setRoutes(routes);

            // Parse tracks
            List<Track> tracks = new ArrayList<>();
            if (gpxObj.has("mTracks")) {
                JSONArray tacksJson = gpxObj.getJSONArray("mTracks");
                if (tacksJson != null) {
                    for (int i = 0; i < tacksJson.length(); i++) {
                        JSONObject trackObj = tacksJson.getJSONObject(i);
                        Track.Builder trackBuilder = new Track.Builder();
                        if (!trackObj.has("mTrackName")) {
                            continue;
                        }
                        trackBuilder.setTrackName(trackObj.getString("mTrackName"));

                        List<TrackSegment> trackSegments = new ArrayList<>();
                        if (!trackObj.has("mTrackSegments")) {
                            continue;
                        }
                        JSONArray trackSegmentsJson = trackObj.getJSONArray("mTrackSegments");
                        for (int j = 0; j < trackSegmentsJson.length(); j++) {
                            JSONObject trackSegmentJsonObject = trackSegmentsJson.getJSONObject(j);
                            TrackSegment.Builder trackSegmentBuilder = new TrackSegment.Builder();

                            List<TrackPoint> trackPoints = new ArrayList<>();
                            if (!trackSegmentJsonObject.has("mTrackPoints")) {
                                continue;
                            }
                            JSONArray trackPointsJsonArray = trackSegmentJsonObject.getJSONArray("mTrackPoints");
                            if (trackPointsJsonArray != null) {
                                for (int h = 0; h < trackPointsJsonArray.length(); h++) {
                                    JSONObject point = trackPointsJsonArray.getJSONObject(h);
                                    TrackPoint.Builder pointBuilder = new TrackPoint.Builder();
                                    fillPointBuilder(point, pointBuilder);
                                    trackPoints.add(pointBuilder.build());
                                }
                            }
                            trackSegmentBuilder.setTrackPoints(trackPoints);
                            trackSegments.add(trackSegmentBuilder.build());
                        }
                        trackBuilder.setTrackSegments(trackSegments);

                        if (trackObj.has("mTrackDesc")) {
                            trackBuilder.setTrackDesc(trackObj.getString("mTrackDesc"));
                        }
                        if (trackObj.has("mTrackCmt")) {
                            trackBuilder.setTrackCmt(trackObj.getString("mTrackCmt"));
                        }
                        if (trackObj.has("mTrackSrc")) {
                            trackBuilder.setTrackSrc(trackObj.getString("mTrackSrc"));
                        }
                        if (trackObj.has("mTrackNumber")) {
                            trackBuilder.setTrackNumber(trackObj.getInt("mTrackNumber"));
                        }
                        if (trackObj.has("mTrackType")) {
                            trackBuilder.setTrackType(trackObj.getString("mTrackType"));
                        }

                        tracks.add(trackBuilder.build());
                    }
                }
            }
            gpxBuilder.setTracks(tracks);

            this._routes.put(gpxName, gpxBuilder.build());
        }


    }

    private static void fillPointBuilder(JSONObject point, Point.Builder pointBuilder) throws JSONException {
        pointBuilder.setLatitude(point.getDouble("mLatitude"));
        pointBuilder.setLongitude(point.getDouble("mLongitude"));
        if (point.has("mElevation")) {
            pointBuilder.setElevation(point.getDouble("mElevation"));
        }
        if (point.has("mName")) {
            pointBuilder.setName(point.getString("mName"));
        }
        if (point.has("mDesc")) {
            pointBuilder.setDesc(point.getString("mDesc"));
        }
        if (point.has("mType")) {
            pointBuilder.setType(point.getString("mType"));
        }
    }

    private JSONArray getJsonArrayTracks(List<Track> tracks) throws JSONException {
        JSONArray tracksJson = new JSONArray();

        for (Track track : tracks) {
            JSONObject trackObj = new JSONObject();
            trackObj.put("mTrackName", track.getTrackName());

            JSONArray trackSegmentsArr = new JSONArray();
            for (TrackSegment segment : track.getTrackSegments()) {
                JSONObject segmentJson = new JSONObject();
                segmentJson.put("mTrackPoints", getJsonArrayPoints(segment.getTrackPoints()));
                trackSegmentsArr.put(segmentJson);
            }
            trackObj.put("mTrackDesc", track.getTrackDesc());
            trackObj.put("mTrackCmt", track.getTrackCmt());
            trackObj.put("mTrackSrc", track.getTrackSrc());
            trackObj.put("mTrackNumber", track.getTrackNumber());
            trackObj.put("mTrackType", track.getTrackType());
            // Omitting link
            tracksJson.put(trackObj);
        }
        return tracksJson;
    }

    private JSONArray getJsonArrayRoutes(List<Route> routes) throws JSONException {
        JSONArray routesArray = new JSONArray();

        for (Route route : routes) {
            JSONObject routeJson = new JSONObject();
            routeJson.put("mRoutePoints", getJsonArrayPoints(route.getRoutePoints()));
            routeJson.put("mRouteName", route.getRouteName());
            routeJson.put("mRouteDesc", route.getRouteDesc());
            routeJson.put("mRouteCmt", route.getRouteCmt());
            routeJson.put("mRouteSrc", route.getRouteSrc());
            routeJson.put("mRouteNumber", route.getRouteNumber());
            routeJson.put("mRouteType", route.getRouteType());
            // Omitting mRouteLink
            routesArray.put(routeJson);
        }
        return routesArray;
    }

    @NonNull
    private static JSONArray getJsonArrayPoints(List<? extends Point> wayPoints) throws JSONException {
        JSONArray waypointsJson = new JSONArray();
        for (Point wayPoint : wayPoints) {
            JSONObject waypointJsonObj = new JSONObject();
            waypointJsonObj.put("mLatitude", wayPoint.getLatitude());
            waypointJsonObj.put("mLongitude", wayPoint.getLongitude());
            waypointJsonObj.put("mElevation", wayPoint.getElevation());
            waypointJsonObj.put("mName", wayPoint.getName());
            waypointJsonObj.put("mDesc", wayPoint.getDesc());
            waypointJsonObj.put("mType", wayPoint.getType());

            waypointsJson.put(waypointJsonObj);
        }
        return waypointsJson;
    }
}
