//
//
//

#ifndef POGODROIDMAPPER_ITEMREMOVER_H
#define POGODROIDMAPPER_ITEMREMOVER_H

#include <mutex>
#include <map>

using namespace std;

class ItemRemover {
public:
    ItemRemover(const ItemRemover&) = delete;
    ItemRemover& operator=(const ItemRemover &) = delete;
    ItemRemover(ItemRemover &&) = delete;
    ItemRemover & operator=(ItemRemover &&) = delete;

    static auto& instance(){
        static ItemRemover ItemRemover;
        return ItemRemover;
    }

    void insertItemsToInventoryData(int item, int count);
    std::map<int, int> popInventoryData();
    void clearInventoryItems();


private:
    ItemRemover () = default;
    std::mutex inventoryMutex;
    std::map<int, int> inventoryData;

};

#endif //POGODROIDMAPPER_ITEMREMOVER_H
