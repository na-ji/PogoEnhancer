//
//
//

#ifndef POGODROID_CALLCOUNTSAMPLER_H
#define POGODROID_CALLCOUNTSAMPLER_H

#include "structs.h"
#include "AbstractSampler.h"

namespace Gum {
    class CallCountSampler : public AbstractSampler {
    public:
        virtual void add_function (void * function_address) = 0;
        virtual Sample peek_total_count () const = 0;
    };
}

#endif //POGODROID_CALLCOUNTSAMPLER_H
