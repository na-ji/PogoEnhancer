//
//
//

#include "GRscGSoListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void GRscGSoListener::on_enter(Gum::AbstractInvocationContext *context) {
    void* giftingRpcService = context->get_nth_argument_ptr(0);
    Logger::debug("Updating (GRscGSo) friends list page ref to " + ProtoCache::convertPointerToReadableString(giftingRpcService));
    ProtoCache::instance().setGiftingRpcService(giftingRpcService);
}

void GRscGSoListener::on_leave(Gum::AbstractInvocationContext *context) {

}
