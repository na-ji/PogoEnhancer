#ifndef POGODROID_LOGGER_H
#define POGODROID_LOGGER_H


#include <string>

using namespace std;
class Logger {
public:
    static void fatal(const string& message);
    static void error(const string& message);
    static void warning(const string& message);
    static void info(const string& message);
    static void debug(const string& message);

    static void pdebug(const string& message);

};


#endif //POGODROID_LOGGER_H
