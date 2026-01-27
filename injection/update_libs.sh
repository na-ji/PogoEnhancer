#!/bin/bash -x
BASEDIR=$(pwd)

# ARM64 old android
cd $(echo $BASEDIR)/old_android/ARM64
make clean && make -j4
mkdir -p $(echo $BASEDIR)/../app/src/main/jniLibs/arm64-v8a/
cp libs/arm64-v8a/inject-lib $(echo $BASEDIR)/../app/src/main/jniLibs/arm64-v8a/liba.so

# ARM64 new android
cd $(echo $BASEDIR)/latest/ARM64
make clean && make -j4
cp libs/arm64-v8a/inject-lib $(echo $BASEDIR)/../app/src/main/jniLibs/arm64-v8a/libh.so

# ARMEABI
cd $(echo $BASEDIR)/ARMEABI
make clean && make -j4
mkdir -p $(echo $BASEDIR)/../app/src/main/jniLibs/armeabi-v7a/
cp libs/armeabi-v7a/inject-lib $(echo $BASEDIR)/../app/src/main/jniLibs/armeabi-v7a/libe.so
