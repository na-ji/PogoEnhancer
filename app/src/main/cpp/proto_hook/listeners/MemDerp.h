//
//
//

#ifndef POGOENHANCER_MEMDERP_H
#define POGOENHANCER_MEMDERP_H


#include "../gumpp_new/AbstractInvocationListener.h"
#include "StaticReplacements.h"

class MemDerp : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

private:
    HashSet_Ulong* set;
    LatLng lastLocation;
    long lastBoost = 0;
};

#endif //POGOENHANCER_MEMDERP_H
