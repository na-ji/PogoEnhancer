#ifndef POGODROID_ENCB_H
#define POGODROID_ENCB_H

#include <osrng.h>
#include <string>

class EncB {
public:
    std::string encrypt(const std::string& plain);
    EncB(const std::string& key, const std::string& salt);
    std::string decrypt(const std::string& encrypted);
private:
    CryptoPP::byte key[ CryptoPP::SHA256::DIGESTSIZE];

    std::string genRandom(size_t length);
};


#endif //POGODROID_ENCB_H
