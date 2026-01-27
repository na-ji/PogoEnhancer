//
//
//

#ifndef POGODROID_RUNTIME_H
#define POGODROID_RUNTIME_H

namespace Gum
{
    class Runtime
    {
    public:
        static void ref ();
        static void unref ();

    private:
        static volatile int ref_count;
    };
}

#endif //POGODROID_RUNTIME_H
