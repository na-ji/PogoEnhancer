//
//
//

#include "CombatDirectorV2Listener.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../InjectionSettings.h"

static void* combatStateV2 = nullptr;
static void* combatChallengeProto = nullptr;
static void* combatEndStateInstance = nullptr;
int32_t combatType = 0;
static void* incidentService = 0;

void CombatDirectorV2::on_enter(Gum::AbstractInvocationContext *context) {
    void* combatStateV2 = context->get_nth_argument_ptr(0);
    void* quests = context->get_nth_argument_ptr(1);

    if(!InjectionSettings::instance().isDisableGrunts()) {
        return;
    }


    //void* stateOfCombat = context->get_nth_argument_ptr(1);
    int stateOfCombat = static_cast<int>(reinterpret_cast<std::uintptr_t>(context->get_nth_argument_ptr(1)));

    int combatTypeOffset = 0;
    int incidentServiceOffset = 0;
    unsigned long combatEndStateOffset = 0;

#if defined(__arm__)
    combatTypeOffset = 0xE4;
#elif defined(__aarch64__)
    combatTypeOffset = 0xE8;
    incidentServiceOffset = 0x98;
    combatEndStateOffset = 0x90;
#else
    return;
#endif

    if (stateOfCombat==3) {
        Logger::debug("Read combatDirectorInstance: " + ProtoCache::instance().convertPointerToReadableString(combatStateV2));
        Logger::debug("Read combatstate: " + std::to_string(stateOfCombat));

        auto *combatTypePtr = reinterpret_cast<int *>(reinterpret_cast<char *>(combatStateV2) +
                combatTypeOffset);
        combatType = *combatTypePtr;

        auto *incidentService = reinterpret_cast<void *>(reinterpret_cast<char *>(combatStateV2) +
                incidentServiceOffset);
        Logger::debug("Read combat incidentService: " + ProtoCache::instance().convertPointerToReadableString(incidentService));
        Logger::debug("Read combat Type: " + std::to_string(combatType));
        if (combatType==5) {

            void *combatReadyStatePtr = ProtoCache::instance().getgIRs();
            bool (*combatReadyState)(void *) = (bool (*)(void *)) (combatReadyStatePtr);
            bool getcombatReadyState = combatReadyState(combatStateV2);

            Logger::debug("Read combatDirectorInstance (getcombatReadyState): " + std::to_string(getcombatReadyState));
            if (getcombatReadyState) {
                void* (*combatEndStateIndstance)(void*);
                combatEndStateIndstance = (void* (*)(void*)) (ProtoCache::instance().getCeW());

                auto *endStateIPtr = reinterpret_cast<void **>(reinterpret_cast<char *>(combatStateV2) +
                                                              combatEndStateOffset);
                void* endState = *endStateIPtr;

                auto *incidentServiceIPtr = reinterpret_cast<void **>(reinterpret_cast<char *>(combatStateV2) +
                                                               incidentServiceOffset);

                void* incidentService = *incidentServiceIPtr;
                 Logger::debug("Read combatDirectorInstance (combatEndStateIndstanceV2): " + ProtoCache::instance().convertPointerToReadableString(endState));
                 if(endState) {
                     void *endInvasionSessionPtr = ProtoCache::instance().getEndInvSess();
                     void *(*endInvasionSession)(void *, int, int, int) = (void *(*)(void *, int, int, int)) (endInvasionSessionPtr);
                     Logger::debug("end combat session");
                     void *transitionToEndStatePtr = ProtoCache::instance().getttEs();
                     void *(*transitionToEndState)(void *) = (void *(*)(void *)) (transitionToEndStatePtr);

                     transitionToEndState(combatStateV2);
                     endInvasionSession(endState, 6, 0, 3);
                     combatEndStateIndstance(incidentService);
                 }
                Logger::debug("combat done");
            }
        }
    }
}

void CombatDirectorV2::on_leave(Gum::AbstractInvocationContext *context) {
    void* retVal = context->get_return_value_ptr();
}
