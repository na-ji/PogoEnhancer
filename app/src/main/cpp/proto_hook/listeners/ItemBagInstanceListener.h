//
//
//

#ifndef DROID_PLUSPLUS_ITEMBAGINSTANCELISTENER_H
#define DROID_PLUSPLUS_ITEMBAGINSTANCELISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class ItemBagInstanceListener : public Gum::AbstractInvocationListener{
public:
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;

};


#endif //DROID_PLUSPLUS_ITEMBAGINSTANCELISTENER_H
