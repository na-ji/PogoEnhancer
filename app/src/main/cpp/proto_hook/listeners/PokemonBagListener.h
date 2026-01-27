#ifndef POGODROID_POKEMONBAGLISTENER_H
#define POGODROID_POKEMONBAGLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class PokemonBagListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_POKEMONBAGLISTENER_H
