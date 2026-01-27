APP_PLATFORM := android-21
APP_ABI := arm64-v8a
APP_STL := c++_static
APP_PIE := true

APP_CXXFLAGS := -Wall -Werror -fpermissive -fpic -std=c++14 -mllvm -enable-strcry -mllvm -enable-funcwra -mllvm -fw_prob=80 -mllvm -fw_times=1 -mllvm -enable-bcfobf -mllvm -bcf_loop=2 -mllvm -bcf_prob=70 -mllvm -enable-cffobf -mllvm -enable-acdobf -mllvm -enable-splitobf -mllvm -enable-subobf
