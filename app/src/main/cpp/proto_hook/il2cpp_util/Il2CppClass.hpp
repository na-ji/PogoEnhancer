#ifndef IL2CPP_CLASS_H
#define IL2CPP_CLASS_H

#include "frida-gumjs.h"
#include "../Logger.h"
#include "../il2cpp/il2cppStructs.h"
#include "Il2CppAddresses.hpp"

namespace Il2CppUtil {

    struct GumV8NativeResource
    {
        void * instance;
        gpointer data;
        gsize size;
        GDestroyNotify notify;
    };

    class Class {
    private:
        Il2CppClass* classPtr = nullptr;
        std::vector<MethodInfo*> methods;

        Il2CppObject* (*il2cppObjectNew)(Il2CppClass*) = nullptr;
        void (*objectInitException)(Il2CppObject*, Il2CppException**) = nullptr;

        void setupMethods() {
            Logger::fatal("Setting up methods.");
            GumAddress il2cppClassGetMethods = Il2CppAddresses::instance().il2cpp_class_get_methods();
            MethodInfo* (*getMethods)(Il2CppClass*, void*) = reinterpret_cast<MethodInfo* (*)(Il2CppClass*, void*)>(il2cppClassGetMethods);

            MethodInfo* methodIter = nullptr;
            void* iter = nullptr;

            // TODO: Comment it again, one of them causes crashes in global metadata

            GumAddress il2cppFreePtr = Il2CppAddresses::instance().il2cpp_free();
            void (*il2cppFree)(void*) = reinterpret_cast<void (*)(void*)>(il2cppFreePtr);

            GumAddress il2cppMethodParamCountPtr = Il2CppAddresses::instance().il2cpp_method_get_param_count();
            uint32_t (*il2cppMethodParamCount)(const MethodInfo *) = reinterpret_cast<uint32_t (*)(const MethodInfo *)>(il2cppMethodParamCountPtr);

            // Retrieve all methods of the class
            while ((methodIter = getMethods(this->classPtr, &iter)) != nullptr) {

                // Get the name of the method
               // const char* methodName = methodGetName(methodIter);
                // Get the return type of the method
               // const Il2CppType* methodReturnType = methodGetReturnType(methodIter);
               // char* returnTypeName = typeGetName(methodReturnType);
                methodIter->parameters_count = il2cppMethodParamCount(methodIter);
                //methodIter->return_type = methodReturnType;
               // methodIter->name = methodName;
                // Print the method name and its return type
                //Logger::fatal("Method Name: " + std::string(methodName));
                //Logger::fatal("Return Type: " + std::string(returnTypeName) + " args count: " + std::to_string(methodIter->parameters_count));

                //Logger::fatal("Parameter count: " + std::to_string(methodIter->parameters_count));
                this->methods.push_back(methodIter);
                if (methodIter->methodPointer == nullptr) {
                    Logger::fatal("Nullpointer method!");
                }
                // Perform necessary memory operations
               // il2cppFree(returnTypeName);
            }
        }



        std::string getNameOfType(const Il2CppType* type) {
            GumAddress typeGetNamePtr = Il2CppAddresses::instance().il2cpp_type_get_name();
            char* (*typeGetName)(const Il2CppType*) = reinterpret_cast<char* (*)(const Il2CppType*)>(typeGetNamePtr);
            return std::string(typeGetName(type));
        }

    public:
        Class(Il2CppClass* classPtr) {
            this->classPtr = classPtr;
        }

        Il2CppClass* getClassPointer() {
            return this->classPtr;
        }

        /**
         * TODO: Create Il2Cpp Util for methods...
         * @param method
         * @return
         */
        std::string getNameOfMethod(MethodInfo* method) {
            GumAddress methodGetNamePtr = Il2CppAddresses::instance().il2cpp_method_get_name();
            const char* (*methodGetName)(const MethodInfo*) = reinterpret_cast<const char* (*)(const MethodInfo*)>(methodGetNamePtr);
            const char* methodName = methodGetName(method);
            return std::string(methodName);
        }

        Il2CppObject* objectNew(bool initException) {
            // Allocate new object
            if (this->il2cppObjectNew == nullptr) {
                GumAddress il2cppObjectNewPtr = Il2CppAddresses::instance().il2cpp_object_new();
                this->il2cppObjectNew = reinterpret_cast<Il2CppObject* (*)(Il2CppClass*)>(il2cppObjectNewPtr);
            }
            Logger::debug("Calling objectNew");
            Il2CppObject* obj = this->il2cppObjectNew(this->classPtr);
            Logger::debug("Fetching init exception");
            if (this->objectInitException == nullptr) {
                Logger::debug("Fetching obj init exception");
                GumAddress objectInitExceptionPtr = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_runtime_object_init_exception");
                this->objectInitException = reinterpret_cast<void (*)(Il2CppObject*, Il2CppException**)>(objectInitExceptionPtr);
            }
            Il2CppException** exceptionArray = static_cast<Il2CppException **>(malloc(
                    sizeof(void *)));

            Logger::debug("Calling init exception");
            if (this->objectInitException == nullptr) {
                Logger::debug("objectInitException is null");
            }
            if (initException) {
                this->objectInitException(obj, exceptionArray);
                Logger::debug("Assigning exception");
                Il2CppException* exception = *exceptionArray;
                if (exception != nullptr) {
                    Logger::debug("Failed initializing object");
                    return nullptr;
                }
            }
            return obj;
        }

        void* getMethodByName(std::string name, int parameterCount) {
            GumAddress methodFromNamePtr = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_class_get_method_from_name");
            void* (*methodFromName)(Il2CppClass*, const char*, int) = reinterpret_cast<void* (*)(Il2CppClass*, const char*, int)>(methodFromNamePtr);

            void* methodPtr = methodFromName(this->classPtr,
                                             name.c_str(),
                                             parameterCount);
            Logger::debug("Found method pointer for " + name + ": " + ProtoCache::convertPointerToReadableString(methodPtr));
            if (methodPtr != nullptr) {
                MethodInfo* method = static_cast<MethodInfo *>(methodFromName(this->classPtr,
                                                                              name.c_str(),
                                                                              parameterCount));
                Logger::debug("Actual method pointer of " + name + ": " + ProtoCache::convertPointerToReadableString(
                        reinterpret_cast<void *>(method->methodPointer)));
                return reinterpret_cast<void *>(method->methodPointer);
            } else {
                return nullptr;
            }
        }

        std::vector<MethodInfo*> getMethodsOfClass() {
            if (methods.empty()) {
                this->setupMethods();
            }
            return this->methods;
        }

        std::vector<MethodInfo*> getMethodsOfClass(const Il2CppType* returnType, int amountArgs) {
            if (methods.empty()) {
                this->setupMethods();
            }

            GumAddress methodGetReturnTypePtr = Il2CppAddresses::instance().il2cpp_method_get_return_type();
            const Il2CppType* (*methodGetReturnType)(const MethodInfo*) = reinterpret_cast<const Il2CppType* (*)(const MethodInfo*)>(methodGetReturnTypePtr);

            std::string nameOfReturnTypeSearchedFor = this->getNameOfType(returnType);
            Logger::debug("Comparing to type: " + nameOfReturnTypeSearchedFor);
            std::vector<MethodInfo*> methodsFound;
            for (auto method : this->methods) {
                Logger::fatal("Method parameter count: " + std::to_string(method->parameters_count));
                if (method->parameters_count != amountArgs) {
                    continue;
                }
                const Il2CppType* methodReturnType = methodGetReturnType(method);
                // char* returnTypeName = typeGetName(methodReturnType);
                std::string nameOfReturnType = this->getNameOfType(methodReturnType);
                //Logger::debug("Method return type inspected: " + nameOfReturnType);
                if (nameOfReturnType.compare(nameOfReturnTypeSearchedFor) == 0) {
                    Logger::debug("Method found: " + std::string(getNameOfMethod(method)));
                    methodsFound.push_back(method);
                }
            }

            return methodsFound;
        }

        std::vector<MethodInfo*> getMethodsOfClass(const Il2CppType* returnType, int amountArgs, int methodFlags) {
            std::vector<MethodInfo*> methodsWithoutFlagFilter = this->getMethodsOfClass(returnType, amountArgs);
            std::vector<MethodInfo*> methodsWithFlagFilter;

            for (auto method : methodsWithoutFlagFilter) {
                /*
                Logger::debug("Method found: " + std::string(method->name));
                    */
                Logger::debug("Method flags: " + std::to_string(method->flags));
                Logger::debug(std::bitset<16>(method->flags).to_string());
                Logger::debug("Method flags and: " + std::to_string(method->flags & methodFlags));
                if (method->flags == methodFlags) {
                    methodsWithFlagFilter.push_back(method);
                }
            }

            return methodsWithFlagFilter;
        }

        std::vector<MethodInfo*> getMethodsOfClass(int amountArgs) {
            if (methods.empty()) {
                this->setupMethods();
            }
            std::vector<MethodInfo*> methodsFound;
            for (auto method : this->methods) {
                //Logger::fatal("Method parameter count: " + std::to_string(method->parameters_count));
                if (method->parameters_count != amountArgs) {
                    continue;
                }
                //std::string nameOfReturnType = this->getNameOfType(method->return_type);
                //Logger::fatal("Method return type inspected: " + nameOfReturnType);
                methodsFound.push_back(method);
            }

            return methodsFound;
        }

        const Il2CppType* getType() {
            GumAddress classGetTypePtr = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_class_get_type");
            const Il2CppType* (*classGetType)(Il2CppClass*) = reinterpret_cast<const Il2CppType* (*)(Il2CppClass*)>(classGetTypePtr);

            return classGetType(this->classPtr);
        }

    };
}

#endif //IL2CPP_CLASS_H
