#ifndef IL2CPP_DOMAIN_H
#define IL2CPP_DOMAIN_H

#include "Il2CppAssembly.hpp"
#include "frida-gumjs.h"
#include "../Logger.h"
#include "../ProtoCache.h"
#include "Il2CppAddresses.hpp"
#include <mutex>



/**
 * The Domain contains all assemblies which can be resolved to images.
 * Images in turn contain all classes.
 */
namespace Il2CppUtil {
    class Domain {
    private:
        std::string name;
        Il2CppDomain* domainPtr = nullptr;
        std::map<std::string, Il2CppUtil::Assembly> assemblies = std::map<std::string, Il2CppUtil::Assembly>();
        std::mutex populationMutex;


        static void signalHandlerSetup( int signum ) {
            Logger::fatal("Received signal: " + std::to_string(signum));
            std::this_thread::sleep_for(std::chrono::milliseconds (100));
        }


        Il2CppDomain* getDomain() {
            sighandler_t oldSignal;
                //
                Logger::fatal("Fetching domain getter");
                GumAddress getDomainPtr = Il2CppAddresses::instance().il2cpp_domain_get();
                if (getDomainPtr == 0) {
                    Logger::fatal("Unable to fetch domain.");
                    //signal(SIGSEGV, SIG_DFL);
                    return nullptr;
                }
                Logger::debug("Got domain getter");
                Il2CppDomain* (*getDomainMethodPtr)() = reinterpret_cast<Il2CppDomain* (*)()>(getDomainPtr);
                if (getDomainMethodPtr == nullptr) {
                    Logger::warning("Failed to find method to fetch domain");
                    //signal(SIGSEGV, SIG_DFL);
                    return nullptr;
                }
                Logger::debug("Calling domain getter");
                //signal(SIGSEGV, SIG_DFL);
            try {
                //oldSignal = signal(SIGSEGV, Domain::signalHandlerSetup);
                Il2CppDomain* domain = getDomainMethodPtr();
                //signal(SIGSEGV, oldSignal);
                return domain;
            } catch (...) {
                Logger::warning("Failed to fetch domain");
                //signal(SIGSEGV, oldSignal);
                return nullptr;
            }
        }

        void* startPointerOfAssemblies(void* domain, uint* sizePointer) {
            GumAddress domainGetAssembliesPtr = Il2CppAddresses::instance().il2cpp_domain_get_assemblies();
            void* (*domainGetAssemblies)(void*, uint*) = reinterpret_cast<void* (*)(void*, uint*)>(domainGetAssembliesPtr);

            return domainGetAssemblies(domain, sizePointer);
        }

        /**
         * https://github.com/vfsfitvnm/frida-il2cpp-bridge/blob/3f05886788bba6c5db1a2682b1a4ced74f8e6448/src/il2cpp/structs/domain.ts#L10
         * @return collection of assemblies
         */
        std::vector<Il2CppUtil::Assembly> fetchAssemblies() {
            Logger::debug("Fetching assemblies");
            uint* sizePointer = new uint;
            void *startPointer = this->startPointerOfAssemblies(this->domainPtr, sizePointer);

            Logger::debug("Start of assemblies at " + ProtoCache::convertPointerToReadableString(startPointer));
            uint count = *sizePointer;
            std::vector <Il2CppUtil::Assembly> assembliesFound = std::vector<Il2CppUtil::Assembly>();

            Logger::debug("Got assembly count " + std::to_string(count) + " starting at " + ProtoCache::convertPointerToReadableString(startPointer));
            for (int i = 0; i < count; i++) {
                void** pointerToAssembly = reinterpret_cast<void **>(
                        reinterpret_cast<char *>(startPointer) + i * sizeof(startPointer));

                Il2CppUtil::Assembly assembly = Il2CppUtil::Assembly(
                        reinterpret_cast<Il2CppAssembly *>(*pointerToAssembly));
                assembliesFound.push_back(assembly);
            }
            // TODO: https://github.com/vfsfitvnm/frida-il2cpp-bridge/blob/master/src/il2cpp/structs/domain.ts#L21

            return assembliesFound;
        }

    public:
        Domain() {
            Logger::debug("New instance of Domain");
        }

        void setup() {
            const std::lock_guard<std::mutex> lock(this->populationMutex);
            Logger::debug("Setting up domain with assemblies at " + ProtoCache::convertPointerToReadableString(&this->assemblies));
            if (this->domainPtr == nullptr || this->assemblies.size() == 0) {
                if (this->domainPtr == nullptr) {
                    Logger::fatal("domain ptr is null");
                }
                if (this->assemblies.size() == 0) {
                    Logger::fatal("Assemblies empty");
                }
                Logger::fatal("Hasn't been setup before");
                this->domainPtr = getDomain();
                if(this->domainPtr == nullptr) {
                    Logger::warning("Cannot setup, domain pointer is null");
                    return;
                }
                auto started = std::chrono::high_resolution_clock::now();
                std::vector<Il2CppUtil::Assembly> assembliesListed = this->fetchAssemblies();
                auto done = std::chrono::high_resolution_clock::now();
                auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(done-started).count();
                Logger::fatal("Time taken to fetch assemblies: " + std::to_string(duration));
                for (Il2CppUtil::Assembly assembly : assembliesListed) {
                    Logger::debug("Emplacing " + assembly.getImage()->getName());
                    Image* image = assembly.getImage();
                    this->assemblies.emplace(image->getName(),
                                             assembly);
                }
                Logger::debug("Done setting up domain");
            }
        }

        Il2CppUtil::Assembly* getAssemblyByName(std::string nameOfAssembly) {
            if (this->domainPtr == nullptr) {
                Logger::warning("Missing " + nameOfAssembly);
                this->setup();
                return &this->assemblies.at(nameOfAssembly);
            }
            const std::lock_guard<std::mutex> lock(this->populationMutex);
            try {
                return &this->assemblies.at(nameOfAssembly);
            } catch (std::out_of_range) {
                Logger::warning("Missing " + nameOfAssembly + ". Total size: " + std::to_string(this->assemblies.size()));
                this->setup();
                Logger::warning("Populated " + nameOfAssembly + ". Total size: " + std::to_string(this->assemblies.size()));
                return nullptr;
            }
        }

        Domain(const Domain &domain) {
            Logger::debug("Domain copy-construct");
            this->assemblies = domain.assemblies;
            this->domainPtr = domain.domainPtr;
            this->name = domain.name;
        }
    };
}

#endif //IL2CPP_DOMAIN_H
