#ifndef POGODROID_ENCQUEUE_H
#define POGODROID_ENCQUEUE_H


#include <queue>
#include <mutex>
#include <thread>
#include <map>

using namespace std;

struct Encountered {
    time_t timeEncountered;
    bool worthEncountering = true;
    bool encounterOpened = false;
};


class EncQueue {
public:
    EncQueue(const EncQueue&) = delete;
    EncQueue& operator=(const EncQueue &) = delete;
    EncQueue(EncQueue &&) = delete;
    EncQueue & operator=(EncQueue &&) = delete;

    static auto& instance(){
        static EncQueue EncQueue;
        return EncQueue;
    }

    void addEncounterSent(unsigned long long encounterId);
    bool worthEncountering(unsigned long long int encounterId);
    void setWasWorthEncountering(unsigned long long encounterId, bool worthEncountering);
    void setEncounterOpened(unsigned long long encounterId);
    void startCleanupThread();

private:
    EncQueue() = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */


    mutex encMutex;
    std::map<unsigned long long, Encountered> encounteredIds;
    void cleanupLoop();
    bool threadStarted = false;
    bool stop = false;
    thread cleanupThread;

    void cleanup();
};


#endif //POGODROID_ENCQUEUE_H
