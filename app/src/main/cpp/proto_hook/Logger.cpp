#include <android/log.h>
#include "Logger.h"
#include "ProtoCache.h"

#define LOG_TAG "ProtoHookC"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define LOGS(...) __android_log_print(ANDROID_LOG_SILENT,LOG_TAG,__VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)

void Logger::fatal(const string& message) {
    LOGF("%s", message.c_str());
}

void Logger::error(const string& message) {
    LOGE("%s", message.c_str());
}

void Logger::warning(const string& message) {
    LOGW("%s", message.c_str());
}

void Logger::info(const string& message) {
    LOGI("%s", message.c_str());
}

void Logger::pdebug(const string& message) {
    LOGD("%s", message.c_str());
}

void Logger::debug(const string& message) {
    LOGD("%s", message.c_str());
}
