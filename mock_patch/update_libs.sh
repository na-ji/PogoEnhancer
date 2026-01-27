#!/bin/bash -x
cd ARM64
make clean
make NDKPATH='/opt/android-sdk/ndk/latest/ndk-build'
mkdir -p ../../app/src/main/jniLibs/arm64-v8a/
mkdir -p ../../app/src/main/jniLibs/armeabi-v7a/
cp libs/arm64-v8a/inject-lib ../../app/src/main/jniLibs/arm64-v8a/libg.so
cp libs/arm64-v8a/inject-lib ../../app/src/main/jniLibs/armeabi-v7a/libg.so
cd ../ARMEABI
make clean
make ndk_path=/opt/android-sdk/ndk/latest/ndk-build
cp libs/armeabi-v7a/inject-lib ../../app/src/main/jniLibs/armeabi-v7a/libf.so
