//
//
//

#include "Testlistener.h"
#include "shared/PokemonProto.h"
#include "../ProtoCache.h"
#include "../Logger.h"
#include "shared/StringHelper.h"

void Testlistener::on_enter(Gum::AbstractInvocationContext *context) {
    void* ptrPtr = context->get_nth_argument_ptr(0);
    void* username = context->get_nth_argument_ptr(1);
    void* password = context->get_nth_argument_ptr(2);



    auto *LoginAgeGateState = reinterpret_cast<void *>(reinterpret_cast<char *>(ptrPtr) +
            0xD8);



    auto *IHoloholoLoginHandler = reinterpret_cast<void *>(reinterpret_cast<char *>(LoginAgeGateState) +
            0x88);

    auto *IAuthService = reinterpret_cast<void *>(reinterpret_cast<char *>(IHoloholoLoginHandler) +
                                                  0x40);


    System_String_o *authProviderId = reinterpret_cast<System_String_o*>(context->get_nth_argument_ptr(2));

    Logger::debug("ptcToken: " + StringHelper::readString(authProviderId));
    Logger::debug("ptcToken IAuthService: " + ProtoCache::instance().convertPointerToReadableString(IAuthService));
    Logger::debug("ptcToken IHoloholoLoginHandler: " + ProtoCache::instance().convertPointerToReadableString(IHoloholoLoginHandler));


}

void Testlistener::on_leave(Gum::AbstractInvocationContext *context) {

}
