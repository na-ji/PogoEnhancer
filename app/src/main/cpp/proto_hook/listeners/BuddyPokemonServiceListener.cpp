#include "BuddyPokemonServiceListener.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"

void BuddyPokemonServiceListener::on_enter(Gum::AbstractInvocationContext *context) {

    void* buddyService = context->get_nth_argument_ptr(0);
    if (!ProtoCache::instance().getBuddyService()) {
        ProtoCache::instance().setBuddyService(buddyService);
        Logger::debug("Got buddyService: " + ProtoCache::convertPointerToReadableString(buddyService));
    }

}

void BuddyPokemonServiceListener::on_leave(Gum::AbstractInvocationContext *context) {

}
