//
//
//

#include "UpdateWildPokemonListener.h"
#include "../ProtoCache.h"
#include "shared/PokemonProto.h"
#include "../Logger.h"
#include "../InjectionSettings.h"
#include "../EncQueue.h"
#include "shared/WildMapPokemon.h"
#include <random>
#include <list>

#define earthRadiusKm 6378.137

static void* monPtr = nullptr;

void UpdateWildPokemonListener::on_enter(Gum::AbstractInvocationContext *context) {
    monPtr = context->get_nth_argument_ptr(0);
}

void UpdateWildPokemonListener::on_leave(Gum::AbstractInvocationContext *context) {
    if(!InjectionSettings::instance().isEnableAutoencounter()) {
        Logger::debug("Autoencounter is disabled (wild)");
        return;
    }

    unsigned long long encounterId = WildMapPokemon::getEncounterId(monPtr);
    Logger::debug("Encounter ID of mon: " + std::to_string(encounterId));

    void* (*startEncounter)(void*);
    startEncounter = (void* (*)(void*)) (ProtoCache::instance().getSer());

    if (startEncounter == nullptr || monPtr == nullptr) {
        Logger::debug("Got invalid address for encounters");
        return;
    } else if (!EncQueue::instance().worthEncountering(encounterId)) {
        Logger::info("Not worth encountering");
        return;
    }
    Logger::debug("Try encountering mon");

    std::time_t curTimeT = std::time(nullptr);
    long currentTime = static_cast<long>(curTimeT);
    // add 1800s (30minutes) to the encounter

    int32_t (*getMonId)(void *);
    getMonId = (int32_t (*)(void *)) (ProtoCache::instance().getGetWildPokemonID());
    int32_t monId = getMonId(monPtr);
    Logger::debug("Mon ID: " + std::to_string(monId));

    void *gameMasterDataInstance = ProtoCache::instance().getGameMasterData();

    bool shouldEncounter = true;
    Logger::debug("gameMasterDataInstance: " + ProtoCache::instance().convertPointerToReadableString(gameMasterDataInstance));

    if (gameMasterDataInstance) {
        void *(*monSettingsProto)(void *, int32_t);
        monSettingsProto = (void *(*)(void *,
                                      int32_t)) (ProtoCache::instance().get_GetPokemonSettingsByID());
        void *monSetting = monSettingsProto(gameMasterDataInstance, monId);


        auto typ1Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_pSpt1o());
        auto *typ1 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(monSetting) + typ1Offset);

        auto typ2Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_pSpt2o());
        auto *typ2 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(monSetting) + typ2Offset);

        shouldEncounter = (ProtoCache::instance().getWildMon(*typ1) and *typ1 > 0) or
                          (ProtoCache::instance().getWildMon(*typ2) and *typ2 > 0);

        Logger::debug("shouldEncounter: " + std::to_string(shouldEncounter));

        if (ProtoCache::instance().getHideWildMon(monId)) {
            shouldEncounter = false;
        }
    }

    if (shouldEncounter) {
        startEncounter(monPtr);
        EncQueue::instance().addEncounterSent(encounterId);
    }

    Logger::pdebug("Done sending encounter");

}

double UpdateWildPokemonListener::deg2rad(double deg) {
    return (deg * M_PI / 180);
}

double UpdateWildPokemonListener::calculateDistanceInMeters(LatLng from, LatLng to) {
    double dLat = deg2rad(to.Latitude) - deg2rad(from.Latitude);
    double dLon = deg2rad(to.Longitude) - deg2rad(from.Longitude);
    double a = sin(dLat / 2) * sin(dLat / 2)
               + cos(deg2rad(from.Latitude)) * cos(deg2rad(to.Latitude))
                 * sin(dLon / 2) * sin(dLon / 2);
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    double d = earthRadiusKm * c;
    return d * 1000;
}
