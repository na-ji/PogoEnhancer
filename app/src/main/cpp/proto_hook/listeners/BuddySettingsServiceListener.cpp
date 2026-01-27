#include "BuddySettingsServiceListener.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"
#include "shared/StringHelper.h"
#include "../UnixSender.h"

void BuddySettingsService::on_enter(Gum::AbstractInvocationContext *context) {

    int useBerryItem = 0;
    int minBerryNeeded = 1;

    if(!InjectionSettings::instance().isUseNanny()) {
        return;
    }

    if (!ProtoCache::instance().getBuddyService() || !ProtoCache::instance().getItemBag() ||
    !ProtoCache::instance().getBuddyRpcService()) {
        Logger::debug("No Buddy Service/RPC or itemBag found!");
        return;
    }

    void *openGiftPtr = ProtoCache::instance().getBuddyOpenGift();
    void (*openGift)(void *) = (void (*)(void *)) (openGiftPtr);

    if(ProtoCache::instance().getBuddyGetGift()) {
        Logger::debug("Buddy found a gift - open it");

        UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Buddy open up a gift",
                                ProtoCache::instance().getSymmKey());
        ProtoCache::instance().setBuddyGetGift(false);

    }

    void *hasBuddyPtr = ProtoCache::instance().getHasBuddy();
    bool (*hasBuddy)(void *) = (bool (*)(void *)) (hasBuddyPtr);
    bool getHasBuddy = hasBuddy(ProtoCache::instance().getBuddyService());

    void *buddyOnMapPtr = ProtoCache::instance().getBuddyOnMap();
    bool (*buddyOnMap)(void *) = (bool (*)(void *)) (buddyOnMapPtr);
    bool getBuddyOnMap = buddyOnMap(ProtoCache::instance().getBuddyService());

    if (!ProtoCache::instance().processBuddy() && getBuddyOnMap) {
        return;
    }
    //Logger::info("Care buddy");

    if(!getBuddyOnMap) {
        minBerryNeeded = 5;
    }

    Logger::debug("Min berries needed: " + to_string(minBerryNeeded));

    void *feedBuddyPtr = ProtoCache::instance().getFeedBuddy();
    void (*feedBuddy)(void *, int, int) = (void (*)(void *, int, int)) (feedBuddyPtr);

    void *petBuddyPtr = ProtoCache::instance().getPetBuddy();
    void (*petBuddy)(void *) = (void (*)(void *)) (petBuddyPtr);

    void *itemCountPtr = ProtoCache::instance().getGet_ItemCount();
    int (*ItemCount)(void *, int) = (int (*)(void *, int)) (itemCountPtr);

    int razzBerryCount = ItemCount(ProtoCache::instance().getItemBag(), 701);
    int nanabBerryCount = ItemCount(ProtoCache::instance().getItemBag(), 703);
    int pinapBerryCount = ItemCount(ProtoCache::instance().getItemBag(), 705);

    if (!getHasBuddy) {
        Logger::debug("No buddy set - breakup");
        UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Theres no buddy",
                                ProtoCache::instance().getSymmKey());
        return;
    }

    if (razzBerryCount>=minBerryNeeded) {
        useBerryItem = 701;
    } else if (nanabBerryCount>=minBerryNeeded) {
        useBerryItem = 703;
    } else if (pinapBerryCount>=minBerryNeeded) {
        useBerryItem = 705;
    } else {
        Logger::debug("Dont found any berry - breakup");
        UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Nanny needs more berries!",
                                ProtoCache::instance().getSymmKey());
        return;
    }

    Logger::debug("Use berry: " + std::to_string(useBerryItem));




    if (!getBuddyOnMap) {
        Logger::debug("Activating buddy nanny");
        UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Activating nanny",
                                ProtoCache::instance().getSymmKey());
        openGift(ProtoCache::instance().getBuddyRpcService());
        feedBuddy(ProtoCache::instance().getBuddyRpcService(), useBerryItem, minBerryNeeded);
        petBuddy(ProtoCache::instance().getBuddyRpcService());
    } else {
        UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Cuddling buddy",
                                ProtoCache::instance().getSymmKey());
        Logger::debug("Cuddling buddy");
        openGift(ProtoCache::instance().getBuddyRpcService());
        feedBuddy(ProtoCache::instance().getBuddyRpcService(), useBerryItem, minBerryNeeded);
        petBuddy(ProtoCache::instance().getBuddyRpcService());
    }

    Logger::debug("Happy buddy left");



}

void BuddySettingsService::on_leave(Gum::AbstractInvocationContext *context) {

}
