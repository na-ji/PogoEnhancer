#ifndef POGODROID_PROTOCACHE_H
#define POGODROID_PROTOCACHE_H

#include <externals/MonoPosixHelper.h>
#include <vector>
#include <map>
#include <mutex>
#include <thread>
#include "listeners/EnhancedThrowType.h"
#include "listeners/FastCatchType.h"
#include "listeners/EasyCatchType.h"
#include "caches/SharedQueue.hpp"
#include <tuple>
#include <set>
#include "json.hpp"
#include <list>
#include "../Util.h"

using namespace nlohmann;

struct DataEntry {
    int methodId;
    std::vector<uint8_t> data;
    long timestamp;
};

enum MonType {
    WILD = 0,
    DISK = 1
};

struct MonEntry {
    void *monPtr = nullptr;
    int monId = 0;
    unsigned long long encounterId = 0;
    MonType monType = WILD;
};

class ProtoCache
{
public:
    ProtoCache(const ProtoCache&) = delete;
    ProtoCache& operator=(const ProtoCache &) = delete;
    ProtoCache(ProtoCache &&) = delete;
    ProtoCache & operator=(ProtoCache &&) = delete;

    static auto& instance(){
        static ProtoCache ProtoCache;
        return ProtoCache;
    }
    static std::vector<std::string> split(const std::string &input, const std::string &regex);

    void parseApplicationSettings(unsigned int settings);

    void addData(int methodId, std::vector<uint8_t> bytes, long timestamp);

    void startUpdateThread();

    void setSymmKey(std::string key);
    std::string getSymmKey();
    void setLatLng(double lat, double lng);
    LatLng getLatLng();

    void setSer(void* newPointer);
    void* getSer();

    void setGetEncounterId(void* ptr);
    void* getGetEncounterId();

    static std::string convertPointerToReadableString(void* ptr);

    void *getGet_IndividualAttack() const;

    void setGet_IndividualAttack(void *get_IndividualAttack);

    void *getGet_IndividualDefense() const;

    void setGet_IndividualDefense(void *get_IndividualDefense);

    void *getGet_IndividualStamina() const;

    void setGet_IndividualStamina(void *get_IndividualStamina);

    void *getGet_Pokemon_RaidMapPokemon() const;

    void setGet_Pokemon_RaidMapPokemon(void *get_Pokemon_RaidMapPokemon);

    void *getSendSearchRpc() const;

    void setSendSearchRpc(void *sendSearchRpc);

private:
    ProtoCache () = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */

    std::mutex latLngMutex;
    volatile LatLng currentLocation = LatLng();
    //Variables used...
    SharedQueue<DataEntry> data;
    void* throttleInstance = nullptr;
public:
    void *getThrottleInstance() const;

    void setThrottleInstance(void *throttleInstance);

private:

    //methods
    void updateLoop();

    //attributes
    std::thread _thread;
    bool _threadStarted = false;
    bool _stopThread = false;
    std::mutex dataMutex;
    std::string symmKey;

    // pointers to methods...
    void* sEr = 0; // wMp_sERo|WildMapPokemon|public override IPromise<PokemonEncounterResponse> SendEncounterRequest()
    void* getEncounterId = 0;
    void* sendSearchRpc = 0;

    //getters of
    // public sealed class PokemonProto : IMessage`1<PokemonProto>, IMessage, IEquatable`1<PokemonProto>, IDeepCloneable`1<PokemonProto> // TypeDefIndex: 8030
    void* get_IndividualAttack = 0;
    void* get_IndividualDefense = 0;
    void* get_IndividualStamina = 0;
    void* get_CpMultiplier = 0;
    void* get_AdditionalCpMultiplier = 0;
    void* get_Shiny = 0;
public:
    void *getGetShiny() const;

    void setGetShiny(void *getShiny);

    void *getGetPokemonDisplay() const;

    void setGetPokemonDisplay(void *getPokemonDisplay);

    void *getEncOutGetPokemon() const;

    void setEncOutGetPokemon(void *encOutGetPokemon);

    void *getEncOutGetIncPokemon() const;

    void setEncOutGetIncPokemon(void *encOutGetIncPokemon);

    void *getIncEncGetResult() const;

    void setIncEncGetResult(void *incEncGetResult);

private:
    void* get_PokemonDisplay = 0;
    void* encOut_get_Pokemon = 0;
    void* encOut_get_IncPokemon = 0;
    void* incEnc_get_Result = 0;
    void* incEnc_get_Pokemon = 0;
    void* set_Cp = 0;
public:
    void *getGet_AdditionalCpMultiplier() const;

    void setGet_AdditionalCpMultiplier(void *get_AdditionalCpMultiplier);

public:
    void *getGet_CpMultiplier() const;

    void setGet_CpMultiplier(void *get_CpMultiplier);

private:

    /*
     * public override PokemonProto get_Pokemon(); // 0x7CCDA8
     * of
     * public class RaidMapPokemon : MapPokemon, IRaidMapPokemon, IMapPokemon // TypeDefIndex: 6358
     */
    void* get_Pokemon_RaidMapPokemon = 0;
    void* wildMonProto_getMon = 0;

    void* list_getCount = 0;

    void* summaryDismissed = 0;

    void* approachComplete = nullptr;
public:
    void *getApproachComplete() const;

    void setApproachComplete(void *approachComplete);

public:
    void *getSummaryDismissed() const;

    void setSummaryDismissed(void *summaryDismissed);

    void *getRunAway() const;

    void setRunAway(void *runAway);

private:
    void* runAway = 0;
    void *stopIsCooldown = 0;
    void *stopIsPlayerRange = 0;
public:
    void *getStopIsCooldown() const;

    void setStopIsCooldown(void *stopIsCooldown);

    void *getStopIsPlayerRange() const;

    void setStopIsPlayerRange(void *stopIsPlayerRange);

    void *getStopStart() const;

    void setStopStart(void *stopStart);

    void *getStopGetSpin() const;

    void setStopGetSpin(void *stopGetSpin);

    void *getStopClean() const;

    void setStopClean(void *stopClean);

    void *getStopComplete() const;

    void setStopComplete(void *stopComplete);

private:
    void *stopStart = 0;
    void *stopGetSpin = 0;
    void *stopClean = 0;
    void *stopComplete = 0;
    void *stopActive = 0;
    void* getCIS = nullptr;
    void* cisAtEnd = nullptr;
    void* readByte = nullptr;
    void* getMethod = nullptr;
    void* rCisLim = nullptr;
    void* textSetTextPtr = nullptr;
    void* textGetTextPtr = nullptr;
    void* getHeightMPtr = nullptr;
    void* getWeightMPtr = nullptr;
    void* getPokemonSettings = nullptr;
    void* getPokedexHeightM = nullptr;
    void* getPokedexWeightKg = nullptr;
    void* mCh_sTTo = nullptr;
    void* gBdP_fIo = nullptr;
    void* gRs_oGo = nullptr;
    void* fRs_rGo = nullptr;
    void* fLp_rCVo = nullptr;
    int lastBall = 0;
public:
    int getLastBall() const;

    void setLastBall(int lastBall);


public:
    void *getGRsOGo() const;

    void setGRsOGo(void *gRsOGo);

    void *getFRsRGo() const;

    void setFRsRGo(void *fRsRGo);

    void *getFLpRCVo() const;

    void setFLpRCVo(void *fLpRCVo);

private:
    void* gBdP_gIo = nullptr;
public:
    void *getGBdPFIo() const;

    void setGBdPFIo(void *gBdPFIo);

    void *getGBdPGIo() const;

    void setGBdPGIo(void *gBdPGIo);

public:
    void *getMChSTTo() const;

    void setMChSTTo(void *mChSTTo);

    void *getSTtMaISo() const;

    void setSTtMaISo(void *sTtMaISo);

private:
    void* sTt_maISo = nullptr;
public:
    void *getTextGetTextPtr() const;

    void setTextGetTextPtr(void *textGetTextPtr);

private:
    std::string latestIVSummary = "";
public:
    const std::string &getLatestIvSummary() const;

    void setLatestIvSummary(const std::string &latestIvSummary);

public:
    void *getTextSetTextPtr() const;

    void setTextSetTextPtr(void *textSetTextPtr);

public:
    void *getGetMethod() const;

    void setGetMethod(void *getMethod);

    void *getGetCis() const;

    void setGetCis(void *getCis);

    void *getStopActive() const;

    void setStopActive(void *stopActive);

    void *getPlayerServiceInstance() const;

    void setPlayerServiceInstance(void *playerServiceInstance);

    void *getServiceBagFull() const;

    void setServiceBagFull(void *serviceBagFull);

    void *getGameMasterData() const;
    void *getItemBagInstance() const;

    void setGameMasterData(void *gameMasterData);
    void setItemBagInstance(void *itemBagInstance);

    void *getGetMonLvl() const;

    void setGetMonLvl(void *monLvl);

private:
    void *getMonLvl = nullptr;
    void *gameMasterData = nullptr;
    void *itemBagGlobalInstance = nullptr;
    void *playerServiceInstance = 0;
    void *serviceBagFull = 0;
    void *getPokemonQuestMon = 0;
    void *getPokemonIncidentMon = 0;
    void *getPokemonDailyMon = 0;
    void *get_isEgg = 0;
    void *get_Gender = 0;
    void *get_WeatherCondition = 0;
    void *sUf = nullptr; // RenderQualityService|public void set_UnlockedFramerate(bool DIMHIILBLKA)
    void* pDc_eAC = nullptr;
    void* pDc_eAhC = nullptr;
    void* pDc_pAC = nullptr;
    void* pP_nNo = nullptr;
    void *endInvSess = nullptr;
    void *comInvBatt = nullptr;

    // used for skipping gift opening
    void* fLp_fRSo = nullptr;
    std::set<void*> cellViewsOfFriendsList = std::set<void*>();
    std::mutex cellViewsOfFriendsListMutex;
public:
    void *getFLpFRSo() const;

    void setFLpFRSo(void *fLpFRSo);

    void resetCellViewsOfFriendsList();
    std::set<void*> getCellViewsOfFriendsList();
    void addCellViewOfFriendsList(void* cellView);

private:
    void* giftingRpcService = nullptr;
public:
    void *getGiftingRpcService() const;

    void setGiftingRpcService(void *giftingRpcService);

    void *getFriendsListPage() const;

    void setFriendsListPage(void *friendsListPage);

    void *getFriendsRpcService() const;

    void setFriendsRpcService(void *friendsRpcService);

private:
    void* friendsListPage = nullptr;
    void* friendsRpcService = nullptr;

public:
    void *getPpNNo() const;

    void setPpNNo(void *pPNNo);
    // pP_nNo|PokemonProto|private string nickname_;
public:
    void *getPDcEAc() const;

    void setPDcEAc(void *pDcEAc);
    // PokemonDetailCamera|public void EvolveAnimationComplete()

    void *getPDeHAc() const;

    void setPDeHAc(void *pDcEAc);
    // PokemonDetailCamera|public void EggHatchAnimationComplete()

    void *getPDpAc() const;

    void setPDpAc(void *pDcEAc);
    // PokemonDetailCamera|public void PurifyAnimationComplete() { }
public:
    void *getSUf() const;

    void setSUf(void *sUf);
    // rQs_sUFo|RenderQualityService|public void set_UnlockedFramerate(bool DIMHIILBLKA)
public:
    void *getGetPokemonIncidentMon() const;

    void setGetPokemonIncidentMon(void *getPokemonIncidentMon);

    void *getGetPokemonDailyMon() const;

    void setGetPokemonDailyMon(void *getPokemonDailyMon);

    void *getGetGender() const;
    void setGetGender(void *getGet_Gender);

    void *getGetWeatherCondition() const;
    void setGetWeatherCondition(void *get_WeatherCondition);

    void *getEndInvSess() const;
    void setEndInvSess(void *get_GetEndInvSess);

    void *getCompletePokestopDialog() const;
    void setCompletePokestopDialog(void *comInvBatt);



public:
    void *getGetPokemonQuestMon() const;

    void setGetPokemonQuestMon(void *getPokemonQuestMon);

public:
    void *getWildMonProto_getMon() const;

    void setWildMonProto_getMon(void *wildMonProto_getMon);

    void *getGet_isEgg() const;

    void setGet_isEgg(void *get_isEgg);

    void *get_getHeightMPtr();

    void set_getHeightMPtr(void *getHeightMPtr);

    void *get_getWeightMPtr();

    void set_getWeightMPtr(void *getWeightMPtr);

    void *get_GetPokemonSettings();

    void set_GetPokemonSettings(void *getPokemonSettings);

    void *get_GetPokedexHeightM();

    void set_GetPokedexHeightM(void *getPokedexHeightM);

    void *get_GetPokedexWeightKg();

    void set_GetPokedexWeightKg(void *getPokedexWeightKg);

private:
    bool replaceCpWithIv;
    EnhancedThrowType enhancedThrowType = EnhancedThrowType::DISABLED;
    bool enableAutospin = false;
    bool enableSkipEncounterIntro = false;
    EasyCatchType easyCatchType = EasyCatchType::NO_EASY_CATCH;
    bool uF = false; // unlock FPS setting
    bool sE = false; // Skip Evolutionanimation
    bool speedupGifting = false;
public:
    bool isSpeedupGifting() const;

public:
    EasyCatchType getEasyCatchType() const;
    bool isUf(); // returns if the FPS have been unlocked
    bool isSe(); // returns if the Evolutionanimation should be skipped
public:
    bool isEnableAutospin() const;
    bool isEnableSkipEncounterIntro() const;

private:
    FastCatchType fastCatchType = FastCatchType::DISABLED_FAST_CATCH;
    std::string gRp_n = "";

public:
    FastCatchType getFastCatchType() const;

private:

    bool exLog = false;
    bool shopHeightWeightValue = false;

    void* itemBagInstance = nullptr;
    void* buddyService = nullptr;
    void* buddyRpcService = nullptr;
    void *itemCount = 0;
    void* recycleItem = 0;
    void* memSetClear = nullptr;
    void *pokemonBagService = nullptr;
    void* hS_c = nullptr; // HashSet<T>::clear
    void* hS_gC = nullptr;
    std::set<uint64_t> cellIdsToRequest;
    std::set<uint64_t> cellIdsRequested;
    std::mutex cellIdMutex;
    unsigned long long cellsLastCleared = 0;
public:
    void *getHsGC() const;

    void setHsGC(void *hSGC);
    // HashSet<T>::getCount
public:
    void *getHsC() const;

    void setHsC(void *hSC);
    // HashSet<T>::clear
public:
    void *getMemSetClear() const;

    void setMemSetClear(void *memSetClear);

public:
    EnhancedThrowType getEnhancedThrowType() const;

    void setItemBag(void *itemBag);
    void *getItemBag() const;

    void setBuddyService(void *buddyService);
    void *getBuddyService() const;

    void setPokemonBagService(void *pokemonBagService);
    void *getPokemonBagService() const;

    void setBuddyRpcService(void *buddyRpcService);
    void *getBuddyRpcService() const;

    void *getGet_ItemCount() const;
    void setGet_ItemCount(void *intmCountPtr);

    void setInvManagementEnabled(bool enabled);
    bool invManagementEnabled = false;
    json inventoryItems;
    json questList;
    json autotransferMonIDs;
    json notAutorunMon;

    bool isInvManagementEnabled() const;

    void setNameReplaceSettings(const json& nameReplaceSettings);
    bool getNameReplaceSettings(const std::string& value);
    json nameReplaceSettings;

    void setInventoryManagementItems(const json& settingsObject);
    int getInventoryManagementItem(int item);

    void setQuestList(const json& qeustList);
    json getQuestList() const;

    void setNotAutotransferMonIds(const json& settingsObject);
    bool getNotAutotransferMonIds(int item);

    void setNotAutorunMon(const json& settingsObject);
    bool getNotAutorunMon(int mon);

    void *getSendRecycleItem() const;
    void setSendRecycleItem(void *recycleItem);

    int itemBagEnter = 0;
    int processBuddyEnter = 0;
    bool firstItemBagEnter = true;
    bool fistProcessBuddyEnter = true;

    bool processItemBag();
    bool processBuddy();
    bool processBuddyFirstTime();

    void *getEncodedHtmlString() const;
    void setEncodedHtmlString(void *encodedHtmlString);
    void* encodedHtmlString = 0;

    void *getPpMove1() const;
    void setPpMove1(void *pp_m1o);
    void* pp_m1o = 0;

    void *getPpMove2() const;
    void setPpMove2(void *pp_m2o);
    void* pp_m2o = 0;

    void setMonMoves(const json& monMoves);
    std::string getMonMoves(const std::string& value);
    json monoMves;

    void setWildMon(const json& wildMon);
    bool getWildMon(const int type);
    json wildMon;

    void setHideWildMon(const json& hideWildMon);
    bool getHideWildMon(const int id);
    bool getHideMonOnMap();
    json hideWildMon;

    void *get_pSpt1o() const;
    void set_pSpt1o(void *pSpt1o);
    void* pSpt1o = 0;

    void *get_pSpt2o() const;
    void set_pSpt2o(void *pSpt2o);
    void* pSpt2o = 0;

    void *get_GetPokemonSettingsByID() const;
    void set_GetPokemonSettingsByID(void *getPokemonSettingsByID);
    void* getPokemonSettingsByID = 0;

    void *getGetWildPokemonID() const;
    void setGetWildPokemonID(void *getWildPokemonID);
    void* getWildPokemonID = 0;

    void *getDestroy() const;
    void setDestroy(void *destroy);
    void* destroy = 0;

    void *get_IncenseEncouterMonField() const;
    void set_IncenseEncouterMonField(void *iEmF);
    void* iEmF = 0;

    void *get_DiskEncouterMonField() const;
    void set_DiskEncouterMonField(void *dEmF);
    void* dEmF = 0;

    void *getMapPokemonOnTap() const;
    void setMapPokemonOnTap(void *mapPokemonOnTap);
    void* mapPokemonOnTap = 0;

    void setGetIncenseEncounterId(void* ptr);
    void* getGetIncenseEncounterId();
    void* getIncenseEncounterId = 0;

    void setSendIncenseEncounterRequestFunctionPointer(void* sendIncenseEncounterRequestFunctionPointer);
    void* getSendIncenseEncounterRequestFunctionPointer();
    void* sendIncenseEncounterRequestFunctionPointer = 0;

    void setSendDiskEncounterRequestFunctionPointer(void* sendDiskEncounterRequestFunctionPointer);
    void* getSendDiskEncounterRequestFunctionPointer();
    void* sendDiskEncounterRequestFunctionPointer = 0;

    void *getUseItemEncounter() const;
    void setUseItemEncounter(void* useItemEncounter);
    void* useItemEncounter = 0;

    void *getSetBerry() const;
    void setSetBerry(void* setBerry);
    void* setBerry = 0;

    void *getGet_CanUseBerry() const;
    void setGet_CanUseBerry(void* canUseBerry);
    void* canUseBerry = 0;

    void *getGet_IMapPokemon() const;
    void setGet_IMapPokemon(void* iMapPokemon);
    void* iMapPokemon = 0;

    void *getGet_EncPokemon() const;
    void setGet_EncPokemon(void* iEncPokemon);
    void* iEncPokemon = 0;

    void *getHasBuddy() const;
    void setHasBuddy(void* hasBuddy);
    void* hasBuddy = 0;

    void *getBuddyOnMap() const;
    void setBuddyOnMap(void* buddyOnMap);
    void* buddyOnMap = 0;

    void *getFeedBuddy() const;
    void setFeedBuddy(void* feedBuddy);
    void* feedBuddy = 0;

    void *getPetBuddy() const;
    void setPetBuddy(void* petBuddy);
    void* petBuddy = 0;

    bool getBuddyGetGift() const;
    void setBuddyGetGift(bool buddyGift);
    bool buddyGift = false;

    void *getBuddyOpenGift() const;
    void setBuddyOpenGift(void* buddyopenGift);
    void* buddyopenGift = 0;

    void *getGetPokemonProtoById() const;
    void setGetPokemonProtoById(void* pokemonProtoById);
    void* pokemonProtoById = 0;

    void *getReleasePokemon() const;
    void setReleasePokemon(void* releasePokemon);
    void* releasePokemon = 0;

    void addEncounterToCheckList(unsigned long long encoutnerId);
    std::list<unsigned long long>  getEncounterToCheckList();

    void addMonToEvolveList(unsigned long long encoutnerId);
    std::list<unsigned long long>  getMonEvolveList();

    void *getII18nInstance() const;
    void setII18nInstance(void* iI18nInstance);
    void* iI18nInstance = 0;

    void *getGetPokemonName() const;
    void setGetPokemonName(void* pokemonName);
    void* pokemonName = 0;

    void *getGetWildMonProtoPokemonProto() const;
    void setGetWildMonProtoPokemonProto(void* getWildMonProtoPokemonProto);
    void* getWildMonProtoPokemonProto = 0;

    void *getGetWildMonProtoMonId() const;
    void setGetWildMonProtoMonId(void* getWildMonProtoMonId);
    void* getWildMonProtoMonId = 0;

    void* sendOff = nullptr;
    void* weirdArg = nullptr;
    void* weirdSecondArg = nullptr;

    void *getGetPokemonSettingsProto() const;
    void setGetPokemonSettingsProto(void* pokemonSettingsProto);
    void* pokemonSettingsProto = 0;

    void *getGetCandyCountForPokemonFamily() const;
    void setGetCandyCountForPokemonFamily(void* candyCountForPokemonFamily);
    void* candyCountForPokemonFamily = 0;


public:
    void *getWeirdArg() const;

    void setWeirdArg(void *weirdArg);
    void *getWeirdSecondArg() const;

    void setWeirdSecondArg(void *weirdArg);

    std::string get_gRp_n();
    void set_gRp_n(std::string val);

    void addCellId(uint64_t cellId);
    std::set<uint64_t> popCellIds();

    void addCellIdRequested(uint64_t cellId);
    std::set<uint64_t> getCellIdsRequested();
public:
    void *getGetAdTargetingInfo() const;

    void setGetAdTargetingInfo(void *adTargetingInfo);
    void* adTargetingInfo = nullptr;
public:
    void *getSendOff() const;

    void setSendOff(void *sendOff);

    void setRpcHandler(void *rpcHandler);
    void* getRpcHandler();
    void* rpcHandler = 0;

    static void send(const DataEntry& proto, const std::string& symKey);

    void setCeW(void *cEw);
    void* getCeW() const;
    void* cEw = 0;

    void setgIRs(void *iRs);
    void* getgIRs() const;
    void* iRs = 0;

    void setttEs(void *tte);
    void* getttEs() const;
    void* tte = 0;

};


#endif //POGODROID_PROTOCACHE_H
