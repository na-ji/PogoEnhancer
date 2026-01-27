//
//
//

#ifndef POGODROID_INVOCATIONLISTENERPROXY_H
#define POGODROID_INVOCATIONLISTENERPROXY_H

#include "structs.h"
#include "InvocationListenerIface.h"
#include "AbstractInvocationListener.h"

namespace Gum {

class InvocationListenerProxy : public Gum::InvocationListenerIface
{
    public:
        InvocationListenerProxy (AbstractInvocationListener * listener);
        virtual ~InvocationListenerProxy ();

        virtual void ref ();
        virtual void unref ();
        virtual void * get_handle () const;

        virtual void on_enter (AbstractInvocationContext * context);
        virtual void on_leave (AbstractInvocationContext * context);

    protected:
        GumInvocationListenerProxy * cproxy;
        AbstractInvocationListener * listener;
    };
}

#endif //POGODROID_INVOCATIONLISTENERPROXY_H
