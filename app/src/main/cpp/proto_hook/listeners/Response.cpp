//
//
//

#include "Response.h"
#include "../Logger.h"
#include "../ProtoCache.h"
#include "../il2cpp/il2cppStructs.h"
#include "shared/LocProv.h"


void readLatLon(void *throttleInstance) {
    int locationProviderOffset;
#if defined(__arm__)
    locationProviderOffset = 0x18;
#elif defined(__aarch64__)
    locationProviderOffset =  0x38;
#else
    Logger::debug("Unsupported arch");
    return;
#endif

    //LatLng** lastLocationPtr = reinterpret_cast<LatLng **>(reinterpret_cast<char*>(throttleInstance) + lastLocationOffset);
    auto **locationProvider = reinterpret_cast<void **>(reinterpret_cast<char *>(throttleInstance) +
                                                        locationProviderOffset);
    LocProv::instance().updateCurrentLocation(*locationProvider, true);
}



void Response::on_enter(Gum::AbstractInvocationContext *context) {
    actionRequestInstance = context->get_nth_argument_ptr(0);
    actionResponseInstance = context->get_nth_argument_ptr(1);
    //Logger::debug("OnResponse called of " +  ProtoCache::convertPointerToReadableString(actionRequestInstance)
    //    + " with response at " + ProtoCache::convertPointerToReadableString(actionResponseInstance));
    if (actionRequestInstance == nullptr || actionResponseInstance == nullptr) {
        Logger::error("Nullpointer response or request, aborting.");
        return;
    }
    ActionRequest *actionRequest = static_cast<ActionRequest *>(actionRequestInstance);
    ActionResponse *actionResponse = static_cast<ActionResponse *>(actionResponseInstance);

    Logger::fatal("Got method " + std::to_string(actionRequest->method));

    void *throttleInstance = ProtoCache::instance().getThrottleInstance();
    if (throttleInstance != nullptr) {
        readLatLon(throttleInstance);
    }
    Logger::debug("Checking pointer coded input stream");
    if (actionResponse->codedInputStream == nullptr) {
        Logger::error("Invalid data in response, aborting.");
        return;
    }
    /*
    Logger::debug("Checking pointer state");
    if (actionResponse->codedInputStream.state == nullptr) {
        Logger::error("State is null, aborting.");
        return;
    }*/
    Logger::debug("Size with cast: " + std::to_string(actionResponse->codedInputStream->state.bufferSize));
    std::vector<uint8_t> dataRead = std::vector<uint8_t>();
    try {
        for (int i = 0; i < actionResponse->codedInputStream->state.bufferSize; i++) {
            //Logger::debug("Pushing data");
            uint8_t foo = actionResponse->codedInputStream->buffer->m_Items[i];
            /*if (foo == 0) {
                return;
            }*/
            dataRead.push_back(foo);
        }
    } catch (...) {
        Logger::error("Failed fetching data");
        return;
    }

    Logger::debug("Size copied: " + std::to_string(dataRead.size()));

    std::time_t curTimeT = std::time(nullptr);
    long currentTime = static_cast<long>(curTimeT);
    //Logger::debug("getCombatChallengeRpcService" + ProtoCache::instance().convertPointerToReadableString(ProtoCache::instance().getCombatChallengeRpcService()));
    //Logger::debug("getRpcHandler" + ProtoCache::instance().convertPointerToReadableString(ProtoCache::instance().getRpcHandler()));

    ProtoCache::instance().addData(actionRequest->method, dataRead, currentTime);
}

void Response::on_leave(Gum::AbstractInvocationContext *context) {

}
