//
//
//

#include "Runtime.h"

#include "frida-gumjs.h"
#ifdef G_OS_WIN32
#include <windows.h>
#endif

namespace Gum
{
    volatile int Runtime::ref_count = 0;

    static void init ()
    {
        gum_init_embedded ();
    }

    static void deinit ()
    {
        gum_deinit_embedded ();
    }

    void Runtime::ref ()
    {
        if (g_atomic_int_add (&ref_count, 1) == 0)
            g_atomic_int_inc (&ref_count);
        init ();
    }

    void Runtime::unref ()
    {
        if (g_atomic_int_dec_and_test (&ref_count))
            deinit ();
    }
}