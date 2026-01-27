#ifndef IL2CPP_ASSEMBLY_H
#define IL2CPP_ASSEMBLY_H

#include "Il2CppImage.hpp"
#include "frida-gumjs.h"
#include "Il2CppAddresses.hpp"

namespace Il2CppUtil {
    class Assembly {
    private:
        Il2CppAssembly* assembly;
        Il2CppUtil::Image image;
    public:
        Image* getImage() {
            return &this->image;
        }

    private:

        Il2CppImage* getImagePtr() {
            GumAddress assemblyGetImagePtr = Il2CppAddresses::instance().il2cpp_assembly_get_image();
            Il2CppImage* (*assemblyGetImage)(void*) = reinterpret_cast<Il2CppImage* (*)(void*)>(assemblyGetImagePtr);

            return assemblyGetImage(this->assembly);
        }

    public:
        Assembly(Il2CppAssembly* ptrToAssembly) {
            //Logger::debug("Assembly constructor");
            this->assembly = ptrToAssembly;
            this->image = Il2CppUtil::Image();
            this->image.init(getImagePtr());
        }

        Il2CppAssembly* getRawAssembly() {
            return this->assembly;
        }


    };
}

#endif //IL2CPP_ASSEMBLY_H
