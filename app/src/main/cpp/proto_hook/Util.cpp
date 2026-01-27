#include "Util.h"
#include <cmath>
#include <cmath>
#include <zconf.h>

#define earthRadiusKm 6378.137


double Util::deg2rad(double deg) {
    return (deg * M_PI / 180);
}

double Util::getDistanceInMeters(const LatLng &from, const LatLng &to) {
    double dLat = deg2rad(to.Latitude) - deg2rad(from.Latitude);
    double dLon = deg2rad(to.Longitude) - deg2rad(from.Longitude);
    double a = sin(dLat / 2) * sin(dLat / 2)
               + cos(deg2rad(from.Latitude)) * cos(deg2rad(to.Latitude))
                 * sin(dLon / 2) * sin(dLon / 2);
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    double d = earthRadiusKm * c;
    return d * 1000;
}
