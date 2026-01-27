//
//
//

#include "CombatDirector.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "../InjectionSettings.h"

static void* combatDirectorInstance = nullptr;

void CombatDirector::on_enter(Gum::AbstractInvocationContext *context) {

    combatDirectorInstance = context->get_nth_argument_ptr(0);

}

void CombatDirector::on_leave(Gum::AbstractInvocationContext *context) {

    if(!InjectionSettings::instance().isDisableGrunts()) {
        return;
    }

    //private void EndInvasionSession(CombatProto.Types.CombatState endState, CombatPlayerFinishState finishState, int remainingPokemon) { }
    //var combatStateRelatedFunction = new NativeFunction(baseAddr.add("0x2BD0D44"),
    //'void', ['pointer', 'int', 'int', 'int']);
    int combatTypeOffset = 0;
    void *endInvasionSessionPtr = ProtoCache::instance().getEndInvSess();
    void *(*endInvasionSession)(void *, int, int, int) = (void *(*)(void *, int, int, int)) (endInvasionSessionPtr);

#if defined(__arm__)
    combatTypeOffset = 0xC4;
#elif defined(__aarch64__)
    combatTypeOffset = 0x140;
#else
    return;
#endif

    int* combatTypePtr =
            reinterpret_cast<int*>(reinterpret_cast<char*>(combatDirectorInstance) + combatTypeOffset);
    int combatType = *combatTypePtr;

    Logger::debug("Read Combat Type" + std::to_string(combatType));

    if (combatType == 5) {
        endInvasionSession(combatDirectorInstance, 6, 0, 3);
    }




}
