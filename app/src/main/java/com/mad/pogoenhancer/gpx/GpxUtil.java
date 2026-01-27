package com.mad.pogoenhancer.gpx;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.mad.pogoenhancer.Logger;
import com.mad.shared.gpx.LatLon;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Route;
import io.ticofab.androidgpxparser.parser.domain.RoutePoint;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;
import io.ticofab.androidgpxparser.parser.domain.WayPoint;

public class GpxUtil {

    public static List<LatLon> transformAllSpotsOfGpxToOneDimension(Gpx gpx) {
        List<LatLon> routeToBeTaken = new ArrayList<>();
        if (gpx != null) {
            readWaypoints(gpx, routeToBeTaken);
            readRoutes(gpx, routeToBeTaken);
            readTracks(gpx, routeToBeTaken);
        }
        return routeToBeTaken;
    }

    public static boolean generateGfx(File file, String name, LatLon location) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        name = "<name>" + name + "</name><trkseg>\n";

        String segments = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        segments += "<trkpt lat=\"" + location.getLat() + "\" lon=\"" + location.getLon() + "\"><time>" + df.format(new Date()) + "</time></trkpt>\n";

        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments);
            writer.append(footer);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            Logger.error("PogoEnhancerJ", "Exception: " + e.getLocalizedMessage());

        }
        return false;
    }

    public static Gpx readToGpx(Context ctx, Uri uri) {
        if (ctx == null) {
            return null;
        }
        ContentResolver contentResolver = ctx.getContentResolver();
        GPXParser gpxParser = new GPXParser();
        Gpx result = null;
        try {
            result = gpxParser.parse(contentResolver.openInputStream(uri));
        } catch (XmlPullParserException e) {
            Logger.error("PogoEnhancerJ", "XML Parse Exception: " + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("PogoEnhancerJ", "I/O Exception: " + e.getLocalizedMessage());
        }

        return result;
    }

    @NotNull
    static Gpx cloneRelevantGpx(Gpx gpx) {
        List<WayPoint> wayPoints = new ArrayList<>();
        List<Track> tracks = new ArrayList<>();
        List<Route> routes = new ArrayList<>();
        Gpx.Builder builder = new Gpx.Builder();
        builder.setVersion(gpx.getVersion());
        builder.setCreator(gpx.getCreator());

        WayPoint.Builder wpBuilder = new WayPoint.Builder();
        for (WayPoint wayPoint : gpx.getWayPoints()) {
            wpBuilder.setLatitude(wayPoint.getLatitude());
            wpBuilder.setLongitude(wayPoint.getLongitude());
            wpBuilder.setElevation(wayPoint.getElevation());
            wayPoints.add(wpBuilder.build());
        }

        Route.Builder routeBuilder = new Route.Builder();
        for (Route route : gpx.getRoutes()) {
            RoutePoint.Builder routePointBuilder = new RoutePoint.Builder();
            List<RoutePoint> points = new ArrayList<>();
            for (RoutePoint routePoint : route.getRoutePoints()) {
                routePointBuilder.setLatitude(routePoint.getLatitude());
                routePointBuilder.setLongitude(routePoint.getLongitude());
                routePointBuilder.setElevation(routePoint.getElevation());
                points.add(routePointBuilder.build());
            }
            routeBuilder.setRoutePoints(points);
            routes.add(routeBuilder.build());
        }

        Track.Builder trackBuilder = new Track.Builder();
        for (Track track : gpx.getTracks()) {
            TrackSegment.Builder trackSegmentBuilder = new TrackSegment.Builder();
            List<TrackSegment> trackSegments = new ArrayList<>();

            for (TrackSegment trackSegment : track.getTrackSegments()) {
                TrackPoint.Builder trackPointBuilder = new TrackPoint.Builder();
                List<TrackPoint> trackPoints = new ArrayList<>();
                for (TrackPoint trackPoint : trackSegment.getTrackPoints()) {
                    trackPointBuilder.setLatitude(trackPoint.getLatitude());
                    trackPointBuilder.setLongitude(trackPoint.getLongitude());
                    trackPointBuilder.setElevation(trackPoint.getElevation());
                    trackPoints.add(trackPointBuilder.build());
                }
                trackSegmentBuilder.setTrackPoints(trackPoints);
                trackSegments.add(trackSegmentBuilder.build());
            }
            trackBuilder.setTrackSegments(trackSegments);
            tracks.add(trackBuilder.build());
        }

        builder.setWayPoints(wayPoints);
        builder.setRoutes(routes);
        builder.setTracks(tracks);
        return builder.build();
    }


    private static void readWaypoints(Gpx gpx, List<LatLon> routeToBeTaken) {
        List<WayPoint> wayPoints = gpx.getWayPoints();
        for (WayPoint wayPoint : wayPoints) {
            Logger.debug("PogoEnhancerJ", "    point: lat " + wayPoint.getLatitude() + ", lon " + wayPoint.getLongitude());
            routeToBeTaken.add(new LatLon(wayPoint.getLatitude(), wayPoint.getLongitude()));

        }
    }

    private static void readRoutes(Gpx gpx, List<LatLon> routeToBeTaken) {
        List<Route> routes = gpx.getRoutes();
        for (Route route : routes) {
            List<RoutePoint> routePoints = route.getRoutePoints();
            for (RoutePoint routePoint : routePoints) {
                routeToBeTaken.add(new LatLon(routePoint.getLatitude(), routePoint.getLongitude()));
            }
        }
    }

    private static void readTracks(Gpx gpx, List<LatLon> routeToBeTaken) {
        List<Track> tracks = gpx.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            Logger.debug("PogoEnhancerJ", "track " + i + ":");
            List<TrackSegment> segments = track.getTrackSegments();
            for (int j = 0; j < segments.size(); j++) {
                TrackSegment segment = segments.get(j);
                Logger.debug("PogoEnhancerJ", "segment " + j + ":");
                for (TrackPoint trackPoint : segment.getTrackPoints()) {
                    Logger.debug("PogoEnhancerJ", "point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
                    routeToBeTaken.add(new LatLon(trackPoint.getLatitude(), trackPoint.getLongitude()));
                }
            }
        }
    }
}
