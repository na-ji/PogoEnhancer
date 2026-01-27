//
//
//

#include "UpdateIncensePokemonListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../InjectionSettings.h"
#include "../EncQueue.h"
#include "shared/IncenseMapPokemon.h"
#include <random>

static void* monPtr = nullptr;

void UpdateIncensePokemonListener::on_enter(Gum::AbstractInvocationContext *context) {
    monPtr = context->get_nth_argument_ptr(0);
}

void UpdateIncensePokemonListener::on_leave(Gum::AbstractInvocationContext *context) {
    if(!InjectionSettings::instance().isEnableAutoencounter()) {
        Logger::debug("Autoencounter is disabled (incense)");
        return;
    }
//    Logger::debug("Update wild pokemon called");
//    void *wildMapPokemon = context->get_nth_argument_ptr(0);
//    Logger::debug("Got wildMapPokemon at " + ProtoCache::convertPointerToReadableString(monPtr));
    unsigned long long encounterId = IncenseMapPokemon::getEncounterId(monPtr);
    Logger::debug("Encounter ID (Incense) of mon: " + std::to_string(encounterId));

    void* (*startEncounter)(void*);
    startEncounter = (void* (*)(void*)) (ProtoCache::instance().getSendIncenseEncounterRequestFunctionPointer());

    if (startEncounter == nullptr || monPtr == nullptr) {
        Logger::debug("Got invalid address for encounters  (Incense)");
        return;
    } else if (!EncQueue::instance().worthEncountering(encounterId)) {
        return;
    }
    Logger::debug("Encountering incense mon");
    startEncounter(monPtr);
    EncQueue::instance().addEncounterSent(encounterId);
    Logger::pdebug("Done sending encounter");
}
