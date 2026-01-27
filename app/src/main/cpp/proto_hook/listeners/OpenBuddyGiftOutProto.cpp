#include "OpenBuddyGiftOutProto.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"

void OpenBuddyGiftOutProto::on_enter(Gum::AbstractInvocationContext *context) {

    ProtoCache::instance().setBuddyGetGift(true);
    Logger::debug("Buddy receive a gift");
}

void OpenBuddyGiftOutProto::on_leave(Gum::AbstractInvocationContext *context) {

}
