//
//
//

#ifndef DROID_PLUSPLUS_ILLL_H
#define DROID_PLUSPLUS_ILLL_H


#include "../gumpp_new/AbstractInvocationListener.h"

/*
 * public PokemonProto get_Pokemon() { }
 * of multiple classes:
 * public sealed class RaidEncounterProto : IMessage<RaidEncounterProto>, IMessage, IEquatable<RaidEncounterProto>, IDeepCloneable<RaidEncounterProto> // TypeDefIndex: 11656
 * public sealed class IncenseEncounterOutProto : IMessage<IncenseEncounterOutProto>, IMessage, IEquatable<IncenseEncounterOutProto>, IDeepCloneable<IncenseEncounterOutProto> // TypeDefIndex: 11205
 * public sealed class QuestPokemonEncounterProto : IMessage<QuestPokemonEncounterProto>, IMessage, IEquatable<QuestPokemonEncounterProto>, IDeepCloneable<QuestPokemonEncounterProto> // TypeDefIndex: 11747
 * public sealed class DiskEncounterOutProto : IMessage<DiskEncounterOutProto>, IMessage, IEquatable<DiskEncounterOutProto>, IDeepCloneable<DiskEncounterOutProto> // TypeDefIndex: 11209
 *
 * public sealed class InvasionEncounterOutProto : IMessage<InvasionEncounterOutProto>, IMessage, IEquatable<InvasionEncounterOutProto>, IDeepCloneable<InvasionEncounterOutProto> // TypeDefIndex: 12290
 * public PokemonProto get_EncounterPokemon() { }
 *
 * public class PhotobombingMapPokemon : MapPokemon, IPhotobombingMapPokemon, IInitializer<GetPhotobombOutProto>, IMapPokemon // TypeDefIndex: 9443
 * public override PokemonProto get_Pokemon() { }
 *
 */
class IlLl : public Gum::AbstractInvocationListener {
public:
    void on_enter(Gum::AbstractInvocationContext *context);
    void on_leave(Gum::AbstractInvocationContext *context);

};



#endif //DROID_PLUSPLUS_ILLL_H
