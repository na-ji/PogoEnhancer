//
//
//

#ifndef POGODROID_STRUCTS_H
#define POGODROID_STRUCTS_H

#include <frida-gumjs.h>

#define GUMPP_MAX_BACKTRACE_DEPTH 16
#define GUMPP_MAX_PATH            260
#define GUMPP_MAX_SYMBOL_NAME     2000

namespace Gum {
    struct CpuContext;
    typedef unsigned long long Sample;
    typedef struct _GumInvocationListenerProxy GumInvocationListenerProxy;

    enum ExampleHookId
    {
        EXAMPLE_HOOK_OPEN,
        EXAMPLE_HOOK_CLOSE
    };

    enum SanityCheckFlags
    {
        CHECK_INSTANCE_LEAKS  = (1 << 0),
        CHECK_BLOCK_LEAKS     = (1 << 1),
        CHECK_BOUNDS          = (1 << 2)
    };

    struct Object
    {
        virtual ~Object () {}

        virtual void ref () = 0;
        virtual void unref () = 0;
        virtual void * get_handle () const = 0;
    };

    typedef void * ReturnAddress;

    struct ReturnAddressArray
    {
        unsigned int len;
        ReturnAddress items[GUMPP_MAX_BACKTRACE_DEPTH];
    };

    struct ReturnAddressDetails
    {
        ReturnAddress address;
        char module_name[GUMPP_MAX_PATH + 1];
        char function_name[GUMPP_MAX_SYMBOL_NAME + 1];
        char file_name[GUMPP_MAX_PATH + 1];
        unsigned int line_number;
    };

    struct PtrArray : public Object
    {
        virtual int length () = 0;
        virtual void * nth (int n) = 0;
    };

    struct String : public Object
    {
        virtual const char * c_str () = 0;
        virtual size_t length () const = 0;
    };

    struct HeapApi
    {
        void * (* malloc) (size_t size);
        void * (* calloc) (size_t num, size_t size);
        void * (* realloc) (void * old_address, size_t new_size);
        void (* free) (void * address);

        /* for Microsoft's Debug CRT: */
        void * (* _malloc_dbg) (size_t size, int block_type, const char * filename, int linenumber);
        void * (* _calloc_dbg) (size_t num, size_t size, int block_type, const char * filename, int linenumber);
        void * (* _realloc_dbg) (void * old_address, size_t new_size, int block_type, const char * filename, int linenumber);
        void (* _free_dbg) (void * address, int block_type);
    };


    struct FunctionMatchCallbacks
    {
        virtual ~FunctionMatchCallbacks () {}

        virtual bool match_should_include (const char * function_name) = 0;
    };

    struct ProfileReport : public Object
    {
        virtual String * emit_xml () = 0;
    };
}
#endif //POGODROID_STRUCTS_H
