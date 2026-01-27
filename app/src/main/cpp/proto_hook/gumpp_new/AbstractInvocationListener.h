//
//
//

#ifndef POGODROID_ABSTRACTINVOCATIONLISTENER_H
#define POGODROID_ABSTRACTINVOCATIONLISTENER_H

#include "AbstractInvocationContext.h"

namespace Gum {
    class AbstractInvocationListener {
    public:
            virtual ~AbstractInvocationListener () {}

            virtual void on_enter (AbstractInvocationContext * context) = 0;
            virtual void on_leave (AbstractInvocationContext * context) = 0;
    };
}

#endif //POGODROID_ABSTRACTINVOCATIONLISTENER_H
