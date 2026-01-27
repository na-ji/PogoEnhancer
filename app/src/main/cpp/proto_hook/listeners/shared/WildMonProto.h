//
//
//

#ifndef POGODROID_WILDMONPROTO_H
#define POGODROID_WILDMONPROTO_H


#include <il2cppStructs.h>
#include "../../ProtoCache.h"

class WildMonProto {
public:
    static void* getMonProto(void *wildMonProto);
    static unsigned long long encounterId(void *wildMonProto);
    static int32_t monId(void *wildMonProto);
    static System_String_o* getSpawnpointId(void *wildMonProto);
    static LatLng getLatLng(void *wildMonProto);
};


#endif //POGODROID_WILDMONPROTO_H
