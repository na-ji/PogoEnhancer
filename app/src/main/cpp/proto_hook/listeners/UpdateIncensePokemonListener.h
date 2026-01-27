//
//
//

#ifndef POGODROID_UPDATEINCENSEPOKEMONLISTENER_H
#define POGODROID_UPDATEINCENSEPOKEMONLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class UpdateIncensePokemonListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;

};


#endif //POGODROID_UPDATEINCENSEPOKEMONLISTENER_H
