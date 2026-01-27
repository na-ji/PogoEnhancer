#include <string>
#include "ProtoConverter.h"
#include <sstream>
#include <iomanip>

//std::string int_to_hex( uint8_t i )
//{
//    if (i == 0) {
//        return "0x00";
//    }
//    std::stringstream stream;
//    stream << "0x"
//           << std::setfill ('0') << std::setw(sizeof(uint8_t)*2)
//           << std::hex << i;
//    return stream.str();
//}

//TODO: use json formatting
std::string ProtoConverter::convertRawProto(long timestamp, int method, std::vector<uint8_t> rawProto) {

    uint8_t rawProtoArr[rawProto.size()];
    std::string rawData = "raw;" + std::to_string(timestamp) + ";" + std::to_string(method) + ";";
    int i, s;
    s = rawProto.size();
    for(i = 0; i < s; i++) {
        rawProtoArr[i] = rawProto[i];
        rawData = rawData.append(std::to_string(rawProto[i]));
        if (i < rawProto.size() - 1) {
            rawData = rawData.append(",");
        }
    }
    return rawData;
}
