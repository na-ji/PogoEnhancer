//
//
//

#include "HAP64.h"

// Fields (rather static..)
unsigned long HAP64::pP_gIAo() {
    return 0x80;
}


unsigned long HAP64::pP_gIDo() {
    return 0x84;
}


unsigned long HAP64::pP_gISo() {
    return 0x88;
}


unsigned long HAP64::pP_gCMo() {
    return 0x8C;
}


unsigned long HAP64::pP_gAcMo() {
    return 0xBC;
}


unsigned long HAP64::pP_gHo() {
    return 0x78;
}


unsigned long HAP64::pP_gWo() {
    return 0x7C;
}


unsigned long HAP64::pP_iEo() {
    return 0x60;
}


unsigned long HAP64::pSp_gPHo() {
    return 0x60;
}


unsigned long HAP64::pSp_gPWo() {
    return 0x64;
}


unsigned long HAP64::pP_gPDo() {
    return 0xE8;
}


unsigned long HAP64::pDp_gSo() {
    return 0x20;
}


unsigned long HAP64::pDp_gGo() {
    return 0x1C;
}


unsigned long HAP64::pDp_gWco() {
    return 0x28;
}


unsigned long HAP64::eOp_gPo() {
    return 0x18;
}


unsigned long HAP64::wPp_gPo() {
    return 0x40;
}


unsigned long HAP64::mPi_gISo() {
    return 0x48;
}

unsigned long HAP64::mCh_sTTo() {
    return 0x28;
}

unsigned long HAP64::sTt_maISo() {
    return 0x34;
}

unsigned long HAP64::pP_nNo() {
    return 0xC8;
}

unsigned long HAP64::gBdP_fIo() {
    return 0x20;
}

unsigned long HAP64::gBdP_gIo() {
    return 0x18;
}

unsigned long HAP64::fLp_fRSo() {
    return 0x208;
}

unsigned long HAP64::pp_m1o() {
    return 0x48;
}

unsigned long HAP64::pp_m2o() {
    return 0x4C;
}

unsigned long HAP64::pSp_t1o() {
    return 0x18;
}

unsigned long HAP64::pSp_t2o() {
    return 0x1C;
}


unsigned long HAP64::cM_mSCo() {
    return 0x20;
}

unsigned long HAP64::iEoP_gPFo() {
    return 0x20;
}

unsigned long HAP64::dEoP_gPFo() {
    return 0x18;
}

unsigned long HAP64::wPp_gEIo() {
    return 0x18;
}

// Methods (change every update)
/**
 * public class PoiItemSpinner : BasePoiSpinner, DGOKHLILKCN // TypeDefIndex: 12322
 * 	private void VQOM]NWNMT]() { } 0x22C7FDC 0x22C8204
 * -> Es gibt mehrere private methoden... //spin
 */
unsigned long HAP64::pIs_sSRo() {
    return 0x5D816F0;
}

/**
 * public class PoiItemSpinner : BasePoiSpinner, DGOKHLILKCN // TypeDefIndex: 12322
 * 		public override void Initialize(DNGMDBDCDIA OTTYNTMU]PM) { }
 */
unsigned long HAP64::pIs_Io() {
    return 0x5D80F40;
}


/**
 * public class PokemonInventoryGuiController : LegacyGuiController, M]YU]VMOOQU, T]ZZWN]YSXZ, AJAJDOCLENJ, IGuiLayerable, IHideable, UN[TPMW]XXX // TypeDefIndex: 11743
 * 	private void OYUMNM[VZVZ(PokemonListLineItemView VSVN[U[UMXV, PokemonProto NYUNOT[]XPR, int [NYPQNXW]VS) { }
 * 	-> Suche nach Anzahl Param, private, erstes Arg PokemonListLineItemView
 */
unsigned long HAP64::pIgC_m1o() {
    return 0x5C46588;
}

/**
 * public class EncounterPokemon : MonoBehaviour, IEncounterPokemon // TypeDefIndex: 7662
 * 	protected virtual float RRWYXRQQO[O() { }
 * 	-> pech gehabt, weiter suchen oder alle X protected float setzen
 */
unsigned long HAP64::eP_gAPo() {
    return 0x65CB9FC;
}

/**
 * public class EncounterPokemon : MonoBehaviour, IEncounterPokemon // TypeDefIndex: 7662
 * protected virtual float [XXPQQR[PYX() { }
 * 	-> pech gehabt, weiter suchen oder alle X protected float setzen -> Nicht möglich, siehe oben
 */
unsigned long HAP64::eP_gDPo() {
    return 0x65CBD24;
}

/**
 * public class Pokeball : MonoBehaviour, IPokeball, AJAJDOCLENJ // TypeDefIndex: 7747
 * 	private int U[XV]OOOUSN(CatchPokemonOutProto NYUNOT[]XPR) { }
 * 	-> Suche auf private, int return, 1 arg, arg CatchPokemonOutProto (sofern nicht obf?)
 */
unsigned long HAP64::cPOp_gCr() {
    return 0x65F2108;
}


/**
 * public class EncounterCaptureState : GameState, ]RYOTVTZRMY // TypeDefIndex: 7614
 * 	private IEnumerator<_IARMeshData> WST[MPPSZ[V(CatchPokemonOutProto PTM[TOPO[PT) { }
 * 		private IEnumerator<ISchedule> RunPokemonCaptured(CatchPokemonOutProto captureResults) { }
 * 	-> Gibt es mehrere derartige -> Vermutlich pech
 */
unsigned long HAP64::eCs_rPCo() {
    return 0x65B8290;
}


/**
 * public sealed class IncenseEncounterOutProto : IMessage<IncenseEncounterOutProto>, IMessage, IEquatable<IncenseEncounterOutProto>, IDeepCloneable<IncenseEncounterOutProto> // TypeDefIndex: 14333
 * 	public IncenseEncounterOutProto.Types.Result W]OZ[W]TOVY() { }
 */
unsigned long HAP64::iEoP_gRo() {
    return 0x6A2B268;
}


/**
 * public class GameMasterData : MonoBehaviour, MRZU]U]QTWX, MXWZMVNQP[R // TypeDefIndex: 128
 * 	public PokemonSettingsProto Get_PokemonSettings(PokemonProto TU[VROTWNSO) { }
 */
unsigned long HAP64::gMd_gPso() {
    return 0x5A178A8;
}


/**
 * public class MapContentHandler : MonoBehaviour, PZSOV]RPXUN // TypeDefIndex: 12128
 * 	private void ZUYTSPQ]TM[(GetMapObjectsOutProto YXWQTSVYPRX) { }
 */
unsigned long HAP64::mCh_gMOcIo() {
    return 0x5D276FC;
}


/**
 * public class OpenGiftGuiController.Factory : InstalledPrefabFactory<OpenGiftGuiController.MWWWRRNPZOW, OpenGiftGuiController> // TypeDefIndex: 5021
 * 	public override OpenGiftGuiController Create(OpenGiftGuiController.MWWWRRNPZOW S]SPVMSZZ]Q) { }
 */
unsigned long HAP64::oGgCf_Co() {
    return 0x6326DC0;
}


/**
 * public class GiftingRpcService : MonoBehaviour, IBalloonAssetManager // TypeDefIndex: 4947
 * 	public KMDIHMDKEBL<OpenGiftOutProto> OpenGift(ulong NONUWTUSURM, string UNMVXXMWTYR, string ZYSPXNMSZ[R) { }
 */
unsigned long HAP64::gRs_oGo() {
    return 0x6308C18;
}


/**
 * public class GiftingRpcService : MonoBehaviour, IBalloonAssetManager // TypeDefIndex: 4947
 *  	public KMDIHMDKEBL<CheckSendGiftOutProto> CheckGiftingStatus(string TTTUY]YMMNQ) { }
 */
unsigned long HAP64::gRs_cGSo() {
    return 0x63092A8;
}


/**
 * public sealed class PokemonProto : CHMFMJDIFHD, EHOKNIMEGKI, IOOEJLHGPGP, KNHNEKJHDKB, HDBOCEGOIDG, EIFHLFELFGK, HLAGEAFIKFI, CHEEHAMOKHJ, IMessage<PokemonProto>, IMessage, IEquatable<PokemonProto>, IDeepCloneable<PokemonProto> // TypeDefIndex: 13889
 * 	public bool TNTUORQ[XTN() { }
 * 	public bool get_IsSelectionDisabled() { }
 */
unsigned long HAP64::pIMs_tSo() {
    return 0x6981B4C;
}


/**
 * public class MapPokestop : MonoBehaviour, BOKOJKGANLG, DNGMDBDCDIA, IPoi, AJAJDOCLENJ // TypeDefIndex: 6749
 * 	private bool ORPSUMSVWXW(PokestopIncidentDisplayProto SNU]NZ]UVPS, bool RQWUNQOUWOQ) { }
 */
unsigned long HAP64::mP_gIiA() {
    return 0x650E708;
}


/**
 * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
 * 	public int SMT]UOW]]OS(Item YYWVYSRSSQM) { }
 * -> Einzige methode mit 1 param und return int
 */
unsigned long HAP64::iBi_gICo() {
    return 0x5EA10F4;
}


/**
 * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
     * 	public KMDIHMDKEBL<RecycleItemOutProto> V[SYUVQQWVS(Item SU]VUXMSVVO, int TNYMWZRV[UW) { }
 * -> No chance
 */
unsigned long HAP64::iBi_rIo() {
    return 0x5EA44AC;
}


/**
 * public static class RQUSOPZUZYR // TypeDefIndex: 685
 * 	public static string R[ZRZTVYRPQ(string YNWSTTZYVUN, bool VX[ZRQSQ]RO) { }
 * 	-> No chance
 * 	// Namespace: Niantic.Holoholo
[Extension]
public static class StringExt // TypeDefIndex: 718
 	private const char ZERO_WIDTH_JOINER = '\x200d';
	private const char VARIATION_SELECTOR_16 = '\xfe0f';
	private static readonly Regex HtmlTagMatcherRegex; // 0x0
	private const string HtmlEntityMatcherExpression = "(&#)([0-9]+)(;)";
	private static readonly MatchEvaluator HtmlEntityMatchEvaluator; // 0x8
	private const string FORMAT_ARGS_REGEX = "{[\\d]+(:[^:}]+)?}";
 	[Extension]
	// RVA: 0x46E3BD8 Offset: 0x46E2BD8 VA: 0x46E3BD8
	public static string HtmlDecode(string toDecode, bool useHex) { }

 original:
 // RVA: 0x46E3AB0 Offset: 0x46E2AB0 VA: 0x46E3AB0
	public static string StripHtmlTags(string sourceToStrip, bool replaceHtmlEntityCharacters) { }

 */
unsigned long HAP64::sE_hDo() {
    return 0x64037F0;
}


/**
 * public class GameMasterData : MonoBehaviour, MRZU]U]QTWX, MXWZMVNQP[R // TypeDefIndex: 128
 * 	public PokemonSettingsProto Get_PokemonSettings(int UPXWUUUOQZW) { }
 */
unsigned long HAP64::gMd_gPsBido() {
    return 0x5A17C54;
}


/**
 * public class HashSet<T> : ICollection<T>, IEnumerable<T>, IEnumerable, ISet<T>, IReadOnlyCollection<T>, ISerializable, IDeserializationCallback // TypeDefIndex: 25306
 * |-RVA: 0x3FF3C84 Offset: 0x3FF3C84 VA: 0x3FF3C84
	|-HashSet<long>.Clear
 * No chance
 */
unsigned long HAP64::hS_co() {
    return 0x4341CDC;
}


/**
 * public class Memderp // TypeDefIndex: 12484
 * 	private void URUXWPWUQVU(HashSet<ulong> WS]RY[ZNMUQ, List<ulong> U[PXUPUR[NS, List<ulong> YQU]MSZRWZS, List<ulong> MQVWPVRWXQR) { }
 * -> No Chance
 * // Namespace: Niantic.Holoholo.Map.S2Cache
public class SpatialCacheManager // TypeDefIndex: 13810
 	// RVA: 0x3F9D1E8 Offset: 0x3F9C1E8 VA: 0x3F9D1E8
	private void UpdateSceneCells(HashSet<ulong> fetchCandidates, List<ulong> addToSceneCells, List<ulong> updatedCells, List<ulong> removeFromSceneCells) { }
 * */
unsigned long HAP64::cM_mSFo() {
    return 0x5DC41CC;
}


/**
 * public abstract class MapPokemon : MonoBehaviour, MMOVZWM]ZQS, AJAJDOCLENJ // TypeDefIndex: 12194
 * 	private void YYZ[XTPV[WS(object TPZX]TMUZOX, EventArgs VNMPTUPWQVQ) { }
 * private void, param mit EventArgs, 2 params
 * 	private void OnTap(object sender, EventArgs e) { }
 */
unsigned long HAP64::mP_oTo() {
    return 0x5D592E8;
}


/**
 * public class EncounterInteractionState : GameState, IAILNNMMFOK, ICustomInstaller // TypeDefIndex: 7625
 * 	private void NRPX[OWYTQY(Item USM[QUXNUST) { } 0x257C7F8
 * 		private void set_ActiveBerry(Item value) { }
 * Prüfen: 1 param -> Item
 */
unsigned long HAP64::eIs_sABo() {
    return 0x65BA014;
}


/**
 * public class ItemBagImpl : HJGGHOKGMEM // TypeDefIndex: 1554
 * 	public KMDIHMDKEBL<UseItemEncounterOutProto> S]QYVS]UNVO(Item SU]VUXMSVVO, MMOVZWM]ZQS TMUXXOMMQ[M) { }
 * 		public IPromise<UseItemEncounterOutProto> UseItemForEncounter(Item item, IMapPokemon encounterPokemon) { }
 */
unsigned long HAP64::iBi_uIfEo() {
    return 0x5EA3E24;
}


/**
 * public class BuddyPokemonService : ObservableService<BuddyPokemonProto>, WO[YOOWXSNS, DDAMPNKNFLJ, IObservableService<BuddyPokemonProto> // TypeDefIndex: 6470
 * 	private void PWUWZZNUYST() { }
 * -> Nope
 * 	public void Initialize() { }
 */
unsigned long HAP64::bPs_iO() {
    return 0x64C47CC;
}


/**
 * public class BuddyRpcService : MonoBehaviour, IBuddyRpcService, KCIINPIAENA // TypeDefIndex: 6179
 * 	private void PWUWZZNUYST() { }
 * -> Einzige private void ohne args
 * 	private void Initialize() { }
 * */
unsigned long HAP64::bRs_iO() {
    return 0x6485D70;
}


/**
     * public class BuddySettingsService : MonoBehaviour, IBuddySettingsService // TypeDefIndex: 6181
 * 	|-BuddySettingsService.ZVNVN[U]XRY<PlayerService.PlayerInfo>
 * -> Nope
 * private void GetMapObjectsCallbackImpl(GetMapObjectsOutProto result) { }
 */
unsigned long HAP64::bSSo_pIo() {
    return 0x5D276FC;
}


/**
 * public sealed class OpenBuddyGiftOutProto : IMessage<OpenBuddyGiftOutProto>, IMessage, IEquatable<OpenBuddyGiftOutProto>, IDeepCloneable<OpenBuddyGiftOutProto> // TypeDefIndex: 16987
 * 	public void XZVM[XWSUMR(BuddyObservedData USM[QUXNUST) { }
 * 1 param von Typ BuddyObservedData
 * 	public void set_ObservedData(BuddyObservedData value) { }
 */
unsigned long HAP64::oBgPo_sBGo() {
    return 0x6E67794;
}


/**
 * public class V[RNSYVYT[O : IPhotoLeaderClaimSessionManager // TypeDefIndex: 1953
 * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
 * 	private void VZ[[XQY]TST() { } 0x3B341C0
 * 	1 von 2, einfach alle hooken die private void sind?
 * 	public void UnlockPokemonTempEvos() { }

 */
unsigned long HAP64::pBi_gSo() {
    return 0x5EB1C14;
}


/**
 * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
 * 		public PokemonProto MMPWYQVRRMS(ulong WXRQWVMRQXQ) { }
 * 			public PokemonProto GetPokemon(ulong id) { }
 */
unsigned long HAP64::pBi_gPpBIo() {
    return 0x5EAF0D0;
}


/**
 * public class PokemonBagImpl : FMLOAMFKNEO // TypeDefIndex: 1572
 * 	public KMDIHMDKEBL<ReleasePokemonOutProto> [Y[]WP[QPSM(PokemonProto TU[VROTWNSO) { }
 * 		public IPromise<ReleasePokemonOutProto> ReleasePokemon(PokemonProto pokemon) { }
 */
unsigned long HAP64::pBi_rPo() {
    return 0x5EAFD8C;
}


/**
 * public sealed class RpcHandler : MonoBehaviour, KEOEMHLJPPM, ICustomInstaller // TypeDefIndex: 2282
 * 	private MEDBDEBEHGJ<OutProtoT> UXQVMXPSSPT<InProtoT, OutProtoT>(int TQNQYV[NSMQ, InProtoT SSXTRUVS]YS, int TRN]WSPY]ZT = 0, int XXRXRQRP[XU = 0, bool NWZYVWX]X[O = False) { }
 * -> Nope oder Anzahl param??
 * 	private IRpcPromise<OutProtoT> SendRpc<InProtoT, OutProtoT>(int method, InProtoT inProto, int rpcRetryDelayMs = 0, int rpcTimeoutMs = 0, bool beTenacious = False) { }
 */
unsigned long HAP64::rH_So() {
    return 0x416C6A4;
}

/**
 * public class ItemBagImpl
* private void AddCameraItem() { }
*/

unsigned long HAP64::iBi_gIo() {
    return 0x5EA102C;
}

/**
 * PlayerService playerinstance
* public IEnumerable<QuestProto> get_Quests() { }
 *
*/

unsigned long HAP64::pS_gQo() {
    return 0x62AF7D4;
}

// TODO: 0.335.0 all below + documentation please

/*
 * public class CombatStateV2
 * // RVA: 0x44A5254 Offset: 0x44A4254 VA: 0x44A5254 Slot: 9
	public void ObserveCombatState(CombatProto.Types.CombatState stateOfCombat) { }
 */
unsigned long HAP64::cD_Io() {
    return 0x6360FB8;
}

/*
 * public class CombatCountdownGui
 * // RVA: 0x45574C4 Offset: 0x45564C4 VA: 0x45574C4
	private IEnumerator<ISchedule> Animate() { }
 */
unsigned long HAP64::cCg_aCo() {
    return 0x640DA3C;
}

/*
 * public class CombatEndState
 * // RVA: 0x44D86AC Offset: 0x44D76AC VA: 0x44D86AC
	private void EndInvasionSession(CombatProto.Types.CombatState endState, CombatPlayerFinishState finishState, int remainingPokemon) { }

 */
unsigned long HAP64::cD_eISo() {
    return 0x63941A4;
}

/*
 * public class IncidentService
 * // RVA: 0x416F3FC Offset: 0x416E3FC VA: 0x416F3FC Slot: 7
	public void StartIncidentEncounter() { }
 */
unsigned long HAP64::cS_gCeWo() {
    return 0x60193A8;
}

/*
 * public class CombatStateV2
 * // RVA: 0x44A42FC Offset: 0x44A32FC VA: 0x44A42FC
	private bool get_CanTransitionToEndCombatState() { }
 */
unsigned long HAP64::cS_giRso() {
    return 0x635FF84;
}

/*
 * public class CombatStateV2
 * // RVA: 0x44A6034 Offset: 0x44A5034 VA: 0x44A6034 Slot: 11
	public void TransitionToEndState() { }
 */
unsigned long HAP64::cS_ttEso() {
    return 0x6361D98;
}


/*
 * public class IncidentService
 * // RVA: 0x416EDE4 Offset: 0x416DDE4 VA: 0x416EDE4
	private void CompletePokestopDialogue() { }
 */
unsigned long HAP64::cD_cPdo() {
    return 0x6018D90;
}


/*
 * public class IncidentService
 * // RVA: 0x416E7D8 Offset: 0x416D7D8 VA: 0x416E7D8
	private void StartDialogue(ClientPokestopNpcDialogueStepProto pokestopDialogue) { }
 */
unsigned long HAP64::cD_sInBo() {
    return 0x6018784;
}

