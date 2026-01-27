//
//
//

#include "UpdateStop.h"
#include "shared/Stop.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"

void UpdateStop::on_enter(Gum::AbstractInvocationContext *context) {
    currentStop = context->get_nth_argument_ptr(0);
}

void UpdateStop::on_leave(Gum::AbstractInvocationContext *context) {
    ProtoCache &protoCache = ProtoCache::instance();
    if (InjectionSettings::instance().isSpin()) {
        Stop::instance().spin(this->currentStop);
    }
}
