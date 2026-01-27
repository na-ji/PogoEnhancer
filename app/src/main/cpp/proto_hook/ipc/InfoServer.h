//
//
//

#ifndef POGOENHANCER_INFOSERVER_H
#define POGOENHANCER_INFOSERVER_H

#include <thread>

class InfoServer {
public:
    InfoServer(const InfoServer&) = delete;
    InfoServer& operator=(const InfoServer &) = delete;
    InfoServer(InfoServer &&) = delete;
    InfoServer & operator=(InfoServer &&) = delete;

    static auto& instance(){
        static InfoServer InfoServer;
        return InfoServer;
    }

    void start();

private:
    static const int MAX_BACKLOG = 20;
    static const int BUFFER_SIZE = 4096;

    InfoServer() = default;
    bool startServer();
    std::thread listenerThread;
    int sockfd;
    int currentSocketFd;

    void cleanupSocket();
};


#endif //POGOENHANCER_INFOSERVER_H
