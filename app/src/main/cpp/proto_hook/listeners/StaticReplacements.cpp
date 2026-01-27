//
//
//

#include <random>
#include "StaticReplacements.h"
#include "../Logger.h"
#include "../EncQueue.h"
#include "../gumpp_new/InvocationContext.h"
#include "../gumpp_new/InterceptorImpl.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../UnixSender.h"
#include "shared/PokemonProto.h"
#include "shared/StringHelper.h"
#include "shared/Stop.h"
#include "shared/WildMonProto.h"
#include <future>

int StaticReplacements::get_StatusIncense() {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    IncEncOutP* instance = reinterpret_cast<IncEncOutP *>(invocationContext->get_nth_argument_ptr(2));
    if (InjectionSettings::instance().isEnableAutorun()) {
        Logger::debug("Checking incense encounter autorun");

        if (instance->pokemon_ == nullptr) {
            Logger::debug("Invalid mon");
            return 2;
        }
        void* pokemonDisplay = PokemonProto::displayProto(instance->pokemon_);
        if (pokemonDisplay == nullptr) {
            Logger::debug("Invalid mon");
            return 2;
        }
        if (!PokemonProto::isToBeEncountered(instance->pokemon_, pokemonDisplay)) {
            Logger::debug("Suppressing encounter since mon is not to be encountered");
            return 2;
        } else {
            //Logger::debug("Incense encounter with: shiny " + std::to_string(isShiny) + " and IV " + std::to_string(ivPercentage));
        }
    }

    void* originalFunctionPtr = invocationContext->get_function();
    int (*originalGetStatus)(void*);
    originalGetStatus = (int (*)(void*))(originalFunctionPtr);
    int originalReturn = originalGetStatus(instance);
    return originalReturn;
}

//	protected virtual float get_AttackProbability() { } // RVA: 0x189E944 Offset: 0x189E944
float StaticReplacements::get_attackProbability() {
    if (ProtoCache::instance().getEasyCatchType() != EasyCatchType::NO_EASY_CATCH) {
        //Logger::debug("Suppressing attacks");
        return 0.0;
    } else {
        //Logger::debug("Fetching original attacks");
        Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
        auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
        void* originalFunctionPtr = invocationContext->get_function();
        void* instance = invocationContext->get_nth_argument_ptr(0);

        float (*originalGetAttackProb)(void*);
        originalGetAttackProb = (float (*)(void*))(originalFunctionPtr);
        return originalGetAttackProb(instance);
    }
}

//protected virtual float get_DodgeProbability() { } // RVA: 0x189E988 Offset: 0x189E988
float StaticReplacements::get_DodgeProbability() {
    if (ProtoCache::instance().getEasyCatchType() == EasyCatchType::IMMOBILIZED) {
        return 0.0;
    } else {
        Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
        auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
        void* originalFunctionPtr = invocationContext->get_function();
        void* instance = invocationContext->get_nth_argument_ptr(0);

        float (*originalGetDodgeProb)(void*);
        originalGetDodgeProb = (float (*)(void*))(originalFunctionPtr);
        return originalGetDodgeProb(instance);
    }
}

//void* StaticReplacements::attemptCapture(void* instance, int ballItem, float circleSize, int boolsEncoded) {
void* StaticReplacements::attemptCapture() {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
#if defined(__arm__)
    void* (*originalAttemptCapture)(void*, int, float, int);
    originalAttemptCapture = (void* (*)(void*, int, float, int))(originalFunctionPtr);
#elif defined(__aarch64__)
    void* (*originalAttemptCapture)(void*, int64_t, int);
    originalAttemptCapture = (void* (*)(void*, int64_t, int))(originalFunctionPtr);
    // float and int in the first int64_t with the order of [circle size, ball item]
#else
    return nullptr;
#endif
    void* instance = invocationContext->get_nth_argument_ptr(0);

    float originalCircleSize = 0.0F;

    Logger::debug("First arg: " + ProtoCache::convertPointerToReadableString(invocationContext->get_nth_argument_ptr(1)));
    Logger::debug("Second arg: " + ProtoCache::convertPointerToReadableString(invocationContext->get_nth_argument_ptr(2)));
    Logger::debug("Third arg: " + ProtoCache::convertPointerToReadableString(invocationContext->get_nth_argument_ptr(3)));

#if defined(__arm__)
    Logger::debug("ARM");
    int ballItem = 0;
    int boolsEncoded = 0;

    ballItem = reinterpret_cast<int> (invocationContext->get_nth_argument_ptr(1));
    //ballItem = static_cast<int>(ballItem_temp);
    void* circleSizePtr = invocationContext->get_nth_argument_ptr(2);

    boolsEncoded = reinterpret_cast<int> (invocationContext->get_nth_argument_ptr(3));

        // TODO: debug...

    Logger::debug("BallItem casted: " + std::to_string(ballItem));
    Logger::debug("Bools encoded casted: " + std::to_string(boolsEncoded));


    Logger::debug("Circle size ptr: " + ProtoCache::convertPointerToReadableString(circleSizePtr));
    std::memcpy(&originalCircleSize, &circleSizePtr, sizeof(originalCircleSize));


//    Logger::debug("Trying to forward throw with reticle size of " + std::to_string(circleSize)
//    + " original float:: " + std::to_string(convertedCirclesize));


#elif defined(__aarch64__)
    Logger::debug("ARM64");

    auto ballItem = reinterpret_cast<uint64_t> (invocationContext->get_nth_argument_ptr(1));
    uint64_t ballItemOnly = ballItem & 0xFFFFFFFF;
    Logger::debug("Item: " + std::to_string(ballItemOnly));

    auto boolsEncoded = reinterpret_cast<int64_t> (invocationContext->get_nth_argument_ptr(2));
    // TODO: cast to int to lose the higher 32bits...

    void* circleSizePtr = invocationContext->get_nth_argument_ptr(1);

    // higher or lower 32bit for third arg?
    uint64_t circleSize = 0;
    std::memcpy(&circleSize, &circleSizePtr, sizeof(circleSize));

    circleSize = circleSize >> 32;
    std::memcpy(&originalCircleSize, &circleSize, sizeof(originalCircleSize));

    uint64_t circleSizeBallItem = 0;

    Logger::debug("Original circle size: " + std::to_string(originalCircleSize));
#else
    Logger::fatal("Unknown arch");
    return nullptr;
#endif

    EnhancedThrowType throwTypeSet = ProtoCache::instance().getEnhancedThrowType();
    if (boolsEncoded == 0x10000 || throwTypeSet == EnhancedThrowType::DISABLED) {
        Logger::debug("Enhanced throw disabled or missed the mon!");
#if defined(__arm__)
        return originalAttemptCapture(instance, ballItem, originalCircleSize, boolsEncoded);
#elif defined(__aarch64__)
        std::memcpy(&circleSizeBallItem, &originalCircleSize, sizeof(originalCircleSize));

        circleSizeBallItem = (circleSizeBallItem << 32) | ballItemOnly;
        Logger::debug("circleSizeBallItem: " + std::to_string(circleSizeBallItem));

        return originalAttemptCapture(instance, circleSizeBallItem, boolsEncoded);
#endif
    }
    std::random_device rd;
    std::mt19937 mt(rd());

    if (throwTypeSet == EnhancedThrowType::EXCELLENT) {
        std::uniform_real_distribution<float> dist(0.09, 0.2);
        float randomizedCircle = dist(mt);
        Logger::debug("Enhancing throw to excellent 24/7 with circle of " + std::to_string(randomizedCircle) + " Original throw: " + std::to_string(originalCircleSize));

#if defined(__arm__)
        return originalAttemptCapture(instance, ballItem, randomizedCircle, throwBools);
#elif defined(__aarch64__)
        std::memcpy(&circleSizeBallItem, &randomizedCircle, sizeof(randomizedCircle));
        circleSizeBallItem = (circleSizeBallItem << 32) | ballItemOnly;
        Logger::debug("circleSizeBallItem: " + std::to_string(circleSizeBallItem));

        return originalAttemptCapture(instance, circleSizeBallItem, throwBools);
#endif
    } else {
        std::uniform_real_distribution<float> dist(0.1, 0.7);
        float randomizedCircle = dist(mt);
        Logger::debug("Enhancing throw to great or better with circle of " + std::to_string(randomizedCircle)
                      + "compared to throw of user: " + std::to_string(originalCircleSize));
        if (randomizedCircle > originalCircleSize) {
#if defined(__arm__)
            return originalAttemptCapture(instance, ballItem, originalCircleSize, throwBools);
#elif defined(__aarch64__)
            std::memcpy(&circleSizeBallItem, &originalCircleSize, sizeof(originalCircleSize));
            circleSizeBallItem = (circleSizeBallItem << 32) | ballItemOnly;
            Logger::debug("circleSizeBallItem: " + std::to_string(circleSizeBallItem));

            return originalAttemptCapture(instance, circleSizeBallItem, throwBools);
#endif
        }

#if defined(__arm__)
        return originalAttemptCapture(instance, ballItem, randomizedCircle, throwBools);
#elif defined(__aarch64__)
        std::memcpy(&circleSizeBallItem, &randomizedCircle, sizeof(randomizedCircle));
        circleSizeBallItem = (circleSizeBallItem << 32) | ballItemOnly;
        Logger::debug("circleSizeBallItem: " + std::to_string(circleSizeBallItem));

        return originalAttemptCapture(instance, circleSizeBallItem, throwBools);
#endif
    }

}

// Replaces Pokeball
//private int GetCaptureShakes(CatchPokemonOutProto proto) { }
int StaticReplacements::gCs() {

    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* catchPokemonOutProto = invocationContext->get_nth_argument_ptr(1);

    if(ProtoCache::instance().getFastCatchType() != FastCatchType::DISABLED_FAST_CATCH) {
        Logger::debug("Fast catch enabled");
        return 0;
    } else {
        Logger::debug("Ordinary catching");

        void* originalFunctionPtr = invocationContext->get_function();
        void* instance = invocationContext->get_nth_argument_ptr(0);
        int (*originalCaptureReason)(void*, void*);
        originalCaptureReason = (int (*)(void*, void*))(originalFunctionPtr);
        int originalReturn = originalCaptureReason(instance, catchPokemonOutProto);
        return originalReturn;
    }
}

/*
 * public class EncounterCaptureState : GameState, IEncounterCaptureState // TypeDefIndex: 6661
 * private IEnumerator`1<ISchedule> RunPokemonCaptured(CatchPokemonOutProto captureResults);
 * allows us to skip the summary foo
 * call SummaryDismissed!
 */
void* StaticReplacements::runPokemonCaptured() {
    Logger::debug("RunPokemonCaptured");
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
    void* instance = invocationContext->get_nth_argument_ptr(0);
    void* catchPokemonProto = invocationContext->get_nth_argument_ptr(1);



    Logger::debug("Calling summary dismissed");
    void (*summaryDismissed)(void*, void*);
    summaryDismissed = (void (*)(void*, void*))(ProtoCache::instance().getSummaryDismissed());
    summaryDismissed(instance, catchPokemonProto);
    return nullptr;
}

/*
 * public class EncounterInteractionState : GameState, IEncounterInteractionState, ICustomInstaller // TypeDefIndex: 6669
 * public void PokemonCaptured(); // RVA: 0x14E863C Offset: 0x14E863C
 * call RunAway to immediately leave the entire encounter having successfully captured the mon...
 */
void StaticReplacements::pokemonCaptured() {
    Logger::debug("(PokemonCaptured) Calling run away");

    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
    void* instance = invocationContext->get_nth_argument_ptr(0);

    void (*runAway)(void*);
    runAway = (void (*)(void*))(ProtoCache::instance().getRunAway());
    runAway(instance);
}

// TODO: check public class PokemonBagImpl : IPokemonBag // TypeDefIndex: 10898 whether pogo-upgrade causes pogo crashes with namereplacement
// 	private void PKCIBJEIFPC(ISpriteWidget GBFFJEIIGPE, PokemonProto NIPKBDHGHPB, bool GLGBMDJPJHF, IDictionary<int, IAssetRequest<Sprite>> EDAMHOGBKGJ) { }
void StaticReplacements::invName(void* pokemonInventoryCellViewInstance, void* spriteWidget, void* pokemonProto, bool arg2, void* dict) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    /*void* instance = invocationContext->get_nth_argument_ptr(0);
    void* textInstance = invocationContext->get_nth_argument_ptr(1);*/
    void* originalFunctionPtr = invocationContext->get_function();
    void (*originalFunction)(void *, void*, void*, bool, void*);
    originalFunction = (void (*)(void *, void*, void*, bool, void*)) (originalFunctionPtr);
    Logger::debug("InvName");
    if (pokemonInventoryCellViewInstance == nullptr || !InjectionSettings::instance().isReplaceCpWithIvPercentage()) {
        Logger::debug("Null in inventory line or IV replacement disabled, calling original");

        originalFunction(pokemonInventoryCellViewInstance, spriteWidget, pokemonProto, arg2, dict);
        return;
    }

    if (pokemonProto == nullptr) {
        originalFunction(pokemonInventoryCellViewInstance, spriteWidget, pokemonProto, arg2, dict);
        return;
    }

    Logger::debug("Constructing IV summary");
    std::string ivSummary = PokemonProto::getIvDataSummary(pokemonProto);
    Logger::debug("Setting nickname");
    PokemonProto::setNickname(pokemonProto, ivSummary);

    Logger::debug("Calling original function...");
    originalFunction(pokemonInventoryCellViewInstance, spriteWidget, pokemonProto, arg2, dict);
}

void StaticReplacements::encName() {
    // public class EncounterNameplate : MonoBehaviour // TypeDefIndex: 8966
    // RVA: 0x2139620 Offset: 0x2139620 VA: 0x2139620
        //public void SetPokemonUI(IMapPokemon mapPokemon, GameplayWeatherProto.Types.WeatherCondition weatherCondition) { }
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* instance = invocationContext->get_nth_argument_ptr(0);
    void* mapMpn = invocationContext->get_nth_argument_ptr(1);
    // now replace the text
    int nameTextOffset = 0;
    void* originalFunctionPtr = invocationContext->get_function();

#if defined(__arm__)
    auto weatherInt = reinterpret_cast<int> (invocationContext->get_nth_argument_ptr(2));
    nameTextOffset = 0x1C;

    void (*originalSetMonUi)(void *, void*, int);
    originalSetMonUi = (void (*)(void *, void*, int)) (originalFunctionPtr);
    originalSetMonUi(instance, mapMpn, weatherInt);
#elif defined(__aarch64__)
    auto weatherInt = reinterpret_cast<uint64_t> (invocationContext->get_nth_argument_ptr(2));
    nameTextOffset = 0x50;

    void (*originalSetMonUi)(void *, void*, uint64_t);
    originalSetMonUi = (void (*)(void *, void*, uint64_t)) (originalFunctionPtr);
    originalSetMonUi(instance, mapMpn, weatherInt);
#else
    return;
#endif
    Logger::debug("Got weather " + std::to_string(weatherInt));

    if (instance == nullptr || !InjectionSettings::instance().isReplaceEncounterNames()) {
        Logger::debug("Null in inventory line or encounter name-replacement disabled, calling original");
        return;
    }
    void** nameTextPtr =
            reinterpret_cast<void**>(reinterpret_cast<char*>(instance) + nameTextOffset);
    void* nameTextInstance = *nameTextPtr;

    if (nameTextInstance == nullptr) {
        Logger::debug("Null text instance");
        return;
    }

    std::string ivSummary = ProtoCache::instance().getLatestIvSummary();
    if (ivSummary.empty()) {
        Logger::debug("Empty IV summary");
        return;
    }

    System_String_o* (*getText)(void *);
    getText = (System_String_o* (*)(void *)) (ProtoCache::instance().getTextGetTextPtr());
    System_String_o* curName = getText(nameTextInstance);

    System_String_o* newName = StringHelper::createString(curName, ivSummary);

    Logger::debug("Setting text");
    void (*setText)(void *, System_String_o*);
    setText = (void (*)(void *, System_String_o*)) (ProtoCache::instance().getTextSetTextPtr());

    newName = StringHelper::encodeString(newName);

    setText(nameTextInstance, newName);

}

void StaticReplacements::sUf(void *instance, bool unlock) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());

    void* originalFunctionPtr = invocationContext->get_function();
    void (*set_UnlockedFramerate)(void *, bool);
    set_UnlockedFramerate = (void (*)(void *, bool)) (originalFunctionPtr);
    set_UnlockedFramerate(instance, true);
}

void StaticReplacements::sEa(void *instance, void *monProto, void *monProtoSnd) {
    return;
    void (*evolveAnimationComplete)(void *);
    evolveAnimationComplete = (void (*)(void *)) (ProtoCache::instance().getPDcEAc());
    evolveAnimationComplete(instance);
}

void StaticReplacements::skipCutscene(void *instance) {
    return;
}

void StaticReplacements::sPa(void *instance) {
    void (*purifyAnimationComplete)(void *);
    purifyAnimationComplete = (void (*)(void *)) (ProtoCache::instance().getPDpAc());
    purifyAnimationComplete(instance);
}

void StaticReplacements::sHa(void* instance, void* getHatchedProto) {
    return;
    void (*hatchAnimationComplete)(void *);
    hatchAnimationComplete = (void (*)(void *)) (ProtoCache::instance().getPDeHAc());
    hatchAnimationComplete(instance);
}

// 	public override OpenGiftGuiController Create(IEnumerable<GiftBoxDetailsProto> AABOFEGGPBL) { }
void* StaticReplacements::createOgGc(void* instance, void* openGiftGuiController) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    if (!ProtoCache::instance().isSpeedupGifting()) {
        // speeding up gifting is not enabled,
        auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());

        void* originalFunctionPtr = invocationContext->get_function();
        void* (*originalFunction)(void*, void*) = (void* (*)(void*, void*)) ((gpointer) (originalFunctionPtr));
        return originalFunction(instance, openGiftGuiController);
    }
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());

    int giftBoxDetailsProtoField;

#if defined(__arm__)
    giftBoxDetailsProtoField = 0x8;
#elif defined(__aarch64__)
    giftBoxDetailsProtoField = 0x10;
#else
    return;
#endif

    auto **wrappedGiftBoxDetailsProto1 = reinterpret_cast<RepeatedField **>(reinterpret_cast<char *>(openGiftGuiController) +
            giftBoxDetailsProtoField);

    RepeatedField* wrappedGiftBoxDetailsProto = *wrappedGiftBoxDetailsProto1;


    void* originalFunctionPtr = invocationContext->get_function();
    void* (*originalCreate)(void *, void*);
    originalCreate = (void* (*)(void *, void*)) (originalFunctionPtr);
    if (!ProtoCache::instance().isSpeedupGifting()) {
        return originalCreate(instance, openGiftGuiController);
    }

    void* friendsListPage = ProtoCache::instance().getFriendsListPage();
    void* friendsRpcService = ProtoCache::instance().getFriendsRpcService();
    void* giftingRpcService = ProtoCache::instance().getGiftingRpcService();

    Logger::pdebug("Unwrapping gift box at friendsListPage " + ProtoCache::convertPointerToReadableString(friendsListPage));
    Logger::pdebug("Unwrapping gift box at friendsRpcService " + ProtoCache::convertPointerToReadableString(friendsRpcService));
    Logger::pdebug("Unwrapping gift box at giftingRpcService " + ProtoCache::convertPointerToReadableString(giftingRpcService));
    Logger::pdebug("Unwrapping gift box at giftcount " + std::to_string(wrappedGiftBoxDetailsProto->___count_2));

    // The RepeatedField only holds no element
    if (wrappedGiftBoxDetailsProto == nullptr || wrappedGiftBoxDetailsProto->___count_2 == 0
        || friendsListPage == nullptr || friendsRpcService == nullptr
        || giftingRpcService == nullptr || Stop::instance().isBagFull()) {
        return originalCreate(instance, openGiftGuiController);
    }
    Logger::pdebug("Trying to open gift");
    Logger::pdebug("Unwrapping gift box at " + ProtoCache::convertPointerToReadableString(wrappedGiftBoxDetailsProto));
    void* giftBoxDetailsProto = wrappedGiftBoxDetailsProto->getItem(0);

    Logger::debug("Gift box at " + ProtoCache::convertPointerToReadableString(giftBoxDetailsProto));
    auto giftboxIdOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGBdPGIo());
    auto *giftboxId = reinterpret_cast<unsigned long *>(reinterpret_cast<char *>(giftBoxDetailsProto) +
            giftboxIdOffset);

    auto senderIdOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGBdPFIo());
    auto **senderId = reinterpret_cast<void **>(reinterpret_cast<char *>(giftBoxDetailsProto) +
            senderIdOffset);

    Logger::pdebug("Sending openGift with senderId at " + ProtoCache::convertPointerToReadableString(*senderId) + " and giftId " + std::to_string(*giftboxId));
    void* (*openGift)(void *, unsigned long, void*, void*);
    openGift = (void* (*)(void *, unsigned long, void*, void*)) (ProtoCache::instance().getGRsOGo());
    openGift(giftingRpcService, *giftboxId, *senderId, *senderId);

    void (*removeGiftbox)(void *, void*, unsigned long);
    removeGiftbox = (void (*)(void *, void*, unsigned long)) (ProtoCache::instance().getFRsRGo());
    Logger::pdebug("Removing gift");
    //removeGiftbox(friendsRpcService, *senderId, *giftboxId);

    //Logger::debug("Refreshing cell views");
    /*void* (*refreshCellView)(void *, void*);
    refreshCellView = (void* (*)(void *, void*)) (ProtoCache::instance().getFLpRCVo());
    set<void*> cellViews = ProtoCache::instance().getCellViewsOfFriendsList();

    for(void* cellView : cellViews) {
        Logger::debug("Refreshing cell view at " + ProtoCache::convertPointerToReadableString(cellView));
        refreshCellView(friendsListPage, cellView);
    }*/

    return nullptr;
}

void StaticReplacements::sBv(void *instance, bool setButtonsVisible) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());

    void* originalFunctionPtr = invocationContext->get_function();
    void (*setButtonsVisibleFunc)(void *, bool);
    setButtonsVisibleFunc = (void (*)(void *, bool)) (originalFunctionPtr);

    if (InjectionSettings::instance().isKeepEncounterUi()) {
        setButtonsVisibleFunc(instance, true);
    } else {
        setButtonsVisibleFunc(instance, setButtonsVisible);
    }
}

// 	private bool CalculateIfIncidentIsActive(PokestopIncidentDisplayProto incidentProto, bool allowCompleted) { }
bool StaticReplacements::disableGrunts(void *instance) {

        //Logger::debug("Not disabling TR");
        Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
        auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
        void* originalFunctionPtr = invocationContext->get_function();
        void* incidentProto = invocationContext->get_nth_argument_ptr(1);
        bool allowCompleted = invocationContext->get_nth_argument<bool>(2);

        bool (*originalDisableTeamRocket)(void*, void*, bool);
        originalDisableTeamRocket = (bool (*)(void*, void*, bool))(originalFunctionPtr);
        return originalDisableTeamRocket(instance, incidentProto, allowCompleted);

}

void StaticReplacements::setVisibleOnMap(void *instance, bool orgValue) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    bool returnValue = orgValue;

    void* originalFunctionPtr = invocationContext->get_function();
    void (*set_VisibleOnMap)(void *, bool);
    set_VisibleOnMap = (void (*)(void *, bool)) (originalFunctionPtr);

    int32_t (*getMonId)(void *);
    getMonId = (int32_t (*)(void *)) (ProtoCache::instance().getGetWildPokemonID());
    int32_t monId = getMonId(instance);

    void *gameMasterDataInstance = ProtoCache::instance().getGameMasterData();

    if (gameMasterDataInstance) {
        void* (*monSettingsProto)(void *, int32_t);
        monSettingsProto = (void *(*)(void *, int32_t)) (ProtoCache::instance().get_GetPokemonSettingsByID());
        void* monSetting = monSettingsProto(gameMasterDataInstance, monId);

        auto typ1Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_pSpt1o());
        auto *typ1 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(monSetting) + typ1Offset);

        auto typ2Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_pSpt2o());
        auto *typ2 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(monSetting) + typ2Offset);

        if(InjectionSettings::instance().isIncreaseVisibility()) {
            returnValue = true;
        }

        if(ProtoCache::instance().getHideMonOnMap()) {

            returnValue = (ProtoCache::instance().getWildMon(*typ1) and *typ1 > 0) or
                          (ProtoCache::instance().getWildMon(*typ2) and *typ2 > 0);

            if(ProtoCache::instance().getHideWildMon(monId)) {
                returnValue = false;
            }

        }

    }

    if (InjectionSettings::instance().isPeplusCatch()) {
        returnValue = false;
    }

    set_VisibleOnMap(instance, returnValue);

}

//private bool ACEFGGKIIBM(EncounterOutProto MHMOGPJFAAG, out bool IDMMIOBEJOF, out bool BLIFDHANKEA) { }
bool StaticReplacements::verifyProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2) {
    Logger::debug("Verify Proto called");
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();

    bool (*originalVerifyProto)(void*, void*, bool*, bool*);
    originalVerifyProto = (bool (*)(void*, void*, bool*, bool*))(originalFunctionPtr);
    bool originalReturn = originalVerifyProto(instance, EncounterOutProto, orgValue1, orgValue2);

    if (EncounterOutProto == nullptr) {
        Logger::debug("verifyProto: Invalid mon");
        return false;
    }

    auto wildPokemonProtoOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getEncOutGetPokemon());
    void **wildPokemonProtoPtr =
            reinterpret_cast<void **>(reinterpret_cast<char *>(EncounterOutProto) + wildPokemonProtoOffset);

    Logger::debug("verifyProto: Fetching wild mon");
    void* wildMon = *wildPokemonProtoPtr;
    if (wildMon == nullptr) {
        Logger::debug("verifyProto: Invalid mon");
        return false;
    }
    Logger::debug("verifyProto: Get pokemon");
    Logger::debug("verifyProto: Wildmon: " + ProtoCache::convertPointerToReadableString(wildMon));

    auto pokemonProtoOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getWildMonProto_getMon());
    void **pokemonProto =
            reinterpret_cast<void **>(reinterpret_cast<char *>(wildMon) + pokemonProtoOffset);

    void* pokemon = *pokemonProto;
    Logger::debug("verifyProto: Pokemon: " + ProtoCache::convertPointerToReadableString(pokemon));
    if (pokemon == nullptr) {
        return originalReturn;
    }
    std::string ivSummary = PokemonProto::getIvDataSummary(pokemon);
    Logger::debug("Got IV Summary " + ivSummary);
    ProtoCache::instance().setLatestIvSummary(ivSummary);


    if (!InjectionSettings::instance().isEnableAutorun() && !InjectionSettings::instance().isEnableAutoencounter()) {
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    void* pokemonDisplay = PokemonProto::displayProto(pokemon);
    unsigned long long encounterId = WildMonProto::encounterId(wildMon);
    EncQueue::instance().setWasWorthEncountering(
            encounterId,
            PokemonProto::isToBeEncountered(pokemon, pokemonDisplay));
    if (EncQueue::instance().worthEncountering(encounterId)) {
        Logger::info("verifyProto: Mon is shiny/ditto or has good iv value - open");
        if(InjectionSettings::instance().isEnableAutoencounter()) {
            void* (*openMon)(void*);
            openMon = (void* (*)(void*)) (ProtoCache::instance().getMapPokemonOnTap());
            if (openMon != nullptr) {
                // Now set worth encountering since it was encountered
                //Logger::info("Setting encounter opened");
                //EncQueue::instance().setEncounterOpened(encounterId);
                EncQueue::instance().setWasWorthEncountering(
                        encounterId,
                        false);
                openMon(instance);
            }
        }
        PokemonProto::sendIvData(pokemon);
        return true;
    }

    if (!InjectionSettings::instance().isEnableAutorun()) {
        Logger::info("verifyProto: Mon is neither shiny/ditto nor has good iv value - let it pass");
        Logger::debug("verifyProto: Done verify proto");
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    Logger::debug("verifyProto: Mon is neither shiny/ditto nor has good iv value - abort");
    Logger::debug("verifyProto: Done verify proto");

    void* (*DestroyOnMap)(void *);
    DestroyOnMap = (void *(*)(void *)) (ProtoCache::instance().getDestroy());
    DestroyOnMap(instance);

    return false;
}

//private bool DNMKEHPFLFH(IncenseEncounterOutProto MHMOGPJFAAG, out bool IDMMIOBEJOF, out bool BLIFDHANKEA) { }
bool StaticReplacements::verifyIncenseProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2) {

    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
    bool (*originalVerifyProto)(void*, void*, bool*, bool*);
    originalVerifyProto = (bool (*)(void*, void*, bool*, bool*))(originalFunctionPtr);
    bool originalReturn = originalVerifyProto(instance, EncounterOutProto, orgValue1, orgValue2);

    if (EncounterOutProto == nullptr) {
        Logger::debug("verifyProto: Invalid mon");
        return false;
    }

    Logger::debug("verifyIncenseProto: Get pokemon");
    auto pokemonProtoOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_IncenseEncouterMonField());
    void **pokemonProto =
            reinterpret_cast<void **>(reinterpret_cast<char *>(EncounterOutProto) + pokemonProtoOffset);

    void* pokemon = *pokemonProto;
    Logger::pdebug("verifyIncenseProto: Pokemon: " + ProtoCache::convertPointerToReadableString(pokemon));

    if (pokemon == nullptr) {
        return originalReturn;
    }
    std::string ivSummary = PokemonProto::getIvDataSummary(pokemon);
    Logger::debug("Got IV Summary " + ivSummary);
    ProtoCache::instance().setLatestIvSummary(ivSummary);

    if (!InjectionSettings::instance().isEnableAutorun() && !InjectionSettings::instance().isEnableAutoencounter()) {
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    void* pokemonDisplay = PokemonProto::displayProto(pokemon);

    if (PokemonProto::isToBeEncountered(pokemon, pokemonDisplay)) {
        Logger::info("verifyIncenseProto: Mon is shiny/ditto or has good iv value - open");
        if(InjectionSettings::instance().isEnableAutoencounter()) {
            void* (*openMon)(void*);
            openMon = (void* (*)(void*)) (ProtoCache::instance().getMapPokemonOnTap());
            if (openMon != nullptr) {
                openMon(instance);
            }
        }
        PokemonProto::sendIvData(pokemon);
        return true;
    }

    if (!InjectionSettings::instance().isEnableAutorun()) {
        Logger::debug("verifyIncenseProto: Mon is neither shiny/ditto nor has good iv value - let it pass");
        Logger::debug("verifyIncenseProto: Done verify proto");
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    Logger::info("verifyIncenseProto: Mon is neither shiny/ditto nor has good iv value - abort");
    Logger::debug("verifyIncenseProto: Done verify proto");

    void* (*DestroyOnMap)(void *);
    DestroyOnMap = (void *(*)(void *)) (ProtoCache::instance().getDestroy());
    DestroyOnMap(instance);

    return false;
}

//private bool PAEKHGOLLJD(DiskEncounterOutProto BNEOLPPFMPM, out bool FGBGOGECPCO, out bool BNKGIJNGFLH) { }
bool StaticReplacements::verifyDiskProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2) {

    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
    bool (*originalVerifyProto)(void*, void*, bool*, bool*);
    originalVerifyProto = (bool (*)(void*, void*, bool*, bool*))(originalFunctionPtr);
    bool originalReturn = originalVerifyProto(instance, EncounterOutProto, orgValue1, orgValue2);

    if (EncounterOutProto == nullptr) {
        Logger::debug("verifyDiskProto: Invalid mon");
        return false;
    }

    Logger::debug("verifyDiskProto: Get pokemon");
    auto pokemonProtoOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_DiskEncouterMonField());
    void **pokemonProto =
            reinterpret_cast<void **>(reinterpret_cast<char *>(EncounterOutProto) + pokemonProtoOffset);

    void* pokemon = *pokemonProto;
    Logger::debug("verifyDiskProto: Pokemon: " + ProtoCache::convertPointerToReadableString(pokemon));

    if (pokemon == nullptr) {
        Logger::debug("verifyDiskProto: Invalid Mon");
        return false;
    }

    std::string ivSummary = PokemonProto::getIvDataSummary(pokemon);
    Logger::debug("Got IV Summary " + ivSummary);
    ProtoCache::instance().setLatestIvSummary(ivSummary);

    if (!InjectionSettings::instance().isEnableAutorun() && !InjectionSettings::instance().isEnableAutoencounter()) {
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    void* pokemonDisplay = PokemonProto::displayProto(pokemon);
    if (PokemonProto::isToBeEncountered(pokemon, pokemonDisplay)) {
        Logger::debug("verifyDiskProto: Mon is shiny/ditto or has good iv value - open");
        if(InjectionSettings::instance().isEnableAutoencounter()) {
            void* (*openMon)(void*);
            openMon = (void* (*)(void*)) (ProtoCache::instance().getMapPokemonOnTap());
            if (openMon != nullptr) {
                openMon(instance);
            }
        }
        PokemonProto::sendIvData(pokemon);
        return true;
    }

    if (!InjectionSettings::instance().isEnableAutorun()) {
        Logger::info(
                "verifyDiskProto: Mon is neither shiny/ditto nor has good iv value - let it pass");
        Logger::debug("verifyDiskProto: Done verify proto");
        PokemonProto::sendIvData(pokemon);
        return originalReturn;
    }

    Logger::info("verifyDiskProto: Mon is neither shiny/ditto nor has good iv value - abort");
    Logger::debug("verifyDiskProto: Done verify proto");

    void* (*DestroyOnMap)(void *);
    DestroyOnMap = (void *(*)(void *)) (ProtoCache::instance().getDestroy());
    DestroyOnMap(instance);

    return false;
}

void StaticReplacements::selPo(void *instance, int ballValue) {
    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();
    void (*originalSelectPokeball)(void*, int);
    originalSelectPokeball = (void (*)(void*, int))(originalFunctionPtr);
    Logger::debug("Ball value: " + std::to_string(ballValue)
    + " vs stored: " + std::to_string(ProtoCache::instance().getLastBall()));
    if (ballValue > 3 || !InjectionSettings::instance().isSaveLastUsedBall()) {
        // not a "normal" encounter where pokeball, superball, hyperball are being used
        // or last ball not to be used
        Logger::debug("Noticed special ball (not saving) " + std::to_string(ballValue));
        originalSelectPokeball(instance, ballValue);
        return;
    } else if (ballValue > 1) {
        Logger::debug("Noticed ball " + std::to_string(ballValue));
        ProtoCache::instance().setLastBall(ballValue);
        originalSelectPokeball(instance, ballValue);
        return;
    } else {
        // Normal encounter, overwrite ball if needed (ballValue must be 1 or 0 basically...)
        if (ProtoCache::instance().getLastBall() > 1
        && ProtoCache::instance().getLastBall() <= 3) {
            Logger::debug("Setting ball " + std::to_string(ProtoCache::instance().getLastBall()));
            originalSelectPokeball(instance, ProtoCache::instance().getLastBall());
            return;
        }
    }
    Logger::debug("No matches for ball selection");
    originalSelectPokeball(instance, ballValue);
}

bool StaticReplacements::toggleSelect(void *instance) {


    Gum::InterceptorImpl interceptor = Gum::InterceptorImpl();
    auto invocationContext = dynamic_cast<Gum::InvocationContext *>(interceptor.get_current_invocation());
    void* originalFunctionPtr = invocationContext->get_function();

    bool (*originaltoggleSelect)(void*);
    originaltoggleSelect = (bool (*)(void*))(originalFunctionPtr);
    bool originalReturn = originaltoggleSelect(instance);

    if (InjectionSettings::instance().isMasstransfer()) {
        return 0;
    }

    return originalReturn;

}

void *StaticReplacements::combatCountdownGui(void* instance) {
    return nullptr;
}

void StaticReplacements::startInvasionNpcBattle(void* instance, void* invasionBattle) {
    Logger::debug("Start combat CompletePokestopDialog");

    void (*completeInvastionBattle)(void *);
    completeInvastionBattle = (void (*)(void *)) (ProtoCache::instance().getCompletePokestopDialog());
    completeInvastionBattle(instance);

}




