//
//
//

#ifndef POGODROID_SYMBOLUTIL_H
#define POGODROID_SYMBOLUTIL_H

#include <vector>
#include "../frida-gumjs.h"
#include "structs.h"
#include "RefPtr.h"

namespace Gum {
    class SymbolUtil {
    public:
        static void *find_function(const char *name) {
            return find_function_ptr(name);
        }

        static std::vector<void *> find_matching_functions(const char *str) {
            RefPtr <PtrArray> functions = RefPtr<PtrArray>(find_matching_functions_array(str));
            std::vector<void *> result;
            for (int i = functions->length() - 1; i >= 0; i--)
                result.push_back(functions->nth(i));
            return result;
        }
    };
}

#endif //POGODROID_SYMBOLUTIL_H
