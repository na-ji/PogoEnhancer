//
//
//

#ifndef POGODROIDMAPPER_RESPONSE_H
#define POGODROIDMAPPER_RESPONSE_H


#include "../gumpp_new/AbstractInvocationListener.h"
#include <mutex>

class Response : public Gum::AbstractInvocationListener {
private:
    void *actionRequestInstance;
    void *actionResponseInstance;
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

    long lasttimeboost = 0;

    bool first = true;
    int count = 0;
    int fallback = 0;
    int boostcounter = 20;

};

#endif //POGODROIDMAPPER_RESPONSE_H
