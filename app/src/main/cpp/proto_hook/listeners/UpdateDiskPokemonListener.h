//
//
//

#ifndef POGODROID_DISKWILDPOKEMONLISTENER_H
#define POGODROID_DISKWILDPOKEMONLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class UpdateDiskPokemonListener : public Gum::AbstractInvocationListener {
private:
    void* monPtr = nullptr;

public:
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;

};


#endif //POGODROID_DISKWILDPOKEMONLISTENER_H
