#ifndef POGODROID_PLAYERINSTANCELISTENER_H
#define POGODROID_PLAYERINSTANCELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class PlayerInstanceListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_PLAYERINSTANCELISTENER_H
