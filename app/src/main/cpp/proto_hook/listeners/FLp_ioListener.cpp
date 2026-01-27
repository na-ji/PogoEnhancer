//
//
//

#include "FLp_ioListener.h"
#include "../ProtoCache.h"
#include "../Logger.h"

void FLp_ioListener::on_enter(Gum::AbstractInvocationContext *context) {
    void* friendsListPage = context->get_nth_argument_ptr(0);
    Logger::debug("Updating friends list page ref to " + ProtoCache::convertPointerToReadableString(friendsListPage));
    ProtoCache::instance().setFriendsListPage(friendsListPage);
    ProtoCache::instance().resetCellViewsOfFriendsList();
}

void FLp_ioListener::on_leave(Gum::AbstractInvocationContext *context) {
    auto friendsRpcServiceOffset = reinterpret_cast<unsigned long>(ProtoCache::instance().getFLpFRSo());
    auto **friendsRpcService = reinterpret_cast<void **>(reinterpret_cast<char *>(ProtoCache::instance().getFriendsListPage()) +
            friendsRpcServiceOffset);

    Logger::debug("Updating friendsRpcService ref to " + ProtoCache::convertPointerToReadableString(*friendsRpcService));
    ProtoCache::instance().setFriendsRpcService(*friendsRpcService);
}
