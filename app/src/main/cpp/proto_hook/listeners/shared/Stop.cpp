//
//
//

#include "Stop.h"
#include "../../ProtoCache.h"
#include "../../Logger.h"

void Stop::spin(void *stop) {
/*
 *   if (!get_IsCoolingDownFunc(mapStopPtr) && get_IsPlayerInRangeFunc(mapStopPtr)) {
        console.log("Cooldown over")
        var pokestopInteractive = startInteractiveModeFunc(mapStopPtr);
        var spinner = get_ItemSpinnerFunc(pokestopInteractive);
        console.log("Got spinner " + spinner)
        sendSearchRpcFunc(spinner);
        console.log("Sent rpc")
        cleanupFunc(spinner);
        completeInteractiveModeFunc(mapStopPtr);
      }
 */
    ProtoCache &protoCache = ProtoCache::instance();

    // Logger::debug("Checking service instance");
    if (this->serviceInstance == nullptr) {
        Logger::debug("ServiceInstance null, fetching it");
        if (protoCache.getPlayerServiceInstance() == nullptr) {
            Logger::debug("getPlayerServiceInstance is null");
            return;
        }
        void* serviceGetInstance = ProtoCache::instance().getPlayerServiceInstance();

        Logger::debug("Got serviceGetInstance at " + ProtoCache::convertPointerToReadableString(serviceGetInstance));

        this->serviceInstance = serviceGetInstance;
    }

    // TODO: Also check if enabled/active?
    // TODO: Check if inventory is full
    //Logger::debug("Checking stop and bag");
    //Logger::debug("is Player in Range: " + std::to_string(isPlayerInRange(stop)));
    //Logger::debug("is Cooling Down: " + std::to_string(isCoolingDown(stop)));
    //Logger::debug("is Active: " + std::to_string(isActive(stop)));
    //Logger::debug("is Bag Full: " + std::to_string(isBagFull()));


    if (!isPlayerInRange(stop) || isCoolingDown(stop) || !isActive(stop) || isBagFull()) {
        return;
    }

    Logger::debug("Trying to spin");

    if (this->startInteractiveMode == nullptr) {
        void* startInteractiveModePtr = ProtoCache::instance().getStopStart();
        this->startInteractiveMode = (void* (*)(void *)) startInteractiveModePtr;
    }

    if (this->sendSearchRpc == nullptr) {
        void* sendSearchRpcPtr = ProtoCache::instance().getSendSearchRpc();
        this->sendSearchRpc = (void (*)(void *)) sendSearchRpcPtr;
    }

    if (this->cleanup == nullptr) {
        void* cleanupPtr = ProtoCache::instance().getStopClean();
        this->cleanup = (void (*)(void *)) cleanupPtr;
    }

    if (this->completeInteractiveMode == nullptr) {
        void* completeInteractiveModePtr = ProtoCache::instance().getStopComplete();
        this->completeInteractiveMode = (void (*)(void *)) completeInteractiveModePtr;
    }

    void* interactiveMode = this->startInteractiveMode(stop);
    void* itemSpinner = getSpinner(interactiveMode);
    this->sendSearchRpc(itemSpinner);
    this->cleanup(itemSpinner);
    this->completeInteractiveMode(stop);
}

bool Stop::isActive(void *stop) {
    if (this->get_isActive == nullptr) {
        void* getIsActivePtr = ProtoCache::instance().getStopActive();
        this->get_isActive = (bool (*)(void *)) getIsActivePtr;
    }

    return this->get_isActive(stop);
}

bool Stop::isPlayerInRange(void *stop) {
    if (this->get_IsPlayerInRange == nullptr) {
        void* getIsInRangePtr = ProtoCache::instance().getStopIsPlayerRange();
        this->get_IsPlayerInRange = (bool (*)(void *)) getIsInRangePtr;
    }

    return this->get_IsPlayerInRange(stop);
}

bool Stop::isBagFull() {
    if (this->serviceInstance == nullptr) {
        Logger::debug("ServiceInstance null, fetching it");
        if (ProtoCache::instance().getPlayerServiceInstance() == nullptr) {
            Logger::debug("getPlayerServiceInstance is null");
            return false;
        }

        this->serviceInstance = ProtoCache::instance().getPlayerServiceInstance();
        Logger::debug("Got serviceGetInstance at " + ProtoCache::convertPointerToReadableString(this->serviceInstance));
    }

    if (this->getBagIsFull == nullptr) {
        void* getBagIsFullPtr = ProtoCache::instance().getServiceBagFull();
        this->getBagIsFull = (bool (*)(void *)) getBagIsFullPtr;
    }

    Logger::debug("Checking if bag is full");
    return this->getBagIsFull(this->serviceInstance);
}

bool Stop::isCoolingDown(void *stop) {
    if (this->get_IsCoolingDown == nullptr) {
        void* getIsCooldownPtr = ProtoCache::instance().getStopIsCooldown();
        this->get_IsCoolingDown = (bool (*)(void *)) getIsCooldownPtr;
    }

    return this->get_IsCoolingDown(stop);
}

void *Stop::getSpinner(void *stopInteractive) {
    auto poiItemSpinnerOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getStopGetSpin());
    void **poiItemSpinnerPtr =
            reinterpret_cast<void **>(reinterpret_cast<char *>(stopInteractive) + poiItemSpinnerOffset);
    return *poiItemSpinnerPtr;
}
