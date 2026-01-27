//
//
//

#ifndef POGODROID_INVOCATIONLISTENERIFACE_H
#define POGODROID_INVOCATIONLISTENERIFACE_H

#include "AbstractInvocationContext.h"

namespace Gum {
    class InvocationListenerIface : public Object {
    public:
        virtual void on_enter (AbstractInvocationContext * context) = 0;
        virtual void on_leave (AbstractInvocationContext * context) = 0;
    };
}

#endif //POGODROID_INVOCATIONLISTENERIFACE_H
