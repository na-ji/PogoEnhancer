#ifndef POGODROID_GETITEMBAGLISTENER_H
#define POGODROID_GETITEMBAGLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class GetItemBagListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_GETITEMBAGLISTENER_H
