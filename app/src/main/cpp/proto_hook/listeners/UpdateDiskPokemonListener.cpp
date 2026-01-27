//
//
//

#include "UpdateDiskPokemonListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../InjectionSettings.h"
#include "../EncQueue.h"
#include "shared/WildMapPokemon.h"
#include <random>
#include <list>


void UpdateDiskPokemonListener::on_enter(Gum::AbstractInvocationContext *context) {
    Logger::debug("UpdateDiskPokemonListener enter");
    monPtr = context->get_nth_argument_ptr(0);
}

void UpdateDiskPokemonListener::on_leave(Gum::AbstractInvocationContext *context) {
    Logger::debug("UpdateDiskPokemonListener leave");
    if(!InjectionSettings::instance().isEnableAutoencounter()) {
        Logger::debug("Autoencounter is disabled (disk mon)");
        return;
    }

    void* (*startEncounter)(void*);
    startEncounter = (void* (*)(void*)) (ProtoCache::instance().getSendDiskEncounterRequestFunctionPointer());

    if (startEncounter == nullptr || monPtr == nullptr) {
        Logger::debug("Got invalid address for encounters");
        return;
    } else {
        Logger::debug("Encountering mon");
    }
    Logger::info("Starting disk encounter");
    // TODO: Check encounter ID already checked?
    startEncounter(monPtr);
    Logger::pdebug("Done sending encounter");

}
