//
//
//

#include "PmSeSo.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void PmSeSo::on_enter(Gum::AbstractInvocationContext *context) {
    // just call the IRenderQualityService of the instance and call the set_UnlockedFramerate(bool) method
    void* performanceMetricsService = context->get_nth_argument_ptr(0);
    int renderQualityServiceOffset = 0;
#if defined(__arm__)
    renderQualityServiceOffset = 0x10;
#elif defined(__aarch64__)
    renderQualityServiceOffset = 0x20;
#else
    return;
#endif

    Logger::debug("Unlocking fps");
    void** renderQualityServicePtr =
            reinterpret_cast<void**>(reinterpret_cast<char*>(performanceMetricsService) + renderQualityServiceOffset);
    void* renderQualityService = *renderQualityServicePtr;

    void (*set_UnlockedFramerate)(void*, bool) = (void (*)(void*, bool))(ProtoCache::instance().getSUf());
    set_UnlockedFramerate(renderQualityService, true);

}

void PmSeSo::on_leave(Gum::AbstractInvocationContext *context) {

}
