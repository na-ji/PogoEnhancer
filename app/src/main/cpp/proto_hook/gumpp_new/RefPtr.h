//
//
//

#ifndef POGODROID_REFPTR_H
#define POGODROID_REFPTR_H

namespace Gum {
    template<typename T>
    class RefPtr {
    public:
        explicit RefPtr(T *ptr_) : ptr(ptr_) {}

        explicit RefPtr(const RefPtr<T> &other) : ptr(other.ptr) {
            if (ptr)
                ptr->ref();
        }

        template<class U>
        RefPtr(const RefPtr<U> &other) : ptr(other.operator->()) {
            if (ptr)
                ptr->ref();
        }

        RefPtr() : ptr(0) {}

        bool is_null() const {
            return ptr == 0 || ptr->get_handle() == 0;
        }

        RefPtr &operator=(const RefPtr &other) {
            RefPtr tmp(other);
            swap(*this, tmp);
            return *this;
        }

        RefPtr &operator=(T *other) {
            RefPtr tmp(other);
            swap(*this, tmp);
            return *this;
        }

        T *operator->() const {
            return ptr;
        }

        T &operator*() const {
            return *ptr;
        }

        operator T *() {
            return ptr;
        }

        static void swap(RefPtr &a, RefPtr &b) {
            T *tmp = a.ptr;
            a.ptr = b.ptr;
            b.ptr = tmp;
        }

        ~RefPtr() {
            if (ptr)
                ptr->unref();
        }

    private:
        T *ptr;
    };
}
#endif //POGODROID_REFPTR_H
