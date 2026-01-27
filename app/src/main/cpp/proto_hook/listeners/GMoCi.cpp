//
//
//

#include <random>
#include "GMoCi.h"
#include "../EncQueue.h"
#include "../Logger.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "shared/LocProv.h"
#include <chrono>
#include "il2cppStructs.h"
#include "../geometry/s2cell.h"
#include "../geometry/s2latlngrect.h"
#include "shared/PokemonProto.h"
#include "shared/WildMonProto.h"
#include "../InfoClient.h"
#include "UpdateWildPokemonListener.h"
#include "shared/StringHelper.h"


double getDistanceInMetersGmo(LatLng currentLatLng, uint64_t cellId) {
    if (cellId == 0) {
        return 0.0;
    }
    Logger::debug("Calculating distance to " + std::to_string(cellId));
    try {
        S2Cell cell = S2Cell(S2CellId(cellId));
        S2LatLngRect bounds = cell.GetRectBound();
        S2LatLng latLng = S2LatLng::FromDegrees(currentLatLng.Latitude, currentLatLng.Longitude);

        S1Angle distanceAngle = bounds.GetDistance(latLng);

        double earthRadiusMeters = 6371010.0;

        double distanceMeters = distanceAngle.radians() * earthRadiusMeters;
        Logger::debug("[" + std::to_string(cellId) + "] Distance: "
                      + std::to_string(distanceMeters)
                      + " (level: " + std::to_string(cell.level()) + ")");

        return distanceMeters;
    } catch(...) {
        Logger::debug("Failed reading distance");
        return 0.0;
    }

}

volatile bool alreadySet = false;

void GMoCi::on_enter(Gum::AbstractInvocationContext *context) {
    Logger::debug("GMO enter");


    void *mapContentHandler = context->get_nth_argument_ptr(0);
    this->instancePtr = mapContentHandler;
    setInterval(mapContentHandler);

    //check for trash mons
    std::list checkList = ProtoCache::instance().getEncounterToCheckList();
    for(auto& MonId:checkList) {
        unsigned long long monId = MonId;
        transferIfApplicable(monId);
    }
    Il2CppUtil::Domain *domain = InfoClient::instance().getDomain();
    Logger::debug("Fetching assembly in GMO processing");
    Il2CppUtil::Assembly *assembly = domain->getAssemblyByName("holo-protos.dll");
    if (assembly == nullptr) {
        Logger::debug("Nullptr assembly, leaving");
        return;
    }
    Il2CppUtil::Image *image = assembly->getImage();

    Il2CppUtil::Class getRoutesProto = image->getClass(ProtoCache::instance().get_gRp_n());
    void *rpcHandler = ProtoCache::instance().getRpcHandler();
    void *weirdArg = ProtoCache::instance().getWeirdSecondArg();
    if (!weirdArg) {
        Logger::debug("Weird arg not set");
        return;
    }
    reqR(getRoutesProto, weirdArg, rpcHandler);

    return;
/*
    int memDerpOffset;
    int gmoCellOffset;
    int rpcHandlerOffset;
#if defined(__arm__)
    memDerpOffset = 0x78;
    gmoCellOffset = 0x8;
    rpcHandlerOffset = 0x18;
#elif defined(__aarch64__)
    memDerpOffset = 0xF0;
    gmoCellOffset = 0x10;
    rpcHandlerOffset = 0x30;
#else
    Logger::debug("Unsupported arch");
        return;
#endif



    try {
        void *getMapObjectsOutProto = context->get_nth_argument_ptr(1);
        auto **rpcHandlerPtr = reinterpret_cast<void **>(reinterpret_cast<char *>(mapContentHandler) +
                                                         rpcHandlerOffset);
        void* rpcHandler = *rpcHandlerPtr;

        ProtoCache::instance().setRpcHandler(rpcHandler);

        auto **cellRepeatedField = reinterpret_cast<RepeatedField **>(reinterpret_cast<char *>(getMapObjectsOutProto) +
                                                                      gmoCellOffset);
        LatLng currentLatLng = ProtoCache::instance().getLatLng();
        void* thirdArg = ProtoCache::instance().getWeirdArg();


        RepeatedField* cells = *cellRepeatedField;
        Il2CppUtil::Domain domain = InfoClient::instance().getDomain();
        Il2CppUtil::Assembly assembly = domain.getAssemblyByName("Assembly-CSharp.dll");
        Il2CppUtil::Image image = assembly.getImage();
        Il2CppUtil::Class encounterProtoKlazz = image.getClass("IBEDHMKFNNL");
        Il2CppUtil::Class fortSearchProtoKlazz = image.getClass("MEIFMHMCHED");
        Il2CppUtil::Class catchPokemonProtoKlazz = image.getClass("ANADMFJODDJ");
        Il2CppUtil::Class useItemCaptureProtoKlazz = image.getClass("IGEFLFADAHE");
        Il2CppUtil::Class evolvePokemonKlazz = image.getClass("JBEKIDNGMLL");


        //check for evolve mons

        std::list evolveList = ProtoCache::instance().getMonEvolveList();
        for(auto& EvolveMon:evolveList) {
            unsigned long long evolveMon = EvolveMon;
            Logger::debug("Evolve mon with proto " + std::to_string(evolveMon));
            evolvePokemon(evolveMon, rpcHandler, thirdArg, evolvePokemonKlazz);
        }

        for (int i = 0; i < cells->getCount(); i++) {
            CellP* cell = static_cast<CellP *>(cells->getItem(i));
            Logger::debug("Handling wild mons");
            RepeatedField* wildMons = cell->wildPokemon_;

            Logger::debug("GMO-CHECK: Rec Cell ID: " + std::to_string(cell->s2CellId_) + " with " + to_string(wildMons->getCount()) + " Mons");

            if (InjectionSettings::instance().isPeplusCatch()) {
                for (int j = 0; j < wildMons->getCount(); j++) {
                    void* wildMonProto = wildMons->getItem(j);
                    this->encWild(wildMonProto, currentLatLng, encounterProtoKlazz, thirdArg, rpcHandler, catchPokemonProtoKlazz,
                                  useItemCaptureProtoKlazz);
                }
            }

            if (InjectionSettings::instance().isPeplusSpin()) {
                RepeatedField* forts = cell->fort_;
                Logger::debug("Handling forts");
                for (int j = 0; j < forts->getCount(); j++) {
                    FortP* fort = static_cast<FortP *>(forts->getItem(j));
                    this->spin(fort, currentLatLng, fortSearchProtoKlazz, thirdArg, rpcHandler);
                }
            }

        }
    } catch (...) {
        Logger::debug("Failed to construct encounter proto");
    }

    void **memDerpInstancePtr = reinterpret_cast<void **>(
            reinterpret_cast<char *>(this->instancePtr) + memDerpOffset);
    void* memDerpInstance = *memDerpInstancePtr;
    //foo_sets(memDerpInstance);
    */
}

void GMoCi::setInterval(
        void *mapContentHandler) const {
    //private SpaceTimeThrottle fetchThrottle; // 0x70
    int maxIntervalOffset;
    int minIntervalOffset;
    int minDistanceOffset;
    int fourthDoubleOffset;
    int fifthDoubleOffset;
    int lastDoubleOffset;

#if defined(__arm__)
    maxIntervalOffset = 0x20;
    minIntervalOffset = 0x18;
    minDistanceOffset = 0x10;
    fourthDoubleOffset = 0x28;
    fifthDoubleOffset = 0x40;
    lastDoubleOffset = 0x48;

#elif defined(__aarch64__)
    minDistanceOffset = 0x28;
    maxIntervalOffset = 0x20;
    minIntervalOffset = 0x18;
    fourthDoubleOffset = 0x30;
    fifthDoubleOffset = 0x48;
    lastDoubleOffset = 0x50;

#else
    Logger::debug("Unsupported arch");
        return;
#endif

    void *throttleInstance = ProtoCache::instance().getThrottleInstance();
    if (throttleInstance == nullptr) {
        auto throttleOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getMChSTTo());
        void **throttleInstancePtr = reinterpret_cast<void **>(
                reinterpret_cast<char *>(mapContentHandler) + throttleOffset);
        throttleInstance = *throttleInstancePtr;
        ProtoCache::instance().setThrottleInstance(mapContentHandler);
    }
    //readLatLon(throttleInstance);

    if (!alreadySet) {
        // manipulate SpaceTimeThrottle instance's
        //	private float maxIntervalS; // 0x34
        Logger::debug("Adjusting intervals");
        auto *minDistanceM = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                        minDistanceOffset);
        Logger::debug("Min Distance: " + std::to_string(*minDistanceM));
        *minDistanceM = 0.000001;

        auto *maxIntervalS = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                        maxIntervalOffset);
        Logger::debug("Max interval: " + std::to_string(*maxIntervalS));
        *maxIntervalS = 1.000001;

        auto *minIntervalS = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                        minIntervalOffset);
        Logger::debug("Min interval: " + std::to_string(*minIntervalS));
        *minIntervalS = 0.5000001;

        auto *fourthDouble = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                        fourthDoubleOffset);

        Logger::debug("Fourth Double: " + std::to_string(*fourthDouble));
        // Setting it triggers 200ms GMOs
        //*fourthDouble = 400.1;
        //*fourthDouble = 1000.5;

        auto *fifthDouble = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                       fifthDoubleOffset);
        Logger::debug("Fifth Double: " + std::to_string(*fifthDouble));
        // somehow stacked to > 1000, then reset for a proper delay
        //*fifthDouble = 1000.2;
        //*fifthDouble = 3.0;

        auto *lastDouble = reinterpret_cast<double *>(reinterpret_cast<char *>(throttleInstance) +
                                                      lastDoubleOffset);
        Logger::debug("Last Double: " + std::to_string(*lastDouble));
        //*lastDouble = 0.0;

        //*lastDouble = 0.0;
        alreadySet = true;
    }
}

/**
 * Tries to clear sets to avoid GMOs being empty due to invalid cells being requests
 * @param memDerpInstance
 */
void GMoCi::foo_sets(void *memDerpInstance) const {
    int firstSetOffset;
    int secondSetOffset;
    int listS2Offset;
#if defined(__arm__)
    firstSetOffset = 0x8;
        secondSetOffset = 0x18;
        listS2Offset = 0x1C;
#elif defined(__aarch64__)
    firstSetOffset = 0x10;
    secondSetOffset = 0x30;
    listS2Offset = 0x38;
#else
    Logger::debug("Unsupported arch");
            return;
#endif
// Clear hashset<ulong> of memDerp
    auto setOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getMemSetClear());
    auto **setToBeClearedPtr = reinterpret_cast<HashSet_Ulong **>(reinterpret_cast<char *>(memDerpInstance) +
                                                                  setOffset);

    // TODO Fiddling
    auto **firstS2CellSetPtr = reinterpret_cast<HashSet_T **>(reinterpret_cast<char *>(memDerpInstance) +
                                                              firstSetOffset);
    auto **secondS2CellSetPtr = reinterpret_cast<HashSet_T **>(reinterpret_cast<char *>(memDerpInstance) +
                                                               secondSetOffset);
    auto **listS2CellPtr = reinterpret_cast<RepeatedField **>(reinterpret_cast<char *>(memDerpInstance) +
                                                              listS2Offset);
    HashSet_T* firstS2CellSet = *firstS2CellSetPtr;
    Logger::debug("First S2Cell Set Size: " + to_string(firstS2CellSet->getCount()));

    HashSet_T* secondS2CellSet = *secondS2CellSetPtr;
    Logger::debug("Second S2Cell Set Size: " + to_string(secondS2CellSet->getCount()));

    RepeatedField* listS2Cell = *listS2CellPtr;
    Logger::debug("List S2Cell Size: " + to_string(listS2Cell->getCount()));

    Logger::debug("Fetching set to be cleared");
    HashSet_Ulong* set = *setToBeClearedPtr;
    Logger::debug("ulong Set Size: " + to_string(set->getCount()));

    /*if (set->getCount() > firstS2CellSet->getCount()) {
        void *hashSetClearPtr = ProtoCache::instance().getHsC();
        void (*hashSetClear)(void *) = (void (*)(void *)) (hashSetClearPtr);
        Logger::debug("MemDerp: Clearing first set");
        hashSetClear(firstS2CellSet);
    }*/

    if (set->getCount() == 0) {
        void *hashSetClearPtr = ProtoCache::instance().getHsC();
        void (*hashSetClear)(void *) = (void (*)(void *)) (hashSetClearPtr);
        Logger::debug("MemDerp: Clearing set (empty set)");
        hashSetClear(set);
        return;
    }
    vector<uint64_t> plainCellIds = set->getPlain();
    LatLng currentLatLng = ProtoCache::instance().getLatLng();
    if (currentLatLng.Latitude < 1 && currentLatLng.Latitude > -1 && currentLatLng.Longitude < 1 && currentLatLng.Longitude > -1) {
        // No data yet
        return;
    }
    // Remove all items that are more than say 500m center away
    Logger::debug("Checking distances");
    unordered_set<uint64_t> toRemove;
    bool cellOnTopFound = false;
    for (uint64_t cellId : plainCellIds) {
        double distance = getDistanceInMetersGmo(currentLatLng, cellId);
        if (distance > 800) {
            Logger::debug("Distance too big of cell " + to_string(cellId) + ": " + to_string(distance));
            toRemove.insert(cellId);
        } else if (distance < 1) {
            cellOnTopFound = true;
        }
    }

    if (toRemove.size() == set->getCount() || !cellOnTopFound) {
        void *hashSetClearPtr = ProtoCache::instance().getHsC();
        void (*hashSetClear)(void *) = (void (*)(void *)) (hashSetClearPtr);
        Logger::debug("MemDerp: Clearing set (equally sized)");
        hashSetClear(set);
    } else if (!toRemove.empty()) {
        //Logger::debug("Removing all");
        /*for (uint64_t idToRemove : toRemove) {
            set->remove(idToRemove);
        }*/
        /*void *hashSetClearPtr = ProtoCache::instance().getHsC();
        void (*hashSetClear)(void *) = (void (*)(void *)) (hashSetClearPtr);
        Logger::debug("MemDerp: Clearing set (data too far away)");
        hashSetClear(this->set);*/
    }
}

void GMoCi::on_leave(Gum::AbstractInvocationContext *context) {
    Logger::debug("Done with GMO...");
}

void GMoCi::spin(FortP *fort, LatLng &currentLatLng, Il2CppUtil::Class &klazz, void* thirdArg,
                 void* rpcHandler) {
    Logger::debug("Trying to handle fort");
    if(!fort->fortId_){
        Logger::debug("Null Pointer - abort");
        return;
    }

    Logger::debug("Fort in GMO: " + StringHelper::readString(fort->fortId_));
    LatLng locationOfFort = LatLng();
    locationOfFort.Latitude = fort->latitude_;
    locationOfFort.Longitude = fort->longitude_;

    bool isClosed = fort->closed_;
    bool isEnabled = fort->enabled_;
    int fortType = fort->fortType_;
    int coolDown = fort->cooldownCompleteMs_;

    Logger::debug("fort Info: closed " + std::to_string(isClosed) + " enabled " + std::to_string(isEnabled));
    Logger::debug("Fort allow checkin " + std::to_string(fort->allowCheckin_));
    Logger::debug("Fort Type " + std::to_string(fortType) + " cooldown " + std::to_string(coolDown));

    double distanceInMeters = UpdateWildPokemonListener::calculateDistanceInMeters(currentLatLng, locationOfFort);
    Logger::debug("Distance to fort: " + std::to_string(distanceInMeters));


    void* serviceGetInstance = ProtoCache::instance().getPlayerServiceInstance();

    void* (*adTargeticngInfoInstance)(void *) = ( void* (*)(void *)) (ProtoCache::instance().getGetAdTargetingInfo());
    void* AdTargeticngInfoInstance = adTargeticngInfoInstance(serviceGetInstance);

    Logger::debug("AdTargetingInfoInstance pointer: " + ProtoCache::convertPointerToReadableString(AdTargeticngInfoInstance));
    void* sendPtr = ProtoCache::instance().getSendOff();
    void* (*sendProto)(void*, int , void*, void*) = reinterpret_cast<void* (*)(void*, int, void*, void*)>(sendPtr);
    if(!LocProv::instance().needCoolDown(currentLatLng) && !isClosed && isEnabled && fortType==1 && distanceInMeters<80) {
        FortSP* fortSearchProto = reinterpret_cast<FortSP *>(klazz.objectNew(true));
        fortSearchProto->id_ = fort->fortId_;
        fortSearchProto->fortLat = locationOfFort.Latitude;
        fortSearchProto->fortLng = locationOfFort.Longitude;
        fortSearchProto->playerLat = currentLatLng.Latitude;
        fortSearchProto->playerLng = currentLatLng.Longitude;
        fortSearchProto->adTargetInfo = AdTargeticngInfoInstance;
        fortSearchProto->geotarget = true;
        Logger::debug("Spinning fort " + StringHelper::readString(fort->fortId_));
        sendProto(rpcHandler, 101, fortSearchProto, thirdArg);
        //LocProv::instance().setLastCooldownLocation(currentLatLng);
    }
}

void GMoCi::transferIfApplicable(unsigned long long monID) {
    void *getProtoByIdPtr = ProtoCache::instance().getGetPokemonProtoById();
    void* (*getProtoById)(void *, unsigned long long) = (void * (*)(void *, unsigned long long)) (getProtoByIdPtr);
    void *monProto  = getProtoById(ProtoCache::instance().getPokemonBagService(), monID);
    Logger::debug("Transferoncatch monProto: " + ProtoCache::convertPointerToReadableString(monProto));

    if (!monProto) {
        ProtoCache::instance().addEncounterToCheckList(monID);
        return;
    }

    if (PokemonProto::transferPokemonOnCatch(monProto)) {
        void *releasePokemonPtr = ProtoCache::instance().getReleasePokemon();
        void* (*releasePokemon)(void *, void *) = (void * (*)(void *, void *)) (releasePokemonPtr);

        releasePokemon(ProtoCache::instance().getPokemonBagService(), monProto);
    }
}

void GMoCi::evolvePokemon(unsigned long long evolveMon, void* rpcHandler, void* thirdArg, Il2CppUtil::Class &klazz) {
    void* sendPtr = ProtoCache::instance().getSendOff();
    void* (*sendProto)(void*, int , void*, void*) = reinterpret_cast<void* (*)(void*, int, void*, void*)>(sendPtr);

    ePpb* evolvePokemonProto = reinterpret_cast<ePpb *>(klazz.objectNew(true));
    evolvePokemonProto->pokemonId_= evolveMon;
    evolvePokemonProto->targetPokemonId_ = 0;
    evolvePokemonProto->targetPokemonForm_ = 0;
    evolvePokemonProto->useSpecial_ = false;
    evolvePokemonProto->JCFABHAOHDF = false;

    sendProto(rpcHandler, 125, evolvePokemonProto, thirdArg);

}



void GMoCi::reqR(Il2CppUtil::Class &getRoutesProtoClass,
                  void *weirdArg, void *rpcHandler) {
    std::vector<uint64_t> cellIdsToRequestEffectively = std::vector<uint64_t>();
    std::set<uint64_t> cellIdsPreviouslyRequested = ProtoCache::instance().getCellIdsRequested();
    std::set<uint64_t> cellIds = ProtoCache::instance().popCellIds();
    for (auto cellId : cellIds) {
        if (cellIdsPreviouslyRequested.count(cellId) > 0) {
            Logger::debug("Routes of cell " + std::to_string(cellId) + " requested previously");
            continue;
        }
        Logger::debug("Requesting routes of cell " + std::to_string(cellId));
        cellIdsToRequestEffectively.push_back(cellId);
        // Rate limit the amount of requests to once per PD injection run for a cell
        ProtoCache::instance().addCellIdRequested(cellId);
    }
    if (cellIdsToRequestEffectively.empty()) {
        Logger::debug("No more cells to fetch routes of");
        return;
    }

    gRp *getRoutesProtoInstance = reinterpret_cast<gRp *>(getRoutesProtoClass.objectNew(true));

    if (getRoutesProtoInstance == nullptr) {
        Logger::debug("Failed initializing routes proto");
        return;
    }
    for (auto cellId : cellIdsToRequestEffectively) {
        getRoutesProtoInstance->cellIdsToRequest->add(cellId);
    }

    void *sendPtr = ProtoCache::instance().getSendOff();
    Logger::debug("Send addr: " + ProtoCache::convertPointerToReadableString(sendPtr));
    Logger::debug("Weird arg: " + ProtoCache::convertPointerToReadableString(weirdArg));
    Logger::debug("Constructed proto: " + ProtoCache::convertPointerToReadableString(getRoutesProtoInstance));
    //void* (*sendProto)(void*, int , void*, int, int, bool) = reinterpret_cast<void* (*)(void*, int, void*, int, int, bool)>(sendPtr);
    void *
    (*sendProto)(void *, int, void *, int, int, bool, void *) = reinterpret_cast<void *(*)(void *,
                                                                                           int,
                                                                                           void *,
                                                                                           int, int,
                                                                                           bool,
                                                                                           void *)>(sendPtr);
    sendProto(rpcHandler, 1405, getRoutesProtoInstance, 0, 0, false, weirdArg);
}

