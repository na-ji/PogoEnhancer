//
//
//

#include "RaidMapPokemonCreateListener.h"
#include "../ProtoCache.h"
#include "shared/PokemonProto.h"
#include "../Logger.h"

/*
 * 	public override RaidMapPokemon Create(RaidEncounterProto proto); // 0x7CD2D0
 * We can call public override PokemonProto get_Pokemon(); // 0x7CCDA8
 * to get the PokemonProto and read IV there :)
 */

void RaidMapPokemon_CreateListener::on_enter(Gum::AbstractInvocationContext *context) {
}

void RaidMapPokemon_CreateListener::on_leave(Gum::AbstractInvocationContext *context) {
    Logger::info("RaidMapPokemon being created");
    void* raidMapPokemon = context->get_return_value_ptr();
    void* get_pokemonPtr = ProtoCache::instance().getGet_Pokemon_RaidMapPokemon();

    int (*get_pokemon)(void *);
    get_pokemon = (int (*)(void *))(get_pokemonPtr);
    int pokemonProto = get_pokemon(raidMapPokemon);

    Logger::info("Trying to read IV of RaidMapPokemon");
    PokemonProto::sendIvData(&pokemonProto);

}