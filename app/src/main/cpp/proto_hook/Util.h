#ifndef POGODROID_UTIL_H
#define POGODROID_UTIL_H

struct LatLng {
    void* unknownField;
    double Latitude;
    double Longitude;
};

class Util {
public:
    static double getDistanceInMeters(const LatLng &from, const LatLng &to);

    static double deg2rad(double deg);
};


#endif //POGODROID_UTIL_H
