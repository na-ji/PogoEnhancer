#ifndef POGODROID_UNIXSENDER_H
#define POGODROID_UNIXSENDER_H

#include <string>

#define LOCAL_SOCKET_SERVER_NAME "protohook"

enum MESSAGE_TYPE {
    RESPONSE_PROTO = 1,
    POKEMON_IV = 2,
    DEBUG_RAW = 3,
    REQUEST_AUTH = 4,
            TOAST = 5,
            COOLDOWN = 6
};

class UnixSender {
public:
    static bool sendMessage(MESSAGE_TYPE type, std::string input, std::string socketName);

private:
    static int sendall(int s, const char *buf, size_t len);
    static int8_t getNthByteOfInt(uint32_t number, int n);
};


#endif //POGODROID_UNIXSENDER_H
