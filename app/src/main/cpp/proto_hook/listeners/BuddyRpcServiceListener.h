#ifndef POGODROID_BUDDYRPCSERVICELISTENER_H
#define POGODROID_BUDDYRPCSERVICELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class BuddyRpcServiceListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_BUDDYRPCSERVICELISTENER_H
