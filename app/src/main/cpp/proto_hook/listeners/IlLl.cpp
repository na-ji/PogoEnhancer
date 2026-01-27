//
//
//

#include "IlLl.h"
#include "shared/PokemonProto.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void IlLl::on_enter(Gum::AbstractInvocationContext *context) {
}

void IlLl::on_leave(Gum::AbstractInvocationContext *context) {
    void* monProto = context->get_return_value_ptr();
    if (monProto != nullptr) {
        std::string ivSummary = PokemonProto::getIvDataSummary(monProto);
        ProtoCache::instance().setLatestIvSummary(ivSummary);
    }
}
