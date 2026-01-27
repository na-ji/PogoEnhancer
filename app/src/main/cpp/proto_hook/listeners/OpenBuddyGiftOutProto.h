#ifndef POGODROID_OPENBUDDYGIFTOUTPROTOLISTENER_H
#define POGODROID_OPENBUDDYGIFTOUTPROTOLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class OpenBuddyGiftOutProto : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_OPENBUDDYGIFTOUTPROTOLISTENER_H
