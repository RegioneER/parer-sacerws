package it.eng.parer.ws.versamento.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipoSalvataggioFile;
import java.time.ZonedDateTime;

/**
 *
 * @author Fioravanti_F
 */
public class StrutturaVersamento implements java.io.Serializable, IDatiSpecEntity, IProfiloEntity {

    private ZonedDateTime dataVersamento;
    //
    private HashMap<String, ComponenteVers> fileAttesi;
    private List<DocumentoVers> documentiAttesi;
    //
    private boolean trovatiIdCompDuplicati;
    private boolean trovatiIdDocDuplicati;
    private String trovataDataNullaIn;
    private boolean corrAllegatiDichiarati;
    private boolean corrAnnessiDichiarati;
    private boolean corrAnnotazioniDichiarati;
    //
    private long idStruttura;
    private long idSubStruttura;
    private long idUser;
    private long idUnitaDoc;
    private String descSubStruttura;
    private boolean strutturaForzaFormato;
    //
    private String urnPartVersatore = "";
    private String urnPartChiaveUd = "";
    //
    private String numeroUdNormalized = "";
    private String urnPartVersatoreNormalized = "";
    private String urnPartChiaveUdNormalized = "";
    //
    private long idRegistroUnitaDoc;
    private long idTipologiaUnitaDocumentaria;
    private String descTipologiaUnitaDocumentaria;
    private CostantiDB.TipoSalvataggioFile tipoSalvataggioFile = TipoSalvataggioFile.BLOB;
    private boolean tpiAbilitato = false;
    // sip vs db
    private boolean configFlagForzaFormato = false;
    private boolean configFlagAbilitaContrColleg = false;
    private boolean configFlagForzaFormatoNumero = false;
    private boolean configFlagForzaHashVers = false;
    private boolean configFlagForzaColleg = false;
    //
    private boolean flagAccettaHashErrato = false;
    private boolean flagAbilitaForzaDocAgg = false;
    private boolean flagForzaDocAgg = false;
    private boolean flagAccettaDocNeg = false;
    private boolean flagAbilitaContrFmtNumero = false;
    private boolean flagAccettaContrHashNeg = false;
    private boolean flagProfiloUdObbOggetto = false;
    private boolean flagProfiloUdObbData = false;
    private boolean flagVerificaHash = false;
    private boolean flagAccettaErroreFmtNumero = false;
    private boolean flagAccettaContrCollegNeg = false;
    //
    private boolean flagAbilitaVerificaFirma = false;
    private boolean flagAbilitaVerificaFirmaSoloCrypto = false;
    //
    private String generazioneRerortVerificaFirma;

    private boolean flagAbilitaConservazioneNonFirmati = false;
    private boolean flagAccettaConservazioneNonFirmatiNeg = false;
    //
    private boolean flagAbilitaControlloRevoca = false;
    private boolean flagAccettaControlloOCSPNegativo = false;
    private boolean flagAccettaControlloOCSPNoScaric = false;
    private boolean flagAccettaControlloOCSPNoValido = false;
    // SIP vs DB
    private boolean configFlagForzaConservazione = false;
    //
    private boolean configFlagForzaControlloRevoca = false;
    private boolean configFlagForzaControlloCrittografico = false;
    private boolean configFlagForzaControlloTrust = false;
    private boolean configFlagForzaControlloCertificato = false;
    private boolean configFlagForzaControlloNonConformita = false;

    private String tiEsitoVerifFirme;
    private String dsMsgEsitoVerifica;
    private List<UnitaDocColl> unitaDocCollegate;
    //
    private ConfigRegAnno configRegAnno;
    private String keyOrdCalcolata;
    private Long progressivoCalcolato;
    private boolean warningFormatoNumero;
    //
    private String sistemaDiMigrazione = null;
    private HashMap<String, DatoSpecifico> datiSpecifici;
    private HashMap<String, DatoSpecifico> datiSpecificiMigrazione;
    private long idRecXsdDatiSpec;
    private long idRecXsdDatiSpecMigrazione;
    //
    private Long idRecUsoXsdProfiloNormativo;
    private String datiC14NProfNormXml;
    //
    private String tpiRootTpi;
    private String tpiRootTpiDaSacer;
    private String tpiRootArkVers;
    private String tpiRootVers;
    //
    private String subPathDataVers;
    private String subPathVersatoreArk;
    private String subPathUnitaDocArk;
    //
    private long totalSizeInBytes;
    //
    private CostantiDB.StatoConservazioneUnitaDoc statoConservazioneUnitaDoc;
    //
    private Date dtInizioCalcoloNewUrn;
    private Date dtVersMax;
    //
    private CSVersatore versatoreNonverificato;
    private CSChiave chiaveNonVerificata;
    //
    private String tiStatoUdDaElab;

    public ZonedDateTime getDataVersamento() {
        return dataVersamento;
    }

    public void setDataVersamento(ZonedDateTime dataVersamento) {
        this.dataVersamento = dataVersamento;
    }

    /**
     * @return the fileAttesi
     */
    public HashMap<String, ComponenteVers> getFileAttesi() {
        return fileAttesi;
    }

    /**
     * @param fileAttesi
     *            the fileAttesi to set
     */
    public void setFileAttesi(HashMap<String, ComponenteVers> fileAttesi) {
        this.fileAttesi = fileAttesi;
    }

    /**
     * @return the documentiAttesi
     */
    public List<DocumentoVers> getDocumentiAttesi() {
        return documentiAttesi;
    }

    /**
     * @param documentiAttesi
     *            the documentiAttesi to set
     */
    public void setDocumentiAttesi(List<DocumentoVers> documentiAttesi) {
        this.documentiAttesi = documentiAttesi;
    }

    /**
     * @return the trovatiDuplicati
     */
    public boolean isTrovatiIdCompDuplicati() {
        return trovatiIdCompDuplicati;
    }

    /**
     * @param trovatiDuplicati
     *            the trovatiDuplicati to set
     */
    public void setTrovatiIdCompDuplicati(boolean trovatiDuplicati) {
        this.trovatiIdCompDuplicati = trovatiDuplicati;
    }

    /**
     * @return the trovatiIdDocDuplicati
     */
    public boolean isTrovatiIdDocDuplicati() {
        return trovatiIdDocDuplicati;
    }

    /**
     * @param trovatiIdDocDuplicati
     *            the trovatiIdDocDuplicati to set
     */
    public void setTrovatiIdDocDuplicati(boolean trovatiIdDocDuplicati) {
        this.trovatiIdDocDuplicati = trovatiIdDocDuplicati;
    }

    public String getTrovataDataNullaIn() {
        return trovataDataNullaIn;
    }

    public void setTrovataDataNullaIn(String trovataDataNullaIn) {
        this.trovataDataNullaIn = trovataDataNullaIn;
    }

    /**
     * @return the corrAllegatiDichiarati
     */
    public boolean isCorrAllegatiDichiarati() {
        return corrAllegatiDichiarati;
    }

    /**
     * @param corrAllegatiDichiarati
     *            the corrAllegatiDichiarati to set
     */
    public void setCorrAllegatiDichiarati(boolean corrAllegatiDichiarati) {
        this.corrAllegatiDichiarati = corrAllegatiDichiarati;
    }

    /**
     * @return the corrAnnessiDichiarati
     */
    public boolean isCorrAnnessiDichiarati() {
        return corrAnnessiDichiarati;
    }

    /**
     * @param corrAnnessiDichiarati
     *            the corrAnnessiDichiarati to set
     */
    public void setCorrAnnessiDichiarati(boolean corrAnnessiDichiarati) {
        this.corrAnnessiDichiarati = corrAnnessiDichiarati;
    }

    /**
     * @return the corrAnnotazioniDichiarati
     */
    public boolean isCorrAnnotazioniDichiarati() {
        return corrAnnotazioniDichiarati;
    }

    /**
     * @param corrAnnotazioniDichiarati
     *            the corrAnnotazioniDichiarati to set
     */
    public void setCorrAnnotazioniDichiarati(boolean corrAnnotazioniDichiarati) {
        this.corrAnnotazioniDichiarati = corrAnnotazioniDichiarati;
    }

    /**
     * @return the idStruttura
     */
    public long getIdStruttura() {
        return idStruttura;
    }

    /**
     * @param idStruttura
     *            the idStruttura to set
     */
    public void setIdStruttura(long idStruttura) {
        this.idStruttura = idStruttura;
    }

    public long getIdSubStruttura() {
        return idSubStruttura;
    }

    public void setIdSubStruttura(long idSubStruttura) {
        this.idSubStruttura = idSubStruttura;
    }

    /**
     * @return the idUser
     */
    public long getIdUser() {
        return idUser;
    }

    /**
     * @param idUser
     *            the idUser to set
     */
    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    /**
     * @return the idTipologiaUnitaDocumentaria
     */
    public long getIdTipologiaUnitaDocumentaria() {
        return idTipologiaUnitaDocumentaria;
    }

    /**
     * @param idTipologiaUnitaDocumentaria
     *            the idTipologiaUnitaDocumentaria to set
     */
    public void setIdTipologiaUnitaDocumentaria(long idTipologiaUnitaDocumentaria) {
        this.idTipologiaUnitaDocumentaria = idTipologiaUnitaDocumentaria;
    }

    public String getDescTipologiaUnitaDocumentaria() {
        return descTipologiaUnitaDocumentaria;
    }

    public void setDescTipologiaUnitaDocumentaria(String descTipologiaUnitaDocumentaria) {
        this.descTipologiaUnitaDocumentaria = descTipologiaUnitaDocumentaria;
    }

    public TipoSalvataggioFile getTipoSalvataggioFile() {
        return tipoSalvataggioFile;
    }

    public void setTipoSalvataggioFile(TipoSalvataggioFile tipoSalvataggioFile) {
        this.tipoSalvataggioFile = tipoSalvataggioFile;
    }

    public boolean isTpiAbilitato() {
        return tpiAbilitato;
    }

    public void setTpiAbilitato(boolean tpiAbilitato) {
        this.tpiAbilitato = tpiAbilitato;
    }

    public boolean isConfigFlagForzaFormato() {
        return configFlagForzaFormato;
    }

    public void setConfigFlagForzaFormato(boolean configFlagForzaFormato) {
        this.configFlagForzaFormato = configFlagForzaFormato;
    }

    public boolean isFlagAccettaHashErrato() {
        return flagAccettaHashErrato;
    }

    public void setFlagAccettaHashErrato(boolean flagAccettaHashErrato) {
        this.flagAccettaHashErrato = flagAccettaHashErrato;
    }

    /**
     * @return the idUnitaDoc
     */
    public long getIdUnitaDoc() {
        return idUnitaDoc;
    }

    /**
     * @param idUnitaDoc
     *            the idUnitaDoc to set
     */
    public void setIdUnitaDoc(long idUnitaDoc) {
        this.idUnitaDoc = idUnitaDoc;
    }

    public String getDescSubStruttura() {
        return descSubStruttura;
    }

    public void setDescSubStruttura(String descSubStruttura) {
        this.descSubStruttura = descSubStruttura;
    }

    public boolean isStrutturaForzaFormato() {
        return strutturaForzaFormato;
    }

    public void setStrutturaForzaFormato(boolean strutturaForzaFormato) {
        this.strutturaForzaFormato = strutturaForzaFormato;
    }

    public String getUrnPartVersatore() {
        return urnPartVersatore;
    }

    public void setUrnPartVersatore(String urnPartVersatore) {
        this.urnPartVersatore = urnPartVersatore;
    }

    public String getUrnPartChiaveUd() {
        return urnPartChiaveUd;
    }

    public void setUrnPartChiaveUd(String urnPartChiaveUd) {
        this.urnPartChiaveUd = urnPartChiaveUd;
    }

    public String getUrnPartChiaveUdNormalized() {
        return urnPartChiaveUdNormalized;
    }

    public void setUrnPartChiaveUdNormalized(String urnPartChiaveUdNormalized) {
        this.urnPartChiaveUdNormalized = urnPartChiaveUdNormalized;
    }

    public String getUrnPartVersatoreNormalized() {
        return urnPartVersatoreNormalized;
    }

    public void setUrnPartVersatoreNormalized(String urnPartVersatoreNormalized) {
        this.urnPartVersatoreNormalized = urnPartVersatoreNormalized;
    }

    public long getIdRegistroUnitaDoc() {
        return idRegistroUnitaDoc;
    }

    public void setIdRegistroUnitaDoc(long idRegistroUnitaDoc) {
        this.idRegistroUnitaDoc = idRegistroUnitaDoc;
    }

    /**
     * @return the dsMsgEsitoVerifica
     */
    public String getDsMsgEsitoVerifica() {
        return dsMsgEsitoVerifica;
    }

    /**
     * @param dsMsgEsitoVerifica
     *            the dsMsgEsitoVerifica to set
     */
    public void setDsMsgEsitoVerifica(String dsMsgEsitoVerifica) {
        this.dsMsgEsitoVerifica = dsMsgEsitoVerifica;
    }

    /**
     * @return the tiEsitoVerifFirme
     */
    public String getTiEsitoVerifFirme() {
        return tiEsitoVerifFirme;
    }

    /**
     * @param tiEsitoVerifFirme
     *            the tiEsitoVerifFirme to set
     */
    public void setTiEsitoVerifFirme(String tiEsitoVerifFirme) {
        this.tiEsitoVerifFirme = tiEsitoVerifFirme;
    }

    /**
     * @return the documentiCollegati
     */
    public List<UnitaDocColl> getUnitaDocCollegate() {
        return unitaDocCollegate;
    }

    /**
     * @param documentiCollegati
     *            the documentiCollegati to set
     */
    public void setUnitaDocCollegate(List<UnitaDocColl> documentiCollegati) {
        this.unitaDocCollegate = documentiCollegati;
    }

    public ConfigRegAnno getConfigRegAnno() {
        return configRegAnno;
    }

    public void setConfigRegAnno(ConfigRegAnno configRegAnno) {
        this.configRegAnno = configRegAnno;
    }

    public String getKeyOrdCalcolata() {
        return keyOrdCalcolata;
    }

    public void setKeyOrdCalcolata(String keyOrdCalcolata) {
        this.keyOrdCalcolata = keyOrdCalcolata;
    }

    public Long getProgressivoCalcolato() {
        return progressivoCalcolato;
    }

    public void setProgressivoCalcolato(Long progressivoCalcolato) {
        this.progressivoCalcolato = progressivoCalcolato;
    }

    public boolean isWarningFormatoNumero() {
        return warningFormatoNumero;
    }

    public void setWarningFormatoNumero(boolean warningFormatoNumero) {
        this.warningFormatoNumero = warningFormatoNumero;
    }

    public String getSistemaDiMigrazione() {
        return sistemaDiMigrazione;
    }

    public void setSistemaDiMigrazione(String sistemaDiMigrazione) {
        this.sistemaDiMigrazione = sistemaDiMigrazione;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecifici() {
        return datiSpecifici;
    }

    @Override
    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecificiMigrazione() {
        return datiSpecificiMigrazione;
    }

    @Override
    public void setDatiSpecificiMigrazione(HashMap<String, DatoSpecifico> datiSpecificiMigrazione) {
        this.datiSpecificiMigrazione = datiSpecificiMigrazione;
    }

    @Override
    public long getIdRecXsdDatiSpec() {
        return idRecXsdDatiSpec;
    }

    @Override
    public long getIdRecXsdDatiSpecMigrazione() {
        return idRecXsdDatiSpecMigrazione;
    }

    @Override
    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    @Override
    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpecMigrazione = idRecXsdDatiSpec;
    }

    public String getTpiRootTpi() {
        return tpiRootTpi;
    }

    public void setTpiRootTpi(String tpiRootTpi) {
        this.tpiRootTpi = tpiRootTpi;
    }

    public String getTpiRootTpiDaSacer() {
        return tpiRootTpiDaSacer;
    }

    public void setTpiRootTpiDaSacer(String tpiRootTpiDaSacer) {
        this.tpiRootTpiDaSacer = tpiRootTpiDaSacer;
    }

    public String getTpiRootArkVers() {
        return tpiRootArkVers;
    }

    public void setTpiRootArkVers(String tpiRootArkVers) {
        this.tpiRootArkVers = tpiRootArkVers;
    }

    public String getTpiRootVers() {
        return tpiRootVers;
    }

    public void setTpiRootVers(String tpiRootVers) {
        this.tpiRootVers = tpiRootVers;
    }

    public String getSubPathDataVers() {
        return subPathDataVers;
    }

    public void setSubPathDataVers(String subPathDataVers) {
        this.subPathDataVers = subPathDataVers;
    }

    public String getSubPathVersatoreArk() {
        return subPathVersatoreArk;
    }

    public void setSubPathVersatoreArk(String subPathVersatoreArk) {
        this.subPathVersatoreArk = subPathVersatoreArk;
    }

    public String getSubPathUnitaDocArk() {
        return subPathUnitaDocArk;
    }

    public void setSubPathUnitaDocArk(String subPathUnitaDocArk) {
        this.subPathUnitaDocArk = subPathUnitaDocArk;
    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(long totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public String getNumeroUdNormalized() {
        return numeroUdNormalized;
    }

    public void setNumeroUdNormalized(String numeroUdNormalized) {
        this.numeroUdNormalized = numeroUdNormalized;
    }

    public boolean isFlagForzaDocAgg() {
        return flagForzaDocAgg;
    }

    public void setFlagForzaDocAgg(boolean flagForzaDocAgg) {
        this.flagForzaDocAgg = flagForzaDocAgg;
    }

    public boolean isFlagAccettaDocNeg() {
        return flagAccettaDocNeg;
    }

    public void setFlagAccettaDocNeg(boolean flagAccettaDocNeg) {
        this.flagAccettaDocNeg = flagAccettaDocNeg;
    }

    public boolean isFlagAbilitaForzaDocAgg() {
        return flagAbilitaForzaDocAgg;
    }

    public void setFlagAbilitaForzaDocAgg(boolean flagAbilitaForzaDocAgg) {
        this.flagAbilitaForzaDocAgg = flagAbilitaForzaDocAgg;
    }

    public CostantiDB.StatoConservazioneUnitaDoc getStatoConservazioneUnitaDoc() {
        return statoConservazioneUnitaDoc;
    }

    public void setStatoConservazioneUnitaDoc(CostantiDB.StatoConservazioneUnitaDoc statoConservazioneUnitaDoc) {
        this.statoConservazioneUnitaDoc = statoConservazioneUnitaDoc;
    }

    public boolean isConfigFlagForzaFormatoNumero() {
        return configFlagForzaFormatoNumero;
    }

    public void setConfigFlagForzaFormatoNumero(boolean configFlagForzaFormatoNumero) {
        this.configFlagForzaFormatoNumero = configFlagForzaFormatoNumero;
    }

    public boolean isConfigFlagForzaHashVers() {
        return configFlagForzaHashVers;
    }

    public void setConfigFlagForzaHashVers(boolean configFlagForzaHashVers) {
        this.configFlagForzaHashVers = configFlagForzaHashVers;
    }

    public boolean isConfigFlagAbilitaContrColleg() {
        return configFlagAbilitaContrColleg;
    }

    public void setConfigFlagAbilitaContrColleg(boolean configFlagAbilitaContrColleg) {
        this.configFlagAbilitaContrColleg = configFlagAbilitaContrColleg;
    }

    public boolean isFlagAbilitaContrFmtNumero() {
        return flagAbilitaContrFmtNumero;
    }

    public void setFlagAbilitaContrFmtNumero(boolean flagAbilitaContrFmtNumero) {
        this.flagAbilitaContrFmtNumero = flagAbilitaContrFmtNumero;
    }

    public boolean isFlagAccettaContrHashNeg() {
        return flagAccettaContrHashNeg;
    }

    public void setFlagAccettaContrHashNeg(boolean flagAccettaContrHashNeg) {
        this.flagAccettaContrHashNeg = flagAccettaContrHashNeg;
    }

    public boolean isFlagProfiloUdObbOggetto() {
        return flagProfiloUdObbOggetto;
    }

    public void setFlagProfiloUdObbOggetto(boolean flagProfiloUdObbOggetto) {
        this.flagProfiloUdObbOggetto = flagProfiloUdObbOggetto;
    }

    public boolean isFlagProfiloUdObbData() {
        return flagProfiloUdObbData;
    }

    public void setFlagProfiloUdObbData(boolean flagProfiloUdObbData) {
        this.flagProfiloUdObbData = flagProfiloUdObbData;
    }

    public boolean isFlagVerificaHash() {
        return flagVerificaHash;
    }

    public void setFlagVerificaHash(boolean flagVerificaHash) {
        this.flagVerificaHash = flagVerificaHash;
    }

    public boolean isConfigFlagForzaColleg() {
        return configFlagForzaColleg;
    }

    public void setConfigFlagForzaColleg(boolean configFlagForzaColleg) {
        this.configFlagForzaColleg = configFlagForzaColleg;
    }

    public boolean isFlagAccettaErroreFmtNumero() {
        return flagAccettaErroreFmtNumero;
    }

    public void setFlagAccettaErroreFmtNumero(boolean flagAccettaErroreFmtNumero) {
        this.flagAccettaErroreFmtNumero = flagAccettaErroreFmtNumero;
    }

    public boolean isFlagAccettaContrCollegNeg() {
        return flagAccettaContrCollegNeg;
    }

    public void setFlagAccettaContrCollegNeg(boolean flagAccettaContrCollegNeg) {
        this.flagAccettaContrCollegNeg = flagAccettaContrCollegNeg;
    }

    public boolean isFlagAbilitaVerificaFirma() {
        return flagAbilitaVerificaFirma;
    }

    public void setFlagAbilitaVerificaFirma(boolean flagAbilitaVerificaFirma) {
        this.flagAbilitaVerificaFirma = flagAbilitaVerificaFirma;
    }

    public boolean isFlagAbilitaVerificaFirmaSoloCrypto() {
        return flagAbilitaVerificaFirmaSoloCrypto;
    }

    public boolean effettuaVerificaFirma() {
        return isFlagAbilitaVerificaFirma();
    }

    public void setFlagAbilitaVerificaFirmaSoloCrypto(boolean flagAbilitaVerificaFirmaSoloCrypto) {
        this.flagAbilitaVerificaFirmaSoloCrypto = flagAbilitaVerificaFirmaSoloCrypto;
    }

    public boolean isFlagAbilitaConservazioneNonFirmati() {
        return flagAbilitaConservazioneNonFirmati;
    }

    public void setFlagAbilitaConservazioneNonFirmati(boolean flagAbilitaConservazioneNonFirmati) {
        this.flagAbilitaConservazioneNonFirmati = flagAbilitaConservazioneNonFirmati;
    }

    public boolean isFlagAccettaConservazioneNonFirmatiNeg() {
        return flagAccettaConservazioneNonFirmatiNeg;
    }

    public void setFlagAccettaConservazioneNonFirmatiNeg(boolean flagAccettaConservazioneNonFirmatiNeg) {
        this.flagAccettaConservazioneNonFirmatiNeg = flagAccettaConservazioneNonFirmatiNeg;
    }

    public boolean isConfigFlagForzaConservazione() {
        return configFlagForzaConservazione;
    }

    public void setConfigFlagForzaConservazione(boolean configFlagForzaConservazione) {
        this.configFlagForzaConservazione = configFlagForzaConservazione;
    }

    /**
     * @return the configFlagForzaControlloCrittografico
     */
    public boolean isConfigFlagForzaControlloCrittografico() {
        return configFlagForzaControlloCrittografico;
    }

    /**
     * @param configFlagForzaControlloCrittografico
     *            the configFlagForzaControlloCrittografico to set
     */
    public void setConfigFlagForzaControlloCrittografico(boolean configFlagForzaControlloCrittografico) {
        this.configFlagForzaControlloCrittografico = configFlagForzaControlloCrittografico;
    }

    /**
     * @return the configFlagForzaControlloTrust
     */
    public boolean isConfigFlagForzaControlloTrust() {
        return configFlagForzaControlloTrust;
    }

    /**
     * @param configFlagForzaControlloTrust
     *            the configFlagForzaControlloTrust to set
     */
    public void setConfigFlagForzaControlloTrust(boolean configFlagForzaControlloTrust) {
        this.configFlagForzaControlloTrust = configFlagForzaControlloTrust;
    }

    /**
     * @return the configFlagForzaControlloCertificato
     */
    public boolean isConfigFlagForzaControlloCertificato() {
        return configFlagForzaControlloCertificato;
    }

    /**
     * @param configFlagForzaControlloCertificato
     *            the configFlagForzaControlloCertificato to set
     */
    public void setConfigFlagForzaControlloCertificato(boolean configFlagForzaControlloCertificato) {
        this.configFlagForzaControlloCertificato = configFlagForzaControlloCertificato;
    }

    /**
     * @return the configFlagForzaControlloNonConformita
     */
    public boolean isConfigFlagForzaControlloNonConformita() {
        return configFlagForzaControlloNonConformita;
    }

    /**
     * @param configFlagForzaControlloNonConformita
     *            the configFlagForzaControlloNonConformita to set
     */
    public void setConfigFlagForzaControlloNonConformita(boolean configFlagForzaControlloNonConformita) {
        this.configFlagForzaControlloNonConformita = configFlagForzaControlloNonConformita;
    }

    public Date getDtInizioCalcoloNewUrn() {
        return dtInizioCalcoloNewUrn;
    }

    public void setDtInizioCalcoloNewUrn(Date dtInizioCalcoloNewUrn) {
        this.dtInizioCalcoloNewUrn = dtInizioCalcoloNewUrn;
    }

    public Date getDtVersMax() {
        return dtVersMax;
    }

    public void setDtVersMax(Date dtVersMax) {
        this.dtVersMax = dtVersMax;
    }

    public boolean isConfigFlagForzaControlloRevoca() {
        return configFlagForzaControlloRevoca;
    }

    /**
     * @param configFlagForzaControlloRevoca
     *            the configFlagForzaControlloRevoca to set
     */
    public void setConfigFlagForzaControlloRevoca(boolean configFlagForzaControlloRevoca) {
        this.configFlagForzaControlloRevoca = configFlagForzaControlloRevoca;
    }

    /**
     * @return the flagAccettaControlloOCSPNegativo
     */
    public boolean isFlagAccettaControlloOCSPNegativo() {
        return flagAccettaControlloOCSPNegativo;
    }

    /**
     * @param flagAccettaControlloOCSPNegativo
     *            the flagAccettaControlloOCSPNegativo to set
     */
    public void setFlagAccettaControlloOCSPNegativo(boolean flagAccettaControlloOCSPNegativo) {
        this.flagAccettaControlloOCSPNegativo = flagAccettaControlloOCSPNegativo;
    }

    /**
     * @return the flagAccettaControlloOCSPNoScaric
     */
    public boolean isFlagAccettaControlloOCSPNoScaric() {
        return flagAccettaControlloOCSPNoScaric;
    }

    /**
     * @param flagAccettaControlloOCSPNoScaric
     *            the flagAccettaControlloOCSPNoScaric to set
     */
    public void setFlagAccettaControlloOCSPNoScaric(boolean flagAccettaControlloOCSPNoScaric) {
        this.flagAccettaControlloOCSPNoScaric = flagAccettaControlloOCSPNoScaric;
    }

    /**
     * @return the flagAccettaControlloOCSPNoValido
     */
    public boolean isFlagAccettaControlloOCSPNoValido() {
        return flagAccettaControlloOCSPNoValido;
    }

    /**
     * @param flagAccettaControlloOCSPNoValido
     *            the flagAccettaControlloOCSPNoValido to set
     */
    public void setFlagAccettaControlloOCSPNoValido(boolean flagAccettaControlloOCSPNoValido) {
        this.flagAccettaControlloOCSPNoValido = flagAccettaControlloOCSPNoValido;
    }

    /**
     * @return the flagAbilitaControlloRevoca
     */
    public boolean isFlagAbilitaControlloRevoca() {
        return flagAbilitaControlloRevoca;
    }

    /**
     * @param flagAbilitaControlloRevoca
     *            the flagAbilitaControlloRevoca to set
     */
    public void setFlagAbilitaControlloRevoca(boolean flagAbilitaControlloRevoca) {
        this.flagAbilitaControlloRevoca = flagAbilitaControlloRevoca;
    }

    public String getGenerazioneRerortVerificaFirma() {
        return generazioneRerortVerificaFirma;
    }

    public void setGenerazioneRerortVerificaFirma(String generazioneRerortVerificaFirma) {
        this.generazioneRerortVerificaFirma = generazioneRerortVerificaFirma;
    }

    public CSVersatore getVersatoreNonverificato() {
        return versatoreNonverificato;
    }

    public void setVersatoreNonverificato(CSVersatore versatoreNonverificato) {
        this.versatoreNonverificato = versatoreNonverificato;
    }

    public CSChiave getChiaveNonVerificata() {
        return chiaveNonVerificata;
    }

    public void setChiaveNonVerificata(CSChiave chiaveNonVerificata) {
        this.chiaveNonVerificata = chiaveNonVerificata;
    }

    public Long getIdRecUsoXsdProfiloNormativo() {
        return idRecUsoXsdProfiloNormativo;
    }

    public void setIdRecUsoXsdProfiloNormativo(Long idRecUsoXsdProfiloNormativo) {
        this.idRecUsoXsdProfiloNormativo = idRecUsoXsdProfiloNormativo;
    }

    public String getDatiC14NProfNormXml() {
        return datiC14NProfNormXml;
    }

    public void setDatiC14NProfNormXml(String datiC14NProfNormXml) {
        this.datiC14NProfNormXml = datiC14NProfNormXml;
    }

    /**
     * @return the tiStatoUdDaElab
     */
    public String getTiStatoUdDaElab() {
        return tiStatoUdDaElab;
    }

    /**
     * @param tiStatoUdDaElab
     *            the tiStatoUdDaElab to set
     */
    public void setTiStatoUdDaElab(String tiStatoUdDaElab) {
        this.tiStatoUdDaElab = tiStatoUdDaElab;
    }
}
