//
//
//

#ifndef DROID_PLUSPLUS_STOPPROXIMITY_H
#define DROID_PLUSPLUS_STOPPROXIMITY_H

#include "../gumpp_new/AbstractInvocationListener.h"

class StopProximity : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;

private:
    void* currentStop = nullptr;
};


#endif //DROID_PLUSPLUS_STOPPROXIMITY_H
