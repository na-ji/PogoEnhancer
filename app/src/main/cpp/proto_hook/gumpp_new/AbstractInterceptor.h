//
//
//

#ifndef POGODROID_ABSTRACTINTERCEPTOR_H
#define POGODROID_ABSTRACTINTERCEPTOR_H

#include "AbstractInvocationContext.h"
#include "AbstractInvocationListener.h"

namespace Gum {

    class AbstractInterceptor : public Gum::Object {
    public:
        virtual bool attach_listener (void * function_address, AbstractInvocationListener * listener, void * listener_function_data = 0) = 0;
        virtual void detach_listener (AbstractInvocationListener * listener) = 0;

        virtual void replace_function (void * function_address, void * replacement_address, void * replacement_function_data = 0) = 0;
        virtual void revert_function (void * function_address) = 0;

        virtual void begin_transaction () = 0;
        virtual void end_transaction () = 0;

        virtual AbstractInvocationContext * get_current_invocation () = 0;

        virtual void ignore_current_thread () = 0;
        virtual void unignore_current_thread () = 0;

        virtual void ignore_other_threads () = 0;
        virtual void unignore_other_threads () = 0;
    };
}

#endif //POGODROID_ABSTRACTINTERCEPTOR_H
