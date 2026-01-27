//
//
//

#include "AutoFeed.h"
#include "../../ProtoCache.h"
#include "../../InjectionSettings.h"
#include "../../Logger.h"

void AutoFeed::feed(void* encState, void* iMapPokemon) {

    if (!ProtoCache::instance().getItemBag()) {
        Logger::debug("No item bag!");
        return;
    }

    if (!InjectionSettings::instance().isPinapMode()) {
        Logger::debug("Pinap Mode is disabled - abort");
        return;
    }

    //check if already fed a berry
    void *canUseBerryPtr = ProtoCache::instance().getGet_CanUseBerry();
    bool (*canUseBerry)(void *) = (bool (*)(void *)) (canUseBerryPtr);
    bool canUseBerryOnMon = canUseBerry(encState);

    if (!canUseBerryOnMon) {
        Logger::debug("Mon already fed - abort");
        return;
    }

    void *itemCountPtr = ProtoCache::instance().getGet_ItemCount();
    int (*getItemCount)(void *, int) = (int (*)(void *, int)) (itemCountPtr);
    int berryCount = getItemCount(ProtoCache::instance().getItemBag(), 705);
    Logger::debug("Found berries count: " + std::to_string(berryCount));

    if (berryCount>0) {
        Logger::debug("Found enough berries");

        Logger::debug("Feed berry (proto)");
        void *useItemPtr = ProtoCache::instance().getUseItemEncounter();
        void* (*useItem)(void *, int, void*) = (void* (*)(void *, int, void*)) (useItemPtr);
        useItem(ProtoCache::instance().getItemBag(), 705, iMapPokemon);

        // set berry on GUI
        void *setBerryPtr = ProtoCache::instance().getSetBerry();
        void* (*setBerry)(void *, int) = (void* (*)(void *, int)) (setBerryPtr);
        Logger::debug("Set Berry (GUI)");
        setBerry(encState, 705);

    } else {
        Logger::debug("Not enought berries - abort");
    };

}
