#include "PokemonBagListener.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"

void PokemonBagListener::on_enter(Gum::AbstractInvocationContext *context) {

    void* pokemonBagService = context->get_nth_argument_ptr(0);
    if (!ProtoCache::instance().getPokemonBagService()) {
        ProtoCache::instance().setPokemonBagService(pokemonBagService);
        Logger::debug("Got pokemonbag: " + ProtoCache::convertPointerToReadableString(pokemonBagService));
    }

}

void PokemonBagListener::on_leave(Gum::AbstractInvocationContext *context) {

}
