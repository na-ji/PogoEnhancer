#include <string>
#include "IncenseMapPokemon.h"
#include "../../Logger.h"
#include "../../ProtoCache.h"

unsigned long long IncenseMapPokemon::getEncounterId(void* mapPokemonInstancePtr) {
    Logger::debug("IncenseMapPokemon::on_enter");

    void* getIncenseEncounterIdPtr = ProtoCache::instance().getGetIncenseEncounterId();
    if(getIncenseEncounterIdPtr) {
        Logger::debug("Valid address for getEncounterId (Incense) received, calling: "
                      + ProtoCache::convertPointerToReadableString(getIncenseEncounterIdPtr) + " on "
                      + ProtoCache::convertPointerToReadableString(mapPokemonInstancePtr));
    } else {
        Logger::debug("Invalid address for getEncounterId (Incense), abort");
        return 0;
    }
    unsigned long long (*getIncenseEncounterId)(void *);
    getIncenseEncounterId = (unsigned long long (*)(void *)) getIncenseEncounterIdPtr;

    unsigned long long encounterId = getIncenseEncounterId(mapPokemonInstancePtr);
    Logger::debug("Retrieved encounter ID (Incense): " + std::to_string(encounterId));
    return encounterId;
}



