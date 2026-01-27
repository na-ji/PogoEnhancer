#ifndef POGODROID_ABSTRACTBACKTRACER_H
#define POGODROID_ABSTRACTBACKTRACER_H

#include "structs.h"

namespace Gum {
    class AbstractBacktracer : public Gum::Object {
    public:
        virtual void generate (const CpuContext * cpu_context, ReturnAddressArray & return_addresses) const = 0;
    };
}

#endif //POGODROID_ABSTRACTBACKTRACER_H
