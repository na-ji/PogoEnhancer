//
//
//

#ifndef POGODROID_IL2CPPSTRUCTS_H
#define POGODROID_IL2CPPSTRUCTS_H


#include "codegen/il2cpp-codegen.h"
#include "object-internals.h"
#include "unordered_set"
#include "../Logger.h"
#include "../ProtoCache.h"
#include "../Util.h"


// System.Int32[] OR System_Int32_array
struct Int32Array  : public Il2CppArray
{
public:
    ALIGN_FIELD (4) int32_t m_Items[1];

};


// System.Byte[]
struct ByteU5BU5D_t3397334013  : public Il2CppArray
{
public:
//    #if __LP64__
#if defined(__arm__)
    ALIGN_FIELD (4) uint8_t m_Items[1];
#elif defined(__aarch64__)
    ALIGN_FIELD (8) uint8_t m_Items[1];
#else
    ALIGN_FIELD (8) uint8_t m_Items[1];
#endif
    //ALIGN_FIELD (4) uint8_t m_Items[1];
//    #else
//    ALIGN_FIELD (4) uint8_t m_Items[1];
//    #endif

};

// System.Byte[]
struct intArrayIl2Cpp  : public Il2CppArray
{
public:
#if defined(__arm__)
    ALIGN_FIELD (4) int32_t m_Items[1];
#elif defined(__arm64__)
    ALIGN_FIELD (8) int32_t m_Items[1];
#else
    ALIGN_FIELD (8) int32_t m_Items[1];
#endif
};

struct  ByteString  : public Il2CppObject
{
    bool leaveOpen;
//    #if __LP64__
#if defined(__arm__)
    ALIGN_FIELD(4) ByteU5BU5D_t3397334013* ___bytes_1;
#elif defined(__arm64__)
    ALIGN_FIELD(8) ByteU5BU5D_t3397334013* ___bytes_1;
#else
    ALIGN_FIELD(8) ByteU5BU5D_t3397334013* ___bytes_1;
#endif
//    #else
    //   ALIGN_FIELD (4) ByteU5BU5D_t3397334013* ___bytes_1;
//    #endif
    int32_t length;

};

struct System_Object_array {
    Il2CppObject obj;
    Il2CppArrayBounds *bounds;
    uintptr_t max_length;
    Il2CppObject* m_Items[65535];
};

struct Il2cppGenericList  : public Il2CppObject {
    System_Object_array* _items;
    int32_t _size;
    int32_t _version;
    Il2CppObject* _syncRoot;

    void* getItem(int index) {
        return _items->m_Items[index];
    }
};

//ClientQuestProto
struct CLQ : public Il2CppObject {
    /*
     private static readonly MessageParser<ClientQuestProto> _parser; // 0x0
	public const int QuestFieldNumber = 1; // 0x0
	private QuestProto quest_; // 0x10
	public const int QuestDisplayFieldNumber = 2; // 0x0
	private QuestDisplayProto questDisplay_; // 0x18
     */
    void* quest;
    void* questDisplay;
};

// QuestListItemData
struct QLID : public Il2CppObject {
    /*
    // Fields
	public readonly QuestDanglingEncounterData danglingEncounterData;
    public readonly DanglingIncidentData danglingIncidentData;
	public readonly ClientQuestProto clientQuestProto;
	public readonly bool emptyChallengeQuest;
	public readonly bool thereAreNoMoreStories;
	public readonly IEnumerable<ClientQuestProto> completedStoryQuests;
     */
    void* danglingEncounterData;
    void* danglingIncidentData;
    CLQ* clientQuestProto;
    bool emptyChallengeQuest;
    bool thereAreNoMoreStories;
    void* completedStoryQuests;
};



struct ByteStringArray : public Il2CppArray
{
#if defined(__arm__)
    ALIGN_FIELD (4) ByteString* m_Items[1];
#elif defined(__arm64__)
    ALIGN_FIELD (8) ByteString* m_Items[1];
#else
    ALIGN_FIELD (8) ByteString* m_Items[1];
#endif
};

struct  RepeatedField : public Il2CppObject
{

    ByteStringArray* ___array_1;
    int32_t ___count_2;

    int32_t getCount() {
        return this->___count_2;
    }

    void* getItem(uint index) {
        System_Object_array* arr = reinterpret_cast<System_Object_array *>(this->___array_1);

        return arr->m_Items[index];
    }
};


// System.Byte[]
struct UInt64ArrayIl2Cpp  : public Il2CppArray
{
public:
    ALIGN_FIELD (8) uint64_t m_Items[1];

    static UInt64ArrayIl2Cpp* create(il2cpp_array_size_t size)
    {
        size_t sizeInt = sizeof(uint64_t); // TODO: Maybe needs to be hardcoded to 8
        int totalSize = sizeof(UInt64ArrayIl2Cpp) + (sizeInt * ((size)));
        Logger::debug("Allocating array of size " + std::to_string(size));
        UInt64ArrayIl2Cpp *myTest = (UInt64ArrayIl2Cpp *)malloc(totalSize);
        myTest->max_length = size;
        return myTest;
    }

    void destroy()
    {
        delete[] (char*)this;
    }
};


struct  RepeatedField_UInt64 : public Il2CppObject
{

    UInt64ArrayIl2Cpp* ___array_1;
    int32_t ___count_2;

    int32_t getCount() {
        return this->___count_2;
    }

    uint64_t getItem(uint index) {
        return this->___array_1->m_Items[index];
    }

    void add(uint64_t item) {
        /* TODO: No idea...
        if ( !(byte_6DE1407 & 1) )
        {
            sub_24D2C5C(56197LL);
            byte_6DE1407 = 1;
        }
         */
        /*
         * if ( !item )
        {
            // Raise nullpointer if item is null...
            return;
        }
*/

        if ( !this->___array_1 ) {
            // Raise nullpointer
            //sub_24FE7B8(0LL);
            Logger::debug("Avoiding nullpointer adding " + std::to_string(item));
            return;
        }
        // Allocate +1 in size array, copy values over...
        Logger::debug("Current max length: " + std::to_string(this->___array_1->max_length));
        UInt64ArrayIl2Cpp* oldValues = this->___array_1;
        this->___array_1 = UInt64ArrayIl2Cpp::create(this->___count_2 + 1);
        for (int i = 0; i <= this->___count_2; i++) {
            this->___array_1->m_Items[i] = oldValues->m_Items[i];
        }
        //this->___array_1->max_length = oldValues->max_length + 1;
        this->___array_1->bounds = oldValues->bounds;
        this->___array_1->klass = oldValues->klass;
        this->___array_1->monitor = oldValues->monitor;

        this->___count_2 = this->___count_2 + 1;
        if ( (unsigned int)this->___count_2 > this->___array_1->max_length)
        {
            // Raise IndexOutOfRangeException
            Logger::debug("Avoiding IndexOutOfRange adding " + std::to_string(item)
                          + " max length: " + std::to_string(this->___array_1->max_length));
            return;

            /*
            v6 = (**(__int64 (__fastcall ***)(Google_Protobuf_Collections_RepeatedField_T__o *, _QWORD))(*(_QWORD *)(*(_QWORD *)(v3 + 24) + 192LL)
                                                                                                         + 160LL))(
                    this,
                            (unsigned int)(this->count + 1));
            v9 = sub_24FF554(v6);
            sub_24FE728(v9, 0LL, 0LL);
             */
        }

        this->___array_1->m_Items[this->___count_2 - 1] = item;
    }
};


struct  Result_t1593651929 : public Il2CppObject
{
public:
    int32_t ___rpcId_0;
    int32_t ___rpcStatus_1;
    RepeatedField * ___rpcPayloads_2;
    int32_t ___actionCount_3;

public:
    //inline static int32_t get_offset_of_rpcId_0() { return static_cast<int32_t>(offsetof(Result_t1593651929, ___rpcId_0)); }
    inline int32_t get_rpcId_0() const { return ___rpcId_0; }
    inline int32_t* get_address_of_rpcId_0() { return &___rpcId_0; }
    inline void set_rpcId_0(int32_t value)
    {
        ___rpcId_0 = value;
    }

    //inline static int32_t get_offset_of_rpcStatus_1() { return static_cast<int32_t>(offsetof(Result_t1593651929, ___rpcStatus_1)); }
    inline int32_t get_rpcStatus_1() const { return ___rpcStatus_1; }
    inline int32_t* get_address_of_rpcStatus_1() { return &___rpcStatus_1; }
    inline void set_rpcStatus_1(int32_t value)
    {
        ___rpcStatus_1 = value;
    }

    //inline static int32_t get_offset_of_rpcPayloads_2() { return static_cast<int32_t>(offsetof(Result_t1593651929, ___rpcPayloads_2)); }
    inline RepeatedField * get_rpcPayloads_2() const { return ___rpcPayloads_2; }
    inline RepeatedField ** get_address_of_rpcPayloads_2() { return &___rpcPayloads_2; }

    //inline static int32_t get_offset_of_actionCount_3() { return static_cast<int32_t>(offsetof(Result_t1593651929, ___actionCount_3)); }
    inline int32_t get_actionCount_3() const { return ___actionCount_3; }
    inline int32_t* get_address_of_actionCount_3() { return &___actionCount_3; }
    inline void set_actionCount_3(int32_t value)
    {
        ___actionCount_3 = value;
    }
};

// System.Int32[]
struct Int32U5BU5D_t3030399641  : public Il2CppArray
{
public:
    ALIGN_FIELD (4) int32_t m_Items[1];

};

struct  IntPtr_t
{
public:
    // System.Void* System.IntPtr::m_value
    void* ___m_value_0;

public:
    inline static int32_t get_offset_of_m_value_0() { return static_cast<int32_t>(offsetof(IntPtr_t, ___m_value_0)); }
    inline void* get_m_value_0() const { return ___m_value_0; }
    inline void** get_address_of_m_value_0() { return &___m_value_0; }
    inline void set_m_value_0(void* value)
    {
        ___m_value_0 = value;
    }
};

struct System_String_o_old : public Il2CppObject {
    int32_t length;
    char16_t chars[60]; // [IL2CPP_ZERO_LEN_ARRAY]

};

struct System_String_o : public Il2CppObject {
    int32_t length;
    char16_t chars[1]; // [IL2CPP_ZERO_LEN_ARRAY]

    static System_String_o* create(size_t length)
    {
        char16_t* buf = new char16_t[sizeof(System_String_o) + length - 1];
        return ::new (buf) System_String_o(length);
    }

    System_String_o(size_t s) : length(s)
            {
            }

    void destroy()
    {
        delete[] (char*)this;
    }
};

/**
 * class HashSet<T>
 */
struct HashSet_Slot_T_Object {
    int32_t hashCode;
    int32_t next;
    Il2CppObject* value;
};

struct HashSet_Slot_Ulong {
    int32_t hashCode;
    int32_t next;
    uint64_t value;
};

struct HashSet_Slot_Ulong_Array : public Il2CppArray {
    HashSet_Slot_Ulong m_Items[1];

};

struct HashSet_Slot_T_Array : public Il2CppArray {
    HashSet_Slot_T_Object m_Items[1];
};

struct HashSet_T : public Il2CppObject
{
    Int32Array* _buckets;
    HashSet_Slot_T_Array* _slots;
    int32_t _count;
    int32_t _lastIndex;
    int32_t _freeList;
    //System_Collections_Generic_IEqualityComparer_T__o* _comparer;
    void* _comparer;    int32_t _version;
    //System_Runtime_Serialization_SerializationInfo_o* _siInfo;
    void* _siInfo;

    int32_t getCount() {
        return this->_count;
    }
};

struct HashSet_Ulong : public Il2CppObject
{

    /**
     * int[] _buckets; // 0x0
     * HashSet_Slot_Ulong[] _slots; // 0x0
     */
    Int32Array* _buckets;
    HashSet_Slot_Ulong_Array* _slots;
    int32_t _count;
    int32_t _lastIndex;
    int32_t _freeList;
    //System_Collections_Generic_IEqualityComparer_T__o* _comparer;
    void* _comparer;
    int32_t _version;
    //System_Runtime_Serialization_SerializationInfo_o* _siInfo;
    void* _siInfo;

    int32_t getCount() {
        return this->_count;
    }

    void remove(uint64_t toBeRemoved) {
        signed int previousSlotIndex; // w28
        int slotIndex; // w26
        if ( !this->_buckets )
            return;
        if ( !this->_slots )
            return;

        HashSet_Slot_Ulong* entryToBeRemoved = getEntry(toBeRemoved);
        if (entryToBeRemoved) {
            Logger::debug("Item with hash " + std::to_string(entryToBeRemoved->hashCode));
        } else {
            Logger::debug("Item not present");
            return;
        }
        if (entryToBeRemoved->hashCode == -1) {
            Logger::debug("Invalid hash, shortcut");
            entryToBeRemoved->value = 0;
            return;
        }
        int bucketIndex = getBucket(toBeRemoved);
        //Logger::debug("BucketIndex: " + std::to_string(bucketIndex));
        if ( bucketIndex >= this->_buckets->max_length )
        {
            Logger::debug("Invalid bucket index");
            // out of bucket range
            return;
        } else if (bucketIndex < 0) {
            Logger::debug("Invalid bucket index (none found)");
            return;
        }
        slotIndex = this->_buckets->m_Items[bucketIndex] - 1;
        if ( slotIndex & 0x80000000 ) {
            //Logger::debug("Invalid initial slot index");
            return;
        }
        previousSlotIndex = -1;
        // Iterate the bucket determined by hashCode
        while ( 1 )
        {
            // Iterate the content of the bucket or break up accordingly
            if ( (unsigned int)slotIndex >= this->_slots->max_length )
            {
                // out of slot range
                //Logger::debug("Out of range 1");
                return;
            } else if ( slotIndex >= this->_slots->max_length )
            {
                // Out of range
                //Logger::debug("Out of range 2");
                return;
            } else if ( this->_slots->m_Items[slotIndex].value == toBeRemoved )
            {
                Logger::debug("Found");
                break;
            }
            previousSlotIndex = slotIndex;
            slotIndex = this->_slots->m_Items[slotIndex].next;
            if ( slotIndex & 0x80000000 )
            {
                Logger::debug("Reached end of bucket without hit");
                return;
            }
        }
        if ( previousSlotIndex & 0x80000000 )
        {
            if ( (unsigned int)slotIndex >= this->_slots->max_length )
            {
                //out of slot range
                //Logger::debug("Out of range 3");
                return;
            } else if ( (unsigned int)bucketIndex >= this->_buckets->max_length )
            {
                //Logger::debug("Out of range 4");
                //out of bucket range
                return;
            }
            //Logger::debug("Adjusting bucket ref");
            this->_buckets->m_Items[bucketIndex] = this->_slots->m_Items[slotIndex].next + 1;
        }
        else
        {
            if ( (unsigned int)slotIndex >= this->_slots->max_length )
            {
                //Logger::debug("Out of range 5");
                //out of slot range
                return;
            } else if ( (unsigned int)previousSlotIndex >= this->_slots->max_length )
            {
                //Logger::debug("Out of range 6");
                //out of slot range
                return;
            }
            //Logger::debug("Adjust next ptr");
            this->_slots->m_Items[previousSlotIndex].next = this->_slots->m_Items[slotIndex].next;
        }

        if ( (unsigned int)slotIndex >= this->_slots->max_length )
        {
            //Logger::debug("Out of range 7");
            //out of slot range
            return;
        }
        this->_slots->m_Items[slotIndex].hashCode = -1;

        if ( (unsigned int)slotIndex >= this->_slots->max_length )
        {
            //Logger::debug("Out of range 8");
            //out of slot range
            return;
        }
        this->_slots->m_Items[slotIndex].value = 0;

        if ( (unsigned int)slotIndex >= this->_slots->max_length )
        {
            //Logger::debug("Out of range 9");
            //out of slot range
            return;
        }
        this->_slots->m_Items[slotIndex].next = this->_freeList;
        this->_count -= 1;
        this->_version += 1;
        Logger::debug("Done deleting. Count: " + std::to_string(this->_count)
                      + " version: " + std::to_string(this->_version));
        if ( this->_count ) {
            this->_freeList = slotIndex;
        }
        else {
            //Logger::debug("resetting lastIndex");
            this->_lastIndex = 0;
        }
    }

    std::vector<uint64_t> getPlain() {
        //Logger::debug("Iterating with last index " + std::to_string(_lastIndex));
        std::vector<uint64_t> allCellIds;
        if (!this->_buckets || !this->_slots) {
            return allCellIds;
        }
        //Logger::debug("Iterating with max length " + std::to_string(this->_slots->max_length));

        for (int slotIndex = 0; slotIndex < this->_slots->max_length; slotIndex++) {
            //Logger::debug("Slot: " + std::to_string(slotIndex + 1));
            int value = this->_slots->m_Items[slotIndex].value;
            //Logger::debug("Value: " + std::to_string(this->_slots->m_Items[slotIndex].value));
            if (value == 0) {
                continue;
            }
            allCellIds.push_back(this->_slots->m_Items[slotIndex].value);
        }
        //Logger::debug("Done iterating with max length " + std::to_string(this->_slots->max_length));
        return allCellIds;
    }

    HashSet_Slot_Ulong* getEntry(uint64_t toBeSearched) {
        std::vector<uint64_t> allCellIds;
        if (!this->_buckets || !this->_slots) {
            return nullptr;
        }
        for (int slotIndex = 0; slotIndex < this->_slots->max_length; slotIndex++) {
            if (this->_slots->m_Items[slotIndex].value == toBeSearched) {
                return &this->_slots->m_Items[slotIndex];
            }
        }
        return nullptr;
    }

    int getBucket(uint64_t toBeSearched) {
        if (!this->_buckets || !this->_slots) {
            return -1;
        }
        // Slots contains all elements
        // Iterate all bucket entries until we find the element we are searching for at slots[i] or a given predecessor?
        HashSet_Slot_Ulong* entryToBeSearchedFor = getEntry(toBeSearched);
        if (entryToBeSearchedFor == nullptr) {
            return -1;
        }
        for (int bucketIndex = 0; bucketIndex < this->_buckets->max_length; bucketIndex++) {
            int slotIndex = this->_buckets->m_Items[bucketIndex] - 1;
            //Logger::debug("Bucket " + std::to_string(bucketIndex) + " with value " +
             //             std::to_string(this->_buckets->m_Items[bucketIndex]));
            if (slotIndex & 0x80000000 || slotIndex >= this->_slots->max_length) {
                // Empty bucket
                //Logger::debug("Empty bucket");
                continue;
            }
            // at least one item, iterate them (via .next)
            int previousSlotIndex = -1;
            while (true) {
                if ((slotIndex & 0x80000000) || slotIndex >= this->_slots->max_length )
                {
                    //Logger::debug("Done with bucket");
                    // Out of range
                    break;
                }
                //Logger::debug("Item " + std::to_string(this->_slots->m_Items[slotIndex].value));
                if ( this->_slots->m_Items[slotIndex].value == toBeSearched )
                {
                    //Logger::debug("Found item in bucket");
                    return bucketIndex;
                }

                //Logger::debug("Checking next item in bucket");
                previousSlotIndex = slotIndex;
                slotIndex = this->_slots->m_Items[slotIndex].next;
            }
        }
        return -1;
    }

    void replace(std::unordered_set<uint64_t> toReplace, uint64_t replaceWith) {
        if (!this->_buckets || !this->_slots) {
            return;
        }
        //Logger::debug("Iterating for replacement with max length " + std::to_string(this->_slots->max_length));

        for (int slotIndex = 0; slotIndex < this->_lastIndex; slotIndex++) {
            //Logger::debug("Slot: " + std::to_string(slotIndex + 1));
            uint64_t value = this->_slots->m_Items[slotIndex].value;
            //Logger::debug("Value: " + std::to_string(value));
            if (toReplace.count(value) > 0) {
                //Logger::debug("Replacing value: " + std::to_string(value));
                this->_slots->m_Items[slotIndex].value = replaceWith;
            }
        }
        //Logger::debug("Done iterating for replacement with max length " + std::to_string(this->_slots->max_length));
    }
};

/**
 * End of HashSet
 */

/**
 * End of HashSet
 */

struct UnknownField : public Il2CppObject {
    /**
     internal sealed class UnknownField // TypeDefIndex: 30604
    {
	// Fields
	private List<ulong> varintList; // 0x10
	private List<uint> fixed32List; // 0x18
	private List<ulong> fixed64List; // 0x20
	private List<ByteString> lengthDelimitedList; // 0x28
	private List<UnknownFieldSet> groupList; // 0x30
     */
    void* variantList;
    void* fixed32List;
    void* fixed64List;
    void* lengthDelimitedList;
    void* groupList;
};

struct UnknownFieldSet : public Il2CppObject {
    /**
     * public sealed class UnknownFieldSet // TypeDefIndex: 29403
{
	// Fields
	private readonly IDictionary<int, UnknownField> fields; // 0x10
	private int lastFieldNumber; // 0x18
	private UnknownField lastField; // 0x20
     */
    void* fields;
    int32_t lastFieldNumber;
    UnknownField* lastField;
};

//EncounterProto
struct EncP : public Il2CppObject {
    /*
	private static readonly MessageParser<EncounterProto> _parser; // 0x0
	private ulong encounterId_; // 0x10
	private string spawnpointId_; // 0x18
	private double playerLatDegrees_; // 0x20
	private double playerLngDegrees_; // 0x28
     */
    void* UnknownFieldSet;
    uint64_t encounterId_;
    void* spawnpointId_;
    double playerLatDegrees_;
    double playerLngDegrees_;
};


//IncenseEncounterOutProto
struct IncEncOutP : public Il2CppObject {
    /*
	// Fields
	private UnknownFieldSet _unknownFields; // 0x10
	private IncenseEncounterOutProto.Types.Result result_; // 0x18
	private PokemonProto pokemon_; // 0x20
	private CaptureProbabilityProto captureProbability_; // 0x28
	private Item activeItem_; // 0x30
	private int arplusAttemptsUntilFlee_; // 0x34
     */
    void* UnknownFieldSet;
    int32_t * result_;
    void* pokemon_;
    void* captureProbability_;
    int32_t activeItem_;
    int32_t arplusAttemptsUntilFlee_;
};

// AdRequestDeviceInfo
struct AdReqDevInf : public Il2CppObject {
    /*
    private AdRequestDeviceInfo.Types.OperatingSystem operatingSystem_; // 0x10
	private string deviceModel_; // 0x18
	private string carrier_; // 0x20
     */
    UnknownFieldSet* _unknownFields;
    int32_t os; // just set to 	public const AdRequestDeviceInfo.Types.OperatingSystem PLATFORM_ANDROID = 1; // 0x0
    void* deviceModel;
    void* carrier;

};

// AdTargetingInfoProto
struct AdTargIP : public Il2CppObject {
    UnknownFieldSet* _unknownFields;
    AdReqDevInf* deviceInfo_;
};

//FortSearchProto
struct FortSP : public Il2CppObject {
    /*
	private string id_; // 0x10
	private double playerLatDegrees_; // 0x18
	private double playerLngDegrees_; // 0x20
	private double fortLatDegrees_; // 0x28
	private double fortLngDegrees_; // 0x30
	private AdTargetingInfoProto adTargetingInfo_; // 0x38
	private bool isPlayerEligibleForGeotargetedQuest_; // 0x40
     */
    UnknownFieldSet* _unknownFields;
    void* id_;
    double playerLat;
    double playerLng;
    double fortLat;
    double fortLng;
    void* adTargetInfo;
    bool geotarget;
};

enum FortType {
    GYM = 0, // 0x0
    CHECKPOINT = 1 // 0x0
};

// PokemonFortProto
struct FortP : public Il2CppObject {
    /*
    private string fortId_; // 0x10
	private int64_t lastModifiedMs_; // 0x18
	private double latitude_; // 0x20
	private double longitude_; // 0x28
	private int32_t team_; // 0x30
	private int32_t guardPokemonId_; // 0x34
	private int32_t guardPokemonLevel_; // 0x38
	private bool enabled_; // 0x3C
	private FortType fortType_; // 0x40
	private int64_t gymPoints_; // 0x48
	private bool isInBattle_; // 0x50
	private static readonly FieldCodec<Item> _repeated_activeFortModifier_codec; // 0x8
	private readonly RepeatedField<Item> activeFortModifier_; // 0x58
	private MapPokemonProto activePokemon_; // 0x60
	private int64_t cooldownCompleteMs_; // 0x68
	private FortSponsor.Types.Sponsor sponsor_; // 0x70
	private FortRenderingType.Types.RenderingType renderingType_; // 0x74
	private int64_t deployLockoutEndMs_; // 0x78
	private PokemonDisplayProto guardPokemonDisplay_; // 0x80
	private bool closed_; // 0x88
	private RaidInfoProto raidInfo_; // 0x90
	private GymDisplayProto gymDisplay_; // 0x98
	private bool visited_; // 0xA0
	private int64_t sameTeamDeployLockoutEndMs_; // 0xA8
	private bool allowCheckin_; // 0xB0
	private string imageUrl_; // 0xB8
	private bool inEvent_; // 0xC0
	private string bannerUrl_; // 0xC8
	private string partnerId_; // 0xD0
	private bool challengeQuestCompleted_; // 0xD8
	private bool isExRaidEligible_; // 0xD9
	private PokestopIncidentDisplayProto pokestopDisplay_; // 0xE0
	private static readonly FieldCodec<PokestopIncidentDisplayProto> _repeated_pokestopDisplays_codec; // 0x10
	private readonly RepeatedField<PokestopIncidentDisplayProto> pokestopDisplays_; // 0xE8
	private bool isArScanEligible_; // 0xF0
	private string geostoreTombstoneMessageKey_; // 0xF8
	private string geostoreSuspensionMessageKey_; // 0x100
    private int32_t CNAGBECENPI; // 0x108
	private int64_t DHGPEHIMNDO; // 0x110
	private int64_t GNKHAEGOENB; // 0x118
	private int64_t EABAPCJCIAD; // 0x120
	private readonly RepeatedField<NEDLIPKOOJM> AADJCLJANAE; // 0x128
     */
    UnknownFieldSet* _unknownFields;
    System_String_o* fortId_; // 0x10
    int64_t lastModifiedMs_; // 0x18
    double latitude_; // 0x20
    double longitude_; // 0x28
    int32_t team_; // 0x30
    int32_t guardPokemonId_; // 0x34
    int32_t guardPokemonLevel_; // 0x38
    bool enabled_; // 0x3C
    FortType fortType_; // 0x40
    int64_t gymPoints_; // 0x48
    bool isInBattle_; // 0x50
    RepeatedField* activeFortModifier_; // 0x58
    void* activePokemon_; // 0x60
    int64_t cooldownCompleteMs_; // 0x68
    int32_t sponsor_; // 0x70
    int32_t renderingType_; // 0x74
    int64_t deployLockoutEndMs_; // 0x78
    void* guardPokemonDisplay_; // 0x80
    bool closed_; // 0x88
    void* raidInfo_; // 0x90
    void* gymDisplay_; // 0x98
    bool visited_; // 0xA0
    int64_t sameTeamDeployLockoutEndMs_; // 0xA8
    bool allowCheckin_; // 0xB0
    System_String_o* imageUrl_; // 0xB8
    bool inEvent_; // 0xC0
    System_String_o* bannerUrl_; // 0xC8
    System_String_o* partnerId_; // 0xD0
    bool challengeQuestCompleted_; // 0xD8
    bool isExRaidEligible_; // 0xD9
    void* pokestopDisplay_; // 0xE0
    RepeatedField* pokestopDisplays_; // 0xE8
    bool isArScanEligible_; // 0xF0
    System_String_o* geostoreTombstoneMessageKey_; // 0xF8
    System_String_o* geostoreSuspensionMessageKey_; // 0x100
    int32_t CNAGBECENPI; // 0x108
    int64_t DHGPEHIMNDO; // 0x110
    int64_t GNKHAEGOENB; // 0x118
    int64_t EABAPCJCIAD; // 0x120
    RepeatedField* AADJCLJANAE; // 0x128

    double getDistance(const LatLng &to) {
        LatLng locationOfFort = LatLng();
        locationOfFort.Latitude = this->latitude_;
        locationOfFort.Longitude = this->longitude_;
        return Util::getDistanceInMeters(locationOfFort, to);
    }

    static bool compareByDistance(FortP& lhs, FortP& rhs, LatLng &to) {
        return lhs.getDistance(to) < rhs.getDistance(to);
    }
};

struct FortDistanceLessThan
{
private:
    LatLng currentLatLng;

public:
    FortDistanceLessThan(LatLng currentPosition)
    {
        this->currentLatLng = currentPosition;
    }

    inline bool operator()(FortP left, FortP right) const
    {
        return left.getDistance(currentLatLng) < right.getDistance(currentLatLng);
    }
};



struct CellP : public Il2CppObject {
    /*
	private uint64_t s2CellId_; // 0x8
	private int64_t asOfTimeMs_; // 0x10
	private readonly RepeatedField<PokemonFortProto> fort_; // 0x18
	private readonly RepeatedField<ClientSpawnPointProto> spawnPoint_; // 0x1C
	private readonly RepeatedField<WildPokemonProto> wildPokemon_; // 0x20
	private readonly RepeatedField<string> deletedObject_; // 0x24
	private bool isTruncatedList_; // 0x28
	private readonly RepeatedField<PokemonSummaryFortProto> fortSummary_; // 0x2C
	private readonly RepeatedField<ClientSpawnPointProto> decimatedSpawnPoint_; // 0x30
	private readonly RepeatedField<MapPokemonProto> catchablePokemon_; // 0x34
	private readonly RepeatedField<NearbyPokemonProto> nearbyPokemon_; // 0x38
     */
    UnknownFieldSet* _unknownFields;
    uint64_t s2CellId_; // 0x8
    int64_t asOfTimeMs_; // 0x10
    RepeatedField* fort_; // 0x18
    RepeatedField* spawnPoint_; // 0x1C
    RepeatedField* wildPokemon_; // 0x20
    RepeatedField* deletedObject_; // 0x24
    bool isTruncatedList_; // 0x28
    RepeatedField* fortSummary_; // 0x2C
    RepeatedField* decimatedSpawnPoint_; // 0x30
    RepeatedField* catchablePokemon_; // 0x34
    RepeatedField* nearbyPokemon_; // 0x38
    System_String_o* someString;

};

enum GnqoPStatus {
    UNSET = 0,
    SUCCESS = 1,
    ERROR_INVALID_DISPLAY = 2
};

struct GnqoP : public Il2CppObject {
    /**
     * GetNewQuestsOutProto
     */
     /*
	private GetNewQuestsOutProto.Types.Status status_; // 0x10
    private readonly RepeatedField<ClientQuestProto> quests_; // 0x18
    private readonly RepeatedField<ClientQuestProto> versionChangedQuests_; // 0x20
      */
     UnknownFieldSet* _unknownFields;
    GnqoPStatus status_;
     RepeatedField* quests_;
     RepeatedField* versionChangedQuests_;
};

struct RqP : public Il2CppObject {
    /** RemoveQuestProto
     	private string questId_; // 0x10

     */
    UnknownFieldSet* _unknownFields;
    System_String_o* questId;

};

struct pPp : public Il2CppObject {
    /**
     // Fields
	public string avatarModel; // 0x10
	private static readonly MessageParser<PlayerAvatarProto> _parser; // 0x0
	public const int AvatarFieldNumber = 8; // 0x0
	private int avatar_; // 0x18
	public const int SkinFieldNumber = 2; // 0x0
	private int skin_; // 0x1C
	public const int HairFieldNumber = 3; // 0x0
	private int hair_; // 0x20
	public const int ShirtFieldNumber = 4; // 0x0
	private int shirt_; // 0x24
	public const int PantsFieldNumber = 5; // 0x0
	private int pants_; // 0x28
	public const int HatFieldNumber = 6; // 0x0
	private int hat_; // 0x2C
	public const int ShoesFieldNumber = 7; // 0x0
	private int shoes_; // 0x30
	public const int EyesFieldNumber = 9; // 0x0
	private int eyes_; // 0x34
	public const int BackpackFieldNumber = 10; // 0x0
	private int backpack_; // 0x38
	public const int AvatarHairFieldNumber = 11; // 0x0
	private string avatarHair_; // 0x40
	public const int AvatarShirtFieldNumber = 12; // 0x0
	private string avatarShirt_; // 0x48
	public const int AvatarPantsFieldNumber = 13; // 0x0
	private string avatarPants_; // 0x50
	public const int AvatarHatFieldNumber = 14; // 0x0
	private string avatarHat_; // 0x58
	public const int AvatarShoesFieldNumber = 15; // 0x0
	private string avatarShoes_; // 0x60
	public const int AvatarEyesFieldNumber = 16; // 0x0
	private string avatarEyes_; // 0x68
	public const int AvatarBackpackFieldNumber = 17; // 0x0
	private string avatarBackpack_; // 0x70
	public const int AvatarGlovesFieldNumber = 18; // 0x0
	private string avatarGloves_; // 0x78
	public const int AvatarSocksFieldNumber = 19; // 0x0
	private string avatarSocks_; // 0x80
	public const int AvatarBeltFieldNumber = 20; // 0x0
	private string avatarBelt_; // 0x88
	public const int AvatarGlassesFieldNumber = 21; // 0x0
	private string avatarGlasses_; // 0x90
	public const int AvatarNecklaceFieldNumber = 22; // 0x0
	private string avatarNecklace_; // 0x98
	public const int AvatarSkinFieldNumber = 23; // 0x0
	private string avatarSkin_; // 0xA0
	public const int AvatarPoseFieldNumber = 24; // 0x0
	private string avatarPose_; // 0xA8
	public const int AvatarFaceFieldNumber = 25; // 0x0
	private string avatarFace_; // 0xB0
     */

    int32_t avatar_;
    int32_t skin_;
    int32_t hair_;
    int32_t shirt_;


};

struct sAp : public Il2CppObject {
    /**
     * // Fields
	private static readonly MessageParser<SetAvatarProto> _parser; // 0x0
	public const int PlayerAvatarProtoFieldNumber = 2; // 0x0
	private PlayerAvatarProto playerAvatarProto_; // 0x10
     */

    pPp* PlayerAvatarProto;


};

struct cPp : public Il2CppObject {
    /*
    public sealed class CatchPokemonProto : IMessage<CatchPokemonProto>, IMessage, IEquatable<CatchPokemonProto>, IDeepCloneable<CatchPokemonProto> // TypeDefIndex: 10123
	// Fields
	private static readonly MessageParser<CatchPokemonProto> _parser; // 0x0
	public const int32_t EncounterIdFieldNumber = 1; // 0x0
	private uint64_t encounterId_; // 0x10
	public const int32_t PokeballFieldNumber = 2; // 0x0
	private int32_t pokeball_; // 0x18
	public const int32_t NormalizedReticleSizeFieldNumber = 3; // 0x0
	private double normalizedReticleSize_; // 0x20
	public const int32_t SpawnPointGuidFieldNumber = 4; // 0x0
	private string spawnPointGuid_; // 0x28
	public const int32_t HitPokemonFieldNumber = 5; // 0x0
	private bool hitPokemon_; // 0x30
	public const int32_t SpinModifierFieldNumber = 6; // 0x0
	private double spinModifier_; // 0x38
	public const int32_t NormalizedHitPositionFieldNumber = 7; // 0x0
	private double normalizedHitPosition_; // 0x40
	public const int32_t ArPlusValuesFieldNumber = 8; // 0x0
	private ARPlusEncounterValuesProto arPlusValues_; // 0x48
*/
    UnknownFieldSet* _unknownFields;
    uint64_t encounterId_;
    int32_t pokeball_;
    double normalizedReticleSize_;
    System_String_o* spawnPointGuid_;
    bool hitPokemon_;
    double spinModifier_;
    double normalizedHitPosition_;
    void* arPlusValues_;

};

struct uiXPb : public Il2CppObject {
    /*
    public sealed class UseItemXpBoostProto : IMessage<UseItemXpBoostProto>, IMessage, IEquatable<UseItemXpBoostProto>, IDeepCloneable<UseItemXpBoostProto> // TypeDefIndex: 12438
    {
    // Fields
    private static readonly MessageParser<UseItemXpBoostProto> _parser; // 0x0
    public const int32_t ItemFieldNumber = 1;
    private Item item_; // 0x10
    };

     */
    UnknownFieldSet* _unknownFields;
    int32_t item_;
};

struct fDpb: public Il2CppObject {
    /*
     * public sealed class FortDetailsProto : IMessage<FortDetailsProto>, IMessage, IEquatable<FortDetailsProto>, IDeepCloneable<FortDetailsProto> // TypeDefIndex: 14622
{
	// Fields
	private static readonly MessageParser<FortDetailsProto> _parser; // 0x0
	public const int32_t IdFieldNumber = 1;
	private string id_; // 0x10
	public const int32_t LatitudeFieldNumber = 2;
	private double latitude_; // 0x18
	public const int32_t LongitudeFieldNumber = 3;
	private double longitude_; // 0x20
     */
    UnknownFieldSet* _unknownFields;
    System_String_o* fortId_; // 0x10
    double latitude_; // 0x18
    double longitude_; // 0x28

};

struct uIcP : public Il2CppObject {
    /*
     * public sealed class UseItemCaptureProto : IMessage<UseItemCaptureProto>, IMessage, IEquatable<UseItemCaptureProto>, IDeepCloneable<UseItemCaptureProto> // TypeDefIndex: 10132
{
	// Fields
	private static readonly MessageParser<UseItemCaptureProto> _parser; // 0x0
	public const int ItemFieldNumber = 1; // 0x0
	private Item item_; // 0x10
	public const int EncounterIdFieldNumber = 2; // 0x0
	private ulong encounterId_; // 0x18
	public const int SpawnPointGuidFieldNumber = 3; // 0x0
	private string spawnPointGuid_; // 0x20
     */
    UnknownFieldSet* _unknownFields;
    int item_;
    unsigned long long encounterId_;
    System_String_o* spawnPointGuid_;

};

struct ePpb : public Il2CppObject {
    /*
     * public sealed class EvolvePokemonProto : IMessage<EvolvePokemonProto>, IMessage, IEquatable<EvolvePokemonProto>, IDeepCloneable<EvolvePokemonProto> // TypeDefIndex: 12499
{
	// Fields
	private static readonly MessageParser<EvolvePokemonProto> _parser; // 0x0
	public const int PokemonIdFieldNumber = 1;
	private ulong pokemonId_; // 0x10
	public const int EvolutionItemRequirementFieldNumber = 2;
	private Item evolutionItemRequirement_; // 0x18
	public const int TargetPokemonIdFieldNumber = 3;
	private HoloPokemonId targetPokemonId_; // 0x1C
	public const int TargetPokemonFormFieldNumber = 4;
	private PokemonDisplayProto.Types.Form targetPokemonForm_; // 0x20
	public const int UseSpecialFieldNumber = 5;
	private bool useSpecial_; // 0x24
	public const int FJOLFDPIEMJ = 6;
	private bool JCFABHAOHDF; // 0x25
     */
    UnknownFieldSet* _unknownFields;
    unsigned long long pokemonId_;
    int evolutionItemRequirement_;
    int targetPokemonId_;
    int targetPokemonForm_;
    bool useSpecial_;
    bool JCFABHAOHDF;

};

struct ePbb : public Il2CppObject {
    /*
     * public sealed class EvolutionBranchProto : IMessage<EvolutionBranchProto>, IMessage, IEquatable<EvolutionBranchProto>, IDeepCloneable<EvolutionBranchProto> // TypeDefIndex: 12599
{
	// Fields
    private HoloPokemonId evolution_; // 0x10
	private Item evolutionItemRequirement_; // 0x14
	private int candyCost_; // 0x18
	private float kmBuddyDistanceRequirement_; // 0x1C
	private PokemonDisplayProto.Types.Form form_; // 0x20
	private BelugaPokemonProto.Types.PokemonGender genderRequirement_; // 0x24
	private Item lureItemRequirement_; // 0x28
	private bool mustBeBuddy_; // 0x2C
	private bool onlyDaytime_; // 0x2D
	private bool onlyNighttime_; // 0x2E
	private int priority_; // 0x30
	private bool noCandyCostViaTrade_; // 0x34
	private HoloTemporaryEvolutionId temporaryEvolution_; // 0x38
	private int temporaryEvolutionEnergyCost_; // 0x3C
	private int temporaryEvolutionEnergyCostSubsequent_; // 0x40
	private static readonly FieldCodec<PIGNJILICFG> _repeated_questDisplay_codec; // 0x8
	private readonly RepeatedField<PIGNJILICFG> questDisplay_; // 0x48
	private bool JHKGGHCMHOP; // 0x50
	private int OJAKCJNPHOB; // 0x54
     */
    UnknownFieldSet* _unknownFields;
    int evolution_;
    int evolutionItemRequirement_;
    int candyCost_;
    double kmBuddyDistanceRequirement_;
    int form_;
    int genderRequirement_;
    int lureItemRequirement_;
    bool mustBeBuddy_;
    bool onlyDaytime_;
    bool onlyNighttime_;
    int priority_;
    bool noCandyCostViaTrade_;
    int temporaryEvolution_;
    int temporaryEvolutionEnergyCost_;
    int temporaryEvolutionEnergyCostSubsequent_;
    RepeatedField *questDisplay_;
    bool JHKGGHCMHOP;
    int OJAKCJNPHOB;
};

struct gMob : public Il2CppObject {
/*
public sealed class GetMapObjectsProto : IMessage<GetMapObjectsProto>, IMessage, IEquatable<GetMapObjectsProto>, IDeepCloneable<GetMapObjectsProto> // TypeDefIndex: 13080
{
    // Fields
private static readonly MessageParser<GetMapObjectsProto> _parser; // 0x0
public const int CellIdFieldNumber = 1;
private static readonly FieldCodec<ulong> _repeated_cellId_codec; // 0x8
private readonly RepeatedField<ulong> cellId_; // 0x10
public const int SinceTimeMsFieldNumber = 2;
private static readonly FieldCodec<long> _repeated_sinceTimeMs_codec; // 0x10
private readonly RepeatedField<long> sinceTimeMs_; // 0x18
public const int PlayerLatFieldNumber = 3;
private double playerLat_; // 0x20
public const int PlayerLngFieldNumber = 4;
private double playerLng_; // 0x28
 */
    UnknownFieldSet* _unknownFields;
    RepeatedField *cellId_;
    RepeatedField *sinceTimeMs_;
    double playerLat_;
    double playerLng_;


};

struct cCcb : public Il2CppObject {
    /*
     * public sealed class CreateCombatChallengeProto : IMessage<CreateCombatChallengeProto>, IMessage, IEquatable<CreateCombatChallengeProto>, IDeepCloneable<CreateCombatChallengeProto> // TypeDefIndex: 13836
{
	// Fields
	private static readonly MessageParser<CreateCombatChallengeProto> _parser; // 0x0
	public const int ChallengeIdFieldNumber = 1;
	private string challengeId_; // 0x10
     */
    UnknownFieldSet* _unknownFields;
    System_String_o_old* challengeId_;

};

struct oCcPb : public Il2CppObject {
    /*
     * public sealed class OpenCombatChallengeProto : IMessage<OpenCombatChallengeProto>, IMessage, IEquatable<OpenCombatChallengeProto>, IDeepCloneable<OpenCombatChallengeProto> // TypeDefIndex: 14147
{
	// Fields
	private static readonly MessageParser<OpenCombatChallengeProto> _parser; // 0x0
	public const int TypeFieldNumber = 1;
	private CombatType type_; // 0x10
	public const int ChallengeIdFieldNumber = 2;
	private string challengeId_; // 0x18
	public const int CombatLeagueTemplateIdFieldNumber = 3;
	private string combatLeagueTemplateId_; // 0x20
	public const int OpponentPlayerIdFieldNumber = 4;
	private string opponentPlayerId_; // 0x28
	public const int AttackingPokemonIdFieldNumber = 5;
	private static readonly FieldCodec<ulong> _repeated_attackingPokemonId_codec; // 0x8
	private readonly RepeatedField<ulong> attackingPokemonId_; // 0x30
     */
    UnknownFieldSet* _unknownFields;
    int type_;
    System_String_o* challengeId_;
    System_String_o* combatLeagueTemplateId_;
    System_String_o* opponentPlayerId_;
    RepeatedField *attackingPokemonId_;

};

struct gCcPd : public Il2CppObject {

};

struct cQpd : public Il2CppObject {
    /*
     * public sealed class ClientQuestProto : IMessage<ClientQuestProto>, IMessage, IEquatable<ClientQuestProto>, IDeepCloneable<ClientQuestProto> // TypeDefIndex: 11079
{
	// Fields
	private static readonly MessageParser<ClientQuestProto> _parser; // 0x0
	public const int QuestFieldNumber = 1; // 0x0
	private QuestProto quest_; // 0x10
	public const int QuestDisplayFieldNumber = 2; // 0x0
	private QuestDisplayProto questDisplay_; // 0x18
     */
    UnknownFieldSet* _unknownFields;
    void* quest_;
    void *questDisplay;


};

struct rQpd : public Il2CppObject {
    /*
     * public sealed class RemoveQuestProto : IMessage<RemoveQuestProto>, IMessage, IEquatable<RemoveQuestProto>, IDeepCloneable<RemoveQuestProto> // TypeDefIndex: 11763
{
	// Fields
	private static readonly MessageParser<RemoveQuestProto> _parser; // 0x0
	public const int QuestIdFieldNumber = 1; // 0x0
	private string questId_; // 0x10
     */
    UnknownFieldSet* _unknownFields;
    System_String_o* questId_;
};

struct tGcd : public Il2CppObject {
    /*
     * public sealed class GetTimedGroupChallengeProto : IMessage<GetTimedGroupChallengeProto>, IMessage, IEquatable<GetTimedGroupChallengeProto>, IDeepCloneable<GetTimedGroupChallengeProto> // TypeDefIndex: 14500
{
	// Fields
	private static readonly MessageParser<GetTimedGroupChallengeProto> _parser; // 0x0
	public const int ChallengeIdFieldNumber = 1;
	private string challengeId_; // 0x10
	public const int ActiveCityHashFieldNumber = 2;
	private string activeCityHash_; // 0x18

     */
    UnknownFieldSet* _unknownFields;
    System_String_o* challengeId_;
    System_String_o* activeCityHash_;
};


struct gRp : public Il2CppObject {
    /**
     * GetRoutesProto first found in 0.277.3 as MUXTRXTW[QW
     */
    /* Fields
     * private static readonly MessageParser<MUXTRXTW[QW> QTYOTOO]XXW; // 0x0
     * public const int QRRWXPWWPXU = 1;
     * private static readonly FieldCodec<ulong> WW]XZNMQY]O; // 0x8
     * private readonly RepeatedField<ulong> TP]VVURQYZV; // 0x10
     */
    UnknownFieldSet* _unknownFields;
    RepeatedField_UInt64* cellIdsToRequest;
    uint32_t requestVersion_;


};


struct gMoP : public Il2CppObject {
    /**
     * GetMapObjectsProto (i.e., the request for the GMO
     */
    /* Fields
     *
	private static readonly MessageParser<GetMapObjectsProto> _parser; // 0x0
	public const int CellIdFieldNumber = 1; // 0x0
	private static readonly FieldCodec<ulong> _repeated_cellId_codec; // 0x8
	private readonly RepeatedField<ulong> cellId_; // 0x10
	public const int SinceTimeMsFieldNumber = 2; // 0x0
	private static readonly FieldCodec<long> _repeated_sinceTimeMs_codec; // 0x10
	private readonly RepeatedField<long> sinceTimeMs_; // 0x18
	public const int PlayerLatFieldNumber = 3; // 0x0
	private double playerLat_; // 0x20
	public const int PlayerLngFieldNumber = 4; // 0x0
	private double playerLng_; // 0x28

     //new

     // Fields
	private static readonly MessageParser<GetMapObjectsProto> _parser; // 0x0
	private UnknownFieldSet _unknownFields; // 0x10
	[SerializeField]
	private static readonly FieldCodec<ulong> _repeated_cellId_codec; // 0x8
	private readonly RepeatedField<ulong> cellId_; // 0x18
	[SerializeField]
	private static readonly FieldCodec<long> _repeated_sinceTimeMs_codec; // 0x10
	private readonly RepeatedField<long> sinceTimeMs_; // 0x20
	[SerializeField]
	private double playerLat_; // 0x28
	[SerializeField]
	private double playerLng_; // 0x30
     */
    UnknownFieldSet* _unknownFields;
    RepeatedField_UInt64* cellId_;
    RepeatedField_UInt64* sinceTimeMs_;
    double playerLat_;
    double playerLng_;
};

enum RpcStatus : uint32_t // TypeDefIndex: 32785
{
    // Fields
    Undefined = 0,
    Success = 1,
    BadResponse = 3,
    ActionError = 4,
    DispatchError = 5,
    ServerError = 6,
    AssignmentError = 7,
    ProtocolError = 8,
    AuthenticationError = 9,
    CancelledRequest = 10,
    UnknownError = 11,
    NoRetriesError = 12,
    UnauthorizedError = 13,
    ParsingError = 14,
    AccessDenied = 15,
    AccessSuspended = 16,
    DeviceIncompatible = 17,
    AccessRateLimited = 18,
    GooglePlayNotReady = 19,
    LoginErrorBail = 20,
};

struct SegmentedBufferHelper // TypeDefIndex: 29400
{
    // Fields
    int32_t totalLength; // 0x0
    void* readOnlySequenceEnumerator; // 0x8
    void* codedInputStream; // 0x40
};

struct ParserInternalState // TypeDefIndex: 29396
{
    // Fields
    int32_t bufferPos; // 0x0
    int32_t bufferSize; // 0x4
    int32_t bufferSizeAfterLimit; // 0x8
    int32_t currentLimit; // 0xC
    int32_t totalBytesRetired; // 0x10
    int32_t recursionDepth; // 0x14
    SegmentedBufferHelper segmentedBufferHelper; // 0x18
    uint32_t lastTag; // 0x60
    uint32_t nextTag; // 0x64
    bool hasNextTag; // 0x68
    int32_t sizeLimit; // 0x6C
    int32_t recursionLimit; // 0x70
    bool discardUnknownFields; // 0x74
    void* extensionRegistry; // 0x78
};

struct InternalCodedInputStream : public Il2CppObject {
    bool leaveOpen; // 0x10
    ALIGN_FIELD(8) ByteU5BU5D_t3397334013* buffer;
    void* input;
    ParserInternalState state; // 0x28
};

struct ActionResponse : public Il2CppObject {
    uint32_t rpcId; // 0x10
    RpcStatus rpcStatus;
    // CodedInputStream <CodedInputStream>k__BackingField; // 0x18
    InternalCodedInputStream* codedInputStream;
};

struct ActionRequest : public Il2CppObject {
    /**
     * 	// Fields
	[CompilerGenerated]
	private int <Method>k__BackingField; // 0x10
	[CompilerGenerated]
	private IMessage <Payload>k__BackingField; // 0x18
	private Action<IActionResponse> outCallback; // 0x20
     */
    uint32_t method; // 0x10
    void* payload;
    void* outCallback;
};


#endif //POGODROID_IL2CPPSTRUCTS_H
