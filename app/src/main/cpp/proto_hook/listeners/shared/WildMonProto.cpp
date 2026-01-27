//
//
//

#include "WildMonProto.h"
#include "PokemonProto.h"
#include "../../Logger.h"
#include "../../ProtoCache.h"

void *WildMonProto::getMonProto(void *wildMonProto) {
    auto pokemonProtoPtrOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetWildMonProtoPokemonProto());
    Logger::debug("Got Wild Pokemon Proto mon proto offset " + std::to_string(pokemonProtoPtrOffset));
    void **pokemonProtoPtr =
            reinterpret_cast<void **>(reinterpret_cast<char *>(wildMonProto) + pokemonProtoPtrOffset);
    Logger::debug("Got Wild Pokemon Proto mon proto ptr at "
                  + ProtoCache::convertPointerToReadableString(pokemonProtoPtr)
                  + " resulting in "
                  + ProtoCache::convertPointerToReadableString(*pokemonProtoPtr));
    return *pokemonProtoPtr;
}

unsigned long long WildMonProto::encounterId(void *wildMonProto) {

    auto encounterIdOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetWildMonProtoMonId());
    auto *encounterId = reinterpret_cast<unsigned long long *>(reinterpret_cast<char *>(wildMonProto) +
            encounterIdOffset);
    return *encounterId;
}

int32_t WildMonProto::monId(void *wildMonProto) {
    Logger::debug("Fetching PokemonProto of Wild Pokemon Proto");
    void* pokemonProto = WildMonProto::getMonProto(wildMonProto);
    Logger::debug("Fetching MonId of Wild Pokemon Proto");
    return PokemonProto::monId(pokemonProto);
}

System_String_o *WildMonProto::getSpawnpointId(void *wildMonProto) {
    int spawnpointIdOffset;
#if defined(__arm__)
    spawnpointIdOffset = 0x28;
#elif defined(__aarch64__)
    spawnpointIdOffset = 0x30;
#else
    Logger::debug("Unsupported arch");
        return;
#endif
    auto **spawnpointIdPtr =
            reinterpret_cast<System_String_o **>(reinterpret_cast<char *>(wildMonProto) + spawnpointIdOffset);
    return *spawnpointIdPtr;
}

LatLng WildMonProto::getLatLng(void *wildMonProto) {
    int latOffset;
    int lngOffset;
#if defined(__arm__)
    latOffset = 0x18;
    lngOffset = 0x20;
#elif defined(__aarch64__)
    latOffset = 0x20;
    lngOffset = 0x28;
#else
    Logger::debug("Unsupported arch");
        return;
#endif
    auto *latitude = reinterpret_cast<double *>(reinterpret_cast<char *>(wildMonProto) +
            latOffset);
    auto *longitude = reinterpret_cast<double *>(reinterpret_cast<char *>(wildMonProto) +
            lngOffset);
    LatLng location = LatLng();
    location.Latitude = *latitude;
    location.Longitude = *longitude;
    return location;
}
