//
//
//

#include "PokemonItemListener.h"
#include "../Logger.h"
#include "../UnixSender.h"
#include "../ProtoCache.h"
#include "shared/PokemonProto.h"

/*
 * We want to listen to
 *	private void <Display>m__1(PokemonListLineItemView view, PokemonProto proto, int index); // 0x7FC1EC
 * The 2nd arg is the PokemonProto of the mon selected and to be displayed in detail
 * found in:
 * public class PokemonInventoryGuiController : GuiController, IPokemonInventoryGuiController, IGuiController // TypeDefIndex: 9463
 */
void PokemonItemListener::on_enter(Gum::AbstractInvocationContext *context) {
    Logger::debug("MonInventoryGui being called...");
    void *pokemonProto = context->get_nth_argument_ptr(2);
    if(pokemonProto != nullptr) {
        PokemonProto::sendIvData(pokemonProto);
    }
}

void PokemonItemListener::on_leave(Gum::AbstractInvocationContext *context) {
    Logger::debug("Leaving MonInventoryGui being called...");

}
