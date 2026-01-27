//
//
//

#include "EncounterInit.h"
#include "../Logger.h"
#include "../ProtoCache.h"

void EncounterInit::on_enter(Gum::AbstractInvocationContext *context) {
    this->encounterState = reinterpret_cast<char*>(context->get_nth_argument_ptr(0));
}

void EncounterInit::on_leave(Gum::AbstractInvocationContext *context) {
    int cameraOffset = 0;
    int tweenDurationOffset = 0;
    int introDurationOffset = 0;
#if defined(__arm__)
    cameraOffset = 0x6C;
    tweenDurationOffset = 0x10;
    introDurationOffset = 0x20;
#elif defined(__aarch64__)
    cameraOffset = 0xD8;
    tweenDurationOffset = 0x1C;
    introDurationOffset = 0x30;
#else
    return;
#endif
    Logger::info("Encounter state: " + ProtoCache::convertPointerToReadableString(this->encounterState));
    void** parkCameraControllerPointer =
            reinterpret_cast<void**>(this->encounterState + cameraOffset);
    Logger::fatal("Park Camera controller: " + ProtoCache::convertPointerToReadableString(parkCameraControllerPointer));
    Logger::fatal("Park camera controller instance: " + ProtoCache::convertPointerToReadableString(*parkCameraControllerPointer));
    void* parkCameraController = *parkCameraControllerPointer;
    if (parkCameraController == nullptr) {
        return;
    }
    Logger::fatal("Park camera controller instance at " + ProtoCache::convertPointerToReadableString(parkCameraController));

    /*
     *         camera.add("0x1C").writeFloat(0);
     *         camera.add("0x30").writeFloat(0);
     */
    auto* tweenDuration = reinterpret_cast<float *>(reinterpret_cast<char*>(parkCameraController) + tweenDurationOffset);
    Logger::fatal("Tween duration: " + std::to_string(*tweenDuration));
    *tweenDuration = 0.0F;
    Logger::fatal("Tween duration: " + std::to_string(*tweenDuration));

    auto* introDuration = reinterpret_cast<float *>(reinterpret_cast<char*>(parkCameraController) + introDurationOffset);
    Logger::fatal("Intro duration: " + std::to_string(*introDuration));
    *introDuration = 0.0F;
    Logger::fatal("Intro duration: " + std::to_string(*introDuration));

}
