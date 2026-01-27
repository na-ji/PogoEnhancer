LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libfrida-core
LOCAL_SRC_FILES := libfrida-core.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include /home/till/Android/Sdk/ndk-bundle/sources/cxx-stl/llvm-libc++/include
LOCAL_MODULE    := inject-lib
LOCAL_SRC_FILES := inject-lib.c
LOCAL_STATIC_LIBRARIES := libfrida-core
LOCAL_CPP_FEATURES := rtti exceptions
include $(BUILD_EXECUTABLE)
