#include <string>
#include "InfoClient.h"
#include "frida-gumjs.h"
#include "Logger.h"
#include "ProtoCache.h"
#include "il2cpp-api-types.h"
#include "il2cpp_util/Il2CppDomain.hpp"

void InfoClient::populate() {
    Logger::pdebug("Population of domain...");
    const std::lock_guard<std::mutex> lock(this->populationMutex);
    this->domain->setup();
}

void InfoClient::init() {
    Logger::warning("Init info");
    InfoClient::instance().populate();
}

Il2CppUtil::Domain* InfoClient::getDomain() {
    const std::lock_guard<std::mutex> lock(this->populationMutex);
    return this->domain;
}
