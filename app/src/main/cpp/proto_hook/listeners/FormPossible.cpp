//
//
//

#include "FormPossible.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void FormPossible::on_enter(Gum::AbstractInvocationContext *context) {
    void* gameMasterDataInstance = context->get_nth_argument_ptr(0);

    //Logger::debug("Game Master Data FormPossible called");
    ProtoCache::instance().setGameMasterData(gameMasterDataInstance);

}

void FormPossible::on_leave(Gum::AbstractInvocationContext *context) {

}
