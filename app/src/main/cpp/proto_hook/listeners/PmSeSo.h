//
//
//

#ifndef POGOENHANCER_PMSESO_H
#define POGOENHANCER_PMSESO_H
#include "../gumpp_new/AbstractInvocationListener.h"

//pMs_eSo|PerformanceMetricsService|public void EncounterStarted()
class PmSeSo  : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);
};


#endif //POGOENHANCER_PMSESO_H
