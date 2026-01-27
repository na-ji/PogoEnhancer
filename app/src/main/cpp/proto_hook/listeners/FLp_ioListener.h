//
//
//

#ifndef POGOENHANCER_FLP_IOLISTENER_H
#define POGOENHANCER_FLP_IOLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

// fLp_io|FriendsListPage|public override void Initialize()
class FLp_ioListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};


#endif //POGOENHANCER_FLP_IOLISTENER_H
