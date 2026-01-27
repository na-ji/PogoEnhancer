# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             d

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
        proto_hook/main.cpp
        proto_hook/Util.cpp
        proto_hook/listeners/Response.cpp
        proto_hook/caches/SharedQueue.hpp
        proto_hook/ProtoCache.cpp
        proto_hook/ProtoConverter.cpp
        proto_hook/gumpp_new/AbstractInvocationContext.h
        proto_hook/gumpp_new/AbstractBacktracer.h
        proto_hook/gumpp_new/AbstractInterceptor.h
        proto_hook/gumpp_new/AbstractInvocationListener.h
        proto_hook/gumpp_new/AbstractProfiler.h
        proto_hook/gumpp_new/AbstractSampler.h
        proto_hook/gumpp_new/AbstractSanityChecker.h
        proto_hook/gumpp_new/BacktracerImpl.hpp
        proto_hook/gumpp_new/CallCountSampler.h
        proto_hook/gumpp_new/InspectorCallbacks.h
        proto_hook/gumpp_new/InterceptorImpl.h
        proto_hook/gumpp_new/InvocationContext.h
        proto_hook/gumpp_new/InvocationListenerIface.h
        proto_hook/gumpp_new/InvocationListenerProxy.h
        proto_hook/gumpp_new/InvocationListenerProxy.cpp
        proto_hook/gumpp_new/ObjectWrapper.hpp
        proto_hook/gumpp_new/PodWrapper.hpp
        proto_hook/gumpp_new/ProfileReportImpl.h
        proto_hook/gumpp_new/RefPtr.h
        proto_hook/gumpp_new/Runtime.h
        proto_hook/gumpp_new/Runtime.cpp
        proto_hook/gumpp_new/structs.h
        proto_hook/gumpp_new/SymbolUtil.h
        proto_hook/UnixSender.cpp
        proto_hook/Encryption.cpp
        proto_hook/Base64.cpp
        proto_hook/Logger.cpp

        proto_hook/listeners/StaticReplacements.cpp
        proto_hook/listeners/PokemonItemListener.cpp
        proto_hook/listeners/RaidMapPokemonCreateListener.cpp
        proto_hook/listeners/shared/PokemonProto.cpp
        proto_hook/listeners/shared/IncenseMapPokemon.cpp
        proto_hook/listeners/shared/AutoFeed.cpp
        proto_hook/InfoClient.cpp
        proto_hook/EncQueue.cpp
        proto_hook/Scanmodes.h
        proto_hook/listeners/UpdateWildPokemonListener.cpp
        proto_hook/listeners/UpdateIncensePokemonListener.cpp
        proto_hook/listeners/PoiItemSpinnerListener.cpp
        proto_hook/listeners/shared/WildMapPokemon.cpp
        proto_hook/listeners/UpdateDiskPokemonListener.cpp
        proto_hook/listeners/BuddyPokemonServiceListener.cpp
        proto_hook/listeners/BuddyRpcServiceListener.cpp
        proto_hook/listeners/BuddySettingsServiceListener.cpp
        proto_hook/listeners/OpenBuddyGiftOutProto.cpp
        proto_hook/listeners/BreakoutFromPokeball.cpp
        proto_hook/listeners/PokemonBagListener.cpp
        proto_hook/InjectionSettings.cpp
        proto_hook/HAP/HAP64.cpp
        proto_hook/listeners/shared/Throw.h
        proto_hook/listeners/shared/Stop.cpp
        proto_hook/listeners/UpdateStop.cpp
        proto_hook/listeners/ItemBagInstanceListener.cpp
        proto_hook/listeners/PlayerServiceInstance.cpp
        proto_hook/HAP/AbstractHAP.cpp
        proto_hook/listeners/StopProximity.cpp
        proto_hook/listeners/QuestGetCpListener.cpp
        proto_hook/listeners/RaidGetCpListener.cpp
        proto_hook/listeners/IncidentGetCpListener.cpp
        proto_hook/listeners/DailyGetCpListener.cpp
        proto_hook/listeners/EncounterInit.cpp
        proto_hook/listeners/FormPossible.cpp
        proto_hook/listeners/shared/StringHelper.cpp
        proto_hook/listeners/shared/WildMonProto.cpp
        proto_hook/listeners/CombatDirectorV2Listener.cpp


        proto_hook/listeners/ePsListener.cpp
        proto_hook/listeners/IlLl.cpp
        proto_hook/listeners/PmSeSo.cpp
        proto_hook/listeners/GMoCi.cpp
        proto_hook/listeners/FLp_ioListener.cpp
        proto_hook/listeners/GWp.cpp
        proto_hook/listeners/GRscGSoListener.cpp
        proto_hook/listeners/CombatDirector.cpp
        proto_hook/listeners/GetItemBagListener.cpp
        proto_hook/listeners/shared/ItemRemover.cpp
        proto_hook/listeners/MemDerp.cpp
        proto_hook/ipc/InfoServer.cpp
        proto_hook/listeners/shared/LocProv.cpp
        proto_hook/json.hpp
        proto_hook/EncB.cpp
        proto_hook/il2cpp/il2cppStructs.h
        #        proto_hook/selinux/patch.c
#        proto_hook/selinux/dispatch.c
        proto_hook/geometry/s2.cc
        proto_hook/geometry/s2cell.cc
        proto_hook/geometry/s2latlngrect.cc
        proto_hook/geometry/s2region.cc
        proto_hook/geometry/s2cap.cc
        proto_hook/geometry/s2latlng.cc
        proto_hook/geometry/base/stringprintf.cc
        proto_hook/geometry/r1interval.h
        proto_hook/geometry/s2edgeutil.cc
        proto_hook/geometry/s1angle.cc
        proto_hook/geometry/s1interval.cc
        proto_hook/geometry/util/coding/coder.cc
        proto_hook/geometry/util/coding/varint.cc
        proto_hook/geometry/s2cellid.cc
        proto_hook/geometry/base/logging.h
        proto_hook/geometry/strings/strutil.h
        proto_hook/geometry/strings/strutil.cc
        proto_hook/geometry/strings/split.cc
        proto_hook/geometry/base/strtoint.cc

        proto_hook/il2cpp_util/Il2CppUtil.hpp
        proto_hook/il2cpp_util/Il2CppClass.hpp
        proto_hook/il2cpp_util/Il2CppImage.hpp
        proto_hook/il2cpp_util/Il2CppDomain.hpp
        proto_hook/il2cpp_util/Il2CppAssembly.hpp
        )

include_directories(proto_hook/il2cpp)
include_directories(proto_hook/include)
include_directories(headers/cryptopp)
include_directories(headers)
include_directories(libs/${ANDROID_ABI}/headers)

set(THREADS_PREFER_PTHREAD_FLAG ON)
#find_package(Threads REQUIRED)

#include(AndroidNdkModules)
#android_ndk_import_module_cpufeatures()

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       d
                       PRIVATE
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcpufeatures.a
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcryptopp_static.a
        #${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libprotobuf.a
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcurl.a
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libz.a
        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libfrida-gumjs.a
        #${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libcrypto.a
        #${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libssl.a
        )
