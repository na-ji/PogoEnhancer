//
//
//

#ifndef POGOENHANCER_COMBATDIRECTOR_H
#define POGOENHANCER_COMBATDIRECTOR_H
#include "../gumpp_new/AbstractInvocationListener.h"


class CombatDirector  : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);
};


#endif //POGOENHANCER_COMBATDIRECTOR_H
