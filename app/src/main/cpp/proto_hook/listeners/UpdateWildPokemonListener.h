//
//
//

#ifndef POGODROID_UPDATEWILDPOKEMONLISTENER_H
#define POGODROID_UPDATEWILDPOKEMONLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"
#include "../Util.h"

class UpdateWildPokemonListener : public Gum::AbstractInvocationListener {
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;



public:
    static double calculateDistanceInMeters(LatLng from, LatLng to);
    static double deg2rad(double deg);
    static void addOpenedWildEncounterId(unsigned long long encounterId);
    static bool checkOpenedWildEncounterId(unsigned long long encounterId);

};


#endif //POGODROID_UPDATEWILDPOKEMONLISTENER_H
