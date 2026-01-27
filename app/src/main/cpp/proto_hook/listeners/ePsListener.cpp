//
//
//

#include "ePsListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"

#include "../InfoClient.h"
#include "../il2cpp/il2cppStructs.h"
#include "../geometry/s2cell.h"
#include "../geometry/s2latlngrect.h"
#include "shared/StringHelper.h"
#include "shared/LocProv.h"

void ePsListener::on_enter(Gum::AbstractInvocationContext *context) {
    void* rpcHandler = context->get_nth_argument_ptr(0);
    void* methodIdPtr = context->get_nth_argument_ptr(1);
    void* thirdArg = context->get_nth_argument_ptr(3);
    void* sixthArg = context->get_nth_argument_ptr(6);
    ProtoCache::instance().setRpcHandler(rpcHandler);

    Logger::debug("Third arg: " + ProtoCache::convertPointerToReadableString(thirdArg));
    Logger::debug("Sixth arg: " + ProtoCache::convertPointerToReadableString(sixthArg));
    // higher or lower 32bit for third arg?
    ProtoCache::instance().setWeirdArg(thirdArg);
    ProtoCache::instance().setWeirdSecondArg(sixthArg);
    int64_t methodId = 0;
    std::memcpy(&methodId, &methodIdPtr, sizeof(methodId));
    if(methodId == 102) {
        Logger::debug(
                "Encounter Third arg: " + ProtoCache::convertPointerToReadableString(thirdArg));

        EncP *encounterProto = reinterpret_cast<EncP *>(context->get_nth_argument_ptr(2));
        Logger::debug("Encounter ID: " + std::to_string(encounterProto->encounterId_));

    } else if (methodId == 903) {
        Logger::debug("Sending remove quest");
        RqP *removeQuestProto = reinterpret_cast<RqP *>(context->get_nth_argument_ptr(2));
        Logger::debug("String ptr: " + ProtoCache::convertPointerToReadableString(removeQuestProto->questId));
    }  else if (methodId == 901) {
        Logger::debug("RPC 901");
    } else if (methodId == 1405) {
        gRp *getRouteProto = reinterpret_cast<gRp *>(context->get_nth_argument_ptr(2));

    } else if (methodId == 106) {
        Logger::debug("GMO is being requested, reading s2 cell IDs");
        gMoP *getMapObjectsProto = reinterpret_cast<gMoP *>(context->get_nth_argument_ptr(2));
        Logger::debug("GMO request contains unknownfields: " + ProtoCache::instance().convertPointerToReadableString(getMapObjectsProto->_unknownFields));
        Logger::debug("GMO request contains "
                      + std::to_string(getMapObjectsProto->cellId_->getCount()) + " cells");
        for (int i = 0; i < getMapObjectsProto->cellId_->getCount(); i++) {
            ProtoCache::instance().addCellId(getMapObjectsProto->cellId_->getItem(i));
        }
    }
    Logger::debug("Send RPC " + std::to_string(methodId));
}

void ePsListener::on_leave(Gum::AbstractInvocationContext *context) {
}
