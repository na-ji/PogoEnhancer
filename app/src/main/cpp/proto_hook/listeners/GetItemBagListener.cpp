#include "GetItemBagListener.h"
#include "../ProtoCache.h"
#include "../InjectionSettings.h"
#include "../Logger.h"
#include "shared/ItemRemover.h"

void GetItemBagListener::on_enter(Gum::AbstractInvocationContext *context) {
}

void GetItemBagListener::on_leave(Gum::AbstractInvocationContext *context) {

    if (!ProtoCache::instance().processItemBag()) {
        return;
    }

    void* retVal = context->get_return_value_ptr();

/**

    if (!ProtoCache::instance().getItemBag()) {
        ProtoCache::instance().setItemBag(retVal);
        Logger::debug("Got itemBag: " + ProtoCache::convertPointerToReadableString(retVal));
    }
**/
    if (!ProtoCache::instance().isInvManagementEnabled()) {
        //"Inventory Management is disabled"
        return;
    }

    // check for safe items
    // std::set<int> safeItems = InjectionSettings::instance().getSafeItems();
    // if (safeItems.empty()) {
        // Logger::debug("Safe items from MAD are empty - skipping deletion for this round");
    //     return;
    // }

    void *itemCountPtr = ProtoCache::instance().getGet_ItemCount();
    int (*getItemCount)(void *, int) = (int (*)(void *, int)) (itemCountPtr);

    // check items and prepare for deletion

    std::vector<int> allItems = {1,2,3,101,102,103,104,201,202,301,401,501,502,503,504,701,703,705,706,708,709,1101,1102,1103,1104,1105,1106,1107,1201,1202,1203,1204,1301,1404};
    // loop and get count

    for(const auto& item : allItems)
    {
        int itemCount = getItemCount(ProtoCache::instance().getItemBag(), item);
        int maxInventoryItems = ProtoCache::instance().getInventoryManagementItem(item);
        int inventoryDelta = itemCount - maxInventoryItems;
        if (inventoryDelta > 0) {
            Logger::debug("Found " + std::to_string(itemCount) + "x item #" + std::to_string(item) + " max: " + std::to_string(maxInventoryItems));
            ItemRemover::instance().insertItemsToInventoryData(item, inventoryDelta);
        }
    }

    // start removing
    ItemRemover::instance().clearInventoryItems();


}
