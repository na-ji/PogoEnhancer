//
//
//

#ifndef POGOENHANCER_GRSCGSOLISTENER_H
#define POGOENHANCER_GRSCGSOLISTENER_H

#include "../gumpp_new/AbstractInvocationListener.h"

//gRs_cGSo|GiftingRpcService|public IPromise<CheckSendGiftOutProto> CheckGiftingStatus(string PCKOLDJHDBJ)
class GRscGSoListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};


#endif //POGOENHANCER_GRSCGSOLISTENER_H
