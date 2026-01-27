#include "BuddyRpcServiceListener.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"

void BuddyRpcServiceListener::on_enter(Gum::AbstractInvocationContext *context) {

    void* buddyRpcService = context->get_nth_argument_ptr(0);
    if (!ProtoCache::instance().getBuddyRpcService()) {
        ProtoCache::instance().setBuddyRpcService(buddyRpcService);
        Logger::debug("Got buddyRpcService: " + ProtoCache::convertPointerToReadableString(buddyRpcService));
    }

}

void BuddyRpcServiceListener::on_leave(Gum::AbstractInvocationContext *context) {

}
