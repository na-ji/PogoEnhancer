//
//
//

#include "InfoServer.h"
#include "../Logger.h"
#include <unistd.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <linux/in.h>
#include <arpa/inet.h>
#include "../json.hpp"
#include "../InjectionSettings.h"
#include "../ProtoCache.h"
#include "../listeners/StaticReplacements.h"
#include "../listeners/shared/LocProv.h"

using namespace nlohmann;
bool first = false;

bool InfoServer::startServer() {
    Logger::debug("Setting up unix socket to listen for setting changes");
    this->cleanupSocket();

    if ((sockfd = ::socket(PF_LOCAL, SOCK_STREAM, 0)) == -1) {
        Logger::fatal("Failed to create socket");
        return false;
    }
    std::string socketName = "proto";
    struct sockaddr_un addr;
    socklen_t len;
    addr.sun_family = AF_LOCAL;
    /* use abstract namespace for socket path */
    addr.sun_path[0] = '\0';
    strcpy(&addr.sun_path[1], socketName.substr(0, 5).c_str());
    len = offsetof(struct sockaddr_un, sun_path) + 1 + strlen(&addr.sun_path[1]);

    int rc = ::bind(sockfd, (struct sockaddr *) &addr, len);
    if (rc != 0) {
        Logger::fatal("Failed binding unix socket");
        return false;
    }

    rc = listen(sockfd, MAX_BACKLOG);
    if (rc != 0) {
        Logger::fatal("Failed listening to unix socket");
        ::close(sockfd);
        return false;
    }

    Logger::pdebug("Waiting for incoming connections");
    struct sockaddr_un cli_addr;
    byte buffer[BUFFER_SIZE + 1];
    while (true) {
        socklen_t cli_len = sizeof(cli_addr);
        if ((currentSocketFd = ::accept(sockfd, (struct sockaddr *)&cli_addr, &cli_len)) == -1) {
            Logger::error("Failed accepting connection");
            continue;
        }
        Logger::debug("Incoming connection!");
        // First read the first 4 bytes, transform to int. That is the length that is to be read. Afterwards, parse the message.
        //lenOfMessage = htonl(lenOfMessage);
        std::string dataReceived;
        int lenRead = 0;
        do {
            bzero(buffer, BUFFER_SIZE + 1);
            lenRead = ::recv(currentSocketFd, buffer, BUFFER_SIZE + 1, 0);
            if (lenRead < 1) {
                break;
            }
            Logger::debug("Read " + std::to_string(lenRead));
            dataReceived.append(std::string(reinterpret_cast<const char *>(&buffer), lenRead));

            //for (int i = 0; i < lenRead; i++) {
            //}
        } while (lenRead > 0);

        Logger::debug("Received message: " + dataReceived);

        try {
            auto jsonContent = json::parse(dataReceived);
            if (jsonContent.contains("settings")) {
                InjectionSettings::instance().updateSettings(jsonContent["settings"]);
                ProtoCache::instance().setNotAutorunMon(jsonContent["settings"]["noautorunmonid"]);
                Logger::debug("Done setting noautorunmonid");
                if(!first) {
                    //GMOBoost::instance().createCombatChallenge();
                    first = true;
                }


            }
            if (jsonContent.contains("inventory")) {
                auto inventorySettings = jsonContent.at("inventory");
                ProtoCache::instance().setInvManagementEnabled(inventorySettings["active"].get<bool>());
                ProtoCache::instance().setInventoryManagementItems(inventorySettings["items"]);
            }
            if (jsonContent.contains("namerepl")) {
                auto nameReplaceSettings = jsonContent.at("namerepl");
                InjectionSettings::instance().setReplaceCpWithIvPercentage(nameReplaceSettings["nameReplace"]);
                InjectionSettings::instance().setReplaceEncounterNames(nameReplaceSettings["nameReplaceEncounter"]);
                ProtoCache::instance().setNameReplaceSettings(nameReplaceSettings["nameValues"]);
            }
            if (jsonContent.contains("moves")) {
                auto nameReplaceSettings = jsonContent.at("moves");
                ProtoCache::instance().setMonMoves(nameReplaceSettings);
            }
            if (jsonContent.contains("wildmonTypes")) {
                auto wildMonSettings = jsonContent.at("wildmonTypes");
                auto monTypes = wildMonSettings.at("types");
                auto hideMonTypes = wildMonSettings.at("hideMons");
                ProtoCache::instance().setWildMon(monTypes);
                ProtoCache::instance().setHideWildMon(hideMonTypes);
            }
            if (jsonContent.contains("autotransfer")) {
                auto autotransferSettings = jsonContent.at("autotransfer");
                InjectionSettings::instance().setEnableAutotransfer(autotransferSettings["active"].get<bool>());
                InjectionSettings::instance().setAutotransferInverted(autotransferSettings["invertedList"].get<bool>());
                ProtoCache::instance().setNotAutotransferMonIds(autotransferSettings["monid"]);

            }

            if (jsonContent.contains("autoencounter")) {
                auto autoencounterSettings = jsonContent.at("autoencounter");
                InjectionSettings::instance().setEnableAutoencounter(autoencounterSettings["active"].get<bool>());

            }

            if (jsonContent.contains("cooldown")) {
                auto cooldownSettings = jsonContent.at("cooldown");
                double lat = cooldownSettings["lat"].get<double>();
                double lng = cooldownSettings["lng"].get<double>();
                long timestamp = cooldownSettings["timestamp"].get<long>();
                LocProv::instance().setInitCooldownInfo(lat, lng, timestamp);
            }

            if (jsonContent.contains("quests")) {

                for (const auto& quest : jsonContent.at("quests")) {
                    Logger::debug(to_string(quest));

                }


            }

            if (jsonContent.contains("transferoncatch")) {
                auto newMonSettings = jsonContent.at("transferoncatch");
                unsigned long long monId = newMonSettings["newmonid"];
                ProtoCache::instance().addEncounterToCheckList(monId);

            }



            if (jsonContent.contains("credentials")) {
                auto credentials = jsonContent.at("credentials");
                InjectionSettings::instance().setDeviceId(credentials["deviceId"].get<string>());
                InjectionSettings::instance().setSessionId(credentials["sessionId"].get<string>());
                InjectionSettings::instance().setAuthHeaderContent(credentials["authHeader"].get<string>());
                InjectionSettings::instance().setPostDestination(credentials["destination"].get<string>());
                InjectionSettings::instance().setOrigin(credentials["origin"].get<string>());
                InjectionSettings::instance().setInitialSettings(credentials["settings"].get<string>());
            }
        } catch (...) {
            Logger::debug("Failed reading or handling data");
        }
        ::close(currentSocketFd);
        Logger::debug("Done processing message");
    }
    return true;
}

void InfoServer::cleanupSocket() {
    const char name[] = "\0protohookout";
    if (access(name, F_OK) != -1) {
        Logger::debug("Cleaning up socket");
        unlink(name);
    }
}

void InfoServer::start() {
    this->listenerThread = std::thread(&InfoServer::startServer, this);
}
