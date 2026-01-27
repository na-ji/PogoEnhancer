//
//
//

#include "ItemRemover.h"
#include "../../ProtoCache.h"
#include "../../InjectionSettings.h"
#include "../../Logger.h"

void ItemRemover::insertItemsToInventoryData(int item, int count) {
    this->inventoryMutex.lock();
    if (this->inventoryData.count(item)) {
        this->inventoryData[item] += count;
    } else {
        this->inventoryData.insert(pair<int, int>(item, count));
    }
    this->inventoryMutex.unlock();
}

std::map<int, int> ItemRemover::popInventoryData() {
    this->inventoryMutex.lock();
    map<int, int> inventory = this->inventoryData;
    this->inventoryData.clear();
    this->inventoryMutex.unlock();
    return inventory;
}

void ItemRemover::clearInventoryItems() {
    void *recycleItemPtr = ProtoCache::instance().getSendRecycleItem();
    void *(*sendRecycleItem)(void *, int, int) = (void *(*)(void *, int, int)) (recycleItemPtr);

    std::map<int, int> inventory = this->popInventoryData();

    for (auto itr = inventory.begin(); itr != inventory.end(); ++itr) {
        void *itemBag = ProtoCache::instance().getItemBag();
        Logger::debug("Recycled: " + std::to_string(itr->second) + "x item #" +
                      std::to_string(itr->first));
        sendRecycleItem(itemBag, itr->first, itr->second);
    }


}

