//
//
//

#include "GWp.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../InjectionSettings.h"
#include "../EncQueue.h"
#include "StaticReplacements.h"
#include "../il2cpp_util/Il2CppDomain.hpp"
#include <string>
#include "../InfoClient.h"


void GWp::on_enter(Gum::AbstractInvocationContext *context) {
    Logger::fatal("Boot init");
    initTriggered = true;
}

void GWp::on_leave(Gum::AbstractInvocationContext *context) {
    Logger::fatal("Leave boot");
    this->setupDone = true;
}

/**
 * Method which sets all hooks which are read by classname / method name etc
 */
void GWp::thirdBatch() {
    Logger::fatal("Placing third batch");
    Il2CppUtil::Domain *domain = InfoClient::instance().getDomain();

    /**
     * Setup images/Assemblies
     */
    Il2CppUtil::Assembly *assemblyDittoApi;
    do {
        assemblyDittoApi = domain->getAssemblyByName("Niantic.Platform.DittoApi.dll");
        if (assemblyDittoApi == nullptr) {
            std::this_thread::sleep_for(std::chrono::milliseconds(200));
            Logger::warning("Assembly not loaded yet");
        }
    } while (assemblyDittoApi == nullptr);
    Il2CppUtil::Image *imageDittoApi = assemblyDittoApi->getImage();

    Il2CppUtil::Assembly *assemblyCSharp;
    do {
        assemblyCSharp = domain->getAssemblyByName("holo-game.dll");
        if (assemblyCSharp == nullptr) {
            std::this_thread::sleep_for(std::chrono::milliseconds(200));
            Logger::warning("Assembly not loaded yet");
        }
    } while (assemblyCSharp == nullptr);
    Il2CppUtil::Image *imageCSharp = assemblyCSharp->getImage();

    Il2CppUtil::Assembly *assemblyUnityEngineUi;
    do {
        assemblyUnityEngineUi = domain->getAssemblyByName("UnityEngine.UI.dll");
        if (assemblyUnityEngineUi == nullptr) {
            std::this_thread::sleep_for(std::chrono::milliseconds(200));
            Logger::warning("Assembly not loaded yet");
        }
    } while (assemblyUnityEngineUi == nullptr);
    Il2CppUtil::Image *imageUnityEngineUi = assemblyUnityEngineUi->getImage();

    Il2CppUtil::Assembly *mscorlib = domain->getAssemblyByName("mscorlib.dll");
    if (mscorlib == nullptr) {
        Logger::debug("Nullptr assembly for \"mscorlib.dll\", leaving");
        Il2CppUtil::Assembly *mscorlibSecond = domain->getAssemblyByName("mscorlib.dll");
        if (mscorlibSecond == nullptr) {
            Logger::debug("Nullptr assemblySecondAttempt for \"mscorlib.dll\", leaving");
            return;
        } else {
            mscorlib = mscorlibSecond;
        }
    }
    Il2CppUtil::Image *mscorlibImage = mscorlib->getImage();
    /**
    * Namespace: Niantic.Platform.Ditto.Rpc
    * internal sealed class ActionRequest : IActionRequest // TypeDefIndex: 32765
    * 	public void OnResponse(IActionResponse response) { }
    */

    Il2CppUtil::Class actionRequest = imageDittoApi->getClass("ActionRequest");
    void *onResponse = actionRequest.getMethodByName("OnResponse", 1);
    // 0x393805C
    interceptor.attach_listener(
            onResponse, &responseListener,
            nullptr);
    Logger::debug("Response listener placed");

    /**
     * class GameMasterData
     * public O]WMVQUYVQS GetFormSettings(XZQV[SV[VWP QYUSNOMWVVN) { }
     * Used to be: baseAddr + HAP::get_gMd_iFPo()
     */
    Logger::debug("Fetching class GameMasterData");
    Il2CppUtil::Class gameMasterDataClass = imageCSharp->getClass("GameMasterData");
    Logger::debug("Trying to fetch GetFormSettings");
    void* gameMasterDataClassGetFormSettings = gameMasterDataClass.getMethodByName("GetFormSettings", 1);
    interceptor.attach_listener(
            gameMasterDataClassGetFormSettings, &formPossible,
            nullptr);
    // First usage of imageCSharp, add some sleep after loading classes for the came to operate
    //std::this_thread::sleep_for(std::chrono::seconds (10));
    Logger::debug("Trying to fetch GetPokemonLevelOfCpMultiplier");
    void* gameMasterDataClassGetPokemonLevelOfCpMultiplier = gameMasterDataClass.getMethodByName("GetPokemonLevelOfCpMultiplier", 1);
    ProtoCache::instance().setGetMonLvl(gameMasterDataClassGetPokemonLevelOfCpMultiplier);


    /**
     * public class Text : MaskableGraphic, ILayoutElement // TypeDefIndex: 28085
     * 	public virtual void set_text(string value) { }
     */
    Logger::debug("Fetching class Text");
    Il2CppUtil::Class text = imageUnityEngineUi->getClass("Text");
    Logger::debug("Trying to fetch set_text");
    void* textSet_text = text.getMethodByName("set_text", 1);
    ProtoCache::instance().setTextSetTextPtr(textSet_text);


    /**
     * public class Text : MaskableGraphic, ILayoutElement // TypeDefIndex: 28085
     * 	public virtual string get_text() { }
     */
    Logger::debug("Trying to fetch get_text");
    void* textGet_text = text.getMethodByName("get_text", 0);
    ProtoCache::instance().setTextGetTextPtr(textGet_text);

    /**
     * public class EncounterNameplate : MonoBehaviour, IHideable // TypeDefIndex: 7777
     * 	public void SetPokemonUI(MMOVZWM]ZQS NTZVVZNNOZR, GameplayWeatherProto.Types.WeatherCondition MW]URZQQ]NS) { }
     */
    Logger::debug("Fetching class EncounterNameplate");
    Il2CppUtil::Class encounterNameplate = imageCSharp->getClass("EncounterNameplate");
    Logger::debug("Trying to fetch SetPokemonUI");
    void* encounterNameplateSetPokemonUI = encounterNameplate.getMethodByName("SetPokemonUI", 2);
    interceptor.replace_function(
            encounterNameplateSetPokemonUI,
            (gpointer) &StaticReplacements::encName, nullptr);

    /**
     * public class FriendsListPage : BaseFriendsListPage // TypeDefIndex: 4860
     * 	public override void Initialize() { }
     */
    Logger::debug("Fetching class FriendsListPage");
    Il2CppUtil::Class friendsListPage = imageCSharp->getClass("FriendsListPage");
    Logger::debug("Trying to fetch Initialize");
    void* friendsListPageInitialize = friendsListPage.getMethodByName("Initialize", 0);
    interceptor.attach_listener(
            friendsListPageInitialize, &fLpIoListener,
            nullptr);

    /**
     * public class FriendsListPage : BaseFriendsListPage // TypeDefIndex: 4860
     * 	public override void RefreshCellView(ConditionalHideAttribute YYSMPWOQ]SS) { }
     */
    Logger::debug("Trying to fetch RefreshCellView");
    void* friendsListPageRefreshCellView = friendsListPage.getMethodByName("RefreshCellView", 1);
    ProtoCache::instance().setFLpRCVo(friendsListPageRefreshCellView);



    /**
     * // Namespace: Niantic.Holoholo.Internationalization
     * public class I18n : MonoBehaviour, ZMXS[NSWTO[, II18n, IARLogHandler // TypeDefIndex: 4576
     * 	public static II18n get_Instance() { }
     */
    Logger::debug("Fetching class I18n");
    Il2CppUtil::Class i18n = imageCSharp->getClass("I18n");
    Logger::debug("Trying to fetch get_Instance");
    void* i18nGet_Instance = i18n.getMethodByName("get_Instance", 0);
    ProtoCache::instance().setII18nInstance(i18nGet_Instance);

/**
 * public class I18n : MonoBehaviour, ZMXS[NSWTO[, II18n, IARLogHandler // TypeDefIndex: 4576
 * 	public string PokemonName(HoloPokemonId TU[VROTWNSO) { }
 */
    Logger::debug("Trying to fetch PokemonName");
    void* i18nPokemonName = i18n.getMethodByName("PokemonName", 1);
    ProtoCache::instance().setGetPokemonName(i18nPokemonName);

    /**
     * public class FriendsRpcService : MonoBehaviour, IFriendsRpcService // TypeDefIndex: 4715
     * 	public void RemoveGiftbox(string MNUVOYZUSOS, ulong NONUWTUSURM) { }
     */
    Logger::debug("Fetching class FriendsRpcService");
    Il2CppUtil::Class friendsRpcService = imageCSharp->getClass("FriendsRpcService");
    Logger::debug("Trying to fetch RemoveGiftbox");
    void* friendsRpcServiceRemoveGiftbox = friendsRpcService.getMethodByName("RemoveGiftbox", 2);
    ProtoCache::instance().setFRsRGo(friendsRpcServiceRemoveGiftbox);


    /**
     * public class TroyDiskMapPlacePokemon : PoiMapPokemon // TypeDefIndex: 12351
     * 	private bool YTZYNPWXXTS(PowerUpPokestopEncounterOutProto NYUNOT[]XPR, out bool NRPVMUONO[], out bool WP[WXQOMNWN) { }
     * -> 3 param
     */
    Logger::debug("Fetching class TroyDiskMapPlacePokemon");
    Il2CppUtil::Class troyDiskMapPlacePokemon = imageCSharp->getClass("TroyDiskMapPlacePokemon");
    Logger::debug("Trying to fetch TroyDiskMapPlacePokemon::verifyProto");
    auto troyDiskMapPlacePokemonVerifyProtoCandidates = troyDiskMapPlacePokemon.getMethodsOfClass(3);
    if (troyDiskMapPlacePokemonVerifyProtoCandidates.empty() || troyDiskMapPlacePokemonVerifyProtoCandidates.size() > 1) {
        Logger::fatal("Failed to find verifyProto");
        exit(1);
    } else {
        void* troyDiskMapPlacePokemonMethodsThreeArgsMethod = reinterpret_cast<void *>(troyDiskMapPlacePokemonVerifyProtoCandidates.at(0)->methodPointer);
        interceptor.replace_function(
                (gpointer) (troyDiskMapPlacePokemonMethodsThreeArgsMethod),
                (gpointer) &StaticReplacements::verifyDiskProto, nullptr);
    }

    /**
     * public class TroyDiskMapPokemon : PoiMapPokemon // TypeDefIndex: 12351
     * 	public override KMDIHMDKEBL<PokemonEncounterMapTile3RequestProto> SendEncounterRequest() { }
     */
    Logger::debug("Trying to fetch SendEncounterRequest");
    void* troyDiskMapPokemonSendEncounterRequest = troyDiskMapPlacePokemon.getMethodByName("SendEncounterRequest", 0);
    ProtoCache::instance().setSendDiskEncounterRequestFunctionPointer(troyDiskMapPokemonSendEncounterRequest);

    /**
     * Niantic.Holoholo.Map
     * class MapPlacePokemon
     * public virtual void Initialize(RXXNX]VMZRZ VTPY]ZWRPT], LatLng QWO[SRVTUQV) { }
     * used to be baseAddr + HAP::get_dMp_iDo()
     */
    Logger::debug("Fetching class MapPlacePokemon");
    Il2CppUtil::Class poiMapPokemonClass = imageCSharp->getClass("MapPlacePokemon");
    Logger::debug("Trying to fetch .ctor");
    void* poiMapPokemonClassInitialize = poiMapPokemonClass.getMethodByName(".ctor", 0);
    interceptor.attach_listener(
            (gpointer) (poiMapPokemonClassInitialize),
            &updateIncensePokemonListener, nullptr);


    /**
 * public class RenderQualityService : MonoBehaviour, IAccountPermissionsService // TypeDefIndex: 2323
	public void set_AppWantsNativeFrameRate(bool value) { }
 */
    Logger::debug("Fetching class RenderQualityService");
    Il2CppUtil::Class renderQualityService = imageCSharp->getClass("RenderQualityService");
    Logger::debug("Trying to fetch set_UnlockedFramerate");
    void* renderQualityServiceSet_UnlockedFramerate = renderQualityService.getMethodByName("set_AppWantsNativeFrameRate", 1);
    ProtoCache::instance().setSUf(renderQualityServiceSet_UnlockedFramerate);

    // Check if the unlock FPS setting was enabled
    if (ProtoCache::instance().isUf()) {
        interceptor.replace_function(
                renderQualityServiceSet_UnlockedFramerate,
                (gpointer) &StaticReplacements::sUf, nullptr);
        /**
         * public class PerformanceMetricsService : MonoBehaviour, BOMJMJCPJEL // TypeDefIndex: 2318
         * 	public void EncounterStarted() { }
         */
        Logger::debug("Fetching class PerformanceMetricsService");
        Il2CppUtil::Class performanceMetricsService = imageCSharp->getClass("PerformanceMetricsService");
        Logger::debug("Trying to fetch EncounterStarted");
        void* performanceMetricsServiceEncounterStarted = performanceMetricsService.getMethodByName("EncounterStarted", 0);
        interceptor.attach_listener(
                performanceMetricsServiceEncounterStarted, &performanceMetricsService_EncounterStartedListener,
                nullptr);

    }

    /**
     * public class PokemonInventoryGuiController : LegacyGuiController, M]YU]VMOOQU, T]ZZWN]YSXZ, AJAJDOCLENJ, IGuiLayerable, IHideable, UN[TPMW]XXX // TypeDefIndex: 11743
     * 	private void OYUMNM[VZVZ(PokemonListLineItemView VSVN[U[UMXV, PokemonProto NYUNOT[]XPR, int [NYPQNXW]VS) { }
     * 	-> Suche nach Anzahl Param, private, erstes Arg PokemonListLineItemView
     */
    Il2CppUtil::Class voidClass = mscorlibImage->getClass("Void");
    // TODO: Searching by type crashes the client somehow
    const Il2CppType *typeOfVoid = voidClass.getType();
    Logger::debug("Fetching class PokemonInventoryGuiController");
    Il2CppUtil::Class pokemonInventoryGuiController = imageCSharp->getClass(
            "PokemonInventoryGuiController");
    Logger::debug("Trying to fetch pokemonItemListener");
/*
    auto pokemonInventoryGuiControllerThreeArgsVoidRet = pokemonInventoryGuiController.getMethodsOfClass(
            typeOfVoid, 3, 12772);
    if (pokemonInventoryGuiControllerThreeArgsVoidRet.empty() ||
        pokemonInventoryGuiControllerThreeArgsVoidRet.size() > 1) {
        Logger::fatal("Failed to find pokemonItemListener");
        exit(1);
    } else {
        Logger::fatal("Found pokemonItemListener");
        auto troyDiskMapPokemonMethodsThreeArgsMethod = pokemonInventoryGuiControllerThreeArgsVoidRet.at(
                0);
        interceptor.attach_listener(reinterpret_cast<void *>(troyDiskMapPokemonMethodsThreeArgsMethod->methodPointer),
                                    &pokemonItemListener, nullptr);
    }
    std::this_thread::sleep_for(std::chrono::seconds (1));
    */

/**
 * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
 * 	public void SelectPokeball(Item POS[WQQWV]Y) { }
 */
    Logger::debug("Fetching class EncounterInteractionState");
    Il2CppUtil::Class encounterInteractionState = imageCSharp->getClass(
            "EncounterInteractionState");
    void *encounterInteractionStateSelectPokeball = encounterInteractionState.getMethodByName(
            "SelectPokeball", 1);
    interceptor.replace_function(
            encounterInteractionStateSelectPokeball,
            (gpointer) &StaticReplacements::selPo, nullptr);

    /**
 * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
 * 	public void IntroCompleted() { }
 */
    Logger::debug("Trying to fetch IntroCompleted");
    void *encounterInteractionStateIntroCompleted = encounterInteractionState.getMethodByName(
            "IntroCompleted", 0);
    interceptor.attach_listener(
            encounterInteractionStateIntroCompleted, &breakoutFromPokeball,
            nullptr);

    /**
 * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
 * 	public void PokemonBrokeOut() { }
 */
    Logger::debug("Trying to fetch PokemonBrokeOut");
    void *encounterInteractionPokemonBrokeOut = encounterInteractionState.getMethodByName(
            "PokemonBrokeOut", 0);
    interceptor.attach_listener(
            encounterInteractionPokemonBrokeOut, &breakoutFromPokeball,
            nullptr);

    /**
     * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
     * 	public KMDIHMDKEBL<CatchPokemonOutProto> AttemptCapture(PokeballThrow ]PWQOVNMRUX) { }
     */
    Logger::debug("Trying to fetch AttemptCapture");
    void *encounterInteractionStateAttemptCapture = encounterInteractionState.getMethodByName(
            "AttemptCapture", 1);
    interceptor.replace_function(
            encounterInteractionStateAttemptCapture,
            (gpointer) &StaticReplacements::attemptCapture, nullptr);

    /**
     * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
     * 	public bool get_CanUseBerry() { }
     */
    Logger::debug("Trying to fetch get_CanUseBerry");
    void *encounterInteractionStateGet_CanUseBerry = encounterInteractionState.getMethodByName(
            "get_CanUseBerry", 0);
    ProtoCache::instance().setGet_CanUseBerry(encounterInteractionStateGet_CanUseBerry);

    /**
     * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
     * 	public IEncounterPokemon get_EncounterPokemon() { }
     */
    Logger::debug("Trying to fetch get_EncounterPokemon");
    void *encounterInteractionStateGet_EncounterPokemon = encounterInteractionState.getMethodByName(
            "get_EncounterPokemon", 0);
    ProtoCache::instance().setGet_EncPokemon(encounterInteractionStateGet_EncounterPokemon);


    if (ProtoCache::instance().getFastCatchType() == FastCatchType::Quick) {
        /**
         * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
         * 	public void PokemonCaptured(CatchPokemonOutProto [ZTWXRNPT]U) { }
         * -> Suche per Name sofern nicht obf
         */
        Logger::debug("Trying to fetch PokemonCaptured");
        void *encounterInteractionStatePokemonCaptured = encounterInteractionState.getMethodByName(
                "PokemonCaptured", 1);
        interceptor.replace_function(
                encounterInteractionStatePokemonCaptured,
                (gpointer) &StaticReplacements::pokemonCaptured, nullptr);

        /**
         * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
         * 	public void RunAway() { }
         */
        Logger::debug("Trying to fetch RunAway");
        void *encounterInteractionStateRunAway = encounterInteractionState.getMethodByName(
                "RunAway", 0);
        ProtoCache::instance().setRunAway(encounterInteractionStateRunAway);

        /**
         * public class EncounterCaptureState : GameState, ]RYOTVTZRMY // TypeDefIndex: 7614
         * 	public void SummaryDismissed(CatchPokemonOutProto PTM[TOPO[PT) { }
         */
        Logger::debug("Fetching class EncounterCaptureState");
        Il2CppUtil::Class encounterCaptureState = imageCSharp->getClass("EncounterCaptureState");
        Logger::debug("Trying to fetch SummaryDismissed");
        void *encounterCaptureStateSummaryDismissed = encounterCaptureState.getMethodByName(
                "SummaryDismissed", 1);
        ProtoCache::instance().setSummaryDismissed(encounterCaptureStateSummaryDismissed);
    }
    //std::this_thread::sleep_for(std::chrono::seconds (1));


    /**
     * public class DefaultEncounter : MonoBehaviour, IEncounterStrategy // TypeDefIndex: 7804
     * 	public bool get_OverridesAttemptCapture() { }
     * 	-> Suche per name sofern nicht obf
     */
    Logger::debug("Fetching class DefaultEncounter");
    Il2CppUtil::Class defaultEncounter = imageCSharp->getClass("DefaultEncounter");
    Logger::debug("Fetching class get_OverridesAttemptCapture");
    void *defaultEncounterGet_OverridesAttemptCapture = defaultEncounter.getMethodByName("get_OverridesAttemptCapture", 0);
    interceptor.replace_function(
            (gpointer) (defaultEncounterGet_OverridesAttemptCapture),
            (gpointer) &StaticReplacements::attemptCapture, nullptr);

    /**
     * public class ResearchEncounter : MonoBehaviour, IEncounterStrategy, AJAJDOCLENJ // TypeDefIndex: 7846
     *  public virtual bool get_OverridesAttemptCapture() { }
     *  -> Suche per Name sofern nicht obfuscated in non-obf dump
     */
    Logger::debug("Fetching class ResearchEncounter");
    Il2CppUtil::Class researchEncounter = imageCSharp->getClass("ResearchEncounter");
    Logger::debug("Fetching class get_OverridesAttemptCapture");
    void *researchEncounterGet_OverridesAttemptCapture = defaultEncounter.getMethodByName("get_OverridesAttemptCapture", 0);
    interceptor.replace_function(
            (gpointer) (researchEncounterGet_OverridesAttemptCapture),
            (gpointer) &StaticReplacements::attemptCapture, nullptr);

    /**
     * public class EncounterPokemon : MonoBehaviour, IEncounterPokemon // TypeDefIndex: 7662
     * 	public MMOVZWM]ZQS get_MapPokemon() { }
     */
    Logger::debug("Fetching class EncounterPokemon");
    Il2CppUtil::Class encounterPokemon = imageCSharp->getClass("EncounterPokemon");
    Logger::debug("Fetching class get_MapPokemon");
    void *encounterPokemonGet_MapPokemon = encounterPokemon.getMethodByName("get_MapPokemon", 0);
    ProtoCache::instance().setGet_IMapPokemon(encounterPokemonGet_MapPokemon);

    /**
     * public class EncounterGuiController : GuiController, TVW[PY]RQ]], T]ZZWN]YSXZ, AJAJDOCLENJ, IGuiLayerable, IHideable, IArmpSerializer<EncounterGuiController.OELBEFKCIMH> // TypeDefIndex: 7770
     * 	public void set_ButtonsVisible(bool USM[QUXNUST) { }
     */
    Logger::debug("Fetching class EncounterGuiController");
    Il2CppUtil::Class encounterGuiControllerClass = imageCSharp->getClass("EncounterGuiController");
    Logger::debug("Fetching class set_ButtonsVisible");
    void *encounterGuiControllerSetButtonsVisible = encounterGuiControllerClass.getMethodByName("set_ButtonsVisible", 1);
    interceptor.replace_function(
            (gpointer) (encounterGuiControllerSetButtonsVisible),
            (gpointer) &StaticReplacements::sBv, nullptr);

    /**
 * public class QuestMapPokemon : MapPokemon // TypeDefIndex: 1847
 * 	public override PokemonProto get_Pokemon() { }
 */
    Logger::debug("Fetching class QuestMapPokemon");
    Il2CppUtil::Class questMapPokemon = imageCSharp->getClass("QuestMapPokemon");
    Logger::debug("Trying to fetch GetFormSettings");
    void* questMapPokemonGet_Pokemon = questMapPokemon.getMethodByName("get_Pokemon", 0);
    ProtoCache::instance().setGetPokemonQuestMon(questMapPokemonGet_Pokemon);

    /**
     * public class QuestMapPokemon : MapPokemon // TypeDefIndex: 1847
     * 	public override int get_Cp() { }
     */
    Logger::debug("Trying to fetch get_Cp");
    void* questMapPokemonget_Cp = questMapPokemon.getMethodByName("get_Cp", 0);
    interceptor.attach_listener(
            questMapPokemonget_Cp, &questGetCpListener,
            nullptr);


    /**
 * public class DailyEncounterMapPokemon : MapPokemon, IArmpSerializer<DailyEncounterMapPokemon.Data>, [OTXY]UWMUY, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12103
 * 	public override PokemonProto get_Pokemon() { }
 */
    Logger::debug("Fetching class DailyEncounterMapPokemon");
    Il2CppUtil::Class dailyEncounterMapPokemon = imageCSharp->getClass("DailyEncounterMapPokemon");
    Logger::debug("Trying to fetch get_Pokemon");
    void* dailyEncounterMapPokemonGet_Pokemon = dailyEncounterMapPokemon.getMethodByName("get_Pokemon", 0);
    ProtoCache::instance().setGetPokemonDailyMon(dailyEncounterMapPokemonGet_Pokemon);

    /**
     * public class DailyEncounterMapPokemon : MapPokemon, IArmpSerializer<DailyEncounterMapPokemon.Data>, [OTXY]UWMUY, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12103
     * 	public override int get_Cp() { }
     */
    Logger::debug("Trying to fetch get_Cp");
    void* dailyEncounterMapPokemonGet_Cp = dailyEncounterMapPokemon.getMethodByName("get_Cp", 0);
    interceptor.attach_listener(
            dailyEncounterMapPokemonGet_Cp, &dailyGetCpListener,
            nullptr);

    /**
 * public class IncidentMapPokemon : MapPokemon, ZUVMOXXN]WR, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12343
 * public override PokemonProto get_Pokemon() { }
 */
    Logger::debug("Fetching class IncidentMapPokemon");
    Il2CppUtil::Class incidentMapPokemon = imageCSharp->getClass("IncidentMapPokemon");
    Logger::debug("Trying to fetch get_Pokemon");
    void* incidentMapPokemonGet_Pokemon = incidentMapPokemon.getMethodByName("get_Pokemon", 0);
    ProtoCache::instance().setGetPokemonIncidentMon(incidentMapPokemonGet_Pokemon);

    /**
     * public class IncidentMapPokemon : MapPokemon, ZUVMOXXN]WR, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12343
     * public override int get_Cp() { }
     */
    Logger::debug("Trying to fetch get_Cp");
    void* incidentMapPokemonget_Cp = incidentMapPokemon.getMethodByName("get_Cp", 0);
    interceptor.attach_listener(
            incidentMapPokemonget_Cp, &incidentGetCpListener,
            nullptr);


/**
 * public class WildMapPokemon : MapPokemon, PMGOBDJDPHL, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12212
 * 	public override KMDIHMDKEBL<PokemonEncounterMapTile3RequestProto> SendEncounterRequest() { }
 */
    Logger::debug("Fetching class WildMapPokemon");
    Il2CppUtil::Class wildMapPokemon = imageCSharp->getClass("WildMapPokemon");
    Logger::debug("Trying to fetch SendEncounterRequest");
    void* wildMapPokemonSendEncounterRequest = wildMapPokemon.getMethodByName("SendEncounterRequest", 0);
    ProtoCache::instance().setSer(wildMapPokemonSendEncounterRequest);

    /**
     * public class WildMapPokemon : MapPokemon, PMGOBDJDPHL, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12212
     * 	public void set_VisibleOnMap(bool USM[QUXNUST) { }
     */
    Logger::debug("Trying to fetch set_VisibleOnMap");
    void* wildMapPokemonSetVisibleOnMap = wildMapPokemon.getMethodByName("set_VisibleOnMap", 1);
    interceptor.replace_function(
            (gpointer) (wildMapPokemonSetVisibleOnMap),
            (gpointer) &StaticReplacements::setVisibleOnMap, nullptr);

    /**
     * public class WildMapPokemon : MapPokemon, PMGOBDJDPHL, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12212
     * 	public override int get_PokemonId() { }
     */
        Logger::debug("Trying to fetch get_PokemonId");
        void* wildMapPokemonGet_PokemonId = wildMapPokemon.getMethodByName("get_PokemonId", 0);
        ProtoCache::instance().setGetWildPokemonID(wildMapPokemonGet_PokemonId);

    /**
 * public class WildMapPokemon : MapPokemon, PMO]OMQZQWV, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12108
 * 	public override ulong get_EncounterId() { }
 * 	-> Suche per Name
*/



    Logger::debug("Trying to fetch get_EncounterId");
    void* wildMapPokemonGet_EncounterId = wildMapPokemon.getMethodByName("get_EncounterId", 0);
    ProtoCache::instance().setGetEncounterId(wildMapPokemonGet_EncounterId);



/**
 * public class WildMapPokemon : MapPokemon, PMGOBDJDPHL, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12212
 * 	private bool WUS]MT[WU[[(EncounterOutProto NYUNOT[]XPR, out bool NRPVMUONO[], out bool WP[WXQOMNWN) { }
 * -> Einzige private bool ODER einfach Anzahl param
 */
    Logger::debug("Trying to fetch verifyProto");
    auto wildMapPokemonMethodsThreeArgs = wildMapPokemon.getMethodsOfClass(3);
    if (wildMapPokemonMethodsThreeArgs.empty() || wildMapPokemonMethodsThreeArgs.size() > 1) {
        Logger::fatal("Failed to find verifyProto");
        exit(1);
    } else {
        void* wildMapPokemonMethodsThreeArgsMethod = reinterpret_cast<void *>(wildMapPokemonMethodsThreeArgs.at(0)->methodPointer);
        interceptor.replace_function(
                wildMapPokemonMethodsThreeArgsMethod,
                (gpointer) &StaticReplacements::verifyProto, nullptr);
    }
    //std::this_thread::sleep_for(std::chrono::seconds (1));

    /**
     * public class WildMapPokemon : MapPokemon, PMGOBDJDPHL, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12212
     * 	public void Initialize(WildPokemonProto NYUNOT[]XPR) { }
     * 	-> Suche per Name
     */
    Logger::debug("Trying to fetch Initialize");
    void* wildMapPokemonInitialize = wildMapPokemon.getMethodByName("Initialize", 1);
    interceptor.attach_listener(
            (gpointer) (wildMapPokemonInitialize),
            &updateWildPokemonListener, nullptr);

    /**
     * public class IncenseMapPokemon : MapPokemon // TypeDefIndex: 12349
     * 	private bool YTZYNPWXXTS(IncenseEncounterOutProto NYUNOT[]XPR, out bool NRPVMUONO[], out bool WP[WXQOMNWN) { }
     * -> über Paramanzahl
     */
    Logger::debug("Fetching class IncenseMapPokemon");
    Il2CppUtil::Class incenseMapPokemon = imageCSharp->getClass("IncenseMapPokemon");
    Logger::debug("Trying to fetch IncenseMapPokemon::verifyProto");
    auto incenseMapPokemonVerifyProtoCandidates = incenseMapPokemon.getMethodsOfClass(3);
    if (incenseMapPokemonVerifyProtoCandidates.empty() || incenseMapPokemonVerifyProtoCandidates.size() > 1) {
        Logger::fatal("Failed to find verifyProto");
        exit(1);
    } else {
        void* incenseMapPokemonMethodsThreeArgsMethod = reinterpret_cast<void *>(incenseMapPokemonVerifyProtoCandidates.at(0)->methodPointer);
        interceptor.replace_function(
                (gpointer) (incenseMapPokemonMethodsThreeArgsMethod),
                (gpointer) &StaticReplacements::verifyIncenseProto, nullptr);
    }

    /**
     * public class RaidMapPokemon : MapPokemon, PMO]OMQZQWV, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12108
     * 	public override PokemonProto get_Pokemon() { }
     * 	-> Suche per Name
     */
    Logger::debug("Fetching class RaidMapPokemon");
    Il2CppUtil::Class raidMapPokemon = imageCSharp->getClass("RaidMapPokemon");
    Logger::debug("Trying to fetch get_Pokemon");
    void* raidMapPokemonGet_Pokemon = raidMapPokemon.getMethodByName("get_Pokemon", 0);
    ProtoCache::instance().setGet_Pokemon_RaidMapPokemon(raidMapPokemonGet_Pokemon);


    /**
     * public class RaidMapPokemon : MapPokemon, PMO]OMQZQWV, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12108
     * public override int get_Cp() { }
     */
    Logger::debug("Trying to fetch get_Cp");
    void* raidMapPokemonget_Cp = raidMapPokemon.getMethodByName("get_Cp", 0);
    interceptor.attach_listener(
            raidMapPokemonget_Cp, &raidGetCpListener,
            nullptr);




    /**
     * public class IncenseMapPokemon : MapPokemon // TypeDefIndex: 12349
     * 	public void Initialize(GetIncensePokemonOutProto NYUNOT[]XPR, Item X]Z]PYSTNSN) { }
     */
    Logger::debug("Trying to fetch Initialize");
    void* incenseMapPokemonInitialize = incenseMapPokemon.getMethodByName("Initialize", 2);
    interceptor.attach_listener(
            incenseMapPokemonInitialize,
            &updateIncensePokemonListener, nullptr);

    /**
     * public class IncenseMapPokemon : MapPokemon // TypeDefIndex: 12349
     * 	public override ulong get_EncounterId() { }
     */
    Logger::debug("Trying to fetch get_EncounterId");
    void* incenseMapPokemonGet_EncounterId = incenseMapPokemon.getMethodByName("get_EncounterId", 0);
    ProtoCache::instance().setGetIncenseEncounterId(incenseMapPokemonGet_EncounterId);

    /**
     * public abstract class MapPokemon : MonoBehaviour, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12194
     * 	public virtual void Destroy() { }
     */
    Logger::debug("Fetching class MapPokemon");
    Il2CppUtil::Class mapPokemon = imageCSharp->getClass("MapPokemon");
    Logger::debug("Trying to fetch Destroy");
    void* mapPokemonDestroy = mapPokemon.getMethodByName("Destroy", 0);
    ProtoCache::instance().setDestroy(mapPokemonDestroy);


    /**
     * public class IncenseMapPokemon : MapPokemon // TypeDefIndex: 12349
     * 	public override KMDIHMDKEBL<PokemonEncounterMapTile3RequestProto> SendEncounterRequest() { }
     */
    Logger::debug("Trying to fetch SendEncounterRequest");
    void* incenseMapPokemonSendEncounterRequest = incenseMapPokemon.getMethodByName("SendEncounterRequest", 0);
    ProtoCache::instance().setSendIncenseEncounterRequestFunctionPointer(incenseMapPokemonSendEncounterRequest);


    /**
     * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
     * 	public bool get_IsCoolingDown() { }
     */
    Logger::debug("Fetching class MapPokestop");
    Il2CppUtil::Class mapPokestop = imageCSharp->getClass("MapPokestop");
    Logger::debug("Trying to fetch get_IsCoolingDown");
    void* mapPokestopGet_IsCoolingDown = mapPokestop.getMethodByName("get_IsCoolingDown", 0);
    ProtoCache::instance().setStopIsCooldown(mapPokestopGet_IsCoolingDown);

    /**
     * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
     * 	public void UpdateData(PokemonFortProto NYUNOT[]XPR) { }
     * 	-> Suche per Name

    Logger::debug("Trying to fetch UpdateData");
    void* mapPokestopUpdateData = mapPokestop.getMethodByName("UpdateData", 1);
    interceptor.attach_listener(
            (gpointer) (mapPokestopUpdateData),
            &updateWildPokemonListener, nullptr);

*/
    /**
    * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
    * 	public bool get_IsPlayerInRange() { }
    */
    Logger::debug("Trying to fetch get_IsPlayerInRange");
    void* mapPokestopGet_IsPlayerInRange = mapPokestop.getMethodByName("get_IsPlayerInRange", 0);
    ProtoCache::instance().setStopIsPlayerRange(mapPokestopGet_IsPlayerInRange);

    /**
    * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
    * 	public MapPokestopInteractive StartInteractiveMode() { }
    */
    Logger::debug("Trying to fetch StartInteractiveMode");
    void* mapPokestopStartInteractiveMode = mapPokestop.getMethodByName("StartInteractiveMode", 0);
    ProtoCache::instance().setStopStart(mapPokestopStartInteractiveMode);

    /**
     * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
     * 	public void CompleteInteractiveMode() { }
     */
    Logger::debug("Trying to fetch CompleteInteractiveMode");
    void* mapPokestopCompleteInteractiveMode = mapPokestop.getMethodByName("CompleteInteractiveMode", 0);
    ProtoCache::instance().setStopComplete(mapPokestopCompleteInteractiveMode);

    /**
     *public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
     * 	public bool get_IsActive() { }
     */
    Logger::debug("Trying to fetch get_IsActive");
    void* mapPokestopGet_IsActive = mapPokestop.getMethodByName("get_IsActive", 0);
    ProtoCache::instance().setStopActive(mapPokestopGet_IsActive);

    /**
     * public class PoiItemSpinner : BasePoiSpinner, DGOKHLILKCN // TypeDefIndex: 12322
     * 	public override void Cleanup() { }
     */
    Logger::debug("Fetching class PoiItemSpinner");
    Il2CppUtil::Class poiItemSpinner = imageCSharp->getClass("PoiItemSpinner");
    Logger::debug("Trying to fetch Cleanup");
    auto poiItemSpinnerNoArgsMethods = poiItemSpinner.getMethodsOfClass(0);
    void* methodPtrCleanup = nullptr;
    for (auto method : poiItemSpinnerNoArgsMethods) {
        std::string methodName = poiItemSpinner.getNameOfMethod(method);
        Logger::debug(methodName);
        if (std::string(methodName).compare("Cleanup") == 0) {
            Logger::debug("Found cleanup");
            methodPtrCleanup = reinterpret_cast<void *>(method->methodPointer);
            break;
        }
    }
    if (methodPtrCleanup == nullptr) {
        Logger::fatal("Failed to find Cleanup");
        exit(1);
    } else {
        Logger::debug("Cleanup found: " + ProtoCache::convertPointerToReadableString(methodPtrCleanup));
        Logger::debug("Should be: " + ProtoCache::convertPointerToReadableString((gpointer) (baseAddr + 0x1C5479C)));
        ProtoCache::instance().setStopClean(methodPtrCleanup);
    }

    /**
     * public class PokemonInventoryCellView : MonoBehaviour, IArmpSerializer<PokemonInventoryCellView.Initializer> // TypeDefIndex: 10147
     * 	private void XMMZUZRTTNS(ISpriteWidget ZSPX]XTOVYQ, PokemonProto NYUNOT[]XPR, bool TOO]NUYVUS[, IDictionary<string, IOIOBFFGHGF<Sprite>> WX[YO]Y[RYV) { }
     * -> 4 params (Only method)
     */
    Logger::debug("Fetching class PokemonInventoryCellView");
    Il2CppUtil::Class pokemonInventoryCellView = imageCSharp->getClass("PokemonInventoryCellView");
    Logger::debug("Trying to fetch pIlL_aMo");
    auto methods = pokemonInventoryCellView.getMethodsOfClass(4);
    if (methods.empty() || methods.size() > 1) {
        Logger::fatal("Failed to find pIlL_aMo");
        exit(1);
    } else {
        void* pokemonInventoryCellViewPIlL_aMo = reinterpret_cast<void *>(methods.at(0)->methodPointer);
        interceptor.replace_function(
                pokemonInventoryCellViewPIlL_aMo,
                (gpointer) &StaticReplacements::invName, nullptr);
    }



    /**
     * public class PlayerService : ObservableService<PlayerService.PlayerInfo>, NNHFDEPHNFB, IObservableService<PlayerService.PlayerInfo>, EMIMIFCDEGB<PlayerService.PlayerInfo> // TypeDefIndex: 570
     * 	public HJGGHOKGMEM get_ItemBag() { }
     * 	// TODO: Wofür brauchen wir den ItemBag listener wenn wir im PlayerService einfach die Methode aufrufen können?
     */
    Logger::debug("Fetching class PlayerService");
    Il2CppUtil::Class playerService = imageCSharp->getClass("PlayerService");
    Logger::debug("Trying to fetch get_ItemBag");
    //void *playerServiceGet_ItemBag = playerService.getMethodByName("get_ItemBag", 0);
    //interceptor.attach_listener(
    //        playerServiceGet_ItemBag, &getItemBagListener,
    //        nullptr);

    /**
     * public class PlayerService : ObservableService<PlayerService.PlayerInfo>, NNHFDEPHNFB, IObservableService<PlayerService.PlayerInfo>, EMIMIFCDEGB<PlayerService.PlayerInfo> // TypeDefIndex: 570
     * 	public bool get_BagIsFull() { }
     */
    Logger::debug("Trying to fetch get_BagIsFull");
    void *playerServiceGet_BagIsFull = playerService.getMethodByName("get_BagIsFull", 0);
    ProtoCache::instance().setServiceBagFull(playerServiceGet_BagIsFull);

    /**
     * Namespace: Niantic.Holoholo
     * class PlayerService
     * public int GetCandyCountForPokemonFamily(Y]ZXNNM[Q[M NQZY[XNQYUT) { }
     * Used to be: baseAddr + HAP::get_pS_gCcFfo()
     */
    Logger::debug("Trying to fetch GetCandyCountForPokemonFamily");
    void *playerServiceClassGetCandyCountForPokemonFamily = playerService.getMethodByName(
            "GetCandyCountForPokemonFamily", 1);
    ProtoCache::instance().setGetCandyCountForPokemonFamily(
            (gpointer) (playerServiceClassGetCandyCountForPokemonFamily));

    /**
     * public class BuddyPokemonService : ObservableService<BuddyPokemonProto>, WO[YOOWXSNS, DDAMPNKNFLJ, IObservableService<BuddyPokemonProto> // TypeDefIndex: 6470
     * 	public bool get_HasBuddy() { }
     */
    Logger::debug("Fetching class BuddyPokemonService");
    Il2CppUtil::Class buddyPokemonService = imageCSharp->getClass("BuddyPokemonService");
    Logger::debug("Trying to fetch get_HasBuddy");
    void* buddyPokemonServiceGet_HasBuddy = buddyPokemonService.getMethodByName("get_HasBuddy", 0);
    ProtoCache::instance().setHasBuddy(buddyPokemonServiceGet_HasBuddy);

    /**
     * public class BuddyPokemonService : ObservableService<BuddyPokemonProto>, WO[YOOWXSNS, DDAMPNKNFLJ, IObservableService<BuddyPokemonProto> // TypeDefIndex: 6470
     * 	public bool get_BuddySpawnedOnMap() { }
     */
    Logger::debug("Trying to fetch get_BuddySpawnedOnMap");
    void* buddyPokemonServiceGet_BuddySpawnedOnMap = buddyPokemonService.getMethodByName("get_BuddySpawnedOnMap", 0);
    ProtoCache::instance().setBuddyOnMap(buddyPokemonServiceGet_BuddySpawnedOnMap);

    /**
     * public class BuddyRpcService : MonoBehaviour, IBuddyRpcService, KCIINPIAENA // TypeDefIndex: 6179
     * 	public KMDIHMDKEBL<BuddyFeedingOutProto> Feed(Item SXW]TNWRYPY, int TRWXTQYSTNO) { }
     */
    Logger::debug("Fetching class BuddyRpcService");
    Il2CppUtil::Class buddyRpcService = imageCSharp->getClass("BuddyRpcService");
    Logger::debug("Trying to fetch Feed");
    void* buddyRpcServiceFeed = buddyRpcService.getMethodByName("Feed", 2);
    ProtoCache::instance().setFeedBuddy(buddyRpcServiceFeed);

    /**
     * public class BuddyRpcService : MonoBehaviour, IBuddyRpcService, KCIINPIAENA // TypeDefIndex: 6179
     * 	public KMDIHMDKEBL<BuddyPettingOutProto> Pet() { }
     */
    Logger::debug("Trying to fetch Pet");
    void* buddyRpcServicePet = buddyRpcService.getMethodByName("Pet", 0);
    ProtoCache::instance().setPetBuddy(buddyRpcServicePet);

    /**
     * public class BuddyRpcService : MonoBehaviour, IBuddyRpcService, KCIINPIAENA // TypeDefIndex: 6179
     * 	public KMDIHMDKEBL<OpenBuddyGiftOutProto> OpenGift() { }
     */
    Logger::debug("Trying to fetch OpenGift");
    void* buddyRpcServiceOpenGift = buddyRpcService.getMethodByName("OpenGift", 0);
    ProtoCache::instance().setBuddyOpenGift(buddyRpcServiceOpenGift);


    // Check if Skip Evolve animation is enabled
    if (ProtoCache::instance().isSe()) {
        // evolveAnimation
        /**
         * public class PokemonDetailCamera : MonoBehaviour // TypeDefIndex: 9792
         * 	public void EvolveAnimationComplete() { }
         */

        Logger::debug("Fetching class PokemonCutsceneService");
        Il2CppUtil::Class pokemonDetailCameraClass = imageCSharp->getClass("PokemonCutsceneService");

        Logger::debug("Trying to fetch ShowEvolveAnimation");
        void* pokemonDetailCameraShowEvolveAnimation = pokemonDetailCameraClass.getMethodByName("PlayEvolveCutscene", 2);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraShowEvolveAnimation),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);


        Logger::debug("Trying to fetch EggHatchAnimationComplete");
        void* pokemonDetailCameraShowEggHatchAnimation = pokemonDetailCameraClass.getMethodByName("PlayEggHatchCutscene", 2);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraShowEggHatchAnimation),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);


        Logger::debug("Trying to fetch PlayPurifyLightSfx");
        void* pokemonDetailCameraPlayPurifyLightSfx = pokemonDetailCameraClass.getMethodByName("PlayPurifyCutscene", 1);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraPlayPurifyLightSfx),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);

        Logger::debug("Trying to fetch PlayNewSpeciesCutscene");
        void* pokemonDetailCameraPlayNewSpeciesCutscene = pokemonDetailCameraClass.getMethodByName("PlayNewSpeciesCutscene", 1);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraPlayNewSpeciesCutscene),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);

        Logger::debug("Trying to fetch PlayMegaEvolveCutscene");
        void* pokemonDetailCameraPlayMegaEvolveCutscene = pokemonDetailCameraClass.getMethodByName("PlayMegaEvolveCutscene", 4);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraPlayMegaEvolveCutscene),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);

        Logger::debug("Trying to fetch PlayUnfusionCutscene");
        void* pokemonDetailCameraPlayUnfusionCutscene = pokemonDetailCameraClass.getMethodByName("PlayUnfusionCutscene", 2);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraPlayUnfusionCutscene),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);

        Logger::debug("Trying to fetch PlayFusionCutscene");
        void* pokemonDetailCameraPlayFusionCutscene = pokemonDetailCameraClass.getMethodByName("PlayFusionCutscene", 2);
        interceptor.replace_function(
                (gpointer) (pokemonDetailCameraPlayFusionCutscene),
                (gpointer) &StaticReplacements::skipCutscene, nullptr);


    }



}

void GWp::setInterceptor(const Gum::InterceptorImpl &interceptorVal) {
    this->interceptor = interceptorVal;
}

void GWp::setBaseAddr(GumAddress baseAddrVal) {
    this->baseAddr = baseAddrVal;
}

/**
 * Methods which are crucial to be set as early as possible by offset provided in HAP
 */
void GWp::placeFirstBatch() {
    Logger::debug("Placing initial hooks");
    // TODO: Add version suppression if set via settings?

    /**
     * public class BuddyPokemonService : ObservableService<BuddyPokemonProto>, WO[YOOWXSNS, DDAMPNKNFLJ, IObservableService<BuddyPokemonProto> // TypeDefIndex: 6470
     * 	private void PWUWZZNUYST() { }
     * -> Nope
     */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->bPs_iO()), &buddyPokemonServiceListener,
            nullptr);


    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->bPs_iO()), &buddyPokemonServiceListener,
            nullptr);

    /**
    * public class PlayerService : ObservableService<PlayerService.PlayerInfo>, NNHFDEPHNFB, IObservableService<PlayerService.PlayerInfo>, EMIMIFCDEGB<PlayerService.PlayerInfo> // TypeDefIndex: 570
    * 	public static PlayerService get_Instance() { }
    */

    //ProtoCache::instance().setPlayerServiceInstance(playerServiceGet_Instance);

    Logger::debug("Trying to set playerInstance Listener");

    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->pS_gQo()), &playerInstanceListener,
            nullptr);
    Logger::debug("PlayerInstance listener placed");

    /**
     * public class GameMasterData : MonoBehaviour, MRZU]U]QTWX, MXWZMVNQP[R // TypeDefIndex: 128
     * 	public PokemonSettingsProto Get_PokemonSettings(int UPXWUUUOQZW) { }
     */
    ProtoCache::instance().set_GetPokemonSettingsByID((gpointer) (baseAddr + hap->gMd_gPsBido()));

    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->rH_So()), &someListener,
            nullptr);


    /**
    * public class BuddyRpcService : MonoBehaviour, IBuddyRpcService, KCIINPIAENA // TypeDefIndex: 6179
    * 	private void PWUWZZNUYST() { }
    * -> Einzige private void ohne args
    */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->bRs_iO()), &buddyRpcServiceListener,
            nullptr);


    /**
     * public class OpenGiftGuiController.Factory : InstalledPrefabFactory<OpenGiftGuiController.MWWWRRNPZOW, OpenGiftGuiController> // TypeDefIndex: 5021
     * 	public override OpenGiftGuiController Create(OpenGiftGuiController.MWWWRRNPZOW S]SPVMSZZ]Q) { }
     */
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->oGgCf_Co()),
            (gpointer) &StaticReplacements::createOgGc, nullptr);

    /**
     * public class GiftingRpcService : MonoBehaviour, IBalloonAssetManager // TypeDefIndex: 4947
     *  	public KMDIHMDKEBL<CheckSendGiftOutProto> CheckGiftingStatus(string TTTUY]YMMNQ) { }
     */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->gRs_cGSo()), &gRscGSoListener,
            nullptr);

    /**
    * public class GiftingRpcService : MonoBehaviour, IBalloonAssetManager // TypeDefIndex: 4947
    * 	public KMDIHMDKEBL<OpenGiftOutProto> OpenGift(ulong NONUWTUSURM, string UNMVXXMWTYR, string ZYSPXNMSZ[R) { }
    */
    ProtoCache::instance().setGRsOGo((gpointer) (baseAddr + hap->gRs_oGo()));



    /**
     * public sealed class OpenBuddyGiftOutProto : IMessage<OpenBuddyGiftOutProto>, IMessage, IEquatable<OpenBuddyGiftOutProto>, IDeepCloneable<OpenBuddyGiftOutProto> // TypeDefIndex: 16987
     * 	public void XZVM[XWSUMR(BuddyObservedData USM[QUXNUST) { }
     * TODO: Search by param types
         * 1 param von Typ BuddyObservedData
     */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->oBgPo_sBGo()), &openBuddyGiftOutProto,
            nullptr);

    /**
     * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
     * 	private void VZ[[XQY]TST() { }
     * 	TODO: 1 von 2, einfach alle hooken die private void sind? Wir setzen ohnehin nur die instance
     */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->pBi_gSo()), &pokemonBagListener,
            nullptr);

    /**
     * get itemBag instance
     */

    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->iBi_gIo()), &itemBagInstanceListener,
            nullptr);


}

/**
 * Methods which need to be set at an early stage
 */
void GWp::placeSecondBatch() {

    // TODO: Switch to HAP usage for the methods replaced/listened to, move others to actual usages
    ProtoCache::instance().setSendOff((gpointer) (baseAddr + hap->rH_So()));

    /**
    * public class BuddySettingsService : MonoBehaviour, IBuddySettingsService // TypeDefIndex: 6181
    * 	|-BuddySettingsService.ZVNVN[U]XRY<PlayerService.PlayerInfo>
    * -> Nope
    */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->bSSo_pIo()), &buddySettingsService,
            nullptr);



    ProtoCache::instance().set_gRp_n(hap->gRp_n());

    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->mCh_gMOcIo()), &getMapObjectsCallbackImplListener,
            nullptr
    );

    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->mCh_gMOcIo()), &getItemBagListener,
            nullptr
    );

    //interceptor.attach_listener(
    //        playerServiceGet_ItemBag, &getItemBagListener,
    //        nullptr);

    /**
     * public class Pokeball : MonoBehaviour, IPokeball, AJAJDOCLENJ // TypeDefIndex: 7747
     * 	private int U[XV]OOOUSN(CatchPokemonOutProto NYUNOT[]XPR) { }
     * 	-> Suche auf private, int return, 1 arg, arg CatchPokemonOutProto (sofern nicht obf?)
     */
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->cPOp_gCr()),
            (gpointer) &StaticReplacements::gCs, nullptr);


    /**
     * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
     * 	private bool ORPSUMSVWXW(PokestopIncidentDisplayProto SNU]NZ]UVPS, bool RQWUNQOUWOQ) { }
     */
    // disable grunts
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->mP_gIiA()),
            (gpointer) &StaticReplacements::disableGrunts, nullptr);


    /**
     * public class PokemonInventoryGuiController : LegacyGuiController, M]YU]VMOOQU, T]ZZWN]YSXZ, AJAJDOCLENJ, IGuiLayerable, IHideable, UN[TPMW]XXX // TypeDefIndex: 11743
     * 	private void OYUMNM[VZVZ(PokemonListLineItemView VSVN[U[UMXV, PokemonProto NYUNOT[]XPR, int [NYPQNXW]VS) { }
     * 	-> Suche nach Anzahl Param, private, erstes Arg PokemonListLineItemView
     */

    interceptor.attach_listener(reinterpret_cast<void *>(baseAddr + hap->pIgC_m1o()),
                                &pokemonItemListener, nullptr);


    /**
     * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
     * 	public int SMT]UOW]]OS(Item YYWVYSRSSQM) { }
     * -> TODO Einzige methode mit 1 param und return int
     */
    ProtoCache::instance().setGet_ItemCount((gpointer) (baseAddr + hap->iBi_gICo()));

    /**
     * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
     * 	public KMDIHMDKEBL<RecycleItemOutProto> V[SYUVQQWVS(Item SU]VUXMSVVO, int TNYMWZRV[UW) { }
     * -> No chance
     */
    ProtoCache::instance().setSendRecycleItem((gpointer) (baseAddr + hap->iBi_rIo()));


    /**
     * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
     * 	public KMDIHMDKEBL<UseItemEncounterOutProto> S]QYVS]UNVO(Item SU]VUXMSVVO, MMOVZWM]ZQS TMUXXOMMQ[M) { }
     */
    ProtoCache::instance().setUseItemEncounter(
            (gpointer) (baseAddr + hap->iBi_uIfEo()));


/**
 * public sealed class IncenseEncounterOutProto : IMessage<IncenseEncounterOutProto>, IMessage, IEquatable<IncenseEncounterOutProto>, IDeepCloneable<IncenseEncounterOutProto> // TypeDefIndex: 14333
 * 	public IncenseEncounterOutProto.Types.Result W]OZ[W]TOVY() { }
 * 	TODO: Suche nach public + Return type
 */
    void *incEncGetResult = (gpointer) (baseAddr + hap->iEoP_gRo());
    ProtoCache::instance().setIncEncGetResult(incEncGetResult);

/**
 * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
 * 		public PokemonProto MMPWYQVRRMS(ulong WXRQWVMRQXQ) { }
 * 		TODO: Suche per return type und 1 param typ ulong?
 */
    ProtoCache::instance().setGetPokemonProtoById((gpointer) (baseAddr + hap->pBi_gPpBIo()));

/**
 * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
 * 	public KMDIHMDKEBL<ReleasePokemonOutProto> [Y[]WP[QPSM(PokemonProto TU[VROTWNSO) { }
 */
    ProtoCache::instance().setReleasePokemon((gpointer) (baseAddr + hap->pBi_rPo()));

/**
 * public sealed class IncenseEncounterOutProto : IMessage<IncenseEncounterOutProto>, IMessage, IEquatable<IncenseEncounterOutProto>, IDeepCloneable<IncenseEncounterOutProto> // TypeDefIndex: 14333
 * 	public IncenseEncounterOutProto.Types.Result W]OZ[W]TOVY() { }
 */
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->iEoP_gRo()),
            (gpointer) &StaticReplacements::get_StatusIncense, nullptr);


    /**
     * public sealed class PokemonProto : CHMFMJDIFHD, EHOKNIMEGKI, IOOEJLHGPGP, KNHNEKJHDKB, HDBOCEGOIDG, EIFHLFELFGK, HLAGEAFIKFI, CHEEHAMOKHJ, IMessage<PokemonProto>, IMessage, IEquatable<PokemonProto>, IDeepCloneable<PokemonProto> // TypeDefIndex: 13889
     * 	public bool TNTUORQ[XTN() { }
     * -> Nope... Just create a struct for retrieving the boolean
     */

    interceptor.replace_function(
            (gpointer) (baseAddr + hap->pIMs_tSo()),
            (gpointer) &StaticReplacements::toggleSelect, nullptr);

    /**
     * public class EncounterPokemon : MonoBehaviour, IEncounterPokemon // TypeDefIndex: 7662
     * 	protected virtual float RRWYXRQQO[O() { }
     * 	-> pech gehabt, weiter suchen oder alle X protected float setzen
     */
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->eP_gAPo()),
            (gpointer) &StaticReplacements::get_attackProbability, nullptr);

    /**
     * public class EncounterPokemon : MonoBehaviour, IEncounterPokemon // TypeDefIndex: 7662
     * protected virtual float [XXPQQR[PYX() { }
     * 	-> pech gehabt, weiter suchen oder alle X protected float setzen -> Nicht möglich, siehe oben
     */
    interceptor.replace_function(
            (gpointer) (baseAddr + hap->eP_gDPo()),
            (gpointer) &StaticReplacements::get_DodgeProbability, nullptr);

    /**
     * public class EncounterCaptureState : GameState, ]RYOTVTZRMY // TypeDefIndex: 7614
     * 	private IEnumerator<_IARMeshData> WST[MPPSZ[V(CatchPokemonOutProto PTM[TOPO[PT) { }
     * 	-> Gibt es mehrere derartige -> Vermutlich pech
     */
    if (ProtoCache::instance().getFastCatchType() == FastCatchType::Quick) {
        Logger::debug("Enabling quick catch");
        interceptor.replace_function(
                (gpointer) (baseAddr + hap->eCs_rPCo()),
                (gpointer) &StaticReplacements::runPokemonCaptured, nullptr);
    }

    /**
     * public static class RQUSOPZUZYR // TypeDefIndex: 685
     * 	public static string R[ZRZTVYRPQ(string YNWSTTZYVUN, bool VX[ZRQSQ]RO) { }
     * 	-> No chance
     */
    ProtoCache::instance().setEncodedHtmlString(
            (gpointer) (baseAddr + hap->sE_hDo()));

    /**
     * public class GameMasterData : MonoBehaviour, MRZU]U]QTWX, MXWZMVNQP[R // TypeDefIndex: 128
     * 	public PokemonSettingsProto Get_PokemonSettings(PokemonProto TU[VROTWNSO) { }
     * 	There are two methods with this name and 1 param -> TODO: Search by param types
     * 	    void *encPokemonSettings = (gpointer) (baseAddrIl2cpp + hap->gMd_gPso());
     * 	    ProtoCache::instance().set_GetPokemonSettings(encPokemonSettings);
     */
    void *encPokemonSettings = (gpointer) (baseAddr + hap->gMd_gPso());
    ProtoCache::instance().set_GetPokemonSettings(encPokemonSettings);



    /**
     * public abstract class MapPokemon : MonoBehaviour, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12194
     * 	private void YYZ[XTPV[WS(object TPZX]TMUZOX, EventArgs VNMPTUPWQVQ) { }
     * private void, param mit EventArgs, 2 params
     * TODO: Search by param types
     */
    ProtoCache::instance().setMapPokemonOnTap((gpointer) (baseAddr + hap->mP_oTo()));


    /**
     * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
     * 	private void NRPX[OWYTQY(Item USM[QUXNUST) { }
     * TODO: 1 param -> Item
     */
    ProtoCache::instance().setSetBerry((gpointer) (baseAddr + hap->eIs_sABo()));

    /**
     * public class Memderp // TypeDefIndex: 12484
     * 	private void URUXWPWUQVU(HashSet<ulong> WS]RY[ZNMUQ, List<ulong> U[PXUPUR[NS, List<ulong> YQU]MSZRWZS, List<ulong> MQVWPVRWXQR) { }
     * -> No Chance
     */
    interceptor.attach_listener((gpointer) (baseAddr + hap->cM_mSFo()),
                                &memDerpListener, nullptr);

    /**
     * public class HashSet<T> : ICollection<T>, IEnumerable<T>, IEnumerable, ISet<T>, IReadOnlyCollection<T>, ISerializable, IDeserializationCallback // TypeDefIndex: 25306
     * |-RVA: 0x3FF3C84 Offset: 0x3FF3C84 VA: 0x3FF3C84
        |-HashSet<long>.Clear
     * No chance
     */
    ProtoCache::instance().setHsC((gpointer) (baseAddr + hap->hS_co()));


    /**
     * public class PoiItemSpinner : BasePoiSpinner, DGOKHLILKCN // TypeDefIndex: 12322
     * 		public override void Initialize(DNGMDBDCDIA OTTYNTMU]PM) { }
     * 		pIs_Io
     */
    interceptor.attach_listener(
            (gpointer) (baseAddr + hap->pIs_Io()), &poiItemSpinnerListener,
            nullptr);


    void *sendSearchRpc = (gpointer) (baseAddr + hap->pIs_sSRo());
    ProtoCache::instance().setSendSearchRpc(sendSearchRpc);

        /**
     * CombatDirector Listener
     */

        interceptor.attach_listener(
                (gpointer) (baseAddr + hap->cD_Io()), &combatDirectorV2,
                nullptr);

        /**
         * Skip Grunt Countdown
         */

        //
        interceptor.replace_function(
                (gpointer) (baseAddr + hap->cCg_aCo()),
                (gpointer) &StaticReplacements::combatCountdownGui, nullptr);

        /**
         * endinvasion
         */

        void *get_end_inv_sess = (gpointer) (baseAddr + hap->cD_eISo());
        ProtoCache::instance().setEndInvSess(get_end_inv_sess);

        void *get_combatEndWrapper = (gpointer) (baseAddr + hap->cS_gCeWo());
        ProtoCache::instance().setCeW(get_combatEndWrapper);

        void *get_combatGetInReadyState = (gpointer) (baseAddr + hap->cS_giRso());
        ProtoCache::instance().setgIRs(get_combatGetInReadyState);

        void *get_transitionToEndState = (gpointer) (baseAddr + hap->cS_ttEso());
        ProtoCache::instance().setttEs(get_transitionToEndState);

        void *get_complete_pokestop_dialog = (gpointer) (baseAddr + hap->cD_cPdo());
        ProtoCache::instance().setCompletePokestopDialog(get_complete_pokestop_dialog);

        interceptor.replace_function(
                (gpointer) (baseAddr + hap->cD_sInBo()),
                (gpointer) &StaticReplacements::startInvasionNpcBattle, nullptr);


    //}


    // TODO: Check whether we want to add the following...
    /**
 * Namespace: Niantic.Holoholo.Connectivity
 * class VersionCheckService
 * private bool RWTXVWOYYSO(Version PQPURNXPUUQ) { }
 * Get const Il2CppType* of System.Boolean and class Version to compare methods
 */
    /*
       Logger::debug("Placing version check hook with base addr ");
       std::vector<unsigned long> versionCheckOffsets = HAP::get_vCs_cVaPfUo();
       for (auto offset : versionCheckOffsets) {
           Logger::debug("Placing version check hook at " + std::to_string(offset));
           interceptor.replace_function(
                   (gpointer) (
                           reinterpret_cast<void *>(baseAddr + offset)),
                   (gpointer) &StaticReplacements::checkVersionPrompt, nullptr);
       }
    */
    /**
     * All field offsets
     */
    void *mapContentHandler_fetchThrottleOffset = (gpointer) (hap->mCh_sTTo());
    ProtoCache::instance().setMChSTTo(mapContentHandler_fetchThrottleOffset);

    void *spaceTimeThrottle_maxIntervalOffset = (gpointer) (hap->sTt_maISo());
    ProtoCache::instance().setSTtMaISo(spaceTimeThrottle_maxIntervalOffset);

    void *wildMonProto_getMon = (gpointer) (hap->wPp_gPo());
    ProtoCache::instance().setWildMonProto_getMon(wildMonProto_getMon);

    // TODO: Can these be set via struct?
    void *get_IndividualAttackPtr = (gpointer) (hap->pP_gIAo());
    ProtoCache::instance().setGet_IndividualAttack(get_IndividualAttackPtr);

    void *get_IndividualDefensePtr = (gpointer) (hap->pP_gIDo());
    ProtoCache::instance().setGet_IndividualDefense(get_IndividualDefensePtr);

    void *get_IndividualStaminaPtr = (gpointer) (hap->pP_gISo());
    ProtoCache::instance().setGet_IndividualStamina(get_IndividualStaminaPtr);

    void *get_isEgg = (gpointer) (hap->pP_iEo());
    ProtoCache::instance().setGet_isEgg(get_isEgg);

    void *get_CpMultiplierPtr = (gpointer) (hap->pP_gCMo());
    ProtoCache::instance().setGet_CpMultiplier(get_CpMultiplierPtr);

    void *get_AdditionalCpMultiplierPtr = (gpointer) (hap->pP_gAcMo());
    ProtoCache::instance().setGet_AdditionalCpMultiplier(get_AdditionalCpMultiplierPtr);

    void *stopGetSpinner = (gpointer) (hap->mPi_gISo());
    ProtoCache::instance().setStopGetSpin(stopGetSpinner);

    void *getShiny = (gpointer) (hap->pDp_gSo());
    ProtoCache::instance().setGetShiny(getShiny);

    void *getGender = (gpointer) (hap->pDp_gGo());
    ProtoCache::instance().setGetGender(getGender);

    void *getWeatherCondition = (gpointer) (hap->pDp_gWco());
    ProtoCache::instance().setGetWeatherCondition(getWeatherCondition);

    void *getPokemonDisplay = (gpointer) (hap->pP_gPDo());
    ProtoCache::instance().setGetPokemonDisplay(getPokemonDisplay);

    void *encOutGetPokemon = (gpointer) (hap->eOp_gPo());
    ProtoCache::instance().setEncOutGetPokemon(encOutGetPokemon);

    ProtoCache::instance().setFLpFRSo((gpointer) (hap->fLp_fRSo()));
    ProtoCache::instance().setGBdPFIo((gpointer) (hap->gBdP_fIo()));
    ProtoCache::instance().setGBdPGIo((gpointer) (hap->gBdP_gIo()));
    ProtoCache::instance().setPpMove1((gpointer) (hap->pp_m1o()));
    ProtoCache::instance().setPpMove2((gpointer) (hap->pp_m2o()));
    ProtoCache::instance().setGetWildMonProtoMonId((gpointer) (hap->wPp_gEIo()));
    ProtoCache::instance().setGetWildMonProtoPokemonProto((gpointer) (hap->wPp_gPo()));

    void *pogoProtoGetHeight = (gpointer) (hap->pP_gHo());
    ProtoCache::instance().set_getHeightMPtr(pogoProtoGetHeight);

    void *pogoProtoGetWeight = (gpointer) (hap->pP_gWo());
    ProtoCache::instance().set_getWeightMPtr(pogoProtoGetWeight);

    void *pokemonSettingPxHeight = (gpointer) (hap->pSp_gPHo());
    ProtoCache::instance().set_GetPokedexHeightM(pokemonSettingPxHeight);

    void *pokemonSettingPxWeight = (gpointer) (hap->pSp_gPWo());
    ProtoCache::instance().set_GetPokedexWeightKg(pokemonSettingPxWeight);

    void *pokemonProto_NicknameOffset = (gpointer) (hap->pP_nNo());
    ProtoCache::instance().setPpNNo(pokemonProto_NicknameOffset);

    ProtoCache::instance().set_pSpt1o((gpointer) (hap->pSp_t1o()));
    ProtoCache::instance().set_pSpt2o((gpointer) (hap->pSp_t2o()));
    ProtoCache::instance().setMemSetClear((gpointer) (hap->cM_mSCo()));
    ProtoCache::instance().set_IncenseEncouterMonField((gpointer) (hap->iEoP_gPFo()));
    ProtoCache::instance().set_DiskEncouterMonField((gpointer) (hap->dEoP_gPFo()));

}
