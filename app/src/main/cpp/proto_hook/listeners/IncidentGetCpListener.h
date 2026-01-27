//
//
//

#ifndef DROID_PLUSPLUS_INCIDENTGETCPLISTENER_H
#define DROID_PLUSPLUS_INCIDENTGETCPLISTENER_H


#include "../gumpp_new/AbstractInvocationListener.h"

class IncidentGetCpListener : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};


#endif //DROID_PLUSPLUS_INCIDENTGETCPLISTENER_H
