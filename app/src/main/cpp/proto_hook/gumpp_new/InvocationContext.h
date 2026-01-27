//
//
//

#ifndef POGODROID_INVOCATIONCONTEXT_H
#define POGODROID_INVOCATIONCONTEXT_H

#include "AbstractInvocationContext.h"

namespace Gum {
    class InvocationContext : public AbstractInvocationContext {
    public:
        InvocationContext(GumInvocationContext *ctx) : context (ctx), parent (NULL)
        {

        }

        virtual ~InvocationContext ()
        {
            delete parent;
        }

        virtual void * get_function () const
        {
            return GUM_FUNCPTR_TO_POINTER (context->function);
        }

        virtual void * get_nth_argument_ptr (unsigned int n) const
        {
            return gum_invocation_context_get_nth_argument (context, n);
        }

        virtual void replace_nth_argument (unsigned int n, void * value)
        {
            gum_invocation_context_replace_nth_argument (context, n, value);
        }

        virtual void * get_return_value_ptr () const
        {
            return gum_invocation_context_get_return_value (context);
        }

        virtual unsigned int get_thread_id () const
        {
            return gum_invocation_context_get_thread_id (context);
        }

        virtual void * get_listener_thread_data_ptr (size_t required_size) const
        {
            return gum_invocation_context_get_listener_thread_data (context, required_size);
        }

        virtual void * get_listener_function_data_ptr () const
        {
            return gum_invocation_context_get_listener_function_data (context);
        }

        virtual void * get_listener_function_invocation_data_ptr (size_t required_size) const
        {
            return gum_invocation_context_get_listener_invocation_data (context, required_size);
        }

        virtual void * get_replacement_function_data_ptr () const
        {
            return gum_invocation_context_get_replacement_data (context);
        }

        virtual CpuContext * get_cpu_context () const
        {
            return reinterpret_cast<CpuContext *> (context->cpu_context);
        }

    private:
        GumInvocationContext * context;
        InvocationContext * parent;
    };
}

#endif //POGODROID_INVOCATIONCONTEXT_H
