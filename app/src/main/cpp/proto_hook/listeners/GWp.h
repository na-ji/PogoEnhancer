//
//
//

#ifndef POGODROIDMAPPER_GWP_H
#define POGODROIDMAPPER_GWP_H


#include <string>
#include "../gumpp_new/AbstractInvocationListener.h"
#include "../gumpp_new/InterceptorImpl.h"
#include "Response.h"
#include "FormPossible.h"
#include "UpdateDiskPokemonListener.h"
#include "GetItemBagListener.h"
#include "UpdateIncensePokemonListener.h"
#include "PokemonItemListener.h"
#include "QuestGetCpListener.h"
#include "RaidGetCpListener.h"
#include "IncidentGetCpListener.h"
#include "DailyGetCpListener.h"
#include "PmSeSo.h"
#include "GMoCi.h"
#include "FLp_ioListener.h"
#include "GRscGSoListener.h"
#include "BreakoutFromPokeball.h"
#include "ePsListener.h"
#include "CombatDirector.h"
#include "MemDerp.h"
#include "BuddyPokemonServiceListener.h"
#include "BuddyRpcServiceListener.h"
#include "BuddySettingsServiceListener.h"
#include "OpenBuddyGiftOutProto.h"
#include "PokemonBagListener.h"
#include "PoiItemSpinnerListener.h"
#include "UpdateWildPokemonListener.h"
#include "UpdateStop.h"
#include "../HAP/HAP64.h"
#include "ItemBagInstanceListener.h"
#include "PlayerServiceInstance.h"
#include "CombatDirectorV2Listener.h"

//public sealed class ClientMapCellProto : IMessage<ClientMapCellProto>, IMessage, IEquatable<ClientMapCellProto>, IDeepCloneable<ClientMapCellProto> // TypeDefIndex: 11286
//  RVA: 0x2C6FC18 Offset: 0x2C6FC18 VA: 0x2C6FC18
//	public RepeatedField<WildPokemonProto> get_WildPokemon() { }
class GWp  : public Gum::AbstractInvocationListener{
private:
    static constexpr const int& PUBLIC_BITMASK = std::integral_constant<int, 0x10000>::value;
    static constexpr const int& PRIVATE_BITMASK = std::integral_constant<int, 0x01111>::value;

    std::unique_ptr<AbstractHAP> hap;

    Gum::InterceptorImpl interceptor;
    GumAddress baseAddr = 0;
    bool setupDone = false;
    Response responseListener;
    FormPossible formPossible;
    UpdateDiskPokemonListener updateDiskPokemonListener;
    GetItemBagListener getItemBagListener;
    UpdateIncensePokemonListener updateIncensePokemonListener;
    PokemonItemListener pokemonItemListener;
    QuestGetCpListener questGetCpListener;
    RaidGetCpListener raidGetCpListener;
    IncidentGetCpListener incidentGetCpListener;
    DailyGetCpListener dailyGetCpListener;
    PmSeSo performanceMetricsService_EncounterStartedListener;
    GMoCi getMapObjectsCallbackImplListener;
    FLp_ioListener fLpIoListener;
    GRscGSoListener gRscGSoListener;
    CombatDirector combatListener;
    MemDerp memDerpListener;
    BreakoutFromPokeball breakoutFromPokeball;
    BuddyPokemonServiceListener buddyPokemonServiceListener;
    BuddyRpcServiceListener buddyRpcServiceListener;
    BuddySettingsService buddySettingsService;
    OpenBuddyGiftOutProto openBuddyGiftOutProto;
    PokemonBagListener pokemonBagListener;
    ePsListener someListener;
    PoiItemSpinnerListener poiItemSpinnerListener;
    UpdateWildPokemonListener updateWildPokemonListener;
    UpdateStop updateStopListener;
    ItemBagInstanceListener itemBagInstanceListener;
    PlayerInstanceListener playerInstanceListener;
    CombatDirectorV2 combatDirectorV2;
public:
    GWp() {
        responseListener = Response();
        formPossible = FormPossible();
        updateDiskPokemonListener = UpdateDiskPokemonListener();
        getItemBagListener = GetItemBagListener();
        updateIncensePokemonListener = UpdateIncensePokemonListener();
        pokemonItemListener = PokemonItemListener();
        questGetCpListener = QuestGetCpListener();
        raidGetCpListener = RaidGetCpListener();
        incidentGetCpListener = IncidentGetCpListener();
        dailyGetCpListener = DailyGetCpListener();
        performanceMetricsService_EncounterStartedListener = PmSeSo();
        getMapObjectsCallbackImplListener = GMoCi();
        fLpIoListener = FLp_ioListener();
        gRscGSoListener = GRscGSoListener();
        combatListener = CombatDirector();
        memDerpListener = MemDerp();
        breakoutFromPokeball = BreakoutFromPokeball();
        buddyPokemonServiceListener = BuddyPokemonServiceListener();
        buddyRpcServiceListener = BuddyRpcServiceListener();
        buddySettingsService = BuddySettingsService();
        openBuddyGiftOutProto = OpenBuddyGiftOutProto();
        pokemonBagListener = PokemonBagListener();
        someListener = ePsListener();
        poiItemSpinnerListener = PoiItemSpinnerListener();
        updateWildPokemonListener = UpdateWildPokemonListener();
        updateStopListener = UpdateStop();
        itemBagInstanceListener = ItemBagInstanceListener();
        playerInstanceListener = PlayerInstanceListener();
        combatDirectorV2 = CombatDirectorV2();

#if defined(__arm__)
        Logger::fatal("ARM is not supported anymore!");
        exit(1);
#elif defined(__aarch64__)
        Logger::fatal("ARM64");
#else
        Logger::fatal("Unknown arch");
    return;
#endif
        std::unique_ptr<AbstractHAP> ptr(new HAP64());
        hap = std::move(ptr);
    }
    bool initTriggered = false;
    void thirdBatch();
    void setBaseAddr(GumAddress baseAddrVal);
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;

    void placeFirstBatch();
    void placeSecondBatch();

    void setInterceptor(const Gum::InterceptorImpl &interceptorVal);
    bool isSetup() {
        return this->setupDone;
    }
};


#endif //POGODROIDMAPPER_GWP_H
