#include "BreakoutFromPokeball.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "shared/AutoFeed.h"



void BreakoutFromPokeball::on_enter(Gum::AbstractInvocationContext *context) {
    encounterInstance = context->get_nth_argument_ptr(0);
}

void BreakoutFromPokeball::on_leave(Gum::AbstractInvocationContext *context) {

    Logger::debug("Found breakout mon encounter: " + ProtoCache::convertPointerToReadableString(encounterInstance));

    void *encounterPokemonPtr = ProtoCache::instance().getGet_EncPokemon();
    void* (*encounterPoke)(void *) = (void* (*)(void *)) (encounterPokemonPtr);
    void* encounterPokemon = encounterPoke(encounterInstance);

    Logger::debug("Found breakout mon encounter Pokemon: " + ProtoCache::convertPointerToReadableString(encounterPokemon));

    void *iMapPokemonPtr = ProtoCache::instance().getGet_IMapPokemon();
    void* (*iMapPokemon)(void *) = (void* (*)(void *)) (iMapPokemonPtr);
    void* mappokemon = iMapPokemon(encounterPokemon);
    Logger::debug("Found mappokemon of encounter: " +
                  ProtoCache::convertPointerToReadableString(mappokemon));

    if (mappokemon) {
        AutoFeed::feed(encounterInstance, mappokemon);
    }

}
