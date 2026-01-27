//
//
//

#ifndef DROID_PLUSPLUS_STRINGHELPER_H
#define DROID_PLUSPLUS_STRINGHELPER_H

#include <string>
#include <map>
#include "../../il2cpp/il2cppStructs.h"

class StringHelper {
public:
    static std::string readString(System_String_o* il2cppString);
    static std::string readStringOld(System_String_o_old* il2cppString);
    static System_String_o_old* convertString(System_String_o_old* original, std::string toBeConverted);
    static System_String_o* createString(System_String_o* original, std::string &val);
    static System_String_o_old *createStringOld(string &val);
    static std::string convertNumberToHtml(int number);
    static System_String_o* encodeString(System_String_o* original);
    static std::string convertNumberToSupHtml(std::string number);
    static std::string convertXsXltoUnicode(std::string value);
    static std::string convertAttackToUnicode(std::string attackName);
    static std::string convertBigSmall(int value);

private:
    static inline std::map<std::string, System_String_o_old* > alreadyAllocatedNames = std::map<std::string, System_String_o_old*>();
    static inline std::map<System_String_o_old*, System_String_o_old* > alreadyConvertedAllocatedNames = std::map<System_String_o_old*, System_String_o_old*>();
    static inline std::map<std::string, const char*> moveIconMapping =
            {
                    { "Bug", "&#2410;" },
                    { "Dark", "&#9680;" },
                    { "Electric", "&#5418;" },
                    { "Dragon", "&#2713;" },
                    { "Fairy", "&#43603;" },
                    { "Fighting", "&#20189;" },
                    { "Fire", "&#2444;" },
                    { "Flying", "&#2468;" },
                    { "Ghost", "&#2415;" },
                    { "Grass", "&#8749;" },
                    { "Ground", "&#9178;" },
                    { "Ice", "&#10048;" },
                    { "Normal", "&#10687;" },
                    { "Poison", "&#2550;" },
                    { "Psychic", "&#2413;" },
                    { "Rock", "&#6671;" },
                    { "Steel", "&#9678;" },
                    { "Water", "&#12316;" },
                    { "unknown", "" }
            };
    static inline std::map<std::string, const char*> xlxsUnicodeMapping =
            {
                    { "XL", "&#739;&#737;" },
                    { "XS", "&#739;&#738;" }
            };

};


#endif //DROID_PLUSPLUS_STRINGHELPER_H
