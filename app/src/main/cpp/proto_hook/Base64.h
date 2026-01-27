#ifndef POGODROID_BASE64_H
#define POGODROID_BASE64_H


#include <string>

class Base64 {
public:
    static std::string decode(std::string const& encoded_string);
    static std::string encode(unsigned char const* bytes_to_encode, unsigned int in_len);
    static bool is_base64(unsigned char c);
};


#endif //POGODROID_BASE64_H
