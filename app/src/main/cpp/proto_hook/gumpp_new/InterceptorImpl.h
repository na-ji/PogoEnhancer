//
//
//

#ifndef POGODROID_INTERCEPTORIMPL_H
#define POGODROID_INTERCEPTORIMPL_H

#include <map>
#include <frida-gumjs.h>
#include "Runtime.h"
#include "InvocationListenerProxy.h"
#include "RefPtr.h"
#include "AbstractInterceptor.h"
#include "ObjectWrapper.hpp"
#include "InvocationContext.h"
#define APPNAME "ProtoHook"

#include <sstream>
namespace Gum {
    class InterceptorImpl : public ObjectWrapper<InterceptorImpl, AbstractInterceptor, GumInterceptor> {
    public:
        InterceptorImpl() {
            Runtime::ref();
            g_mutex_init (&mutex);
            assign_handle (gum_interceptor_obtain ());
        }

        virtual ~InterceptorImpl ()
        {
            g_mutex_clear (&mutex);
            Runtime::unref ();
        }

        virtual bool attach_listener (void * function_address, AbstractInvocationListener * listener, void * listener_function_data)
        {
            RefPtr<InvocationListenerProxy> proxy;

            g_mutex_lock (&mutex);
            ProxyMap::iterator it = proxy_by_listener.find (listener);
            if (it == proxy_by_listener.end ())
            {
                proxy = RefPtr<InvocationListenerProxy> (new InvocationListenerProxy (listener));
                proxy_by_listener[listener] = proxy;
            }
            else
            {
                proxy = it->second;
            }
            g_mutex_unlock (&mutex);

            //GumAttachReturn attach_ret = gum_interceptor_attach_listener (handle, function_address, GUM_INVOCATION_LISTENER (proxy->get_handle ()), listener_function_data);
            GumAttachReturn attach_ret = gum_interceptor_attach (handle, function_address, GUM_INVOCATION_LISTENER (proxy->get_handle ()), listener_function_data);
            return (attach_ret == GUM_ATTACH_OK);
        }

        virtual void detach_listener (AbstractInvocationListener * listener)
        {
            RefPtr<InvocationListenerProxy> proxy;

            g_mutex_lock (&mutex);
            ProxyMap::iterator it = proxy_by_listener.find (listener);
            if (it != proxy_by_listener.end ())
            {
                proxy = RefPtr<InvocationListenerProxy> (it->second);
                proxy_by_listener.erase (it);
            }
            g_mutex_unlock (&mutex);

            if (proxy.is_null ())
                return;

            //gum_interceptor_detach_listener(handle, GUM_INVOCATION_LISTENER (proxy->get_handle ()));
            gum_interceptor_detach (handle, GUM_INVOCATION_LISTENER (proxy->get_handle ()));
        }

        virtual void replace_function (void * function_address, void * replacement_address, void * replacement_function_data)
        {
#if defined(__arm__)
            gum_interceptor_replace (handle, function_address, replacement_address, replacement_function_data);
#elif defined(__aarch64__)
            gum_interceptor_replace (handle, function_address, replacement_address, replacement_function_data, nullptr);
#else
            Logger::debug("Unsupported arch");
        return;
#endif
        }

        virtual void revert_function (void * function_address)
        {
            //gum_interceptor_revert_function(handle, function_address);
            gum_interceptor_revert (handle, function_address);
        }

        virtual void begin_transaction ()
        {
            gum_interceptor_begin_transaction (handle);
        }

        virtual void end_transaction ()
        {
//            __android_log_print(ANDROID_LOG_FATAL, APPNAME, "Calling gum_interceptor_end_transaction.");
            const void * address = static_cast<const void*>(handle);
//            std::stringstream ss;
//            ss << address;
//            std::string name = ss.str();
//            __android_log_print(ANDROID_LOG_FATAL, APPNAME, "%s", name.c_str());

            gum_interceptor_end_transaction (handle);
        }

        virtual AbstractInvocationContext * get_current_invocation ()
        {
            GumInvocationContext * context = gum_interceptor_get_current_invocation ();
            if (context == NULL)
                return NULL;
            return new InvocationContext (context);
        }

        virtual void ignore_current_thread ()
        {
            gum_interceptor_ignore_current_thread (handle);
        }

        virtual void unignore_current_thread ()
        {
            gum_interceptor_unignore_current_thread (handle);
        }

        virtual void ignore_other_threads ()
        {
            gum_interceptor_ignore_other_threads (handle);
        }

        virtual void unignore_other_threads ()
        {
            gum_interceptor_unignore_other_threads (handle);
        }

    private:
        GMutex mutex;

        typedef std::map<AbstractInvocationListener *, RefPtr<InvocationListenerProxy> > ProxyMap;
        ProxyMap proxy_by_listener;
    };

//    extern "C" AbstractInterceptor * Interceptor_obtain (void) { return new InterceptorImpl; }
}

#endif //POGODROID_INTERCEPTORIMPL_H
