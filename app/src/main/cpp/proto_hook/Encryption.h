#ifndef POGODROID_ENCRYPTION_H
#define POGODROID_ENCRYPTION_H


class Encryption {
public:
    static std::string encryptSymm(std::string plain);
    static std::string hexHash(const std::string &plain);

private:
    static std::string genRandom( size_t length );

};


#endif //POGODROID_ENCRYPTION_H
