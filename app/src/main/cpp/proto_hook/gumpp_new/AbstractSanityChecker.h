//
//
//

#ifndef POGODROID_ABSTRACTSANITYCHECKER_H
#define POGODROID_ABSTRACTSANITYCHECKER_H

#include "structs.h"

namespace Gum {
    class AbstractSanityChecker : Gum::Object {
    public:
        virtual void enable_backtraces_for_blocks_of_all_sizes () = 0;
        virtual void enable_backtraces_for_blocks_of_size (unsigned int size) = 0;
        virtual void set_front_alignment_granularity (unsigned int granularity) = 0;

        virtual void begin (unsigned int flags) = 0;
        virtual bool end () = 0;
    };
}

#endif //POGODROID_ABSTRACTSANITYCHECKER_H
