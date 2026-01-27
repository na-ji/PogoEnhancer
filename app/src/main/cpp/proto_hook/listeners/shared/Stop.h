//
//
//

#ifndef DROID_PLUSPLUS_STOP_H
#define DROID_PLUSPLUS_STOP_H


class Stop {
public:
    Stop(const Stop&) = delete;
    Stop& operator=(const Stop &) = delete;
    Stop(Stop &&) = delete;
    Stop & operator=(Stop &&) = delete;

    static auto& instance(){
        static Stop Stop;
        return Stop;
    }
    bool isBagFull();

    void spin(void* stop);

private:
    Stop () = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */

    void* serviceInstance;
    bool isActive(void* stop);
    bool isPlayerInRange(void* stop);
    bool isCoolingDown(void* stop);
    void* getSpinner(void *stopInteractive);

    // functions to call to avoid instance fetching of protocache
    bool (*get_isActive)(void *) = nullptr;
    bool (*get_IsPlayerInRange)(void *) = nullptr;
    bool (*getBagIsFull)(void *) = nullptr;
    bool (*get_IsCoolingDown)(void *) = nullptr;

    void* (*startInteractiveMode)(void *) = nullptr;
    void (*sendSearchRpc)(void *) = nullptr;
    void (*cleanup)(void *) = nullptr;
    void (*completeInteractiveMode)(void *) = nullptr;

};

#endif //DROID_PLUSPLUS_STOP_H
