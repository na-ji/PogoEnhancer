//
//
//

#ifndef POGOENHANCER_GMOCI_H
#define POGOENHANCER_GMOCI_H

#include "../gumpp_new/AbstractInvocationListener.h"
#include "../Util.h"
#include "../il2cpp_util/Il2CppClass.hpp"


//public class MapContentHandler : MonoBehaviour, ICellManager // TypeDefIndex: 9307
// RVA: 0x1CC72DC Offset: 0x1CC72DC VA: 0x1CC72DC
//	private void GetMapObjectsCallbackImpl(GetMapObjectsProto request, GetMapObjectsOutProto result) { }
class GMoCi : public Gum::AbstractInvocationListener{
public:
    void on_enter(Gum::AbstractInvocationContext *context) override  __attribute((__annotate__(("fw_prob=100"))));

    void on_leave(Gum::AbstractInvocationContext *context) override  __attribute((__annotate__(("fw_prob=100"))));
private:
    void* instancePtr = nullptr;


    bool first = true;
    int count = 0;
    int fallback = 0;
    int boostcounter = 20;


    void foo_sets(void *memDerpInstance) const;
    void encWild(void* wildMonProto, LatLng &currentLatLng, Il2CppUtil::Class &klazz,
                 void* thirdArg, void* rpcHandler, Il2CppUtil::Class &send,
                 Il2CppUtil::Class &item) const;

    void setInterval(void *mapContentHandler) const;

    void spin(FortP *pP, LatLng &currentLatLng, Il2CppUtil::Class &klazz, void* thirdArg,
              void* rpcHandler);

    static void transferIfApplicable(unsigned long long int monID);
    static void evolvePokemon(unsigned long long evolveMon, void* rpcHandler, void* thirdArg, Il2CppUtil::Class &klazz);
    static void reqR(Il2CppUtil::Class &getRoutesProtoClass, void *weirdArg, void *rpcHandler);
};

#endif //POGOENHANCER_GMOCI_H
