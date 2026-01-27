//
//
//

#ifndef POGODROID_INVOCATIONCONTEXTIMPL_H
#define POGODROID_INVOCATIONCONTEXTIMPL_H

#include "structs.h"

namespace Gum {
    class AbstractInvocationContext {
    public:
        virtual ~AbstractInvocationContext () {}

        virtual void * get_function () const = 0;

        template <typename T>
        T get_nth_argument (unsigned int n) const
        {
            return static_cast<T> (get_nth_argument_ptr (n));
        }
        virtual void * get_nth_argument_ptr (unsigned int n) const = 0;
        virtual void replace_nth_argument (unsigned int n, void * value) = 0;
        template <typename T>
        T get_return_value () const
        {
            return static_cast<T> (get_return_value_ptr ());
        }
        virtual void * get_return_value_ptr () const = 0;

        virtual unsigned int get_thread_id () const = 0;

        template <typename T>
        T * get_listener_thread_data () const
        {
            return static_cast<T *> (get_listener_thread_data_ptr (sizeof (T)));
        }
        virtual void * get_listener_thread_data_ptr (size_t required_size) const = 0;
        template <typename T>
        T * get_listener_function_data () const
        {
            return static_cast<T *> (get_listener_function_data_ptr ());
        }
        virtual void * get_listener_function_data_ptr () const = 0;
        template <typename T>
        T * get_listener_function_invocation_data () const
        {
            return static_cast<T *> (get_listener_function_invocation_data_ptr (sizeof (T)));
        }
        virtual void * get_listener_function_invocation_data_ptr (size_t required_size) const = 0;

        template <typename T>
        T * get_replacement_function_data () const
        {
            return static_cast<T *> (get_replacement_function_data_ptr ());
        }
        virtual void * get_replacement_function_data_ptr () const = 0;

        virtual CpuContext * get_cpu_context () const = 0;
    };
}

#endif //POGODROID_INVOCATIONCONTEXTIMPL_H
