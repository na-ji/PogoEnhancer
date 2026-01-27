//
//
//

#ifndef POGODROID_EPSLISTENER_H
#define POGODROID_EPSLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class ePsListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};

#endif //POGODROID_EPSLISTENER_H
