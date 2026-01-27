//
//
//

#include "ItemBagInstanceListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void ItemBagInstanceListener::on_enter(Gum::AbstractInvocationContext *context) {
    void *itemBagPtr = context->get_nth_argument_ptr(0);
    Logger::debug("Getting itemBag Instance: " + ProtoCache::instance().convertPointerToReadableString(itemBagPtr));
    ProtoCache::instance().setItemBagInstance(itemBagPtr);
    if (!ProtoCache::instance().getItemBag()) {
        ProtoCache::instance().setItemBag(itemBagPtr);
    }

}

void ItemBagInstanceListener::on_leave(Gum::AbstractInvocationContext *context) {
    void* retVal = context->get_return_value_ptr();


}
