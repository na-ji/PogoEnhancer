//
//
//

#include "RaidGetCpListener.h"
#include "../ProtoCache.h"
#include "shared/PokemonProto.h"

void RaidGetCpListener::on_enter(Gum::AbstractInvocationContext *context) {
    if(ProtoCache::instance().getTypecode() >= Userlevel::PREMIUM) {
        void *questMon = context->get_nth_argument_ptr(0);

        void* (*getMonProto)(void *);
        getMonProto = (void* (*)(void *))(ProtoCache::instance().getGet_Pokemon_RaidMapPokemon());
        void* monProto = getMonProto(questMon);

        PokemonProto::sendIvData(monProto);

        std::string ivSummary = PokemonProto::getIvDataSummary(monProto);
        ProtoCache::instance().setLatestIvSummary(ivSummary);
    }
}

void RaidGetCpListener::on_leave(Gum::AbstractInvocationContext *context) {

}
