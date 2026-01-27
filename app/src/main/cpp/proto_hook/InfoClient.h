#ifndef POGODROID_INFOCLIENT_H
#define POGODROID_INFOCLIENT_H


#include <map>
#include <class-internals.h>
#include "il2cpp_util/Il2CppDomain.hpp"

class InfoClient {
public:
    InfoClient(const InfoClient&) = delete;
    InfoClient& operator=(const InfoClient &) = delete;
    InfoClient(InfoClient &&) = delete;
    InfoClient & operator=(InfoClient &&) = delete;

    static auto& instance(){
        static InfoClient InfoClient;
        Logger::debug("InfoClient instance " + ProtoCache::convertPointerToReadableString(&InfoClient));
        return InfoClient;
    }

    static void init();
private:
    InfoClient () = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */
    std::mutex populationMutex;

    Il2CppUtil::Domain* domain = new Il2CppUtil::Domain();
public:
    Il2CppUtil::Domain* getDomain();

private:
    void populate();
};


#endif //POGODROID_INFOCLIENT_H
