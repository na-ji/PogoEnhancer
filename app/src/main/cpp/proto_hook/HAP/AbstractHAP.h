//
//
//

#ifndef DROID_PLUSPLUS_ABSTRACTHAP_H
#define DROID_PLUSPLUS_ABSTRACTHAP_H


#include <string>

class AbstractHAP {
public:
    virtual unsigned long wPp_gPo() = 0;
    virtual unsigned long pIs_sSRo() = 0;
    virtual unsigned long pIs_Io() = 0;
    virtual unsigned long pIgC_m1o() = 0;
    virtual unsigned long pP_gIAo() = 0;
    virtual unsigned long pP_gIDo() = 0;
    virtual unsigned long pP_gISo() = 0;
    virtual unsigned long pP_gCMo() = 0;
    virtual unsigned long pP_gAcMo() = 0;

    virtual unsigned long eP_gAPo() = 0;
    virtual unsigned long cPOp_gCr() = 0;

    virtual unsigned long eCs_rPCo() = 0;

    virtual unsigned long mPi_gISo() = 0;

    virtual unsigned long eP_gDPo() = 0;

    virtual unsigned long iEoP_gRo() = 0;

    virtual unsigned long eOp_gPo() = 0;

    virtual unsigned long pP_gPDo() = 0;

    virtual unsigned long pDp_gSo() = 0;

    virtual unsigned long pP_iEo() = 0;
    virtual unsigned long pDp_gGo() = 0;
    virtual unsigned long pDp_gWco() = 0;

    // xsxl

    virtual unsigned long pP_gHo() = 0;
    virtual unsigned long pP_gWo() = 0;
    virtual unsigned long gMd_gPso() = 0;
    virtual unsigned long pSp_gPHo() = 0;
    virtual unsigned long pSp_gPWo() = 0;

    virtual unsigned long pP_nNo() = 0; // pP_nNo|PokemonProto|private string nickname_;
    virtual unsigned long mCh_gMOcIo() = 0; // mCh_gMOcIo|MapContentHandler|private void GetMapObjectsCallbackImpl(GetMapObjectsProto request, GetMapObjectsOutProto result)
    virtual unsigned long mCh_sTTo() = 0; // mCh_sTTo|MapContentHandler|private SpaceTimeThrottle fetchThrottle;
    virtual unsigned long sTt_maISo() = 0; // sTt_maISo|SpaceTimeThrottle|private float maxIntervalS;

    virtual unsigned long oGgCf_Co() = 0; // oGgCf_Co|OpenGiftGuiController.Factory|public override OpenGiftGuiController Create(IEnumerable<GiftBoxDetailsProto> ADIGGJPHIDF)
    virtual unsigned long gRs_oGo() = 0; // gRs_oGo|GiftingRpcService|public IPromise<OpenGiftOutProto> OpenGift(ulong IGFFEGMPAIC, string JHOENBKOEJI)
    virtual unsigned long gBdP_fIo() = 0; // gBdP_fIo|GiftBoxDetailsProto|private string senderId_;
    virtual unsigned long gBdP_gIo() = 0; // gBdP_gIo|GiftBoxDetailsProto|private ulong giftboxId_;
    virtual unsigned long gRs_cGSo() = 0; // gRs_cGSo|GiftingRpcService|public IPromise<CheckSendGiftOutProto> CheckGiftingStatus(string PCKOLDJHDBJ)
    virtual unsigned long fLp_fRSo() = 0; // fLp_fRSo|FriendsListPage|private readonly IFriendsRpcService DEEOPFBJFLB;
    virtual unsigned long pIMs_tSo() = 0; // pIMs_tSo|PokemonInventoryMultiSelect|public string ToggleSelection(PokemonProto FLIJJDGHPCE, bool FFEKCHPDNCO)

    virtual unsigned long mP_gIiA() = 0;

    virtual unsigned long iBi_gICo() = 0;
    virtual unsigned long iBi_rIo() =0;

    virtual unsigned long pp_m1o() =0;
    virtual unsigned long pp_m2o() =0;
    virtual unsigned long sE_hDo() =0;

    virtual unsigned long pSp_t1o() = 0;
    virtual unsigned long pSp_t2o() = 0;
    virtual unsigned long gMd_gPsBido() = 0;

    virtual unsigned long iEoP_gPFo() = 0;
    virtual unsigned long dEoP_gPFo() = 0;

    virtual unsigned long mP_oTo() = 0;

    /**
     * Temporary fix for memleak of nia
     * //public void JAICCKEKKGA(ICellManager IJLOBPJGNJK, IDictionary<int, ViewPoint> GOBINFLOANM) { }
        	private HashSet<ulong> FDMJCEEIACO; // 0x30
     * @return
     */
    virtual unsigned long cM_mSCo() = 0;
    // 	public void JAICCKEKKGA(ICellManager IJLOBPJGNJK, IDictionary<int, ViewPoint> GOBINFLOANM) { }
    virtual unsigned long cM_mSFo() = 0;
    virtual unsigned long hS_co() = 0; // HashSet<ulong>::clear

    // pinap mode (autofeed)
    virtual unsigned long eIs_sABo() = 0; //private void set_ActiveBerry(Item value) { }
    virtual unsigned long iBi_uIfEo() = 0; //public IPromise<UseItemEncounterOutProto> UseItemForEncounter(Item item, IMapPokemon encounterPokemon) { }
    virtual unsigned long bPs_iO() = 0;
    virtual unsigned long bRs_iO() = 0;
    virtual unsigned long bSSo_pIo() = 0;
    virtual unsigned long oBgPo_sBGo() = 0;
    virtual unsigned long pBi_gSo() = 0;
    virtual unsigned long pBi_gPpBIo() = 0;
    virtual unsigned long pBi_rPo() = 0;

    virtual unsigned long wPp_gEIo() = 0;
    virtual unsigned long rH_So() = 0;

    virtual unsigned long iBi_gIo() = 0;
    virtual unsigned long pS_gQo() = 0;

    virtual unsigned long cD_Io() = 0;
    virtual unsigned long cCg_aCo() = 0;
    virtual unsigned long cD_eISo() = 0;
    virtual unsigned long cS_gCeWo() = 0;
    virtual unsigned long cS_giRso() = 0;
    virtual unsigned long cS_ttEso() = 0;
    virtual unsigned long cD_cPdo() = 0;
    virtual unsigned long cD_sInBo() = 0;

    std::string gRp_n();



};


#endif //DROID_PLUSPLUS_ABSTRACTHAP_H
