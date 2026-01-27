//
//
//

#ifndef DROID_PLUSPLUS_QUESTGETCPLISTENER_H
#define DROID_PLUSPLUS_QUESTGETCPLISTENER_H
#include "../gumpp_new/AbstractInvocationListener.h"


class QuestGetCpListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};

#endif //DROID_PLUSPLUS_QUESTGETCPLISTENER_H
