//
//
//

#include "MemDerp.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../geometry/s2cell.h"
#include "../geometry/s2latlngrect.h"
#include "shared/LocProv.h"
#include "shared/StringHelper.h"


void MemDerp::on_enter(Gum::AbstractInvocationContext *context) {
/*
    // Clear hashset<ulong> of memDerp
    auto setOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getMemSetClear());
    auto **setToBeClearedPtr = reinterpret_cast<HashSet_Ulong **>(reinterpret_cast<char *>(self) +
                                                                  setOffset);
    HashSet_Ulong* set = *setToBeClearedPtr;
    Logger::debug("Set size: " + std::to_string(set->getCount()));
*/
    void* thisInstance = context->get_nth_argument_ptr(0);
    //Logger::debug("MemDerp");

    void *throttleInstance = ProtoCache::instance().getThrottleInstance();
    if (throttleInstance == nullptr) {
        return;
    }
    //Logger::debug("Checking for a changed location");
    //readLatLon(throttleInstance);
}

void MemDerp::on_leave(Gum::AbstractInvocationContext *context) {


}
