#ifndef POGODROID_PROTOCONVERTER_H
#define POGODROID_PROTOCONVERTER_H

#include <externals/MonoPosixHelper.h>
#include <vector>

class ProtoConverter {
public:
    static std::string convertRawProto(long timestamp, int method, std::vector<uint8_t> rawProto);

private:
    static std::string convertRawGetMapObjects(uint8_t rawProto[]);
};


#endif //POGODROID_PROTOCONVERTER_H
