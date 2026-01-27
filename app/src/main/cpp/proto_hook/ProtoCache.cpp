#include "ProtoCache.h"
#include "ProtoConverter.h"
#include "InfoClient.h"
#include "UnixSender.h"
#include <android/log.h>
#include <string>
#include <map>
#include <sstream>
#include <utility>
#include "Logger.h"
#include "EncQueue.h"
#include "InjectionSettings.h"
#include <tuple>
#include <list>
#include "listeners/shared/LocProv.h"
#include <future>
#include <regex>



using namespace std;
using namespace nlohmann;

std::list<unsigned long long> checkEncounterReleaseListe;
std::mutex EncounterReleaseMutex;

std::list<unsigned long long> monEvolveList;
std::mutex EvolveListMutex;

void ProtoCache::addData(int methodId, std::vector<uint8_t> bytes, long timestamp) {
    DataEntry entry = DataEntry();
    entry.data = bytes;
    entry.timestamp = timestamp;
    entry.methodId = methodId;
    this->data.push_back(entry);
}

void ProtoCache::updateLoop() {
    while (!this->_stopThread) {
        auto proto = this->data.front();
        this->data.pop_front();

        switch (proto.methodId) {
                case 0:
//                        Logger::info("Sending unknown proto type");
                //case 104: FORT_DETAILS_VALUE not used atm
                case 102:
                    break;
                //case 156: GYM_GET_INFO_VALUE not used atm
                // case 5: DOWNLOAD_SETTINGS_VALUE not used atm
                //case 126: GET_HATCHED_EGGS_VALUE not used atm
                //case 137: RECYCLE_INVENTORY_ITEM_VALUE not used atm
                //case 4: GET_HOLO_INVENTORY_VALUE not used atm
                // case 101: FORT_SEARCH_VALUE not used atm
                // case 901: GET_QUEST_DETAILS_VALUE not used atm
                //case 900: GET_NEW_QUESTS_VALUE not used atm
                //case 816: FETCH_ALL_NEWS_VALUE not used atm
                case 143: //INCSENCE_ENCOUNTER
                case 145: //DISK_ENCOUNTER
                case 154: //USE_ITEM_ENCOUNTER
                case 904: //QUEST_ENCOUNTER
                case 801: //SFIDA_ACTION_LOG
                    break;
                case 101:
                    LocProv::instance().setLastCooldownLocation(ProtoCache::instance().getLatLng());
                    break;
                //case 2:   //GET_PLAYER not used atm
                case 103: //CATCH_POKEMON
                    LocProv::instance().setLastCooldownLocation(ProtoCache::instance().getLatLng());
                    break;
                case 163: //GET_RAID_DETAILS
//                    Logger::info("Sending raid details");
                    break;
                case 1405: // routes
                    break;
                case 900:
                    break;
                case 106:
//                        EncQueue::instance().resetSleeptime();
                    break;
                default:
                    // TODO: Debugging
                    continue;

            }

        Logger::debug("Sending MethodID: " + std::to_string(proto.methodId));

        std::async(&ProtoCache::send, proto, this->symmKey);
    }
}

void ProtoCache::send(const DataEntry& proto, const std::string& symKey) {
    std::string responsePlain =
            ProtoConverter::convertRawProto(proto.timestamp, proto.methodId, proto.data);

    Logger::debug("responsePlain: " + responsePlain);

    if (!responsePlain.empty()) {
        Logger::debug("Sending data via socket");
        UnixSender::sendMessage(MESSAGE_TYPE::RESPONSE_PROTO, responsePlain, symKey);
        Logger::debug("Done sending data via socket");
    } else {
        Logger::debug("Nothing to send");
    }
}

void ProtoCache::startUpdateThread() {
    if (this->_threadStarted) return;

    this->_thread = std::thread(&ProtoCache::updateLoop, this);
    this->_thread.detach();
    this->_threadStarted = true;
}

void ProtoCache::setSymmKey(std::string key) {
    if(this->symmKey.empty()) {
        if(key.length() > 16) {
            key.resize(16);
        } else if (key.length() < 16) {
            int toBeAppended = 16 - key.length();
            for(int i = 1; i < toBeAppended + 1; i++) {
                key += std::to_string(i);
            }
            key.resize(16);
        }
        this->symmKey = key;
//        LOGD("Key to be used: %s", key.c_str());
    }
}

std::string ProtoCache::getSymmKey() {
    return this->symmKey;
}

void ProtoCache::setSer(void *newPointer) {
    if(newPointer) {
        Logger::debug("Found newPointer");
    } else {
        Logger::debug("Could not find newPointer");
    }
    if(this->sEr) {
        Logger::debug("sEr already set");
    } else {
        Logger::debug("Setting new sEr");
        this->sEr = newPointer;
    }
}

void* ProtoCache::getSer() {
    return this->sEr;
}

std::string ProtoCache::convertPointerToReadableString(void* ptr) {
    const void * address = static_cast<const void*>(ptr);
    std::stringstream ss;
    ss << address;
    std::string name = ss.str();
    return name;
}

void ProtoCache::setGetEncounterId(void *ptr) {
    if(this->getEncounterId) {
        Logger::debug("GetEncounterId already set");
    } else {
        Logger::debug("Setting new GetEncounterId");
        this->getEncounterId = ptr;
    }
}

void *ProtoCache::getGetEncounterId() {
    return this->getEncounterId;
}

void *ProtoCache::getGet_IndividualAttack() const {
    return this->get_IndividualAttack;
}

void ProtoCache::setGet_IndividualAttack(void *get_IndividualAttack) {
    if(this->get_IndividualAttack) {
        Logger::debug("get_IndividualAttack already set");
    } else {
        Logger::debug("Setting new get_IndividualAttack");
        this->get_IndividualAttack = get_IndividualAttack;
    }
}

void *ProtoCache::getGet_IndividualDefense() const {
    return this->get_IndividualDefense;
}

void ProtoCache::setGet_IndividualDefense(void *get_IndividualDefense) {
    if(this->get_IndividualDefense) {
        Logger::debug("get_IndividualDefense already set");
    } else {
        Logger::debug("Setting new get_IndividualDefense");
        this->get_IndividualDefense = get_IndividualDefense;
    }
}

void *ProtoCache::getGet_isEgg() const {
    return this->get_isEgg;
}

void ProtoCache::setGet_isEgg(void *get_isEgg) {
    if(this->get_isEgg) {
        Logger::debug("get_isEgg already set");
    } else {
        Logger::debug("Setting new get_isEgg");
        this->get_isEgg = get_isEgg;
    }
}

void *ProtoCache::getGet_IndividualStamina() const {
    return get_IndividualStamina;
}

void ProtoCache::setGet_IndividualStamina(void *get_IndividualStamina) {
    if(this->get_IndividualStamina) {
        Logger::debug("get_IndividualStamina already set");
    } else {
        Logger::debug("Setting new get_IndividualStamina");
        this->get_IndividualStamina = get_IndividualStamina;
    }
}

void *ProtoCache::getGet_Pokemon_RaidMapPokemon() const {
    return get_Pokemon_RaidMapPokemon;
}

void ProtoCache::setGet_Pokemon_RaidMapPokemon(void *get_Pokemon_RaidMapPokemon) {
    if(this->get_Pokemon_RaidMapPokemon) {
        Logger::debug("get_Pokemon_RaidMapPokemon already set");
    } else {
        Logger::debug("Setting new get_Pokemon_RaidMapPokemon");
        this->get_Pokemon_RaidMapPokemon = get_Pokemon_RaidMapPokemon;
    }
}

void *ProtoCache::getSendSearchRpc() const {
    return sendSearchRpc;
}

void ProtoCache::setSendSearchRpc(void *sendSearchRpc) {
    if(this->sendSearchRpc) {
        Logger::debug("sendSearchRpc already set");
    } else {
        Logger::debug("Setting new sendSearchRpc");
        this->sendSearchRpc = sendSearchRpc;
    }
}

void *ProtoCache::getWildMonProto_getMon() const {
    return wildMonProto_getMon;
}

void ProtoCache::setWildMonProto_getMon(void *wildMonProto_getMon) {
    if(this->wildMonProto_getMon) {
        Logger::debug("wildMonProto_getMon already set");
    } else {
        Logger::debug("Setting new wildMonProto_getMon");
        this->wildMonProto_getMon = wildMonProto_getMon;
    }
}

void ProtoCache::parseApplicationSettings(unsigned int settings) {
    Logger::debug("Parsing application settings: " + std::to_string(settings));
    if ((settings & 0x2) == 0x2) {
        Logger::debug("Setting replaceCpWithIv true");
        this->replaceCpWithIv = true;
        InjectionSettings::instance().setReplaceCpWithIvPercentage(true);
    }

    // parse the enhanced throw settings
    if ((settings & 0x4) == 0x4) {
        Logger::debug("Enhanced throw randomized enabled");
        this->enhancedThrowType = EnhancedThrowType::RANDOMIZED_GREAT_EXCELLENT;
    } else if ((settings & 0x8) == 0x8) {
        Logger::debug("Enhanced throw excellent enabled");
        this->enhancedThrowType = EnhancedThrowType::EXCELLENT;
    }

    if ((settings & 16) == 16) {
        Logger::debug("Speedup catch enabled");
        this->fastCatchType = FastCatchType ::SPEEDUP;
    } else if ((settings & 64) == 64) {
        Logger::debug("Quick catch enabled");
        this->fastCatchType = FastCatchType ::Quick;
    }

    if ((settings & 32) == 32) {
        Logger::debug("Autospin enabled");
        InjectionSettings::instance().setSpin(true);
    }

    if ((settings & 128) == 128) {
        Logger::debug("Skip encounter intro enabled");
        //this->enableSkipEncounterIntro = true;
    }

    // easy catch
    if ((settings & 256) == 256) {
        Logger::debug("Easy catch pacifist enabled");
        this->easyCatchType = EasyCatchType::PACIFIST;
    } else if ((settings & 512) == 512) {
        Logger::debug("Easy catch immobilized enabled");
        this->easyCatchType = EasyCatchType::IMMOBILIZED;
    } else {
        Logger::debug("Easy catch: Nope");
    }
    int autorunIv = (settings >> 10) & 127;
    InjectionSettings::instance().setAutorunMinIv(autorunIv);
    //this->autorunMinIv = (settings >> 10) & 127;
    Logger::debug("autorun Min IV to encounter: " + std::to_string(autorunIv));
    int ivSet = InjectionSettings::instance().getAutorunMinIv();
    Logger::debug("autorun Min IV to encounter (retrieved): " + std::to_string(ivSet));

    if (settings & 131072) {
        Logger::debug("Name replacement for encounters enabled");
        InjectionSettings::instance().setReplaceEncounterNames(true);
    }

    if (settings & 262144) {
        Logger::debug("Show weight/height values");
        this->shopHeightWeightValue = true;
        InjectionSettings::instance().setShowXLXS(true);
    }

    if (settings & (1 << 19)) {
        Logger::debug("FPS unlocked");
        this->uF = true;
    }

    if (settings & (1 << 20)) {
        Logger::debug("Skip evolve");
        this->sE = true;
    }

    if (settings & (1 << 21)) {
        Logger::debug("Speed up gifting");
        this->speedupGifting = true;
    }

    if (settings & (1 << 22)) {
        Logger::debug("Mass transfer enabled");
        InjectionSettings::instance().setMasstransfer(true);
    }

    if (settings & (1 << 23)) {
        Logger::debug("Keep encounter UI enabled");
        InjectionSettings::instance().setKeepEncounterUi(true);
    }

    if (settings & (1 << 24)) {
        Logger::debug("Disable grunts");
        InjectionSettings::instance().setDisableGrunts(true);
    }
    Logger::debug("autorun Min IV to encounter: " + std::to_string(InjectionSettings::instance().getAutorunMinIv()));

    if (settings & (1 << 25)) {
        Logger::debug("Enable autorun");
        InjectionSettings::instance().setEnableAutorun(true);
    }

    if (settings & (1 << 26)) {
        Logger::debug("Increase mon visibility");
        InjectionSettings::instance().setIncreaseVisibility(true);
    }

    if (settings & (1 << 27)) {
        Logger::debug("Save last used ball");
        InjectionSettings::instance().setSaveLastUsedBall(true);
    }

    if (settings & (1 << 28)) {
        Logger::debug("Enable pinap mode");
        InjectionSettings::instance().setPinapMode(true);
    }

    if (settings & (1 << 29)) {
        Logger::debug("Use nanny");
        InjectionSettings::instance().setUseNanny(true);
    }

    if (settings & (1 << 30)) {
        Logger::debug("Enable autotransfer");
        InjectionSettings::instance().setEnableAutotransfer(true);
    }

    if (settings & (1 << 31)) {
        Logger::debug("Enable autoencounter");
        InjectionSettings::instance().setEnableAutoencounter(true);
    }

    Logger::debug("autorun Min IV to encounter: " + std::to_string(InjectionSettings::instance().getAutorunMinIv()));
    Logger::debug("Done applying settings!");
}

void *ProtoCache::getGet_CpMultiplier() const {
    return get_CpMultiplier;
}

void ProtoCache::setGet_CpMultiplier(void *get_CpMultiplier) {
    if(this->get_CpMultiplier) {
        Logger::debug("get_CpMultiplier already set");
    } else {
        Logger::debug("Setting new get_CpMultiplier");
        this->get_CpMultiplier = get_CpMultiplier;
    }
}

void *ProtoCache::getGet_AdditionalCpMultiplier() const {
    return get_AdditionalCpMultiplier;
}

void ProtoCache::setGet_AdditionalCpMultiplier(void *get_AdditionalCpMultiplier) {
    if(this->get_AdditionalCpMultiplier) {
        Logger::debug("get_AdditionalCpMultiplier already set");
    } else {
        Logger::debug("Setting new get_AdditionalCpMultiplier");
        this->get_AdditionalCpMultiplier = get_AdditionalCpMultiplier;
    }
}

EnhancedThrowType ProtoCache::getEnhancedThrowType() const {
    return enhancedThrowType;
}

bool ProtoCache::isEnableAutospin() const {
    return enableAutospin;
}

void *ProtoCache::getSummaryDismissed() const {
    return summaryDismissed;
}

void ProtoCache::setSummaryDismissed(void *summaryDismissed) {
    if(this->summaryDismissed) {
        Logger::debug("summaryDismissed already set");
    } else {
        Logger::debug("Setting new summaryDismissed");
        this->summaryDismissed = summaryDismissed;
    }
}

void *ProtoCache::getRunAway() const {
    return runAway;
}

void ProtoCache::setRunAway(void *runAway) {
    if(this->runAway) {
        Logger::debug("runAway already set");
    } else {
        Logger::debug("Setting new runAway");
        this->runAway = runAway;
    }
}

FastCatchType ProtoCache::getFastCatchType() const {
    return fastCatchType;
}

void *ProtoCache::getStopIsCooldown() const {
    return stopIsCooldown;
}

void ProtoCache::setStopIsCooldown(void *stopIsCooldown) {
    if(this->stopIsCooldown) {
        Logger::debug("stopIsCooldown already set");
    } else {
        Logger::debug("Setting new stopIsCooldown");
        this->stopIsCooldown = stopIsCooldown;
    }
}

void *ProtoCache::getStopIsPlayerRange() const {
    return stopIsPlayerRange;
}

void ProtoCache::setStopIsPlayerRange(void *stopIsPlayerRange) {
    if(this->stopIsPlayerRange) {
        Logger::debug("stopIsPlayerRange already set");
    } else {
        Logger::debug("Setting new stopIsPlayerRange");
        this->stopIsPlayerRange = stopIsPlayerRange;
    }
}

void *ProtoCache::getStopStart() const {
    return stopStart;
}

void ProtoCache::setStopStart(void *stopStart) {
    if(this->stopStart) {
        Logger::debug("stopStart already set");
    } else {
        Logger::debug("Setting new stopStart");
        this->stopStart = stopStart;
    }
}

void *ProtoCache::getStopGetSpin() const {
    return stopGetSpin;
}

void ProtoCache::setStopGetSpin(void *stopGetSpin) {
    if(this->stopGetSpin) {
        Logger::debug("stopGetSpin already set");
    } else {
        Logger::debug("Setting new stopGetSpin");
        this->stopGetSpin = stopGetSpin;
    }
}

void *ProtoCache::getStopClean() const {
    return stopClean;
}

void ProtoCache::setStopClean(void *stopClean) {
    if(this->stopClean) {
        Logger::debug("stopClean already set");
    } else {
        Logger::debug("Setting new stopClean");
        this->stopClean = stopClean;
    }
}

void *ProtoCache::getStopComplete() const {
    return stopComplete;
}

void ProtoCache::setStopComplete(void *stopComplete) {
    if(this->stopComplete) {
        Logger::debug("stopComplete already set");
    } else {
        Logger::debug("Setting new stopComplete");
        this->stopComplete = stopComplete;
    }
}

void *ProtoCache::getStopActive() const {
    return stopActive;
}

void ProtoCache::setStopActive(void *stopActive) {
    if(this->stopActive) {
        Logger::debug("stopComplete already set");
    } else {
        Logger::debug("Setting new stopComplete");
        this->stopActive = stopActive;
    }
}

void *ProtoCache::getPlayerServiceInstance() const {
    return playerServiceInstance;
}

void ProtoCache::setPlayerServiceInstance(void *playerServiceInstance) {
    if(this->playerServiceInstance) {
        Logger::debug("playerServiceInstance already set");
    } else {
        Logger::debug("Setting new playerServiceInstance");
        this->playerServiceInstance = playerServiceInstance;
    }
}

void *ProtoCache::getServiceBagFull() const {
    return serviceBagFull;
}

void ProtoCache::setServiceBagFull(void *serviceBagFull) {
    if(this->serviceBagFull) {
        Logger::debug("serviceBagFull already set");
    } else {
        Logger::debug("Setting new serviceBagFull");
        this->serviceBagFull = serviceBagFull;
    }
}

void *ProtoCache::getGetPokemonQuestMon() const {
    return getPokemonQuestMon;
}

void ProtoCache::setGetPokemonQuestMon(void *getPokemonQuestMon) {
    if(this->getPokemonQuestMon) {
        Logger::debug("getPokemonQuestMon already set");
    } else {
        Logger::debug("Setting new getPokemonQuestMon");
        this->getPokemonQuestMon = getPokemonQuestMon;
    }
}

void *ProtoCache::getGetPokemonIncidentMon() const {
    return getPokemonIncidentMon;
}

void ProtoCache::setGetPokemonIncidentMon(void *getPokemonIncidentMon) {
    if(this->getPokemonIncidentMon) {
        Logger::debug("getPokemonIncidentMon already set");
    } else {
        Logger::debug("Setting new getPokemonIncidentMon");
        this->getPokemonIncidentMon = getPokemonIncidentMon;
    }
}

void *ProtoCache::getGetPokemonDailyMon() const {
    return getPokemonDailyMon;
}

void ProtoCache::setGetPokemonDailyMon(void *getPokemonDailyMon) {
    if(this->getPokemonDailyMon) {
        Logger::debug("getPokemonDailyMon already set");
    } else {
        Logger::debug("Setting new getPokemonDailyMon");
        this->getPokemonDailyMon = getPokemonDailyMon;
    }
}

void *ProtoCache::getApproachComplete() const {
    return approachComplete;
}

void ProtoCache::setApproachComplete(void *approachComplete) {
    if(this->approachComplete) {
        Logger::debug("approachComplete already set");
    } else {
        Logger::debug("Setting new approachComplete");
        this->approachComplete = approachComplete;
    }
}

bool ProtoCache::isEnableSkipEncounterIntro() const {
    return this->enableSkipEncounterIntro;
}

EasyCatchType ProtoCache::getEasyCatchType() const {
    return this->easyCatchType;
}

void *ProtoCache::getGetShiny() const {
    return get_Shiny;
}

void ProtoCache::setGetShiny(void *get_Shiny) {
    if(this->get_Shiny) {
        Logger::debug("get_Shiny already set");
    } else {
        Logger::debug("Setting new get_Shiny");
        this->get_Shiny = get_Shiny;
    }
}

void *ProtoCache::getGetGender() const {
    return get_Gender;
}

void ProtoCache::setGetGender(void *get_Gender) {
    if(this->get_Gender) {
        Logger::debug("get_Gender already set");
    } else {
        Logger::debug("Setting new get_Gender");
        this->get_Gender = get_Gender;
    }
}

void *ProtoCache::getGetWeatherCondition() const {
    return get_WeatherCondition;
}

void ProtoCache::setGetWeatherCondition(void *get_WeatherCondition) {
    if(this->get_WeatherCondition) {
        Logger::debug("get_WeatherCondition already set");
    } else {
        Logger::debug("Setting new get_WeatherCondition");
        this->get_WeatherCondition = get_WeatherCondition;
    }
}

void *ProtoCache::getGetPokemonDisplay() const {
    return get_PokemonDisplay;
}

void ProtoCache::setGetPokemonDisplay(void *get_PokemonDisplay) {
    if(this->get_PokemonDisplay) {
        Logger::debug("get_PokemonDisplay already set");
    } else {
        Logger::debug("Setting new get_PokemonDisplay");
        this->get_PokemonDisplay = get_PokemonDisplay;
    }
}

void *ProtoCache::getEncOutGetPokemon() const {
    return encOut_get_Pokemon;
}

void ProtoCache::setEncOutGetPokemon(void *encOut_get_Pokemon) {
    if(this->encOut_get_Pokemon) {
        Logger::debug("encOut_get_Pokemon already set");
    } else {
        Logger::debug("Setting new encOut_get_Pokemon");
        this->encOut_get_Pokemon = encOut_get_Pokemon;
    }
}

void *ProtoCache::getEncOutGetIncPokemon() const {
    return encOut_get_IncPokemon;
}

void ProtoCache::setEncOutGetIncPokemon(void *encOut_get_IncPokemon) {
    if(this->encOut_get_IncPokemon) {
        Logger::debug("encOut_get_IncPokemon already set");
    } else {
        Logger::debug("Setting new encOut_get_IncPokemon");
        this->encOut_get_IncPokemon = encOut_get_IncPokemon;
    }
}

void *ProtoCache::getIncEncGetResult() const {
    return incEnc_get_Result;
}

void ProtoCache::setIncEncGetResult(void *incEnc_get_Result) {
    if(this->incEnc_get_Result) {
        Logger::debug("incEnc_get_Result already set");
    } else {
        Logger::debug("Setting new incEnc_get_Result");
        this->incEnc_get_Result = incEnc_get_Result;
    }
}

void *ProtoCache::getGetCis() const {
    return getCIS;
}

void ProtoCache::setGetCis(void *getCIS) {
    if (this->getCIS) {
        Logger::debug("getCIS already set");
    } else {
        Logger::debug("New getCIS");
        this->getCIS = getCIS;
    }
}

void *ProtoCache::getGetMethod() const {
    return getMethod;
}

void ProtoCache::setGetMethod(void *getMethod) {
    if (this->getMethod) {
        Logger::debug("getMethod already set");
    } else {
        Logger::debug("New getMethod");
        this->getMethod = getMethod;
    }
}

void *ProtoCache::getGameMasterData() const {
    return this->gameMasterData;
}

void *ProtoCache::getItemBagInstance() const {
    return this->itemBagGlobalInstance;
}


void ProtoCache::setItemBagInstance(void *itemBagGlobalInstance) {
    if (this->itemBagGlobalInstance) {
    } else {
        Logger::debug("New itemBagInstance");
        this->itemBagGlobalInstance = itemBagGlobalInstance;
    }
}


void ProtoCache::setGameMasterData(void *gameMasterData) {
    if (this->gameMasterData) {
        // Logger::debug("gameMasterData already set");
    } else {
        Logger::debug("New gameMasterData");
        this->gameMasterData = gameMasterData;
    }
}

void *ProtoCache::getGetMonLvl() const {
    return this->getMonLvl;
}

void ProtoCache::setGetMonLvl(void *getMonLvl) {
    if (this->getMonLvl) {
        Logger::debug("getMonLvl already set");
    } else {
        Logger::debug("New getMonLvl");
        this->getMonLvl = getMonLvl;
    }
}

void *ProtoCache::getTextSetTextPtr() const {
    return textSetTextPtr;
}

void ProtoCache::setTextSetTextPtr(void *textSetTextPtr) {
    if (this->textSetTextPtr) {
        Logger::debug("textSetTextPtr already set");
    } else {
        Logger::debug("New textSetTextPtr");
        this->textSetTextPtr = textSetTextPtr;
    }
}

const string &ProtoCache::getLatestIvSummary() const {
    return latestIVSummary;
}

void ProtoCache::setLatestIvSummary(const string &latestIvSummary) {
    latestIVSummary = latestIvSummary;
}

void *ProtoCache::getTextGetTextPtr() const {
    return textGetTextPtr;
}

void ProtoCache::setTextGetTextPtr(void *textGetTextPtr) {
    if (this->textGetTextPtr) {
        Logger::debug("textGetTextPtr already set");
    } else {
        Logger::debug("New textGetTextPtr");
        this->textGetTextPtr = textGetTextPtr;
    }
}

void ProtoCache::set_getHeightMPtr(void *getHeightMPtr) {
    if (this->getHeightMPtr) {
        Logger::debug("getHeightMPtr already set");
    } else {
        Logger::debug("New getHeightMPtr");
        this->getHeightMPtr = getHeightMPtr;
    }
}

void *ProtoCache::get_getHeightMPtr() {
    return this->getHeightMPtr;
}

void ProtoCache::set_getWeightMPtr(void *getWeightMPtr) {
    if (this->getWeightMPtr) {
        Logger::debug("getWeightMPtr already set");
    } else {
        Logger::debug("New getWeightMPtr");
        this->getWeightMPtr = getWeightMPtr;
    }
}

void *ProtoCache::get_getWeightMPtr() {
    return this->getWeightMPtr;
}

void ProtoCache::set_GetPokemonSettings(void *getPokemonSettings) {
    if (this->getPokemonSettings) {
        Logger::debug("getPokemonSettings already set");
    } else {
        Logger::debug("New getPokemonSettings");
        this->getPokemonSettings = getPokemonSettings;
    }
}

void *ProtoCache::get_GetPokemonSettings() {
    return this->getPokemonSettings;
}

void ProtoCache::set_GetPokedexHeightM(void *getPokedexHeightM) {
    if (this->getPokedexHeightM) {
        Logger::debug("getPokedexHeightM already set");
    } else {
        Logger::debug("New getPokedexHeightM");
        this->getPokedexHeightM = getPokedexHeightM;
    }
}

void *ProtoCache::get_GetPokedexHeightM() {
    return this->getPokedexHeightM;
}

void ProtoCache::set_GetPokedexWeightKg(void *getPokedexWeightKg) {
    if (this->getPokedexWeightKg) {
        Logger::debug("getPokedexWeightKg already set");
    } else {
        Logger::debug("New getPokedexWeightKg");
        this->getPokedexWeightKg = getPokedexWeightKg;
    }
}

void *ProtoCache::get_GetPokedexWeightKg() {
    return this->getPokedexWeightKg;
}

void *ProtoCache::getSUf() const {
    return sUf;
}

void ProtoCache::setSUf(void *sUf) {
    if (this->sUf) {
        Logger::debug("sUf already set");
    } else {
        Logger::debug("New sUf");
        this->sUf = sUf;
    }
}

void *ProtoCache::getEndInvSess() const {
    return endInvSess;
}

void ProtoCache::setEndInvSess(void *endInvSess) {
    if (this->endInvSess) {
        Logger::debug("endInvSess already set");
    } else {
        Logger::debug("New endInvSess");
        this->endInvSess = endInvSess;
    }
}

void *ProtoCache::getCompletePokestopDialog() const {
    return comInvBatt;
}

void ProtoCache::setCompletePokestopDialog(void *comInvBatt) {
    if (this->comInvBatt) {
        Logger::debug("comInvBatt already set");
    } else {
        Logger::debug("New comInvBatt");
        this->comInvBatt = comInvBatt;
    }
}

bool ProtoCache::isUf() {
    return this->uF;
}

bool ProtoCache::isSe() {
    return this->sE;
}

void *ProtoCache::getPDcEAc() const {
    return pDc_eAC;
}

void ProtoCache::setPDcEAc(void *pDcEAc) {
    if (this->pDc_eAC) {
        Logger::debug("pDc_eAC already set");
    } else {
        Logger::debug("New pDc_eAC");
        this->pDc_eAC = pDcEAc;
    }
}

void *ProtoCache::getPDeHAc() const {
    return pDc_eAhC;
}

void ProtoCache::setPDeHAc(void *pDcEhAc) {
    if (this->pDc_eAhC) {
        Logger::debug("pDc_eAhC already set");
    } else {
        Logger::debug("New pDc_eAhC");
        this->pDc_eAhC = pDcEhAc;
    }
}

void *ProtoCache::getPDpAc() const {
    return pDc_pAC;
}

void ProtoCache::setPDpAc(void *pDpAc) {
    if (this->pDc_pAC) {
        Logger::debug("pDc_pAC already set");
    } else {
        Logger::debug("pDc_pAC pDc_eAhC");
        this->pDc_pAC = pDpAc;
    }
}

void *ProtoCache::getPpNNo() const {
    return pP_nNo;
}

void ProtoCache::setPpNNo(void *pPNNo) {
    if (this->pP_nNo) {
        Logger::debug("pP_nNo already set");
    } else {
        Logger::debug("New pP_nNo");
        this->pP_nNo = pPNNo;
    }
}

void *ProtoCache::getMChSTTo() const {
    return mCh_sTTo;
}

void ProtoCache::setMChSTTo(void *mChSTTo) {
    if (this->mCh_sTTo) {
        Logger::debug("mCh_sTTo already set");
    } else {
        Logger::debug("New mCh_sTTo");
        this->mCh_sTTo = mChSTTo;
    }
}

void *ProtoCache::getSTtMaISo() const {
    return sTt_maISo;
}

void ProtoCache::setSTtMaISo(void *sTtMaISo) {
    if (this->sTt_maISo) {
        Logger::debug("sTt_maISo already set");
    } else {
        Logger::debug("New sTt_maISo");
        this->sTt_maISo = sTtMaISo;
    }
}

void *ProtoCache::getGBdPFIo() const {
    return gBdP_fIo;
}

void ProtoCache::setGBdPFIo(void *gBdPFIo) {
    if (this->gBdP_fIo) {
        Logger::debug("gBdP_fIo already set");
    } else {
        Logger::debug("New gBdP_fIo");
        this->gBdP_fIo = gBdPFIo;
    }
}

void *ProtoCache::getGBdPGIo() const {
    return gBdP_gIo;
}

void ProtoCache::setGBdPGIo(void *gBdPGIo) {
    if (this->gBdP_gIo) {
        Logger::debug("gBdP_gIo already set");
    } else {
        Logger::debug("New gBdP_gIo");
        this->gBdP_gIo = gBdPGIo;
    }
}

void *ProtoCache::getGRsOGo() const {
    return gRs_oGo;
}

void ProtoCache::setGRsOGo(void *gRsOGo) {
    if (this->gRs_oGo) {
        Logger::debug("gRs_oGo already set");
    } else {
        Logger::debug("New gRs_oGo");
        this->gRs_oGo = gRsOGo;
    }
}

void *ProtoCache::getFRsRGo() const {
    return fRs_rGo;
}

void ProtoCache::setFRsRGo(void *fRsRGo) {
    if (this->fRs_rGo) {
        Logger::debug("fRs_rGo already set");
    } else {
        Logger::debug("New fRs_rGo");
        this->fRs_rGo = fRsRGo;
    }
}

void *ProtoCache::getFLpRCVo() const {
    return fLp_rCVo;
}

void ProtoCache::setFLpRCVo(void *fLpRCVo) {
    if (this->fLp_rCVo) {
        Logger::debug("fLp_rCVo already set");
    } else {
        Logger::debug("New fLp_rCVo");
        this->fLp_rCVo = fLpRCVo;
    }
}

void *ProtoCache::getGiftingRpcService() const {
    return giftingRpcService;
}

void ProtoCache::setGiftingRpcService(void *giftingRpcService) {
    ProtoCache::giftingRpcService = giftingRpcService;
}

void *ProtoCache::getFriendsListPage() const {
    return friendsListPage;
}

void ProtoCache::setFriendsListPage(void *friendsListPage) {
    ProtoCache::friendsListPage = friendsListPage;
}

void *ProtoCache::getFriendsRpcService() const {
    return friendsRpcService;
}

void ProtoCache::setFriendsRpcService(void *friendsRpcService) {
    ProtoCache::friendsRpcService = friendsRpcService;
}

void *ProtoCache::getFLpFRSo() const {
    return fLp_fRSo;
}

void ProtoCache::setFLpFRSo(void *fLpFRSo) {
    if (this->fLp_fRSo) {
        Logger::debug("fLp_fRSo already set");
    } else {
        Logger::debug("New fLp_fRSo");
        this->fLp_fRSo = fLpFRSo;
    }
}

void ProtoCache::resetCellViewsOfFriendsList() {
    cellViewsOfFriendsListMutex.lock();
    this->cellViewsOfFriendsList.clear();
    cellViewsOfFriendsListMutex.unlock();
}

std::set<void *> ProtoCache::getCellViewsOfFriendsList() {
    cellViewsOfFriendsListMutex.lock();
    set<void*> retViews = set<void*>(this->cellViewsOfFriendsList.begin(), this->cellViewsOfFriendsList.end());
    cellViewsOfFriendsListMutex.unlock();
    return retViews;
}

void ProtoCache::addCellViewOfFriendsList(void* cellView) {
    cellViewsOfFriendsListMutex.lock();
    this->cellViewsOfFriendsList.insert(cellView);
    cellViewsOfFriendsListMutex.unlock();
}

bool ProtoCache::isSpeedupGifting() const {
    return speedupGifting;
}

void ProtoCache::setItemBag(void *itemBag) {
    if(this->itemBagInstance) {
        Logger::debug("itemBag already set");
    } else {
        Logger::debug("Setting new itemBag");
        this->itemBagInstance = itemBag;
    }
}

void *ProtoCache::getItemBag() const {
    return this->itemBagInstance;
}

void ProtoCache::setBuddyService(void *buddyService) {
    if(this->buddyService) {
        Logger::debug("buddyService already set");
    } else {
        Logger::debug("Setting new buddyService");
        this->buddyService = buddyService;
    }
}

void *ProtoCache::getBuddyService() const {
    return this->buddyService;
}

void ProtoCache::setPokemonBagService(void *pokemonBagService) {
    if(this->pokemonBagService) {
        Logger::debug("pokemonBagService already set");
    } else {
        Logger::debug("Setting new pokemonBagService");
        this->pokemonBagService = pokemonBagService;
    }
}

void *ProtoCache::getPokemonBagService() const {
    return this->pokemonBagService;
}

void ProtoCache::setBuddyRpcService(void *buddyRpcService) {
    if(this->buddyRpcService) {
        Logger::debug("buddyRpcService already set");
    } else {
        Logger::debug("Setting new buddyRpcService");
        this->buddyRpcService = buddyRpcService;
    }
}

bool ProtoCache::getBuddyGetGift() const {
    return this->buddyGift;
}

void ProtoCache::setBuddyGetGift(bool buddyGift) {
    this->buddyGift = buddyGift;
}

void *ProtoCache::getBuddyRpcService() const {
    return this->buddyRpcService;
}

void *ProtoCache::getGet_ItemCount() const {
    return itemCount;
}

void ProtoCache::setGet_ItemCount(void *itemCountPtr) {
    if(this->itemCount) {
        Logger::debug("itemCountPtr already set");
    } else {
        Logger::debug("Setting new itemCountPtr");
        this->itemCount = itemCountPtr;
    }
}

void ProtoCache::setInvManagementEnabled(bool enabled) {
    this->invManagementEnabled = enabled;
}

bool ProtoCache::isInvManagementEnabled() const {
    return invManagementEnabled;
}

void ProtoCache::setQuestList(const json& questList) {
    ProtoCache::questList = questList;
}

json ProtoCache::getQuestList() const {
    return this->questList;
}

void ProtoCache::setInventoryManagementItems(const json& inventoryManagement) {
    ProtoCache::inventoryItems = inventoryManagement;
}

int ProtoCache::getInventoryManagementItem(int item) {

    string invItem = "item_" + to_string(item);
    if (!inventoryItems.contains(invItem)) {
        return 99999;
    }
    try {
        int itemCountToKeep = inventoryItems[invItem].get<int>();
        if (itemCountToKeep >= 0) {
            return itemCountToKeep;
        }
    } catch(...) {
        return 99999;
    }
    return 99999;
}

void ProtoCache::setNameReplaceSettings(const json& nameReplaceSettings) {
    Logger::debug("Replacing " + to_string(nameReplaceSettings));
    ProtoCache::nameReplaceSettings = nameReplaceSettings;
}

bool ProtoCache::getNameReplaceSettings(const std::string& value) {
    if(!nameReplaceSettings.contains(value)) {
        return true;
    }

    try {
        auto valueSetting = nameReplaceSettings[value].get<bool>();
        return valueSetting;
    } catch(...) {
        return true;
    }
}

void *ProtoCache::getSendRecycleItem() const {
    return this->recycleItem;
}

void ProtoCache::setSendRecycleItem(void *recycleItem) {
    if(this->recycleItem) {
        Logger::debug("recycleItem already set");
    } else {
        Logger::debug("Setting new recycleItem");
        this->recycleItem = recycleItem;
    }
}

bool ProtoCache::processItemBag() {
    if (this->firstItemBagEnter) {
        this->firstItemBagEnter = false;
        return true;
    }

    this->itemBagEnter += 1;
    if (this->itemBagEnter >= 20) {
        this->itemBagEnter = 0;
        return true;
    }

    return false;

}

bool ProtoCache::processBuddyFirstTime() {
    return this->fistProcessBuddyEnter;
}

bool ProtoCache::processBuddy() {
    if (this->fistProcessBuddyEnter) {
        this->fistProcessBuddyEnter = false;
        return true;
    }

    this->processBuddyEnter += 1;
    if (this->processBuddyEnter >= 100) {
        this->processBuddyEnter = 0;
        return true;
    }

    return false;

}

void *ProtoCache::getEncodedHtmlString() const {
    return this->encodedHtmlString;
}

void ProtoCache::setEncodedHtmlString(void *encodedHtmlString) {
    if(this->encodedHtmlString) {
        Logger::debug("encodedHtmlString already set");
    } else {
        Logger::debug("Setting new encodedHtmlString");
        this->encodedHtmlString = encodedHtmlString;
    }
}

void *ProtoCache::getPpMove1() const {
    return pp_m1o;
}

void ProtoCache::setPpMove1(void *pp_m1o) {
    if (this->pp_m1o) {
        Logger::debug("pp_m1o already set");
    } else {
        Logger::debug("New pp_m1o");
        this->pp_m1o = pp_m1o;
    }
}

void *ProtoCache::getPpMove2() const {
    return pp_m2o;
}

void ProtoCache::setPpMove2(void *pp_m2o) {
    if (this->pp_m2o) {
        Logger::debug("pp_m2o already set");
    } else {
        Logger::debug("New pp_m2o");
        this->pp_m2o = pp_m2o;
    }
}

void ProtoCache::setMonMoves(const json& monMoves) {
    ProtoCache::monoMves = monMoves;
}

std::string ProtoCache::getMonMoves(const std::string& value) {
    if(!monoMves.contains(value)) {
        return "unknown";
    }
    try {
        auto moveType = monoMves[value]["type"].get<std::string>();
        return moveType;
    } catch(...) {
        return "unknown";
    }

}

void ProtoCache::setWildMon(const json& wildMon) {
    ProtoCache::wildMon = wildMon;
}

bool ProtoCache::getWildMon(const int type) {

    string monType = "type_" + to_string(type);
    if (!wildMon.contains(monType)) {
        return true;
    }
    try {
        bool showWildMon = wildMon[monType].get<bool >();
        return showWildMon;
    } catch(...) {
        return false;
    }
}

void ProtoCache::setHideWildMon(const json& hideWildMon) {
    ProtoCache::hideWildMon = hideWildMon;
}

bool ProtoCache::getHideWildMon(const int mon) {

    string monID = "mon_" + to_string(mon);
    if (!hideWildMon.contains(monID)) {
        return false;
    }
    return true;
}

void ProtoCache::setNotAutotransferMonIds(const json& autotransferMonIDs) {
    ProtoCache::autotransferMonIDs = autotransferMonIDs;
}

bool ProtoCache::getNotAutotransferMonIds(const int mon) {

    string monID = "mon_" + to_string(mon);
    bool transferInvert = InjectionSettings::instance().isAutotransferInverted();
    if (!autotransferMonIDs.contains(monID)) {
        return !transferInvert;
    }
    return transferInvert;
}

void ProtoCache::setNotAutorunMon(const json& notAutorunMon) {
    ProtoCache::notAutorunMon = notAutorunMon;
}

bool ProtoCache::getNotAutorunMon(const int mon) {

    string monID = "mon_" + to_string(mon);
    if (notAutorunMon.contains(monID)) {
        return true;
    }
    return false;
}

bool ProtoCache::getHideMonOnMap() {
    if (!hideWildMon.contains("hideonmap")) {
        return false;
    }
    try {
        bool showMonOnMap = hideWildMon["hideonmap"].get<bool>();
        return showMonOnMap;
    } catch(...) {
        return false;
    }
}

void *ProtoCache::get_pSpt1o() const {
    return pSpt1o;
}

void ProtoCache::set_pSpt1o(void *pSpt1o) {
    if (this->pSpt1o) {
        Logger::debug("pSpt1o already set");
    } else {
        Logger::debug("New pSpt1o");
        this->pSpt1o = pSpt1o;
    }
}

void *ProtoCache::get_pSpt2o() const {
    return pSpt2o;
}

void ProtoCache::set_pSpt2o(void *pSpt2o) {
    if (this->pSpt2o) {
        Logger::debug("pSpt2o already set");
    } else {
        Logger::debug("New pSpt2o");
        this->pSpt2o = pSpt2o;
    }
}

void ProtoCache::set_GetPokemonSettingsByID(void *getPokemonSettingsByID) {
    if (this->getPokemonSettingsByID) {
        Logger::debug("getPokemonSettingsByID already set");
    } else {
        Logger::debug("New getPokemonSettingsByID");
        this->getPokemonSettingsByID = getPokemonSettingsByID;
    }
}

void *ProtoCache::get_GetPokemonSettingsByID() const {
    return this->getPokemonSettingsByID;
}

void *ProtoCache::getGetWildPokemonID() const {
    return this->getWildPokemonID;
}

void ProtoCache::setGetWildPokemonID(void *getWildPokemonID) {
    if(this->getWildPokemonID) {
        Logger::debug("getWildPokemonID already set");
    } else {
        Logger::debug("Setting new getWildPokemonID");
        this->getWildPokemonID = getWildPokemonID;
    }
}

void *ProtoCache::getMemSetClear() const {
    return memSetClear;
}

void ProtoCache::setMemSetClear(void *memSetClear) {
    if(this->memSetClear) {
        Logger::debug("memSetClear already set");
    } else {
        Logger::debug("Setting new memSetClear");
        this->memSetClear = memSetClear;
    }
}

void *ProtoCache::getHsC() const {
    return hS_c;
}

void ProtoCache::setHsC(void *hSC) {
    if(this->hS_c) {
        Logger::debug("hS_c already set");
    } else {
        Logger::debug("Setting new hS_c");
        this->hS_c = hSC;
    }
}

void *ProtoCache::getHsGC() const {
    return hS_gC;
}

void ProtoCache::setHsGC(void *hSGC) {
    if(this->hS_gC) {
        Logger::debug("hS_gC already set");
    } else {
        Logger::debug("Setting new hS_gC");
        this->hS_gC = hSGC;
    }
}

void *ProtoCache::getDestroy() const {
    return destroy;
}

void ProtoCache::setDestroy(void *destroy) {
    if(this->destroy) {
        Logger::debug("destroy already set");
    } else {
        Logger::debug("Setting new destroy");
        this->destroy = destroy;
    }
}

void *ProtoCache::get_IncenseEncouterMonField() const {
    return iEmF;
}

void ProtoCache::set_IncenseEncouterMonField(void *iEmF) {
    if(this->iEmF) {
        Logger::debug("iEmF already set");
    } else {
        Logger::debug("Setting new iEmF");
        this->iEmF = iEmF;
    }
}

void *ProtoCache::get_DiskEncouterMonField() const {
    return dEmF;
}

void ProtoCache::set_DiskEncouterMonField(void *dEmF) {
    if(this->dEmF) {
        Logger::debug("dEmF already set");
    } else {
        Logger::debug("Setting new dEmF");
        this->dEmF = dEmF;
    }
}

void *ProtoCache::getMapPokemonOnTap() const {
    return mapPokemonOnTap;
}

void ProtoCache::setMapPokemonOnTap(void *mapPokemonOnTap) {
    if (this->mapPokemonOnTap) {
        Logger::debug("mapPokemonOnTap already set");
    } else {
        Logger::debug("Setting new mapPokemonOnTap");
        this->mapPokemonOnTap = mapPokemonOnTap;
    }
}

void ProtoCache::setGetIncenseEncounterId(void *getIncenseEncounterId) {
    if(this->getIncenseEncounterId) {
        Logger::debug("GetIncenseEncounterId already set");
    } else {
        Logger::debug("Setting new GetIncenseEncounterId");
        this->getIncenseEncounterId = getIncenseEncounterId;
    }
}

void *ProtoCache::getGetIncenseEncounterId() {
    return this->getIncenseEncounterId;
}

void ProtoCache::setSendIncenseEncounterRequestFunctionPointer(void *sendIncenseEncounterRequestFunctionPointer) {
    if(this->sendIncenseEncounterRequestFunctionPointer) {
        Logger::debug("sendIncenseEncounterRequestFunctionPointer already set");
    } else {
        Logger::debug("Setting new Incenseencounterrequestfunctionpointer");
        this->sendIncenseEncounterRequestFunctionPointer = sendIncenseEncounterRequestFunctionPointer;
    }
}

void* ProtoCache::getSendIncenseEncounterRequestFunctionPointer() {
    return this->sendIncenseEncounterRequestFunctionPointer;
}

void ProtoCache::setSendDiskEncounterRequestFunctionPointer(void *sendDiskEncounterRequestFunctionPointer) {
    if(this->sendDiskEncounterRequestFunctionPointer) {
        Logger::debug("sendDiskEncounterRequestFunctionPointer already set");
    } else {
        Logger::debug("Setting new sendDiskEncounterRequestFunctionPointer");
        this->sendDiskEncounterRequestFunctionPointer = sendDiskEncounterRequestFunctionPointer;
    }
}

void* ProtoCache::getSendDiskEncounterRequestFunctionPointer() {
    return this->sendDiskEncounterRequestFunctionPointer;
}



void ProtoCache::setLatLng(double lat, double lng) {
    this->latLngMutex.lock();
    Logger::debug("Registering location: " + std::to_string(lat) + ", " + std::to_string(lng));
    this->currentLocation.Latitude = lat;
    this->currentLocation.Longitude = lng;
    this->latLngMutex.unlock();
}

LatLng ProtoCache::getLatLng() {
    this->latLngMutex.lock();
    LatLng latLngCopy = LatLng();
    latLngCopy.Latitude = this->currentLocation.Latitude;
    latLngCopy.Longitude = this->currentLocation.Longitude;
    this->latLngMutex.unlock();

    return latLngCopy;
}

void *ProtoCache::getThrottleInstance() const {
    return throttleInstance;
}

void ProtoCache::setThrottleInstance(void *throttleInstance) {
    if (this->throttleInstance) {
        return;
    }
    this->throttleInstance = throttleInstance;
}

int ProtoCache::getLastBall() const {
    return lastBall;
}

void ProtoCache::setLastBall(int newLastBall) {
    this->lastBall = newLastBall;
}

void *ProtoCache::getUseItemEncounter() const {
    return useItemEncounter;
}

void ProtoCache::setUseItemEncounter(void *useItemEncounter) {
    if (this->useItemEncounter) {
        Logger::debug("useItemEncounter already set");
    } else {
        Logger::debug("Setting new useItemEncounter");
        this->useItemEncounter = useItemEncounter;
    }
}

void *ProtoCache::getSetBerry() const {
    return setBerry;
}

void ProtoCache::setSetBerry(void *setBerry) {
    if (this->setBerry) {
        Logger::debug("setBerry already set");
    } else {
        Logger::debug("Setting new setBerry");
        this->setBerry = setBerry;
    }
}


void *ProtoCache::getGet_CanUseBerry() const {
    return canUseBerry;
}

void ProtoCache::setGet_CanUseBerry(void *canUseBerry) {
    if (this->canUseBerry) {
        Logger::debug("canUseBerry already set");
    } else {
        Logger::debug("Setting new canUseBerry");
        this->canUseBerry = canUseBerry;
    }
}

void *ProtoCache::getGet_IMapPokemon() const {
    return iMapPokemon;
}

void ProtoCache::setGet_IMapPokemon(void *iMapPokemon) {
    if (this->iMapPokemon) {
        Logger::debug("iMapPokemon already set");
    } else {
        Logger::debug("Setting new iMapPokemon");
        this->iMapPokemon = iMapPokemon;
    }
}

void *ProtoCache::getGet_EncPokemon() const {
    return iEncPokemon;
}

void ProtoCache::setGet_EncPokemon(void *iEncPokemon) {
    if (this->iEncPokemon) {
        Logger::debug("iEncPokemon already set");
    } else {
        Logger::debug("Setting new iEncPokemon");
        this->iEncPokemon = iEncPokemon;
    }
}

void *ProtoCache::getHasBuddy() const {
    return hasBuddy;
}

void ProtoCache::setHasBuddy(void *hasBuddy) {
    if (this->hasBuddy) {
        Logger::debug("hasBuddy already set");
    } else {
        Logger::debug("Setting new hasBuddy");
        this->hasBuddy = hasBuddy;
    }
}

void *ProtoCache::getBuddyOnMap() const {
    return buddyOnMap;
}

void ProtoCache::setBuddyOnMap(void *buddyOnMap) {
    if (this->buddyOnMap) {
        Logger::debug("buddyOnMap already set");
    } else {
        Logger::debug("Setting new buddyOnMap");
        this->buddyOnMap = buddyOnMap;
    }
}

void *ProtoCache::getFeedBuddy() const {
    return feedBuddy;
}

void ProtoCache::setFeedBuddy(void *feedBuddy) {
    if (this->feedBuddy) {
        Logger::debug("feedBuddy already set");
    } else {
        Logger::debug("Setting new feedBuddy");
        this->feedBuddy = feedBuddy;
    }
}

void *ProtoCache::getPetBuddy() const {
    return petBuddy;
}

void ProtoCache::setPetBuddy(void *petBuddy) {
    if (this->petBuddy) {
        Logger::debug("petBuddy already set");
    } else {
        Logger::debug("Setting new petBuddy");
        this->petBuddy = petBuddy;
    }
}

void *ProtoCache::getBuddyOpenGift() const {
    return buddyopenGift;
}

void ProtoCache::setBuddyOpenGift(void *buddyopenGift) {
    if (this->buddyopenGift) {
        Logger::debug("buddyopenGift already set");
    } else {
        Logger::debug("Setting new buddyopenGift");
        this->buddyopenGift = buddyopenGift;
    }
}

void *ProtoCache::getGetPokemonProtoById() const {
    return pokemonProtoById;
}

void ProtoCache::setGetPokemonProtoById(void *pokemonProtoById) {
    if (this->pokemonProtoById) {
        Logger::debug("pokemonProtoById already set");
    } else {
        Logger::debug("Setting new pokemonProtoById");
        this->pokemonProtoById = pokemonProtoById;
    }
}

void *ProtoCache::getReleasePokemon() const {
    return releasePokemon;
}

void ProtoCache::setReleasePokemon(void *releasePokemon) {
    if (this->releasePokemon) {
        Logger::debug("releasePokemon already set");
    } else {
        Logger::debug("Setting new releasePokemon");
        this->releasePokemon = releasePokemon;
    }
}


void ProtoCache::addEncounterToCheckList(unsigned long long encounterId) {
    if(InjectionSettings::instance().isEnableAutotransfer()) {
        EncounterReleaseMutex.lock();
        checkEncounterReleaseListe.push_back(encounterId);
        EncounterReleaseMutex.unlock();
    }
}

std::list<unsigned long long>  ProtoCache::getEncounterToCheckList() {
    EncounterReleaseMutex.lock();
    std::list tmp = checkEncounterReleaseListe;
    checkEncounterReleaseListe.clear();
    EncounterReleaseMutex.unlock();
    return tmp;

}

void ProtoCache::addMonToEvolveList(unsigned long long evolveProto) {
    EncounterReleaseMutex.lock();
    monEvolveList.push_back(evolveProto);
    EncounterReleaseMutex.unlock();
}

std::list<unsigned long long>  ProtoCache::getMonEvolveList() {
    EvolveListMutex.lock();
    std::list tmp = monEvolveList;
    monEvolveList.clear();
    EvolveListMutex.unlock();
    return tmp;
}

void *ProtoCache::getII18nInstance() const {
    return iI18nInstance;
}

void ProtoCache::setII18nInstance(void *iI18nInstance) {
    if (this->iI18nInstance) {
        Logger::debug("iI18nInstance already set");
    } else {
        Logger::debug("Setting new iI18nInstance");
        this->iI18nInstance = iI18nInstance;
    }
}

void *ProtoCache::getGetPokemonName() const {
    return pokemonName;
}

void ProtoCache::setGetPokemonName(void *pokemonName) {
    if (this->pokemonName) {
        Logger::debug("pokemonName already set");
    } else {
        Logger::debug("Setting new pokemonName");
        this->pokemonName = pokemonName;
    }
}

void ProtoCache::setGetWildMonProtoPokemonProto(void *ptr) {
    if(this->getWildMonProtoPokemonProto) {
        Logger::debug("getWildMonProtoPokemonProto already set");
    } else {
        Logger::debug("Setting new getWildMonProtoPokemonProto");
        this->getWildMonProtoPokemonProto = ptr;
    }
}

void *ProtoCache::getGetWildMonProtoPokemonProto() const{
    return this->getWildMonProtoPokemonProto;
}

void ProtoCache::setGetWildMonProtoMonId(void *ptr) {
    if(this->getWildMonProtoMonId) {
        Logger::debug("getWildMonProtoMonId already set");
    } else {
        Logger::debug("Setting new getWildMonProtoMonId");
        this->getWildMonProtoMonId = ptr;
    }
}

void *ProtoCache::getGetWildMonProtoMonId() const {
    return this->getWildMonProtoMonId;
}

void *ProtoCache::getSendOff() const {
    return sendOff;
}

void ProtoCache::setSendOff(void *sendOff) {
    if(this->sendOff) {
        Logger::debug("sendOff already set");
    } else {
        Logger::debug("Setting new sendOff");
        this->sendOff = sendOff;
    }
}

void *ProtoCache::getWeirdArg() const {
    return weirdArg;
}

void ProtoCache::setWeirdArg(void *weirdArg) {
    this->weirdArg = weirdArg;
}

void *ProtoCache::getGetAdTargetingInfo() const {
    return adTargetingInfo;
}

void ProtoCache::setGetAdTargetingInfo(void *adTargetingInfo) {
    this->adTargetingInfo = adTargetingInfo;
}

void *ProtoCache::getGetPokemonSettingsProto() const {
    return pokemonSettingsProto;
}

void ProtoCache::setGetPokemonSettingsProto(void *pokemonSettingsProto) {
    this->pokemonSettingsProto = pokemonSettingsProto;
}

void *ProtoCache::getGetCandyCountForPokemonFamily() const {
    return candyCountForPokemonFamily;
}

void ProtoCache::setGetCandyCountForPokemonFamily(void *candyCountForPokemonFamily) {
    this->candyCountForPokemonFamily = candyCountForPokemonFamily;
}

void ProtoCache::setRpcHandler(void *handler) {
    if (this->rpcHandler != nullptr) {
        return;
    }
    this->rpcHandler = handler;
}

void* ProtoCache::getRpcHandler() {
    return this->rpcHandler;
}

std::string ProtoCache::get_gRp_n() {
    return this->gRp_n;
}

void ProtoCache::set_gRp_n(std::string val) {
    this->gRp_n = val;
}

std::set<uint64_t> ProtoCache::popCellIds() {
    std::lock_guard<std::mutex> guard(this->cellIdMutex);
    std::set<uint64_t> cellIdsTmp = std::set<uint64_t>();
    for (auto cellId : this->cellIdsToRequest) {
        cellIdsTmp.insert(cellId);
    }
    this->cellIdsToRequest.clear();
    return cellIdsTmp;
}

void ProtoCache::addCellIdRequested(uint64_t cellId) {
    std::lock_guard<std::mutex> guard(this->cellIdMutex);
    this->cellIdsRequested.insert(cellId);
}

std::set<uint64_t> ProtoCache::getCellIdsRequested() {
    std::lock_guard<std::mutex> guard(this->cellIdMutex);
    std::time_t curTimeT = std::time(nullptr);
    unsigned long long currtime = static_cast<long>(curTimeT);
    if (this->cellsLastCleared + 3600 < currtime) {
        // An hour has passed, clear the cell IDs to request routes freshly
        this->cellIdsRequested.clear();
        this->cellsLastCleared = currtime;
    }

    std::set<uint64_t> cellIdsTmp = std::set<uint64_t>();
    for (auto cellId : this->cellIdsRequested) {
        cellIdsTmp.insert(cellId);
    }
    return cellIdsTmp;
}


void ProtoCache::addCellId(uint64_t cellId) {
    std::lock_guard<std::mutex> guard(this->cellIdMutex);
    this->cellIdsToRequest.insert(cellId);
}

void *ProtoCache::getWeirdSecondArg() const {
    return weirdSecondArg;
}

void ProtoCache::setWeirdSecondArg(void *weirdArg) {
    ProtoCache::weirdSecondArg = weirdArg;
}

void *ProtoCache::getCeW() const {
    return cEw;
}

void ProtoCache::setCeW(void *cEw) {
    ProtoCache::cEw = cEw;
}

void *ProtoCache::getgIRs() const {
    return iRs;
}

void ProtoCache::setgIRs(void *iRs) {
    ProtoCache::iRs = iRs;
}

void *ProtoCache::getttEs() const {
    return tte;
}

void ProtoCache::setttEs(void *tte) {
    ProtoCache::tte = tte;
}

vector<string> ProtoCache::split(const string& input, const string& delim) {
    // passing -1 as the submatch index parameter performs splitting
    regex re(delim);
    sregex_token_iterator first{input.begin(), input.end(), re, -1}; sregex_token_iterator last;
    return {first, last};
}
