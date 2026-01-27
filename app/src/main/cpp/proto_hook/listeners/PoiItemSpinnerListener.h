//
//
//

#ifndef POGODROID_POIITEMSPINNERLISTENER_H
#define POGODROID_POIITEMSPINNERLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class PoiItemSpinnerListener : public Gum::AbstractInvocationListener{
public:
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_POIITEMSPINNERLISTENER_H
