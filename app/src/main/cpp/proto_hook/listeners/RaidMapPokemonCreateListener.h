//
//
//

#ifndef POGODROID_RAIDMAPPOKEMONCREATELISTENER_H
#define POGODROID_RAIDMAPPOKEMONCREATELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

////public override RaidMapPokemon Create(RaidEncounterProto proto); // 0x7CD2D0
class RaidMapPokemon_CreateListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);
};


#endif //POGODROID_RAIDMAPPOKEMONCREATELISTENER_H
