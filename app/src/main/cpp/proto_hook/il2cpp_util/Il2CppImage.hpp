#ifndef IL2CPP_IMAGE_H
#define IL2CPP_IMAGE_H

#include "Il2CppClass.hpp"
#include "../Logger.h"
#include "frida-gumjs.h"
#include "../ProtoCache.h"
#include "Il2CppAddresses.hpp"

namespace Il2CppUtil {
    class Image {
    private:
        std::string name;
        Il2CppImage* pointerToImage;

        Il2CppClass* (*imageGetClass)(void*, uint);
        uint32_t (*imageGetClassCount)(void*);

        std::map<std::string, Il2CppUtil::Class> classesByName;

        Il2CppClass* getClassPtr(uint index) {
            Il2CppClass* klazz = imageGetClass(this->pointerToImage, index);
            return klazz;
        }

        uint32_t getClassCount() {
            return this->imageGetClassCount(this->pointerToImage);
        }

        Il2CppUtil::Class getClass(Il2CppClass* classPtr) {
            return Il2CppUtil::Class(classPtr);
        }

        void populateClassesByName() {

            //Logger::fatal("Reading image");
         //   Logger::fatal(this->pointerToImage->name);

            uint32_t classCount = this->getClassCount();
            Logger::debug("Found class count to be at " + std::to_string(classCount));

            //0xB7ED4380
            for (int i = 0; i < classCount; i++) {
                Il2CppClass* klazz = getClassPtr(i);
                if (klazz && klazz->name) {

                    //Logger::debug("Class " + std::to_string(i) + " at " + ProtoCache::convertPointerToReadableString(klazz));
                    //ßLogger::debug(klazz->name);

                    std::string decoded = "";

                    char charOfString;
                    for (int j = 0; j < std::string(klazz->name).length(); j++) {
                        charOfString = std::string(klazz->name).at(j);
                        if (charOfString == '\x02' || charOfString == '\x03') {
                            //Logger::debug("Control sign x02 or x03 is present!");
                            continue;
                        }
                        //Logger::debug(std::to_string(charOfString));
                        decoded.push_back(static_cast<char>(charOfString));
                        if (charOfString == '\0') {
                            break;
                        }
                    }

                    //Logger::debug(decoded);
                    this->classesByName.emplace(decoded, this->getClass(klazz));
                    if (decoded.rfind("OpenGiftGuiController", 0) == 0) {
                        Logger::debug(klazz->name);
                        Logger::fatal("Found clazz OpenGiftGuiController");
                    }

                    /*
                    if (strcmp("BootInstaller", klazz->name) == 0) {
                        Logger::fatal("Found clazz BootInstaller (BootInstaller)");
                    } /*else if (strcmp("Boolean", klazz->name) == 0) {
                        ptrdiff_t bytes = ((char *)baseAddr) - ((char *)klazz);
                        Logger::fatal("Found clazz Boolean (System.Boolean) at " + ProtoCache::convertPointerToReadableString(reinterpret_cast<void *>(bytes)));
                    }

                    /*else if (strcmp("\x02riordsknmej\x02", klazz->name) == 0) {
                        Logger::debug("Found MapPokestop");
                    } else if (decoded.compare("riordsknmej") == 0) {
                        Logger::debug("Found MapPokestop without control sequence");
                    } else if (reinterpret_cast<void *>(baseAddr + 0xB7ED4380) == klazz) {
                        Logger::debug("Klazz looked for is " + std::string(klazz->name));
                    } else if (strcmp("String", klazz->name) == 0) {
                        Logger::debug("Found String");
                    } else if (strcmp("RepeatedField`1", klazz->name) == 0) {
                        Logger::debug("RepeatedField");
                        Logger::debug(this->pointerToImage->name);
                    }
                     */
                }
            }
        }

    public:
        void init(Il2CppImage* imagePtr) {
            this->pointerToImage = imagePtr;
            GumAddress imageGetClassCountPtr = Il2CppAddresses::instance().il2cpp_image_get_class_count();
            this->imageGetClassCount = reinterpret_cast<uint32_t (*)(void*)>(imageGetClassCountPtr);

            GumAddress imageGetClassPtr = Il2CppAddresses::instance().il2cpp_image_get_class();
            this->imageGetClass = reinterpret_cast<Il2CppClass *(*)(void*, uint)>(imageGetClassPtr);
            this->populateClassesByName();
        }

        bool isSetup() {
            return !this->classesByName.empty();
        }

        Il2CppUtil::Class getClass(std::string className) {
            if (!isSetup()) {
                Logger::fatal("Setting up classes");
                this->populateClassesByName();
                Logger::fatal("Done setting up classes");
            }
            //const std::lock_guard<std::mutex> lock(this->accessMutex);
            if (this->classesByName.count(className) == 0) {
                Logger::fatal("Class " + className + " cannot be found.");
                return nullptr;
            }
            return this->classesByName.at(className);
        }

        std::string getName() {
            return this->pointerToImage->name;
        }

        Image() {
            this->classesByName = std::map<std::string, Il2CppUtil::Class>();
            this->pointerToImage = nullptr;
            this->name = "";
            this->imageGetClassCount = nullptr;
            this->imageGetClass = nullptr;
        }
    };
}

#endif //IL2CPP_IMAGE_H
