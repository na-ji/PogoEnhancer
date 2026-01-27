//
//
//

#include "LocProv.h"
#include "../../Logger.h"
#include "../../ProtoCache.h"
#include "../../geometry/s2cell.h"
#include "../../geometry/s2latlngrect.h"
#include "../../UnixSender.h"

#define earthRadiusKm 6378.137


void LocProv::updateCurrentLocation(void *locationProvider, bool nativeProvider) {
    int latlngOffset;
#if defined(__arm__)
    if (nativeProvider) {
        latlngOffset = 0x10;
    } else {
        latlngOffset = 0x18;
    }
#elif defined(__aarch64__)
    latlngOffset = 0x20;
#else
    Logger::debug("Unsupported arch");
        return;
#endif

    // Get Stop Lat / Lng
    //Logger::debug("Current location in LocationProvider at " +
    //             ProtoCache::convertPointerToReadableString(locationProvider));

    auto *currentLocation = reinterpret_cast<LatLng *>(reinterpret_cast<char *>(locationProvider) +
                                                       latlngOffset);
    ProtoCache::instance().setLatLng(currentLocation->Latitude, currentLocation->Longitude);
}

void LocProv::setLocation(void *locationProvider, LatLng location) {
    int latlngOffset;
#if defined(__arm__)
    latlngOffset = 0x10;
#elif defined(__aarch64__)
    latlngOffset = 0x20;
#else
    Logger::debug("Unsupported arch");
        return;
#endif

    auto *currentLocation = reinterpret_cast<LatLng *>(reinterpret_cast<char *>(locationProvider) +
                                                       latlngOffset);
    Logger::debug("Old location: " + std::to_string(currentLocation->Latitude) + ", " + std::to_string(currentLocation->Longitude)
                  + " modified to " + std::to_string(location.Latitude) + ", " + std::to_string(location.Longitude) );
    currentLocation->Latitude = location.Latitude;
    currentLocation->Longitude = location.Longitude;
}

LatLng LocProv::getCenterOfS2Cell(uint64_t cellId) {
    LatLng centerLatLng;

    Logger::debug("Calculating center lat/lng of cell " + std::to_string(cellId));
    try {
        S2Cell cell = S2Cell(S2CellId(cellId));
        S2LatLngRect bounds = cell.GetRectBound();

        centerLatLng.Latitude = bounds.GetCenter().lat().degrees();
        centerLatLng.Longitude = bounds.GetCenter().lng().degrees();

        return centerLatLng;

    } catch(...) {
        Logger::debug("Failed reading center lat/lng from cell");
        return centerLatLng;
    }
}

void LocProv::setLastCooldownLocation(LatLng lastcooldownlocation) {

    if(needCoolDown(lastcooldownlocation)) {
        Logger::debug("Need more cooldown - dont set this location");
        return;
    }

    this->lastCooldownLocationLock.lock();
    Logger::debug("Last Cooldown location: " + std::to_string(lastcooldownlocation.Latitude) + ", " + std::to_string(lastcooldownlocation.Longitude));
    this->lastcooldownlocation = lastcooldownlocation;
    this->lastCooldownLocationLock.unlock();

    std::time_t curTimeT = std::time(nullptr);
    this->lastcooldowntime = curTimeT;
    std::string sendText = to_string(lastcooldownlocation.Latitude) + "," +
                           to_string(lastcooldownlocation.Longitude) + "," + to_string(curTimeT);

    UnixSender::sendMessage(MESSAGE_TYPE::COOLDOWN, sendText, ProtoCache::instance().getSymmKey());
}


LatLng LocProv::getLastCooldownLocation() {
    lastCooldownLocationLock.lock();
    LatLng latLngCopy = LatLng();
    latLngCopy = lastcooldownlocation;
    lastCooldownLocationLock.unlock();
    return latLngCopy;
}

void LocProv::setInitCooldownInfo(double lat, double lng, long timestamp) {
    this->lastcooldownlocation.Latitude = lat;
    this->lastcooldownlocation.Longitude = lng;
    this->lastcooldowntime = timestamp;
}

bool LocProv::needCoolDown(LatLng currentLocation) {
    if (this->lastcooldownlocation.Latitude == 0.0 || this->lastcooldownlocation.Longitude == 0.0 || this->lastcooldowntime == 0.0) {
        // seems to be a fresh round / PD start - return true
        return false;
    }

    LatLng lastAction = LatLng();
    lastAction.Latitude = this->lastcooldownlocation.Latitude;
    lastAction.Longitude = this->lastcooldownlocation.Longitude;
    double speed = 16.67; // for calculation of cooldown
    double distance = calculateDistanceInMeters(this->lastcooldownlocation, currentLocation);  // distance in meters
    Logger::info("Distance to last Action in meters: " + std::to_string(distance));

    // MAD Chaos for getting cooldown delay
    if (distance >= 1335000) {
        speed = 180.43;
    } else if (distance >= 1100000) {
        speed = 176.2820513;
    } else if (distance >= 1020000) {
        speed = 168.3168317;
    } else if (distance >= 1007000) {
        speed = 171.2585034;
    } else if (distance >= 948000) {
        speed = 166.3157895;
    } else if (distance >= 900000) {
        speed = 164.8351648;
    } else if (distance >= 897000) {
        speed = 166.1111111;
    } else if (distance >= 839000) {
        speed = 158.9015152;
    } else if (distance >= 802000) {
        speed = 155.1269841;
    } else if (distance >= 751000) {
        speed = 152.6422764;
    } else if (distance >= 700000) {
        speed = 151.5151515;
    } else if (distance >= 650000) {
        speed = 146.3963964;
    } else if (distance >= 600000) {
        speed = 142.8571429;
    } else if (distance >= 550000) {
        speed = 138.8888889;
    } else if (distance >= 500000) {
        speed = 134.4086022;
    } else if (distance >= 450000) {
        speed = 129.3103448;
    } else if (distance >= 400000) {
        speed = 123.4567901;
    } else if (distance >= 350000) {
        speed = 116.6666667;
    } else if (distance >= 328000) {
        speed = 113.8888889;
    } else if (distance >= 300000) {
        speed = 108.6956522;
    } else if (distance >= 250000) {
        speed = 101.6260163;
    } else if (distance >= 201000) {
        speed = 90.54054054;
    } else if (distance >= 175000) {
        speed = 85.78431373;
    } else if (distance >= 150000) {
        speed = 78.125;
    } else if (distance >= 125000) {
        speed = 71.83908046;
    } else if (distance >= 100000) {
        speed = 64.1025641;
    } else if (distance >= 90000) {
        speed = 60;
    } else if (distance >= 80000) {
        speed = 55.55555556;
    } else if (distance >= 70000) {
        speed = 50.72463768;
    } else if (distance >= 60000) {
        speed = 47.61904762;
    } else if (distance >= 45000) {
        speed = 39.47368421;
    } else if (distance >= 40000) {
        speed = 35.0877193;
    } else if (distance >= 35000) {
        speed = 32.40740741;
    } else if (distance >= 30000) {
        speed = 29.41176471;
    } else if (distance >= 25000) {
        speed = 27.77777778;
    } else if (distance >= 20000) {
        speed = 27.77777778;
    } else if (distance >= 15000) {
        speed = 25.77777778;
    } else if (distance >= 10000) {
        speed = 23.80952381;
    } else if (distance >= 8000) {
        speed = 22.66666667;
    } else if (distance >= 5000) {
        speed = 22.34137623;
    } else if (distance >= 4000) {
        speed = 22.22222222;
    }

    Logger::info("Speed: " + std::to_string(speed));

    double delay_used = distance / speed;
    if (delay_used > 7200) {
        delay_used = 7200;
    }
    Logger::info("Cooldown delay: " + std::to_string(delay_used));

    // getting current time
    std::time_t curTimeT = std::time(nullptr);
    long currentTime = static_cast<long>(curTimeT);
    Logger::info("Current time: " + std::to_string(currentTime));
    Logger::info("Last action time: " + std::to_string(this->lastcooldowntime));

    // check if cooldown is reached

    if ((lastcooldowntime + delay_used) <= currentTime) {
        Logger::info("Cooled down - do action");
        return false;
    }
    // we need more time
    Logger::info("Still need more cooldown");
    return true;

}

double deg2rad(double deg) {
    return (deg * M_PI / 180);
}

// https://stackoverflow.com/questions/639695/how-to-convert-latitude-or-longitude-to-meters
double LocProv::calculateDistanceInMeters(LatLng from, LatLng to) {
    double dLat = deg2rad(to.Latitude) - deg2rad(from.Latitude);
    double dLon = deg2rad(to.Longitude) - deg2rad(from.Longitude);
    double a = sin(dLat / 2) * sin(dLat / 2)
               + cos(deg2rad(from.Latitude)) * cos(deg2rad(to.Latitude))
                 * sin(dLon / 2) * sin(dLon / 2);
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    double d = earthRadiusKm * c;
    return d * 1000;
}

