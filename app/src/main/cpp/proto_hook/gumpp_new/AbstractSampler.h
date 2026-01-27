//
//
//

#ifndef POGODROID_ABSTRACTSAMPLER_H
#define POGODROID_ABSTRACTSAMPLER_H

#include "structs.h"

namespace Gum {
    class AbstractSampler : Gum::Object {
    public:
        virtual Sample sample () const = 0;
    };
}

#endif //POGODROID_ABSTRACTSAMPLER_H
