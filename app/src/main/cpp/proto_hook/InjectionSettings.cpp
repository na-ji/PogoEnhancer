#include <curl/curl.h>
#include <thread>
#include "InjectionSettings.h"
#include "Logger.h"
#include "ProtoCache.h"
#include "UnixSender.h"


void InjectionSettings::updateSettings(const json& settingsObject) {
    Logger::debug("Reading settings update");
    if (settingsObject.contains("replace_encounter_name")) {
        Logger::debug("Setting replace_encounter_name");
        this->replaceEncounterNames = settingsObject["replace_encounter_name"];
    }
    if (settingsObject.contains("overlay_show_xsxl")) {
        Logger::debug("Setting overlay_show_xsxl");
        this->showXLXS = settingsObject["overlay_show_xsxl"];
    }
    if (settingsObject.contains("replace_cp_with_iv_percentage")) {
        Logger::debug("Setting replace_cp_with_iv_percentage");
        this->replaceCpWithIvPercentage = settingsObject["replace_cp_with_iv_percentage"];
    }
    if (settingsObject.contains("disable_grunts")) {
        Logger::debug("Setting disable_grunts");
        this->disableGrunts = settingsObject["disable_grunts"];
    }
    if (settingsObject.contains("increase_visibility")) {
        Logger::debug("Setting increase_visibility");
        this->increaseVisibility = settingsObject["increase_visibility"];
    }
    if (settingsObject.contains("enable_autorun")) {
        Logger::debug("Setting enable_autorun");
        this->enableAutorun = settingsObject["enable_autorun"];
    }
    if (settingsObject.contains("enable_autotransfer")) {
        Logger::debug("Setting enable_autotransfer");
        this->enableAutotransfer = settingsObject["enable_autotransfer"];

        if (this->enableAutotransfer) {
            UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Autotransfer is enabled!",
                                    ProtoCache::instance().getSymmKey());
        } else {
            UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Autotransfer is disabled!",
                                    ProtoCache::instance().getSymmKey());
        }

    }

    if (settingsObject.contains("enable_autoencounter")) {
        Logger::debug("Setting enable_autoencounter");
        this->enableAutoencounter = settingsObject["enable_autoencounter"];

        if (this->enableAutoencounter) {
            UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Autoencounter is enabled!",
                                    ProtoCache::instance().getSymmKey());
        } else {
            UnixSender::sendMessage(MESSAGE_TYPE::TOAST, "Autoencounter is disabled!",
                                    ProtoCache::instance().getSymmKey());
        }

    }

    if (settingsObject.contains("enable_peplus_autofeed")) {
        Logger::debug("Setting enable_peplus_autofeed");
        this->peplus_feed = settingsObject["enable_peplus_autofeed"];
    }

    if (settingsObject.contains("enable_peplus_autospin")) {
        Logger::debug("Setting enable_peplus_autospin");
        this->peplus_spin = settingsObject["enable_peplus_autospin"];
    }

    if (settingsObject.contains("enable_peplus_autocatch")) {
        Logger::debug("Setting enable_peplus_autocatch");
        this->peplus_catch = settingsObject["enable_peplus_autocatch"];
    }

    if (settingsObject.contains("masstransfer")) {
        Logger::debug("Setting masstransfer");
        this->masstransfer = settingsObject["masstransfer"];
    }
    if (settingsObject.contains("keep_enc_ui")) {
        Logger::debug("Setting keep_enc_ui");
        this->keepEncounterUi = settingsObject["keep_enc_ui"];
    }
    if (settingsObject.contains("enable_autospin")) {
        Logger::debug("Setting autospin");
        this->spin = settingsObject["enable_autospin"];
    }
    if (settingsObject.contains("save_last_used_ball")) {
        Logger::debug("Setting save_last_used_ball");
        this->saveLastUsedBall = settingsObject["save_last_used_ball"];
    }
    if (settingsObject.contains("pinap_mode")) {
        Logger::debug("Setting pinap_mode");
        this->pinapMode = settingsObject["pinap_mode"];
    }
    if (settingsObject.contains("use_nanny")) {
        Logger::debug("Setting use nanny");
        this->useNanny = settingsObject["use_nanny"];
    }
    if (settingsObject.contains("autorun_min_iv")) {
        Logger::debug("Setting autorun_min_iv");
        int autorunIv = 0;
        if (settingsObject["autorun_min_iv"].is_number_integer()) {
            autorunIv = settingsObject["autorun_min_iv"].get<int>();
        } else if (settingsObject["autorun_min_iv"].is_string()) {
            auto autorunIvStr = settingsObject["autorun_min_iv"].get<std::string>();
            autorunIv = std::stoi(autorunIvStr);
        }
        this->autorunMinIv = autorunIv;
        Logger::debug("Setting new autorun min IV " + std::to_string(this->autorunMinIv));
    }
}

bool InjectionSettings::isReplaceEncounterNames() const {
    return replaceEncounterNames;
}

bool InjectionSettings::isShowXLXS() const {
    return showXLXS;
}

bool InjectionSettings::isReplaceCpWithIvPercentage() const {
    return replaceCpWithIvPercentage;
}

bool InjectionSettings::isDisableGrunts() const {
    return disableGrunts;
}

bool InjectionSettings::isIncreaseVisibility() const {
    return increaseVisibility;
}

bool InjectionSettings::isEnableAutorun() const {
    return enableAutorun;
}

bool InjectionSettings::isEnableAutotransfer() const {
    return enableAutotransfer;
}

bool InjectionSettings::isEnableAutoencounter() const {
    return enableAutoencounter;
}

bool InjectionSettings::isAutotransferInverted() const {
    return autotransferInverted;
}

bool InjectionSettings::isMasstransfer() const {
    return masstransfer;
}

bool InjectionSettings::isKeepEncounterUi() const {
    return keepEncounterUi;
}

int InjectionSettings::getAutorunMinIv() const {
    return this->autorunMinIv;
}

bool InjectionSettings::isSpin() const {
    return this->spin;
}

bool InjectionSettings::isSaveLastUsedBall() const {
    return this->saveLastUsedBall;
}

bool InjectionSettings::isPinapMode() const {
    return this->pinapMode;
}

bool InjectionSettings::isUseNanny() const {
    return this->useNanny;
}

bool InjectionSettings::isPeplusSpin() const {
    return this->peplus_spin;
}

bool InjectionSettings::isPeplusCatch() const  {
    return this->peplus_catch;
}

bool InjectionSettings::isPeplusFeed() const {
    return this->peplus_feed;
}

const string &InjectionSettings::getUserId() const {
    return userId;
}

void InjectionSettings::setUserId(const string &userId) {
    InjectionSettings::userId = userId;
}

const string &InjectionSettings::getSessionId() const {
    return sessionId;
}

void InjectionSettings::setSessionId(const string &sessionId) {
    InjectionSettings::sessionId = sessionId;
}

const string &InjectionSettings::getDeviceId() const {
    return deviceId;
}

void InjectionSettings::setDeviceId(const string &deviceId) {
    InjectionSettings::deviceId = deviceId;
}

const string &InjectionSettings::getInitialSettings() const {
    return initialSettings;
}

void InjectionSettings::setInitialSettings(const string &initialSettings) {
    InjectionSettings::initialSettings = initialSettings;
}

string InjectionSettings::decodeHexStringToString(const string& hexEncoded) {
    int len = hexEncoded.length();
    std::string newString;
    for(int i=0; i< len; i+=2)
    {
        string byte = hexEncoded.substr(i,2);
        char chr = (char) (int)strtol(byte.c_str(), nullptr, 16);
        newString.push_back(chr);
    }
    return newString;
}