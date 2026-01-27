//
//
//

#include "StringHelper.h"
#include "../../ProtoCache.h"
#include "../../Logger.h"
#include "../../il2cpp_util/Il2CppDomain.hpp"
#include "../../InfoClient.h"


std::string StringHelper::readString(System_String_o *il2cppString) {
    std::string decoded = "";
    Logger::debug("Reading string of size " + std::to_string(il2cppString->length));
    char16_t charOfString;
    for (int i = 0; i < il2cppString->length; i++) {
        charOfString = il2cppString->chars[i];
        //Logger::debug(std::to_string(charOfString));
        decoded.push_back(static_cast<char>(charOfString));
        if (charOfString == '\0') {
            break;
        }
    }

    return decoded;
}

std::string StringHelper::readStringOld(System_String_o_old *il2cppString) {
    std::string decoded = "";
    Logger::debug("Reading string of size " + std::to_string(il2cppString->length));
    char16_t charOfString;
    for (int i = 0; i < il2cppString->length; i++) {
        charOfString = il2cppString->chars[i];
        //Logger::debug(std::to_string(charOfString));
        decoded.push_back(static_cast<char>(charOfString));
        if (charOfString == '\0') {
            break;
        }
    }

    return decoded;
}

System_String_o_old* StringHelper::convertString(System_String_o_old* original, std::string toBeConverted) {
/*
    if (StringHelper::alreadyAllocatedNames.find(toBeConverted) != StringHelper::alreadyAllocatedNames.end()) {
        // we have previously created that string...
        Logger::debug("Already constructed string, reusing...");
        return StringHelper::alreadyAllocatedNames.at(toBeConverted);
    }

    Il2CppUtil::Domain* domain = InfoClient::instance().getDomain();
    Il2CppUtil::Assembly* assembly = domain->getAssemblyByName("mscorlib.dll");
    Il2CppUtil::Image* image = assembly->getImage();
    Il2CppUtil::Class stringKlazz = image->getClass("String");
    auto* systemString = reinterpret_cast<System_String_o_old *>(stringKlazz.objectNew(true));
 */
    auto* converted = new System_String_o_old();

    Logger::debug("Constructing string");
    converted->length = static_cast<int32_t>(toBeConverted.size());
    for(size_t i = 0, iMax = toBeConverted.size(); i <= iMax; ++i)
    {
        converted->chars[i] = static_cast<char16_t >(
                static_cast<unsigned char>(toBeConverted[i])
        );
        // Logger::debug(std::to_string(converted->chars[i]));
    }
    converted->chars[toBeConverted.size()] = '\0';
    converted->klass = original->klass;
    converted->monitor = original->monitor;

    // TODO: Only replace pointer here and modify length?
    //original->chars = converted->chars;
    //original->length = static_cast<int32_t>(toBeConverted.size());
    StringHelper::alreadyAllocatedNames.emplace(toBeConverted, converted);
    return converted;
}


System_String_o_old* StringHelper::createStringOld(std::string &val) {
    Il2CppUtil::Domain* domain = InfoClient::instance().getDomain();
    // TODO: Before constructing a new instance of String, first check if the val to be set fits into the already allocated string!
    Il2CppUtil::Assembly* assembly = domain->getAssemblyByName("mscorlib.dll");
    Il2CppUtil::Image* image = assembly->getImage();
    Il2CppUtil::Class stringKlazz = image->getClass("String");
    auto* systemString = reinterpret_cast<System_String_o_old *>(stringKlazz.objectNew(true));
    auto* systemStringToUse = new System_String_o_old();
    //return StringHelper::convertString(systemString, val);
    /*void* stringCtorPtr = ProtoCache::instance().getStringCtor();
    void (*stringCtor)(void*, char*) = reinterpret_cast<void (*)(void*, char*)>(stringCtorPtr);*/
    //auto *cstr = new char16_t[val.length() + 1];
    for(size_t i = 0, iMax = val.size(); i <= iMax; ++i)
    {
        systemStringToUse->chars[i] = static_cast<char16_t >(
                static_cast<unsigned char>(val[i])
        );
        // Logger::debug(std::to_string(converted->chars[i]));
    }
    systemStringToUse->chars[val.size()] = '\0';
    systemStringToUse->length = static_cast<int32_t>(val.size());
    systemStringToUse->klass = systemString->klass;
    systemStringToUse->monitor = systemString->monitor;
    //strcpy(systemString->chars, val.c_str());
    //stringCtor(systemString, cstr);
    return systemStringToUse;
}

System_String_o *StringHelper::createString(System_String_o* original, std::string &val) {
    if (original->length >= val.size()) {
        Logger::debug("String fits into existing allocated one");
        // The value to be set fits into the string, replace the value
        for(size_t i = 0, iMax = val.size(); i <= iMax; ++i)
        {
            original->chars[i] = static_cast<char16_t >(
                    static_cast<unsigned char>(val[i])
            );
            // Logger::debug(std::to_string(converted->chars[i]));
        }
        original->chars[val.size()] = '\0';
        original->length = static_cast<int32_t>(val.size());
        return original;
    } else {
        Logger::debug("Need to construct string due to size issues");
        /*
         * Il2CppUtil::Domain* domain = InfoClient::instance().getDomain();
        Il2CppUtil::Assembly* assembly = domain->getAssemblyByName("mscorlib.dll");
        if (assembly == nullptr) {
            return nullptr;
        }
        Il2CppUtil::Image* image = assembly->getImage();
        Il2CppUtil::Class stringKlazz = image->getClass("String");
        //auto* systemString = reinterpret_cast<System_String_o *>(stringKlazz.objectNew(true));
         */
        System_String_o* systemStringToUse = System_String_o::create(val.size() + 1);
        //return StringHelper::convertString(systemString, val);
        /*void* stringCtorPtr = ProtoCache::instance().getStringCtor();
        void (*stringCtor)(void*, char*) = reinterpret_cast<void (*)(void*, char*)>(stringCtorPtr);*/
        //auto *cstr = new char16_t[val.length() + 1];
        for(size_t i = 0, iMax = val.size(); i <= iMax; ++i)
        {
            systemStringToUse->chars[i] = static_cast<char16_t >(
                    static_cast<unsigned char>(val[i])
            );
            // Logger::debug(std::to_string(converted->chars[i]));
        }
        systemStringToUse->chars[val.size()] = '\0';
        systemStringToUse->length = static_cast<int32_t>(val.size());
        systemStringToUse->klass = original->klass;
        systemStringToUse->monitor = original->monitor;
        //strcpy(systemString->chars, val.c_str());
        //stringCtor(systemString, cstr);
        return systemStringToUse;
    }
}



std::string StringHelper::convertNumberToHtml(int number) {
    int helper = 0;
    if (number == 0) {
        helper = 9450;
    } else if (number > 0 && number <= 20) {
        helper = 9311 + number;
    } else if (number > 20 && number <= 50) {
        helper = 12860 + number;
    }
    return "&#" + to_string(helper) + ";";
}

System_String_o* StringHelper::encodeString(System_String_o* original) {
    //if (StringHelper::alreadyConvertedAllocatedNames.find(original) != StringHelper::alreadyConvertedAllocatedNames.end()) {
    //    Logger::debug("Already converted string, reusing...");
    //    return StringHelper::alreadyConvertedAllocatedNames.at(original);
    //}
    System_String_o *converted;
    System_String_o *(*encodeHtmlString)(System_String_o*, bool);
    encodeHtmlString = (System_String_o *(*)(System_String_o*, bool))
            (ProtoCache::instance().getEncodedHtmlString());
    converted = encodeHtmlString(original, true);
    //StringHelper::alreadyConvertedAllocatedNames.emplace(original, converted);

    return converted;
}

std::string StringHelper::convertNumberToSupHtml(std::string number) {
    std::string returnValue;
    for (char const &c: number) {
        int value = c - 48;
        if (value == 0) {
            returnValue += "&#8304;";
        } else if (value == 1) {
            returnValue += "&#0185;";
        } else if (value == 2) {
            returnValue += "&#0178;";
        } else if (value == 3) {
            returnValue += "&#0179;";
        } else if (value > 3) {
            int helperValue = 8304 + value;
            returnValue += "&#" + to_string(helperValue) + ";";
        }
    }
    return returnValue;
}

std::string StringHelper::convertBigSmall(int value){
    std::string returnValue;
    if (value==1) {
        // big
        returnValue = "&#8330;&#8330;";
    } else if (value == 2) {
        // small
        returnValue = "&#8315;&#8315;";
    }

    return returnValue;
}

std::string StringHelper::convertXsXltoUnicode(std::string value) {
    if (StringHelper::xlxsUnicodeMapping.find(value) != StringHelper::xlxsUnicodeMapping.end()) {
        return StringHelper::xlxsUnicodeMapping.at(value);
    }
    return "";
}

std::string StringHelper::convertAttackToUnicode(std::string attackName) {
    if (StringHelper::moveIconMapping.find(attackName) != StringHelper::moveIconMapping.end()) {
        return StringHelper::moveIconMapping.at(attackName);
    }
    return "";
}
