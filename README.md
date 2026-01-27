# PogoEnhancer
PogoEnhancer was an app to extend the functionality of a certain app (let's just call it Pogo) by runtime injection into the target process.
It offered functionality such as displaying nearby monsters and events (raids) by reading the target application's traffic (encoded in Protobuf payloads).

# External Dependencies
External dependencies such as libraries and icons were used as part of this project.

## Libraries
* Frida.re https://frida.re/ for runtime injections and reverse engineering
* Furtif/POGOProtos https://github.com/Furtif/POGOProtos (and private derivates) for Protobuf payload decoding
* nlohmann/json https://github.com/nlohmann/json
* CryptoPP https://www.cryptopp.com/
* Curl https://curl.haxx.se
* virtual-joystick-android https://github.com/controlwear/virtual-joystick-android
* OpenSSL https://www.openssl.org/

## Icons
* Map location icon by https://www.flaticon.com/authors/smashicons
* Gym icon by https://www.flaticon.com/de/autoren/roundicons-freebies

# Development
The hidden Android API is used for a small portion of the compilation regarding location mocking.
If mock logcations are set, it is not needed. However, as long as the "legacy" usage of internal APIs (with the APK setup as a system app), you need to setup your environment (IDE) for the hidden API usage. See https://github.com/anggrayudi/android-hidden-api

# Compilation
1. If obfuscation is enabled, copy app/windows_orig.txt to the root dir named windows.txt: `cp app/windows_orig.txt windows.txt`
2. Set home dir of gradle (CI/CD) `export GRADLE_USER_HOME=$(pwd)/.gradle`
3. Make gradle executable `chmod +x ./gradlew`
4. Compile mock patching binaries `cd mock_patch && ./update_libs.sh && cd ..`
5. Compile injection binaries `cd injection && ./update_libs.sh && cd ..`
6. Execute gradle with environment variabls set for setting versions `./gradlew assembleRelease -Prelease=true -Pversion_id_patch=${VERSION_ID_PATCH} -Pversion_id_minor=${VERSION_ID_MINOR} --stacktrace`