#include "EncQueue.h"
#include "ProtoCache.h"
#include "Logger.h"
#include "InjectionSettings.h"



void EncQueue::addEncounterSent(unsigned long long encounterId) {
    const std::lock_guard<std::mutex> lock(this->encMutex);
    std::time_t now = std::time(nullptr);
    Encountered encounteredData = Encountered();
    encounteredData.timeEncountered = now;
    this->encounteredIds.emplace(encounterId, encounteredData);
}

bool EncQueue::worthEncountering(unsigned long long encounterId) {
    const std::lock_guard<std::mutex> lock(this->encMutex);
    if (this->encounteredIds.count(encounterId) == 0) {
        // Not encountered yet, worth sending encounter
        Logger::info("Not encountered yet");
        return true;
    }
    // Now check the values...
    auto &encounteredData = encounteredIds[encounterId];
    // Only encounter if it was not opened before and it is worth encountering
    if (encounteredData.encounterOpened) {
        Logger::info(std::to_string(encounterId) + " was encountered before");
        return false;
    }
    return encounteredData.worthEncountering;
}

void EncQueue::setWasWorthEncountering(unsigned long long int encounterId, bool worthEncountering) {
    if (this->encounteredIds.count(encounterId) == 0) {
        this->addEncounterSent(encounterId);
    }
    const std::lock_guard<std::mutex> lock(this->encMutex);
    auto &encounteredData = encounteredIds[encounterId];
    encounteredData.worthEncountering = worthEncountering;
}

void EncQueue::setEncounterOpened(unsigned long long int encounterId) {
    if (this->encounteredIds.count(encounterId) == 0) {
        this->addEncounterSent(encounterId);
    }
    const std::lock_guard<std::mutex> lock(this->encMutex);
    auto &encounteredData = encounteredIds[encounterId];
    encounteredData.encounterOpened = true;
}

void EncQueue::cleanupLoop() {
    while (!this->stop) {
        this->cleanup();
        std::this_thread::sleep_for(std::chrono::seconds(30));
    }
}

void EncQueue::cleanup() {
    const std::lock_guard<std::mutex> lock(this->encMutex);
    vector<unsigned long long> encounterIdsToCleanup;
    std::time_t now = std::time(nullptr);
    for (auto entry : this->encounteredIds) {
        if (entry.second.timeEncountered + 60 < now) {
            // Entry was added more than 60 seconds ago, remove it
            encounterIdsToCleanup.push_back(entry.first);
        }
    }
    for (auto encounterId : encounterIdsToCleanup) {
        this->encounteredIds.erase(encounterId);
    }
}

void EncQueue::startCleanupThread() {
    if (this->threadStarted) return;

    this->cleanupThread = std::thread(&EncQueue::cleanupLoop, this);
    this->cleanupThread.detach();
    this->threadStarted = true;
}