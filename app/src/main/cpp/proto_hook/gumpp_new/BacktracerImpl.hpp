//
//
//

#ifndef POGODROID_BACKTRACERIMPL_H
#define POGODROID_BACKTRACERIMPL_H

#include "ObjectWrapper.hpp"
#include "AbstractBacktracer.h"
#include "Runtime.h"

namespace Gum {
    class BacktracerImpl : public ObjectWrapper<BacktracerImpl, AbstractBacktracer, GumBacktracer> {
    public:
        BacktracerImpl (GumBacktracer * handle)
        {
            assign_handle (handle);
        }

        virtual ~BacktracerImpl ()
        {
            Runtime::unref ();
        }

        virtual void generate (const CpuContext * cpu_context, ReturnAddressArray & return_addresses) const
        {
            gum_backtracer_generate (handle, reinterpret_cast<const GumCpuContext *> (cpu_context), reinterpret_cast<GumReturnAddressArray *> (&return_addresses));
        }
    };

    extern "C" AbstractBacktracer * Backtracer_make_accurate ()
    {
        Runtime::ref ();
        GumBacktracer * handle = gum_backtracer_make_accurate ();
        if (handle == NULL)
        {
            Runtime::unref ();
            return nullptr;
        }
        return new BacktracerImpl (handle);
    }

    extern "C" AbstractBacktracer * Backtracer_make_fuzzy ()
    {
        Runtime::ref ();
        GumBacktracer * handle = gum_backtracer_make_fuzzy ();
        if (handle == NULL)
        {
            Runtime::unref ();
            return nullptr;
        }
        return new BacktracerImpl (handle);
    }
}

#endif //POGODROID_BACKTRACERIMPL_H
