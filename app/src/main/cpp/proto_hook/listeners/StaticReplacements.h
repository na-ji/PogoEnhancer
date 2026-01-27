//
//
//

#ifndef POGODROID_STATICREPLACEMENTS_H
#define POGODROID_STATICREPLACEMENTS_H


#include "../il2cpp/il2cppStructs.h"

class StaticReplacements {
public:

    static int get_StatusIncense();

    static float get_attackProbability();

    static void *combatCountdownGui(void* instance);

    static void startInvasionNpcBattle(void* instance, void* invasionBattle);

//    void *attemptCapture(void *instance, int ballItem, float circleSize, int boolsEncoded);

    static void* attemptCapture();

    static int gCs();

    static void *runPokemonCaptured();

    static void pokemonCaptured();

    static float get_DodgeProbability();
    static void invName(void* pokemonInventoryCellViewInstance, void* spriteWidget, void* pokemonProto, bool arg2, void* dict);

    static void encName();

    static void sUf(void* instance, bool unlock); // rQs_sUFo|RenderQualityService|public void set_UnlockedFramerate(bool DIMHIILBLKA)

    static void sEa(void* instance, void* monProto, void* monProtoSnd); // public void ShowEvolveAnimation(PokemonProto BBKEPGDLBHC, PokemonProto FKEKEOCNAGH)

    static void skipCutscene(void* instance);

    static void sHa(void* instance, void* getHatchedProto); // public void ShowEggHatchAnimation(GetHatchedEggsOutProto BEJKDECBIBC)

    static void sPa(void* instance); // public void ShowPurifyAnimation()

    static void* createOgGc(void* instance, void* openGiftGuiController); // public override OpenGiftGuiController Create(IEnumerable<GiftBoxDetailsProto> AABOFEGGPBL) { }

    static void sBv(void* instance, bool setButtonsVisible); // public void set_ButtonsVisible(bool JBGJOKOFPHK) { } in EncounterGuiController

    static bool disableGrunts(void* instance);

    static void setVisibleOnMap(void* instance, bool value);

    static bool toggleSelect(void *instance);

    static bool verifyProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2);
    static bool verifyIncenseProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2);
    static bool verifyDiskProto(void *instance, void *EncounterOutProto, bool *orgValue1, bool *orgValue2);
    static void selPo(void *instance, int ballValue);
private:
    static const int throwBools = 0x101;


};

#endif //POGODROID_STATICREPLACEMENTS_H
