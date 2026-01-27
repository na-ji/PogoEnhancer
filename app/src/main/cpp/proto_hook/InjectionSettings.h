#ifndef POGODROID_INJECTIONSETTINGS_H
#define POGODROID_INJECTIONSETTINGS_H

#include <string>
#include <vector>
#include <thread>
#include <map>
#include "Scanmodes.h"
#include "json.hpp"
#include "Logger.h"

using namespace std;
using namespace nlohmann;

class InjectionSettings {
public:
    InjectionSettings(const InjectionSettings&) = delete;
    InjectionSettings& operator=(const InjectionSettings &) = delete;
    InjectionSettings(InjectionSettings &&) = delete;
    InjectionSettings & operator=(InjectionSettings &&) = delete;

    static auto& instance(){
        static InjectionSettings InjectionSettings;
        return InjectionSettings;
    }

    void updateSettings(const json& settingsObject);

    const string &getPostDestination() const {
        return postDestination;
    }

    void setPostDestination(const string &postDestination) {
        this->postDestination = postDestination;
    }

    const string &getOrigin() const {
        return origin;
    }

    void setOrigin(const string &origin) {
        this->origin = origin;
    }

    const string &getAuthHeaderContent() const {
        return authHeaderContent;
    }

    void setAuthHeaderContent(const string &authHeaderContent) {
        this->authHeaderContent = authHeaderContent;
    }

    void setReplaceEncounterNames(bool value) {
        this->replaceEncounterNames = value;
    }

    void setShowXLXS(bool value) {
        this->showXLXS = value;
    }

    void setReplaceCpWithIvPercentage(bool value) {
        this->replaceCpWithIvPercentage = value;
    }

    void setDisableGrunts(bool value) {
        this->disableGrunts = value;
    }

    void setIncreaseVisibility(bool value) {
        this->increaseVisibility = value;
    }

    void setEnableAutorun(bool value) {
        this->enableAutorun = value;
    }

    void setEnableAutotransfer(bool value) {
        this->enableAutotransfer = value;
    }

    void setEnableAutoencounter(bool value) {
        this->enableAutoencounter = value;
    }

    void setAutotransferInverted(bool value) {
        this->autotransferInverted = value;
    }

    void setMasstransfer(bool value) {
        this->masstransfer = value;
    }

    void setKeepEncounterUi(bool value) {
        this->keepEncounterUi = value;
    }

    void setSpin(bool value) {
        this->spin = value;
    }

    void setSaveLastUsedBall(bool value) {
        this->saveLastUsedBall = value;
    }

    void setPinapMode(bool value) {
        this->pinapMode = value;
    }

    void setUseNanny(bool value) {
        this->useNanny = value;
    }

    void setAutorunMinIv(int value) {
        Logger::debug("autorun Min IV to be set: " + std::to_string(value));
        this->autorunMinIv = value;
        Logger::debug("autorun Min IV set: " + std::to_string(this->autorunMinIv));
    }

private:
    string origin = "";
    string authHeaderContent = "";
    string postDestination = "";
    string userId = "";
    string sessionId = "";
    string initialSettings = "";
    string deviceId = "";

    bool replaceEncounterNames = false;
    bool showXLXS = false;
    bool replaceCpWithIvPercentage = false;
    bool disableGrunts = false;
    bool increaseVisibility = false;
    bool enableAutorun = false;
    bool enableAutotransfer = false;
    bool enableAutoencounter = false;
    bool autotransferInverted = false;
    bool masstransfer = false;
    bool keepEncounterUi = false;
    int autorunMinIv = 0;
    bool spin = false;
    bool saveLastUsedBall = false;
    bool pinapMode = false;
    bool useNanny = false;

    bool peplus_spin = false;
    bool peplus_catch = false;
    bool peplus_feed = false;

public:
    bool isKeepEncounterUi() const;
    bool isReplaceEncounterNames() const;
    bool isShowXLXS() const;
    bool isReplaceCpWithIvPercentage() const;
    bool isDisableGrunts() const;
    bool isIncreaseVisibility() const;
    bool isEnableAutorun() const;
    bool isEnableAutotransfer() const;
    bool isEnableAutoencounter() const;
    bool isAutotransferInverted() const;
    bool isMasstransfer() const;
    bool isSpin() const;
    bool isSaveLastUsedBall() const;
    bool isPinapMode() const;
    bool isUseNanny() const;

    bool isPeplusSpin() const;
    bool isPeplusCatch() const;
    bool isPeplusFeed() const;

    int getAutorunMinIv() const;
    const string &getInitialSettings() const;

    void setInitialSettings(const string &initialSettings);

    const string &getUserId() const;

    void setUserId(const string &userId);

    const string &getSessionId() const;

    void setSessionId(const string &sessionId);

    const string &getDeviceId() const;

    void setDeviceId(const string &deviceId);
    static string decodeHexStringToString(const string &hexEncoded);

private:

    InjectionSettings() = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */
};


#endif //POGODROID_INJECTIONSETTINGS_H
