#include <sys/socket.h>
#include <endian.h>
#include <sys/un.h>
#include "UnixSender.h"
#include <zconf.h>
#include <errno.h>
#include <unistd.h>
#include "Encryption.h"
#include "Logger.h"

using namespace std;

int8_t UnixSender::getNthByteOfInt(uint32_t number, int n) {
    int8_t ret = -1;
        if(n < 0 || n > 3) {
            ret = -1;
        } else {
            ret = (number >> (8*n)) & 0xff;
        }
        return ret;
//    if (n < 0 || n > 3) {
//        return -1;
//    } else {
//        return (number >> (8*n)) & 0xff;
//    }
}

int UnixSender::sendall(int s, const char *buf, size_t len)
{
    size_t total = 0;        // how many bytes we've sent
    size_t bytesleft = len; // how many we have left to send
    int n = -1;

//    send(s, (char*)len, 4, 0);
    while(total < len) {
        n = send(s, buf + total, bytesleft, 0);
        if (n == -1) {
            break;
        }
        total += n;
        bytesleft -=n;
    }

    len = total; // return number actually sent here

//    IF (n == -1)
//        return -1;
//    ELSE
//        RETURN(N(0))
//    ENDIF
    int retval = -1;
    if(n != -1) {
        retval = 0;
    }

    return retval;
//    return n==-1?-1:0; // return -1 onm failure, 0 on success
}

bool UnixSender::sendMessage(MESSAGE_TYPE type, std::string input, std::string socketName) {
    int err = 0;
    int sk;
    string toSend = "";

    input = std::to_string(type) + ";" + input;

    std::string toBeSent = Encryption::encryptSymm(input);
    if (toBeSent.empty()) {
        return false;
    }
//    LOGD("Sending: %s", toBeSent.c_str());

    uint32_t lengthOfMessage = toBeSent.length();
    lengthOfMessage = htonl(lengthOfMessage);
    char bytesOfLength[4];
    for(int i = 0; i < sizeof(uint32_t); i++) {
        bytesOfLength[i] = UnixSender::getNthByteOfInt(lengthOfMessage, i);
        toSend += bytesOfLength[i];
    }
    toSend += toBeSent;
    if (toSend.empty()) {
        Logger::debug("Nothing to be sent...");
        return false;
    }
    struct sockaddr_un addr;
    socklen_t len;
    addr.sun_family = AF_LOCAL;
    /* use abstract namespace for socket path */
    addr.sun_path[0] = '\0';
    strcpy(&addr.sun_path[1], socketName.substr(0, 5).c_str());
    len = offsetof(struct sockaddr_un, sun_path) + 1 + strlen(&addr.sun_path[1]);

//    LOGD("In clientSocketThreadNative() : Before creating socket");
    sk = socket(PF_LOCAL, SOCK_STREAM, 0);
    if (sk < 0) {
        err = errno;
        Logger::debug("Could not open up Unix socket");
        errno = err;
        return false;
    }

//    LOGD("In clientSocketThreadNative() : Before connecting to Java LocalSocketServer");
    if (connect(sk, (struct sockaddr *) &addr, len) < 0) {
        err = errno;
        Logger::debug("Failed connecting to unix socket");
        close(sk);
        errno = err;
        return false;
    }

    const char* charPtr = toSend.c_str();
    Logger::debug("Sending to app: " + toSend);

//    LOGD("In clientSocketThreadNative() : Connecting to Java LocalSocketServer succeed");
    int sent = sendall(sk, charPtr, reinterpret_cast<size_t>(toSend.length()));
//    LOGD("In clientSocketThreadNative() : close(%d)", sk);

    int result = close(sk);
    //Logger::debug("Send close() returned " + std::to_string(result));
    return sent == 0;
}
