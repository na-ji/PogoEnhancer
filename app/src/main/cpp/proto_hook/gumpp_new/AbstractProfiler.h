//
//
//

#ifndef POGODROID_ABSTRACTPROFILER_H
#define POGODROID_ABSTRACTPROFILER_H

#include "structs.h"
#include "AbstractSampler.h"
#include "InspectorCallbacks.h"

namespace Gum {
    class AbstractProfiler {
    public:
        virtual void instrument_functions_matching (const char * match_str, AbstractSampler * sampler, FunctionMatchCallbacks * match_callbacks = 0) = 0;
        virtual bool instrument_function (void * function_address, AbstractSampler * sampler) = 0;
        virtual bool instrument_function_with_inspector (void * function_address, AbstractSampler * sampler, InspectorCallbacks * inspector_callbacks) = 0;

        virtual ProfileReport * generate_report () = 0;
    };
}

#endif //POGODROID_ABSTRACTPROFILER_H
