//
//
//

#include "PokemonProto.h"
#include "../../ProtoCache.h"
#include "../../Logger.h"
#include "../../UnixSender.h"
#include "StringHelper.h"
#include <string>
#include <list>
#include <fstream>
#include "../InjectionSettings.h"
#include "../../il2cpp_util/Il2CppDomain.hpp"
#include "../../InfoClient.h"
//#include "../../json.hpp"
//using namespace nlohmann;

void PokemonProto::sendIvData(void *pokemonProto) {
    bool is_Egg = isEgg(pokemonProto);
    if (is_Egg) {
        return;
    }

    float mon_height = height(pokemonProto);
    float mon_weight = weight(pokemonProto);
    float cp_multiplier = cpMult(pokemonProto);
    float additional_cp_multiplier = addCp(pokemonProto);
    int individual_attack = attack(pokemonProto);
    int individual_defense = defence(pokemonProto);
    int individual_stamina = stamina(pokemonProto);
    //Logger::debug("cp_multiplier:" + to_string(cp_multiplier));
    //Logger::debug("additional_cp_multiplier:" + to_string(additional_cp_multiplier));

    void *gameMasterDataInstance = ProtoCache::instance().getGameMasterData();
    int32_t monLvl = 0;
    string weightXSXL;
    string heightXSXL;

    //Logger::debug("Gamemaster instance: " + ProtoCache::convertPointerToReadableString(gameMasterDataInstance));
    if (gameMasterDataInstance) {
#if defined(__arm__)
        int (*getMonLevel)(void *, float);
        getMonLevel = (int (*)(void *, float))(ProtoCache::instance().getGetMonLvl());
        monLvl = getMonLevel(gameMasterDataInstance, cp_multiplier + additional_cp_multiplier);

#elif defined(__aarch64__)
        int (*getMonLevel)(float, void *);
        getMonLevel = (int (*)(float, void *)) (ProtoCache::instance().getGetMonLvl());
        monLvl = getMonLevel(cp_multiplier + additional_cp_multiplier, gameMasterDataInstance);
#else
        return;
#endif
    }

    if (gameMasterDataInstance && InjectionSettings::instance().isShowXLXS()) {
        void *(*getMonSett)(void *, void *);
        getMonSett = (void *(*)(void *, void *)) (ProtoCache::instance().get_GetPokemonSettings());
        void *monSett = getMonSett(gameMasterDataInstance, pokemonProto);

        float monStdHeight = dexHeight(monSett);
        float monStdWeight = dexWeight(monSett);

        //Logger::debug("StdWeight:" + to_string(monStdWeight));
        //Logger::debug("StdWHeight:" + to_string(monStdHeight));

        //Logger::debug("Weight:" + to_string(mon_weight));
        //Logger::debug("Height:" + to_string(mon_height));


        float monHeightDiv = monStdHeight / 8;
        float monWeightDiv = monStdWeight / 8;

        if (mon_height > (monStdHeight + (2 * monHeightDiv))) {
            heightXSXL = "H XL";
        }
        if (mon_height < (monStdHeight - (2 * monHeightDiv))) {
            heightXSXL = "H XS";
        }

        if (mon_weight > (monStdWeight + (2 * monWeightDiv))) {
            weightXSXL = "W XL ";
        }
        if (mon_weight < (monStdWeight - (2 * monWeightDiv))) {
            weightXSXL = "W XS ";
        }
    }

    void *pokemonDisplay = displayProto(pokemonProto);

    // typ 0=egg / 1=mon
    int typ = 1;
    bool is_Shiny = isShiny(pokemonDisplay);
    int gender = getGender(pokemonDisplay);
    int weather = getWeatherCondition(pokemonDisplay) + 1;
    bool ditto = isDitto(pokemonProto);


    std::vector<string> statsToDisplay = std::vector<string>();
    statsToDisplay.push_back(std::to_string(individual_attack));
    statsToDisplay.push_back(std::to_string(individual_defense));
    statsToDisplay.push_back(std::to_string(individual_stamina));
    statsToDisplay.push_back(std::to_string(cp_multiplier));
    statsToDisplay.push_back(std::to_string(additional_cp_multiplier));
    statsToDisplay.push_back(std::to_string(monLvl));
    statsToDisplay.push_back(std::to_string(is_Shiny));
    statsToDisplay.push_back(std::to_string(typ));
    statsToDisplay.push_back(std::to_string(gender));
    statsToDisplay.push_back(std::to_string(weather));
    statsToDisplay.push_back(heightXSXL);
    statsToDisplay.push_back(weightXSXL);
    statsToDisplay.push_back(std::to_string(ditto));

    std::string toBeSent;

    for (const std::string &value : statsToDisplay) {
        toBeSent.append(value);
        toBeSent.append(",");
    }
    toBeSent = toBeSent.substr(0, toBeSent.size() - 1);

    Logger::debug("Sending IV data: " + toBeSent);
    UnixSender::sendMessage(MESSAGE_TYPE::POKEMON_IV, toBeSent,
                            ProtoCache::instance().getSymmKey());
}

double PokemonProto::getIvPercentage(void *pokemonProto) {
    int individual_attack = attack(pokemonProto);
    int individual_defense = defence(pokemonProto);
    int individual_stamina = stamina(pokemonProto);

    double ivPercentage =
            100.0 * (individual_attack + individual_defense + individual_stamina) / 45.0;
    return ivPercentage;
}

bool PokemonProto::isDitto(void *pokemonProto) {
    std::set<int> potential_dittos {92, 96, 216, 223, 316, 322, 434, 557, 590};

    bool valid_atk = attack(pokemonProto) < 4;
    bool valid_def = defence(pokemonProto) < 4;
    bool valid_sta = stamina(pokemonProto) < 4;
    int monId = PokemonProto::monId(pokemonProto);
    Logger::debug("isDitto: MonId: " + to_string(monId));
    float cp_multiplier = cpMult(pokemonProto);
    bool valid_boost_attrs = valid_atk or valid_def or valid_sta or cp_multiplier < .3;
    Logger::debug("isDitto: cp_multiplier: " + to_string(cp_multiplier));
    Logger::debug("isDitto: valid_boost_attrs: " + to_string(valid_boost_attrs));

    void *pokemonDisplay = displayProto(pokemonProto);
    int weather = getWeatherCondition(pokemonDisplay);
    Logger::debug("isDitto: weather: " + to_string(weather));

    if(potential_dittos.find(monId) != potential_dittos.end()){
    //if(std::find(potential_dittos.begin(), potential_dittos.end(), monId) == potential_dittos.end()) {
        Logger::debug("isDitto: monId is potential ditto");
        if (weather > 0 && valid_boost_attrs) {
            Logger::debug("isDitto: ditto found (weather + value)");
            return true;
        } else if (weather == 0 && cp_multiplier > 0.733) {
            Logger::debug("isDitto: ditto found (cp_multiplier)");
            return true;
        }
    }
    return false;
}

bool PokemonProto::isShiny(void *pokemonDisplay) {
    auto isShinyOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetShiny());
    Logger::debug("Reading isShiny of " + ProtoCache::convertPointerToReadableString(pokemonDisplay)
                  + " at offset " + std::to_string(isShinyOffset));
    auto *isShiny = reinterpret_cast<bool *>(reinterpret_cast<char *>(pokemonDisplay) + isShinyOffset);
    Logger::debug("Reading isShiny of " + ProtoCache::convertPointerToReadableString(pokemonDisplay)
                  + " at offset " + std::to_string(isShinyOffset) + " at " +
                  ProtoCache::convertPointerToReadableString(isShiny)
                  + " resulted in " + std::to_string(*isShiny));
    return *isShiny;
}

int PokemonProto::getGender(void *pokemonDisplay) {
    auto genderOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetGender());
    auto *gender = reinterpret_cast<int *>(reinterpret_cast<char *>(pokemonDisplay) + genderOffset);
    return *gender;
}

int PokemonProto::getWeatherCondition(void *pokemonDisplay) {
    auto weatherConditionOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetWeatherCondition());
    auto *weatherCondition = reinterpret_cast<int *>(reinterpret_cast<char *>(pokemonDisplay) + weatherConditionOffset);
    return *weatherCondition;
}

std::string PokemonProto::getIvDataSummary(void *pokemonProto) {
    vector<string> gender_array{"", "&#9794;", "&#9792;", "&#9892;"};

    float mon_height = height(pokemonProto);
    float mon_weight = weight(pokemonProto);
    float cp_multiplier = cpMult(pokemonProto);
    float additional_cp_multiplier = addCp(pokemonProto);
    int individual_attack = attack(pokemonProto);
    int individual_defense = defence(pokemonProto);
    int individual_stamina = stamina(pokemonProto);

    bool is_Egg = isEgg(pokemonProto);
    if (is_Egg) {
        return "Egg";
    }

    Logger::debug("Fetching display proto");
    void *pokemonDisplay = displayProto(pokemonProto);
    Logger::debug("Reading gender");
    int gender = getGender(pokemonDisplay);
    Logger::debug("Fetching game master data instance");
    void *gameMasterDataInstance = ProtoCache::instance().getGameMasterData();
    int32_t monLvl = -1;
    string weightXSXL;
    string heightXSXL;


    //Logger::debug("Gamemaster instance: " + ProtoCache::convertPointerToReadableString(gameMasterDataInstance));
    if (gameMasterDataInstance) {
#if defined(__arm__)
        int (*getMonLevel)(void *, float);
        getMonLevel = (int (*)(void *, float))(ProtoCache::instance().getGetMonLvl());
        monLvl = getMonLevel(gameMasterDataInstance, cp_multiplier + additional_cp_multiplier);
#elif defined(__aarch64__)
        int (*getMonLevel)(float, void *);
        getMonLevel = (int (*)(float, void *)) (ProtoCache::instance().getGetMonLvl());
        monLvl = getMonLevel(cp_multiplier + additional_cp_multiplier, gameMasterDataInstance);
#else
        return;
#endif
    }

    if (gameMasterDataInstance) {
        Logger::debug("HeightWeight values enabled");
        void *(*getMonSett)(void *, void *);
        getMonSett = (void *(*)(void *, void *)) (ProtoCache::instance().get_GetPokemonSettings());

        Logger::debug("Fetching mon settings of " + ProtoCache::convertPointerToReadableString(gameMasterDataInstance));
        void *monSett = getMonSett(gameMasterDataInstance, pokemonProto);

        Logger::debug("Reading std height of " + ProtoCache::convertPointerToReadableString(monSett));
        float monStdHeight = dexHeight(monSett);
        Logger::debug("Reading std weight");
        float monStdWeight = dexWeight(monSett);

        float monHeightDiv = monStdHeight / 8;
        float monWeightDiv = monStdWeight / 8;

        Logger::debug("Evaluating height and weight...");

        if (mon_height > (monStdHeight + (2 * monHeightDiv))) {
            heightXSXL = StringHelper::convertXsXltoUnicode("XL");
        }
        if (mon_height < (monStdHeight - (2 * monHeightDiv))) {
            heightXSXL = StringHelper::convertXsXltoUnicode("XS");
        }

        if (mon_weight > (monStdWeight + (2 * monWeightDiv))) {
            weightXSXL = StringHelper::convertXsXltoUnicode("XL");
        }
        if (mon_weight < (monStdWeight - (2 * monWeightDiv))) {
            weightXSXL = StringHelper::convertXsXltoUnicode("XS");
        }
    }

    std::string ivSummary;

    if (ProtoCache::instance().getNameReplaceSettings("monName")) {
        std::string monName = getPokemonName(pokemonProto);
        ivSummary.append( monName + "&#10;");
    }

    if (ProtoCache::instance().getNameReplaceSettings("IvPercentage")) {
        double ivPercentage = PokemonProto::getIvPercentage(pokemonProto);
        ivSummary.append(std::to_string((int) ivPercentage) + "");
    }

    if (ProtoCache::instance().getNameReplaceSettings("IvValues")) {
        ivSummary.append(" " + StringHelper::convertNumberToHtml(individual_attack)
                         +  StringHelper::convertNumberToHtml(individual_defense)
                         +  StringHelper::convertNumberToHtml(individual_stamina));
    }

    if (monLvl != -1 && ProtoCache::instance().getNameReplaceSettings("Lvl")) {
        ivSummary.append("" + StringHelper::convertNumberToSupHtml(to_string(monLvl)) + "");
    }

    if (ProtoCache::instance().getNameReplaceSettings("Gender")) {
        ivSummary.append(gender_array[gender]);
    }

    if ((!heightXSXL.empty() or !weightXSXL.empty()) && ProtoCache::instance().getNameReplaceSettings("XsXl")) {
        ivSummary.append(weightXSXL + heightXSXL);
    }

    if (ProtoCache::instance().getNameReplaceSettings("Moveset")) {
        std::string move1 = StringHelper::convertAttackToUnicode(PokemonProto::move1(pokemonProto));
        std::string move2 = StringHelper::convertAttackToUnicode(PokemonProto::move2(pokemonProto));
        ivSummary.append(" " + (move1) + (move2));
    }

    if (ProtoCache::instance().getNameReplaceSettings("Specialmon") && gameMasterDataInstance) {
        int monId = PokemonProto::monId(pokemonProto);
        if (monId == 19 && mon_weight < 2.40625) {
            ivSummary.append(StringHelper::convertBigSmall(2));
        }
        if (monId == 129 && mon_weight > 13.125) {
            ivSummary.append(StringHelper::convertBigSmall(1));
        }
    }

    if(isDitto(pokemonProto)) {
        ivSummary.append(" - DITTO");
    }

    //ivSummary.append("&#10;" + nickName);

    Logger::debug("Returning ivSummary: " + ivSummary);


    return ivSummary;
}

int32_t PokemonProto::attack(void *pokemonProto) {
    // TODO: adjust protocache...
    auto attackOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_IndividualAttack());
    auto *attack = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
                                                  attackOffset);
    return *attack;
}

int32_t PokemonProto::defence(void *pokemonProto) {
    // TODO: adjust protocache...
    auto defenceOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_IndividualDefense());
    auto *defence = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
                                                defenceOffset);
    return *defence;
}

int32_t PokemonProto::stamina(void *pokemonProto) {
    // TODO: adjust protocache...
    auto staminaOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_IndividualStamina());
    auto *stamina = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
                                                staminaOffset);
    return *stamina;
}

float PokemonProto::cpMult(void *pokemonProto) {
    // TODO: adjust protocache...
    auto cpMultOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_CpMultiplier());
    auto *cpMult = reinterpret_cast<float *>(reinterpret_cast<char *>(pokemonProto) + cpMultOffset);
    Logger::debug("Reading cpMult of " + ProtoCache::convertPointerToReadableString(pokemonProto)
                  + " at offset " + std::to_string(cpMultOffset) + " at " +
                  ProtoCache::convertPointerToReadableString(cpMult)
                  + " resulted in " + std::to_string(*cpMult));
    return *cpMult;
}

float PokemonProto::addCp(void *pokemonProto) {
    // TODO: adjust protocache...
    auto additionalCpOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_AdditionalCpMultiplier());
    auto *additionalCp = reinterpret_cast<float *>(reinterpret_cast<char *>(pokemonProto) +
                                                   additionalCpOffset);
    return *additionalCp;
}

float PokemonProto::height(void *pokemonProto) {
    // TODO: adjust protocache...
    auto heightOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_getHeightMPtr());
    auto *height = reinterpret_cast<float *>(reinterpret_cast<char *>(pokemonProto) + heightOffset);
    Logger::debug("Reading height at offset " + std::to_string(heightOffset) + " resulted in " +
                  std::to_string(*height));
    return *height;
}

float PokemonProto::weight(void *pokemonProto) {
    // TODO: adjust protocache...
    auto weightOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_getWeightMPtr());
    auto *weight = reinterpret_cast<float *>(reinterpret_cast<char *>(pokemonProto) + weightOffset);
    return *weight;
}

bool PokemonProto::isEgg(void *pokemonProto) {
    // TODO: adjust protocache...
    auto isEggOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGet_isEgg());
    auto *isEgg = reinterpret_cast<bool *>(reinterpret_cast<char *>(pokemonProto) + isEggOffset);
    return *isEgg;
}

float PokemonProto::dexHeight(void *monSettings) {
    auto pokedexHeightOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_GetPokedexHeightM());
    auto *pokedexHeight = reinterpret_cast<float *>(reinterpret_cast<char *>(monSettings) +
                                                    pokedexHeightOffset);

    return *pokedexHeight;
}

float PokemonProto::dexWeight(void *monSettings) {
    auto pokedexWeightOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().get_GetPokedexWeightKg());
    auto *pokedexWeight = reinterpret_cast<float *>(reinterpret_cast<char *>(monSettings) +
                                                    pokedexWeightOffset);

    return *pokedexWeight;
}

unsigned long long PokemonProto::pokemonId(void *pokemonProto) {
    auto *pokedexWeight = reinterpret_cast<unsigned long long *>(reinterpret_cast<char *>(pokemonProto) +
            0x30);

    return *pokedexWeight;
}

std::string PokemonProto::move1(void *pokemonProto) {
    auto move1Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().getPpMove1());
    auto *move1 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
            move1Offset);

    std::string moveType = ProtoCache::instance().getMonMoves(to_string(*move1));
    return moveType;
}

std::string PokemonProto::move2(void *pokemonProto) {
    auto move2Offset = reinterpret_cast<unsigned long>(ProtoCache::instance().getPpMove2());
    auto *move2 = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
            move2Offset);

    std::string moveType = ProtoCache::instance().getMonMoves(to_string(*move2));
    return moveType;
}

int32_t PokemonProto::pokeballID(void *pokemonProto) {

    int pokeballIDOffset;

    #if defined(__arm__)
    pokeballIDOffset = 0x70;
#elif defined(__aarch64__)
    pokeballIDOffset = 0x90;
#else
    return;
#endif

    auto *pokeballID = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
            pokeballIDOffset);
    return *pokeballID;
}

int32_t PokemonProto::monId(void *pokemonProto) {
    int monIdOffset;

#if defined(__arm__)
    monIdOffset = 0x20;
#elif defined(__aarch64__)
    monIdOffset = 0x38;
#else
    return;
#endif
    auto *monId = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonProto) +
            monIdOffset);
    return *monId;
}

void *PokemonProto::displayProto(void *pokemonProto) {
    auto displayProtoOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getGetPokemonDisplay());
    void **displayProto =
            reinterpret_cast<void **>(reinterpret_cast<char *>(pokemonProto) + displayProtoOffset);
    Logger::debug("Reading displayProto of " + ProtoCache::convertPointerToReadableString(pokemonProto)
                  + " at offset " + std::to_string(displayProtoOffset) + " at " +
                  ProtoCache::convertPointerToReadableString(displayProto)
                  + " resulted in " + ProtoCache::convertPointerToReadableString(*displayProto));
    return *displayProto;
}

void PokemonProto::setNickname(void *pokemonProto, std::string &newName) {
    auto nicknameOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getPpNNo());
    auto **nicknamePtr =
            reinterpret_cast<System_String_o **>(reinterpret_cast<char *>(pokemonProto) + nicknameOffset);
    System_String_o* curName = *nicknamePtr;

    System_String_o* newNameSystemString = StringHelper::createString(curName, newName);

    newNameSystemString = StringHelper::encodeString(newNameSystemString);

    *nicknamePtr = newNameSystemString;
}

int32_t PokemonProto::getFormOfPokemon(void *pokemonDisplay) {
    //void* pokemonSettingsProto = PokemonProto::getPokemonSettingsProto(pokemonProto);
    auto *formId = reinterpret_cast<int *>(reinterpret_cast<char *>(pokemonDisplay) +
            0x1C);
    return *formId;
}

bool PokemonProto::monValueHelper(void *pokemonProto, void* pokemonDisplay) {
    Logger::debug("Checking if shiny");
    bool isShiny = PokemonProto::isShiny(pokemonDisplay);
    bool isDitto = PokemonProto::isDitto(pokemonProto);
    if (isShiny || isDitto) {
        return true;
    }
    Logger::debug("Checking percentage...");
    double ivPercentage = PokemonProto::getIvPercentage(pokemonProto);
    int autorunMinIv = InjectionSettings::instance().getAutorunMinIv();
    Logger::debug("IV of mon %: " + std::to_string(ivPercentage)
                  + " vs autorunIV " + std::to_string(autorunMinIv));
    if (autorunMinIv == 102 && (ivPercentage < 1 || ivPercentage > 99)) {
        // Only encounter if IV == 0 or 100 (%) OR shiny
        return true;
    } else if (ivPercentage >= autorunMinIv) {
        return true;
    }

    return false;
}

bool PokemonProto::isToBeEncountered(void *pokemonProto, void* pokemonDisplay) {
    int monId = PokemonProto::monId(pokemonProto);
    if (ProtoCache::instance().getNotAutorunMon(monId)) {
        return true;
    }
    return monValueHelper(pokemonProto, pokemonDisplay);
}

bool PokemonProto::isToBeNotAutoTransferred(void *pokemonProto, void* pokemonDisplay) {
    int monId = PokemonProto::monId(pokemonProto);
    if (!ProtoCache::instance().getNotAutotransferMonIds(monId)) {
        return true;
    }
    return monValueHelper(pokemonProto, pokemonDisplay);
}
/*
bool PokemonProto::HasFormAndCostumeCaptured(int monID, void *pokemonDisplay) {

    if (ProtoCache::instance().getPlayerServiceInstance() == nullptr) {
        Logger::debug("getPlayerServiceInstance is null");
        return false;
    }

    void* serviceGetInstancePtr = ProtoCache::instance().getPlayerServiceInstance();
    void* (*serviceGetInstance)() = (void* (*)()) serviceGetInstancePtr;
    void* serviceInstance = serviceGetInstance();

    void* (*getPokedex)(void *) = (void* (*)(void*)) (ProtoCache::instance().getPokedexService());
    // serviceGetInstance ist ne Methode, kein pointer
    void* pokedex = getPokedex(serviceInstance);

    Logger::debug("Using Pokedex " + ProtoCache::convertPointerToReadableString(pokedex));

    void *hasCapturedPtr = ProtoCache::instance().getHasFormAndCostumeCaptured();
    bool (*hasCaptured)(void *, int, void *) = (bool (*)(void *, int, void *)) (hasCapturedPtr);
    Logger::fatal("pokedex info: " + std::to_string(hasCaptured(pokedex, monID, pokemonDisplay)));

    return hasCaptured(pokedex, monID, pokemonDisplay);

}
*/

bool PokemonProto::transferPokemonOnCatch(void *pokemonProto) {

    void *pokemonDisplay = PokemonProto::displayProto(pokemonProto);

    int monId = PokemonProto::monId(pokemonProto);
    int ballId = PokemonProto::pokeballID(pokemonProto);

    return !(PokemonProto::isToBeNotAutoTransferred(pokemonProto, pokemonDisplay) || ballId > 3
             || !ProtoCache::instance().getNotAutotransferMonIds(monId) || monId == 132);

}

std::string PokemonProto::getPokemonName(void* pokemonProto) {

    int monId = PokemonProto::monId(pokemonProto);

    void* getII18nInstancePtr = ProtoCache::instance().getII18nInstance();
    void* (*getII18nInstance)() = (void* (*)()) getII18nInstancePtr;
    void* i18nServiceInstance = getII18nInstance();

    void *getNamePtr = ProtoCache::instance().getGetPokemonName();
    System_String_o* (*getName)(void *, int) = ( System_String_o* (*)(void *, int)) (getNamePtr);
    System_String_o* SytemMonNname = getName(i18nServiceInstance, monId);
    std::string monName = StringHelper::readString(SytemMonNname);

    return monName;

}

void* PokemonProto::getPokemonSettingsProto(void* pokemonProto) {
    void *gameMasterDataInstance = ProtoCache::instance().getGameMasterData();
    void* (*monSettingsProto)(void *, int32_t);
    monSettingsProto = (void *(*)(void *, int32_t)) (ProtoCache::instance().get_GetPokemonSettingsByID());
    int monId = PokemonProto::monId(pokemonProto);
    void* monSetting = monSettingsProto(gameMasterDataInstance, monId);
    return monSetting;
}

int PokemonProto::getPokemonSettingsFamilyID(void* pokemonProto) {
    void* pokemonSettingsProto = PokemonProto::getPokemonSettingsProto(pokemonProto);
    auto *familyId = reinterpret_cast<int *>(reinterpret_cast<char *>(pokemonSettingsProto) +
            0x78);
    return *familyId;
}

int32_t PokemonProto::getCandyCountForPokemonFamily(void* pokemonProto) {

    /*
    void* serviceGetInstancePtr = ProtoCache::instance().getPlayerServiceInstance();
    void* (*serviceGetInstanceInstance)() = (void* (*)()) serviceGetInstancePtr;
    void* ServiceGetInstanceInstance = serviceGetInstanceInstance();
*/

    void* serviceGetInstance = ProtoCache::instance().getPlayerServiceInstance();


    void *getCandyCountPtr = ProtoCache::instance().getGetCandyCountForPokemonFamily();
    int (*getName)(void *, int) = ( int (*)(void *, int)) (getCandyCountPtr);
    int candy = getName(serviceGetInstance, getPokemonSettingsFamilyID(pokemonProto));

    return candy;
}

int32_t PokemonProto::getEvolutionPips(void* pokemonProto) {
    void* pokemonSettingsProto = PokemonProto::getPokemonSettingsProto(pokemonProto);
    auto *candyCount = reinterpret_cast<int32_t *>(reinterpret_cast<char *>(pokemonSettingsProto) +
            0x58);
    return *candyCount;
}

bool PokemonProto::evolveMon(void* pokemonProto, void* pokemonSettingsProto, int candyFamily) {
    return false;
    auto **evoRepeatedField = reinterpret_cast<RepeatedField **>(reinterpret_cast<char *>(pokemonSettingsProto) +
            0x90);

    RepeatedField* evos = *evoRepeatedField;
    if (evos->getCount() == 0 || evos->getCount() > 1) {
        return false;
    }

    ePbb* evo = static_cast<ePbb *>(evos->getItem(0));
    int monID = evo->evolution_;
    int candyCost_ = evo->candyCost_;
    int lureItemRequirement_ = evo->lureItemRequirement_;
    int evolutionItemRequirement_ = evo->evolutionItemRequirement_;
    bool mustBeBuddy_ = evo->mustBeBuddy_;
    bool onlyDaytime_ = evo->onlyDaytime_;
    bool onlyNighttime_ = evo->onlyNighttime_;

    Logger::debug("monID " + std::to_string(monID));
    Logger::debug("candyCost_ " + std::to_string(candyCost_));
    Logger::debug("lureItemRequirement_ " + std::to_string(lureItemRequirement_));
    Logger::debug("evolutionItemRequirement_ " + std::to_string(evolutionItemRequirement_));
    Logger::debug("mustBeBuddy_ " + std::to_string(mustBeBuddy_));
    Logger::debug("onlyDaytime_ " + std::to_string(onlyDaytime_));
    Logger::debug("onlyNighttime_ " + std::to_string(onlyNighttime_));

    if(!mustBeBuddy_ && lureItemRequirement_ == 0 && candyFamily >= candyCost_ && candyCost_ <= 50 &&
            evolutionItemRequirement_ == 0 && !onlyDaytime_ && !onlyNighttime_) {
        ProtoCache::instance().addMonToEvolveList(PokemonProto::pokemonId(pokemonProto));
        Logger::debug("Added mon to evolve List");
        return true;
    }

    return false;

}