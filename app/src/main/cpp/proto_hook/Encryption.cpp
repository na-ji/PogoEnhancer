#include <string>
#include <rijndael.h>
#include <zconf.h>
#include <modes.h>
#include <filters.h>
#include "Encryption.h"
#include "Base64.h"
#include "ProtoCache.h"
#include <cryptlib.h>
#include <aes.h>

#include <osrng.h>
#include <hex.h>
#include <random>

using namespace CryptoPP;

std::string Encryption::genRandom( size_t length )
{
    std::string const default_chars =
            "abcdefghijklmnaoqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    std::mt19937_64 gen { std::random_device()() };

    std::uniform_int_distribution<size_t> dist { 0, default_chars.length()-1 };

    std::string ret;

    std::generate_n(std::back_inserter(ret), length, [&] { return default_chars[dist(gen)]; });
    return ret;
}

//encrypts and encodes to base64
std::string Encryption::encryptSymm(std::string plain) {
    if (plain.empty()) {
        return "";
    }
//    OBF_BEGIN
//
// Create Cipher Text
//
    CryptoPP::AutoSeededRandomPool prng;
//    std::string keyString = "abcdefghijklmopq";
    std::string keyString = ProtoCache::instance().getSymmKey();
    if (keyString.empty()) {
        return "";
    }
    CryptoPP::SecByteBlock key(reinterpret_cast<const CryptoPP::byte*>(keyString.data()), keyString.size());

    std::string ivString = Encryption::genRandom(16);
    CryptoPP::SecByteBlock iv(reinterpret_cast<const CryptoPP::byte*>(ivString.data()), ivString.size());

//    std::string ivString = std::string(reinterpret_cast<const char*>(iv));


    //Key and IV setup
    //AES encryption uses a secret key of a variable length (128-bit, 196-bit or 256-
    //bit). This key is secretly exchanged between two parties before communication
    //begins. DEFAULT_KEYLENGTH= 16 bytes

//    CryptoPP::byte key[ CryptoPP::AES::DEFAULT_KEYLENGTH ], iv[ CryptoPP::AES::BLOCKSIZE ];
//    memset( key, 0x00, CryptoPP::AES::DEFAULT_KEYLENGTH );
//    memset( iv, 0x00, CryptoPP::AES::BLOCKSIZE );

    std::string ciphertext;

    CryptoPP::AES::Encryption aesEncryption(key, CryptoPP::AES::DEFAULT_KEYLENGTH);
    CryptoPP::CBC_Mode_ExternalCipher::Encryption cbcEncryption(aesEncryption, iv);
//    CryptoPP::

//    StringSource encryptor(key, true, new StreamTransformationFilter(aesEncryption, new HexEncoder( new StringSink(ciphertext))) );
//    CryptoPP::StringSource encryptor(plain, true,
//                                     new CryptoPP::StreamTransformationFilter(
//                                             aesEncryption,
//                                             new CryptoPP::StringSink(ciphertext)) );
    CryptoPP::StreamTransformationFilter stfEncryptor(cbcEncryption, new CryptoPP::StringSink(ciphertext), CryptoPP::StreamTransformationFilter::PKCS_PADDING);
    stfEncryptor.Put(reinterpret_cast<const unsigned char*> (plain.c_str()), plain.length()); //removed +1
    stfEncryptor.MessageEnd();


    ciphertext = Base64::encode(reinterpret_cast<const unsigned char *>(ciphertext.c_str()), ciphertext.length());
    std::string ivReadable = Base64::encode(reinterpret_cast<const unsigned char *>(ivString.c_str()), ivString.length());
//    RETURN(ivReadable + ";" + ciphertext);
//    OBF_END
    return ivReadable + ";" + ciphertext;
}


std::string Encryption::hexHash(const std::string& plain) {
    CryptoPP::SHA256 hash;
    byte digest[ CryptoPP::SHA256::DIGESTSIZE ];

    hash.CalculateDigest( digest, (byte*) plain.c_str(), plain.length() );

    CryptoPP::HexEncoder encoder;
    std::string output;
    encoder.Attach( new CryptoPP::StringSink( output ) );
    encoder.Put( digest, sizeof(digest) );
    encoder.MessageEnd();
    return output;
}
