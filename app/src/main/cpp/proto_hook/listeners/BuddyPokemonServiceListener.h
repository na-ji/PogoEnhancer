#ifndef POGODROID_BUDDYPOKEMONSERVICELISTENER_H
#define POGODROID_BUDDYPOKEMONSERVICELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class BuddyPokemonServiceListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_BUDDYPOKEMONSERVICELISTENER_H
