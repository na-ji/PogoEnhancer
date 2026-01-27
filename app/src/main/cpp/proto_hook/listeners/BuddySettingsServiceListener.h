#ifndef POGODROID_BUDDYSETTINGSSERVICELISTENER_H
#define POGODROID_BUDDYSETTINGSSERVICELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class BuddySettingsService : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_BUDDYSETTINGSSERVICELISTENER_H
