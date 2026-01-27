//
//
//

#include <string>
#include "WildMapPokemon.h"
#include "../../Logger.h"
#include "../../ProtoCache.h"

unsigned long long WildMapPokemon::getEncounterId(void* mapPokemonInstancePtr) {
    void* getEncounterIdPtr = ProtoCache::instance().getGetEncounterId();
    if(getEncounterIdPtr) {
        Logger::debug("Valid address for getEncounterId received, calling: "
                      + ProtoCache::convertPointerToReadableString(getEncounterIdPtr) + " on "
                      + ProtoCache::convertPointerToReadableString(mapPokemonInstancePtr));
    } else {
        Logger::debug("Invalid address for getEncounterId, abort");
        return 0;
    }
    unsigned long long (*getEncounterId)(void *);
    getEncounterId = (unsigned long long (*)(void *)) getEncounterIdPtr;

    unsigned long long encounterId = getEncounterId(mapPokemonInstancePtr);
    Logger::debug("Retrieved encounter ID: " + std::to_string(encounterId));
    return encounterId;
}


