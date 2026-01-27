#ifndef POGODROID_BREAKOUTFROMPOKEBALL_H
#define POGODROID_BREAKOUTFROMPOKEBALL_H


#include "../gumpp_new/AbstractInvocationListener.h"

class BreakoutFromPokeball : public Gum::AbstractInvocationListener {
private:
    void* encounterInstance = nullptr;
public:
    void on_enter(Gum::AbstractInvocationContext *context) override;

    void on_leave(Gum::AbstractInvocationContext *context) override;
};


#endif //POGODROID_BREAKOUTFROMPOKEBALL_H
