#include <osrng.h>
#include <random>
#include <modes.h>
#include "EncB.h"
#include "Base64.h"


std::string EncB::genRandom( size_t length )
{
    std::string const default_chars =
            "abcdefghijklmnaoqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    std::mt19937_64 gen { std::random_device()() };

    std::uniform_int_distribution<size_t> dist { 0, default_chars.length()-1 };

    std::string ret;

    std::generate_n(std::back_inserter(ret), length, [&] { return default_chars[dist(gen)]; });
    return ret;
}

std::string EncB::encrypt(const std::string& plain) {
    if (plain.empty()) {
        return "";
    }
    CryptoPP::AutoSeededRandomPool prng;

    std::string ivString = genRandom(16);
    CryptoPP::SecByteBlock iv(reinterpret_cast<const CryptoPP::byte*>(ivString.data()), ivString.size());

    std::string ciphertext;
    CryptoPP::AES::Encryption aesEncryption(this->key, 32);
    CryptoPP::CBC_Mode_ExternalCipher::Encryption cbcEncryption(aesEncryption, iv);
    CryptoPP::StreamTransformationFilter stfEncryptor(cbcEncryption, new CryptoPP::StringSink(ciphertext), CryptoPP::StreamTransformationFilter::PKCS_PADDING);
    stfEncryptor.Put(reinterpret_cast<const unsigned char*> (plain.c_str()), plain.length()); //removed +1
    stfEncryptor.MessageEnd();

    ciphertext = Base64::encode(reinterpret_cast<const unsigned char *>(ciphertext.c_str()), ciphertext.length());
    std::string ivReadable = Base64::encode(reinterpret_cast<const unsigned char *>(ivString.c_str()), ivString.length());
    return ivReadable + ";" + ciphertext;
}

EncB::EncB(const std::string& key, const std::string& salt) {
    CryptoPP::SHA256 hash;
    std::string message = key + salt;
    hash.CalculateDigest( this->key, (CryptoPP::byte*) message.c_str(), message.length() );
}

std::string EncB::decrypt(const std::string& encrypted) {
    std::string delimiter = ";";
    if (encrypted.empty() || encrypted.find(delimiter) == std::string::npos) {
        return "";
    }
    std::string token = encrypted.substr(0, encrypted.find(delimiter));

    std::vector<std::string> splitUp = std::vector<std::string>();
    size_t last = 0;
    size_t next = 0;
    while ((next = encrypted.find(delimiter, last)) != std::string::npos) {
        splitUp.push_back(encrypted.substr(last, next - last));
        last = next + 1;
    }
    splitUp.push_back(encrypted.substr(last));
    if (splitUp.size() != 2) {
        return "";
    }

    std::string iv = splitUp.at(0);
    std::string encryptedData = splitUp.at(1);

    // Base64 decode both -> raw IV and encrypted data
    std::string ivAsString = Base64::decode(iv);
    std::string encryptedDataAsString = Base64::decode(encryptedData);

    CryptoPP::SecByteBlock ivBlock(reinterpret_cast<const CryptoPP::byte*>(ivAsString.data()), ivAsString.size());
    CryptoPP::AES::Decryption aesDecryption(this->key, 32);
    CryptoPP::CBC_Mode_ExternalCipher::Decryption cbcDecryption(aesDecryption, ivBlock);
    std::string decrypted;
    try {
        CryptoPP::StreamTransformationFilter stfDecryptor(cbcDecryption, new CryptoPP::StringSink(decrypted));
        stfDecryptor.Put(reinterpret_cast<const unsigned char*> (encryptedDataAsString.c_str()), encryptedDataAsString.length(), CryptoPP::StreamTransformationFilter::PKCS_PADDING); //removed +1
        stfDecryptor.MessageEnd();
    } catch (CryptoPP::Exception exception) {
        return exception.GetWhat();
    }

    return decrypted;
}
