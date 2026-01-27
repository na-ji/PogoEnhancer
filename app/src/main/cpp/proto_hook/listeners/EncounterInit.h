//
//
//

#ifndef DROID_PLUSPLUS_ENCOUNTERINIT_H
#define DROID_PLUSPLUS_ENCOUNTERINIT_H


#include "../gumpp_new/AbstractInvocationListener.h"

class EncounterInit : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

private:
    char* encounterState = nullptr;
};


#endif //DROID_PLUSPLUS_ENCOUNTERINIT_H
