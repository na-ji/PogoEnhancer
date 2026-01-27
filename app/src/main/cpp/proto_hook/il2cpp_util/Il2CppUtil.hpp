#ifndef IL2CPP_UTIL_H
#define IL2CPP_UTIL_H

#include "frida-gumjs.h"


namespace Il2CppUtil {
    class Util {
    public:
        static void* findExportByName(std::string name) {
            return gum_module_find_export_by_name("libil2cpp.so", name);
        }

        static void* findIl2CppExportByName(std::string name) {
            return gum_module_find_export_by_name("libil2cpp.so", "il2cpp_" + name);
        }
    };
}

#endif //IL2CPP_UTIL_H
