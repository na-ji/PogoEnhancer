#include "PlayerServiceInstance.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"

void PlayerInstanceListener::on_enter(Gum::AbstractInvocationContext *context) {

    void* playerInstance = context->get_nth_argument_ptr(0);
    if (!ProtoCache::instance().getPlayerServiceInstance()) {
        ProtoCache::instance().setPlayerServiceInstance(playerInstance);
        Logger::debug("Got playerInstance: " + ProtoCache::convertPointerToReadableString(playerInstance));
    }

}

void PlayerInstanceListener::on_leave(Gum::AbstractInvocationContext *context) {
    void* retVal = context->get_return_value_ptr();
}
