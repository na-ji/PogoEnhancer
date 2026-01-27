
#include <fcntl.h>
#include <android/log.h>
#include <string>
#include <zconf.h>
#include "frida-gumjs.h"
#include "gumpp_new/InterceptorImpl.h"
#include "ProtoCache.h"
#include <curl/curl.h>
#include "Logger.h"
#include "InjectionSettings.h"
#include "Base64.h"
#include "EncQueue.h"
#include <chrono>
#include <thread>
#include <android/log.h>
#include <future>
#include "ipc/InfoServer.h"
#include "UnixSender.h"
#include "listeners/GWp.h"
#include "InfoClient.h"

//#include "selinux/patch.h"

#define LOG_TAG "ProtoHookC"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static int replacement_open(const char *path, int oflag, ...);

void proto_hook_main(const gchar *data, gboolean *stay_resident) __attribute((__annotate__(("fw_prob=100"))));
void proto_hook_main(const gchar *data, gboolean *stay_resident) {
    *stay_resident = TRUE;
    __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, "Hi");
    auto hookStart = std::chrono::high_resolution_clock::now();

    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, "Looking up il2cpp");
    GumAddress baseAddr = 0;
    while (baseAddr == 0) {
        baseAddr = gum_module_find_base_address("libil2cpp.so");
        if (baseAddr == 0) {
            Logger::fatal("libil2cpp not loaded yet");
            std::this_thread::sleep_for(std::chrono::milliseconds(50));
        }
    }
    bool waitForBootInit = true;
    GWp gWp = GWp();
    gWp.setInterceptor(interceptor);
    gWp.setBaseAddr(baseAddr);
    GumAddress il2cppInit = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_init");
    interceptor.attach_listener(
            (gpointer) (il2cppInit), &gWp,
            nullptr);
    GumAddress il2cpp_get_corlib = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_get_corlib");
    void *
    (*getCorlib)() = reinterpret_cast<void *(*)()>(il2cpp_get_corlib);
    void* corlib = getCorlib();
    if (corlib == nullptr) {
        Logger::fatal("Corlib is null, attaching init-listener");
        interceptor.attach_listener(
                (gpointer) (il2cppInit), &gWp,
                nullptr);


    } else {
        Logger::fatal("Corlib already present, trying to hook normally.");
        waitForBootInit = false;
    }

    std::string raw_input = std::string(data);
    if (raw_input.empty()) {
        Logger::fatal("GTFO " + raw_input + " size: " + std::to_string(raw_input.size()));
        exit(1);
    }
    ProtoCache::instance().setSymmKey(raw_input);
    InfoServer::instance().start();

//    frida_selinux_apply_policy_patch();
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Anyone out there?");
    ProtoCache::instance();
    InjectionSettings::instance();
    EncQueue::instance();
    // Now wait for credentials to be passed by the Java code
    UnixSender::sendMessage(MESSAGE_TYPE::REQUEST_AUTH, "huehueduedurp", ProtoCache::instance().getSymmKey());

    while (InjectionSettings::instance().getUserId().empty()) {
        std::this_thread::sleep_for(std::chrono::seconds(5));
        UnixSender::sendMessage(MESSAGE_TYPE::REQUEST_AUTH, "huehueduedurp", ProtoCache::instance().getSymmKey());
    }
    Logger::fatal("Trying to set initial hooks");
    /**
     * Sets initial hooks needed by offset to avoid crashing of the process.
     * Places hooks for, e.g., login choice and version check suppression
     */
    interceptor.begin_transaction();
    gWp.placeFirstBatch();
    interceptor.end_transaction();

    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "Anyone out there? (2)");

    long pages = sysconf(_SC_PHYS_PAGES);
    long page_size = sysconf(_SC_PAGE_SIZE);
    long long memorySizeInBytes = pages * page_size;
    Logger::fatal("Total memory size in bytes: " + std::to_string(memorySizeInBytes));
    long baseSleepMs = 5500;
    long minimumSleepMs = 4500;
    long minimumRamFastMode = 3 * pow(10, 9);
    double reductionFactor = minimumRamFastMode / memorySizeInBytes;
    // 3GB is a factor of 1 whereas increasing RAM up to, say 8GB, decreases to 500ms minimum sleep
    if (waitForBootInit) {
        Logger::fatal("Waiting for boot init");
        auto started = std::chrono::high_resolution_clock::now();
        bool initTriggeredSeen = false;
        bool extendWaiting = true; // Assume slow initial startup (factor 2)
        do {
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
            if (!initTriggeredSeen) {
                if (!gWp.initTriggered) {
                    auto initNotTriggered = std::chrono::high_resolution_clock::now();
                    auto duration = std::chrono::duration_cast<std::chrono::seconds>(initNotTriggered-started).count();
                    if (duration > 6) {
                        Logger::fatal("Timeout waiting for boot init");
                        break;
                    }
                } else if (gWp.initTriggered) {
                    initTriggeredSeen = true;
                    auto initNotTriggered = std::chrono::high_resolution_clock::now();
                    auto duration = std::chrono::duration_cast<std::chrono::seconds>(initNotTriggered-started).count();
                    Logger::fatal("Init triggered seen initially");
                    if (duration >= 3) {
                        Logger::fatal("Extend waiting");
                        extendWaiting = false;
                    }
                }
            }
        } while (!gWp.isSetup());
        Logger::fatal("Done waiting for boot init");
        auto done = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(done-started).count();
        baseSleepMs = duration * 3;
        if (extendWaiting) {
            baseSleepMs *= 2;
        }
        Logger::fatal("Sleeping for " + std::to_string(baseSleepMs) + " milliseconds");
    }
    if (memorySizeInBytes <= minimumRamFastMode) { // 3GB limit
        Logger::fatal("Slow system assumed, sleeping longer");
        std::this_thread::sleep_for(std::chrono::milliseconds(baseSleepMs));
    } else if (reductionFactor * baseSleepMs > minimumSleepMs) {
        long sleepDuration = (long) (reductionFactor * baseSleepMs);
        Logger::fatal("Faster system assumed, sleeping less long: " + std::to_string(sleepDuration));
        std::this_thread::sleep_for(std::chrono::milliseconds(sleepDuration));
    } else {
        Logger::fatal("Fast system assumed, sleeping " + std::to_string(minimumSleepMs));
        std::this_thread::sleep_for(std::chrono::milliseconds (minimumSleepMs));
    }

    Logger::fatal("Good morning");
    Logger::debug("Injection got data: " + raw_input);

    string settings = InjectionSettings::decodeHexStringToString(InjectionSettings::instance().getInitialSettings());
    Logger::fatal("Settings: " + settings);
    std::vector<string> settingsSplit = ProtoCache::split(settings, ",");

    if (!settingsSplit.at(0).empty()) {
        Logger::debug(settingsSplit.at(0));
        int settingNum = std::stoi(settingsSplit.at(0));
        ProtoCache::instance().parseApplicationSettings(settingNum);
    }

    ProtoCache::instance().startUpdateThread();
    EncQueue::instance().startCleanupThread();
    std::this_thread::sleep_for(std::chrono::seconds(10));

    Logger::debug("Hooking..");
    interceptor.begin_transaction();
    gWp.placeSecondBatch();

    Logger::info("Good to go");
    std::future<void> readClasses = std::async(&InfoClient::init);

    readClasses.get();
    gWp.thirdBatch();
    interceptor.end_transaction();
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"
    while (true) {
        gWp.isSetup();
        std::this_thread::sleep_for(std::chrono::minutes(60 * 3));
    }
#pragma clang diagnostic pop
}

static int replacement_open(const char *path, int oflag, ...) {
    return 0;
}
