//
//
//

#ifndef DROID_THROW_H
#define DROID_THROW_H

#include <object-internals.h>
#include "Items.h"

struct PokeballThrow : public Il2CppObject
{
public:
    // Fields
    static PokeballThrow NoThrowAttempted; // 0x0
    enum Item BallType; // 0x8
    float ReticleSize; // 0xC
    bool HitBullseye; // 0x10
    bool Spinning; // 0x11
    bool Missed; // 0x12

};


#endif //DROID_THROW_H
