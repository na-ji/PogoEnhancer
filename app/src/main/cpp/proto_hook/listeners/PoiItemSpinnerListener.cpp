//
//
//

#include "PoiItemSpinnerListener.h"
#include "../Logger.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"


static bool second = false;
static void* instanceOfPoiSpinner = nullptr;

void PoiItemSpinnerListener::on_enter(Gum::AbstractInvocationContext *context) {
    Logger::debug("Spinner loading");
    instanceOfPoiSpinner = context->get_nth_argument_ptr(0);
}

void PoiItemSpinnerListener::on_leave(Gum::AbstractInvocationContext *context) {

//
//    if(!second) {
//        second = true;
//        return;
//    } else {
//        second = false;
//    }
    ProtoCache &protoCache = ProtoCache::instance();
    if(protoCache.getTypecode() == Userlevel::NONE
       || protoCache.getTypecode() == Userlevel::FREE) {
        Logger::pdebug("Insufficient rights");
        return;
    }
//    } else if (InjectionSettings::instance().getScanmode() == Scanmode::NOTHING) {
//        Logger::pdebug("No scanmode set");
//        return;
//    }
//    void* instance = context->get_return_value_ptr();
//    void* instance = context->get_nth_argument_ptr(0);
    if (instanceOfPoiSpinner == nullptr) {
        Logger::debug("invalid poi address");
        return;
    }
    void* instance = instanceOfPoiSpinner;
    Logger::debug("Got PoiItemSpinner at " + ProtoCache::convertPointerToReadableString(instance));

    void* sendSearch = protoCache.getSendSearchRpc();
    Logger::debug("Got sendSearch at " + ProtoCache::convertPointerToReadableString(sendSearch));

    if(instance != nullptr && sendSearch != nullptr
        && InjectionSettings::instance().isSpin()) {
        Logger::debug("Sending search...");

        void* (*sendSearchRpc)(void*);
        sendSearchRpc = (void* (*)(void*)) (sendSearch);
        sendSearchRpc(instance);
        Logger::debug("Done sending search request");
    }
    instanceOfPoiSpinner = nullptr;
}