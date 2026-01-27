//
//
//

#ifndef POGODROID_POKEMONPROTO_H
#define POGODROID_POKEMONPROTO_H

#include <string>

class PokemonProto {
public:
    static void sendIvData(void* pokemonProto);

    static std::string getIvDataSummary(void* pokemonProto);

    static double getIvPercentage(void *pokemonProto);

    static bool isShiny(void *pokemonProto);

    static int getGender(void *pokemonProto);

    static int getWeatherCondition(void *pokemonProto);

    static void* displayProto(void *pokemonProto);
    static void setNickname(void *pokemonProto, std::string &newName);
    static int32_t ivPercentage(int32_t attack, int32_t defense, int32_t stamina);
    static int32_t monId(void *pokemonProto);

    static bool isDitto(void *pokemonProto);
    static bool isToBeEncountered(void *pokemonProto, void *pokemonDisplay);
    static bool isToBeNotAutoTransferred(void *pokemonProto, void *pokemonDisplay);
    static bool monValueHelper(void *pokemonProto, void* pokemonDisplay);

    static bool transferPokemonOnCatch(void *pokemonProto);

    static std::string getPokemonName(void* pokemonProto);

    static int32_t getCandyCountForPokemonFamily(void* pokemonProto);
    static int32_t getCandyCountForEvolve(void* pokemonProto);

private:
    static int32_t attack(void *pokemonProto);
    static int32_t defence(void *pokemonProto);
    static int32_t stamina(void *pokemonProto);
    static float cpMult(void *pokemonProto);
    static float addCp(void *pokemonProto);
    static float height(void *pokemonProto);
    static float weight(void *pokemonProto);
    static bool isEgg(void *pokemonProto);
    static float dexHeight(void *monSettings);
    static float dexWeight(void *monSettings);
    static std::string move1(void *pokemonProto);
    static std::string move2(void *pokemonProto);
    static int32_t pokeballID(void *pokemonProto);
    static void* getPokemonSettingsProto(void* pokemonProto);
    static int32_t getPokemonSettingsFamilyID(void* pokemonProto);
    static int32_t getEvolutionPips(void* pokemonProto);
    static bool evolveMon(void* pokemonProto, void* pokemonSettingsProto, int baseMonId);
    static int32_t getFormOfPokemon(void *pokemonDisplay);
    static unsigned long long pokemonId(void *pokemonProto);

};


#endif //POGODROID_POKEMONPROTO_H
