//
//
//

#ifndef POGODROID_LOCPROV_H
#define POGODROID_LOCPROV_H

// 2 Implementations exist
//public class NativeLocationProvider : MonoBehaviour, ILocationProvider // TypeDefIndex: 18141
//public class LocationProviderAdapter : ILocationProvider // TypeDefIndex: 12892

#include <cstdint>
#include "../../ProtoCache.h"

/**
 * To select one of the 2, select it via boolean
 */
class LocProv {
public:
    /**
     *
     * @param locationProvider
     * @param nativeProvider true if the NativeLocationProvider is to be used
     */

    LocProv(const LocProv&) = delete;
    LocProv& operator=(const LocProv &) = delete;
    LocProv(LocProv &&) = delete;
    LocProv & operator=(LocProv &&) = delete;

    static auto& instance(){
        static LocProv LocProv;
        return LocProv;
    }

    static void updateCurrentLocation(void* locationProvider, bool nativeProvider);

    static void setLocation(void* locationProvider, LatLng location);
    static LatLng getCenterOfS2Cell(uint64_t cellId);
    void setLastCooldownLocation(LatLng lastcooldownlocation);
    LatLng getLastCooldownLocation();

    bool needCoolDown(LatLng currentLocation);
    static double calculateDistanceInMeters(LatLng from, LatLng to);

    void setInitCooldownInfo(double lat, double lng, long timestamp);


private:
    LocProv () = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */

    double lastcooldowntime = 0.0;
    std::mutex lastCooldownLocationLock;
    LatLng lastcooldownlocation;


};


#endif //POGODROID_LOCPROV_H
