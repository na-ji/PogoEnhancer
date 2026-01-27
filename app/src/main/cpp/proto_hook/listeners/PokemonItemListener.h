//
//
//

#ifndef POGODROID_POKEMONITEMLISTENER_H
#define POGODROID_POKEMONITEMLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class PokemonItemListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};


#endif //POGODROID_POKEMONITEMLISTENER_H
