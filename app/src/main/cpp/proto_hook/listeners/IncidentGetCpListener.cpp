//
//
//

#include "IncidentGetCpListener.h"
#include "../ProtoCache.h"
#include "shared/PokemonProto.h"

void IncidentGetCpListener::on_enter(Gum::AbstractInvocationContext *context) {
    void *questMon = context->get_nth_argument_ptr(0);

    void* (*getMonProto)(void *);
    getMonProto = (void* (*)(void *))(ProtoCache::instance().getGetPokemonIncidentMon());
    void* monProto = getMonProto(questMon);

    PokemonProto::sendIvData(monProto);

    std::string ivSummary = PokemonProto::getIvDataSummary(monProto);
    ProtoCache::instance().setLatestIvSummary(ivSummary);
}

void IncidentGetCpListener::on_leave(Gum::AbstractInvocationContext *context) {

}
