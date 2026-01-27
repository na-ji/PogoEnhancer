//
//
//

#ifndef POGODROID_INSPECTORCALLBACKS_H
#define POGODROID_INSPECTORCALLBACKS_H

#include "AbsractInvocationContext.h"

namespace Gum {
    class InspectorCallbacks {
    public:
        virtual ~InspectorCallbacks () {}

        virtual void inspect_worst_case (AbstractInvocationContext * context, char * output_buf, unsigned int output_buf_len) = 0;
    };
}

#endif //POGODROID_INSPECTORCALLBACKS_H
