/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.dto;

import it.eng.parer.entity.constraint.AroUpdUnitaDoc.AroUpdUDTiStatoUpdElencoVers;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSVersatore;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipoSalvataggioFile;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.IDatiSpecEntity;

/**
 *
 * @author sinatti_s
 */
public class StrutturaUpdVers implements java.io.Serializable, IDatiSpecEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -3628770831632889102L;

    private Date dataVersamento;
    private Date dtVersMax;
    private Date dtInizioCalcoloNewUrn;

    private String versioneIndiceSipNonVerificata = "N/A";
    private String descTipologiaUnitaDocumentariaNonVerificata;
    private String descTipoDocPrincipaleNonVerificato;
    private String descTipologiaUnitaDocumentaria;
    private String descTipologiaUnitaDocumentariaUnknown;;
    private String urnPartChiaveUd;
    private String urnPartVersatore;
    private String cdKeyNormalized;
    //
    private String sistemaDiMigrazione;
    private String tipoConservazione;
    //
    private String tipoAggiornamento;
    private String noteAggiornamento;
    private String hashIndiceSipXml;
    private String algoritmoHashIndiceSIP;
    private String encodingHashIndiceSIP;
    //

    private long idRegistro;
    private long idUd;
    private long idStruttura;
    private long idUser;
    private long idOrgEnteConv;
    private long idTipoUDNonVerificata;
    private long idTipoUDUnknown;
    private long idTipoREGUnknown;
    private long idTipoDOCPRINCUnknown;
    private long idTipologiaUnitaDocumentaria;
    private long idTipoDocPrincipale;
    private long idRecXsdDatiSpec;
    private long idRecXsdDatiSpecMigrazione;
    private long idRecAggiornamentoDB;

    private HashMap<String, DatoSpecifico> datiSpecifici;
    private HashMap<String, DatoSpecifico> datiSpecificiMigrazione;
    private HashMap<String, UpdComponenteVers> componentiAttesi;
    private HashMap<String, UpdComponenteVers> sottoComponentiAttesi;
    //
    private List<UpdDocumentoVers> documentiAttesi;
    private List<UpdUnitaDocColl> unitaDocCollegate;

    private boolean versatoreVerificato = false;
    //
    // private boolean trovatiIdCompDuplicati;
    private boolean trovatiIdDocDuplicati;

    // Nota : solo in presenza di documento e quindi del tag mi aspetto di
    // effettuare il controllo
    private Boolean corrAllegatiDichiarati = null;
    private Boolean corrAnnessiDichiarati = null;
    private Boolean corrAnnotazioniDichiarati = null;
    //

    // vedi 1.1.3. se indicatore di “Forza aggiornamento metadati in archivio” su
    // unita doc da aggiornare e’ nullo
    private Boolean flForzaAggiornamento = null;
    private Boolean flForzaCollegamento = null;

    private CostantiDB.TipoSalvataggioFile tipoSalvataggioFile = TipoSalvataggioFile.BLOB;
    //
    private CSVersatore versatoreNonverificato;
    private CSChiave chiaveNonVerificata;
    //
    private FlControlliUpd flControlliUpd;
    private CostantiDB.StatoConservazioneUnitaDoc statoConservazioneUnitaDoc;
    //
    AroUpdUDTiStatoUpdElencoVers tiStatoUpdElencoVers;

    @Override
    public long getIdRecXsdDatiSpec() {
        return this.idRecXsdDatiSpec;
    }

    @Override
    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecifici() {
        return this.datiSpecifici;
    }

    @Override
    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
    }

    @Override
    public long getIdRecXsdDatiSpecMigrazione() {
        return this.idRecXsdDatiSpecMigrazione;
    }

    @Override
    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpecMigrazione = idRecXsdDatiSpec;
    }

    public long getIdRecAggiornamentoDB() {
        return this.idRecAggiornamentoDB;
    }

    public void setIdRecAggiornamentoDB(long idRecAggiornamentoDB) {
        this.idRecAggiornamentoDB = idRecAggiornamentoDB;
    }

    @Override
    public HashMap<String, DatoSpecifico> getDatiSpecificiMigrazione() {
        return this.datiSpecificiMigrazione;
    }

    @Override
    public void setDatiSpecificiMigrazione(HashMap<String, DatoSpecifico> datiSpecificiMigrazione) {
        this.datiSpecificiMigrazione = datiSpecificiMigrazione;
    }

    public Date getDataVersamento() {
        return dataVersamento;
    }

    public void setDataVersamento(Date dataVersamento) {
        this.dataVersamento = dataVersamento;
    }

    public long getIdUd() {
        return idUd;
    }

    public void setIdUd(long idUd) {
        this.idUd = idUd;
    }

    public long getIdStruttura() {
        return idStruttura;
    }

    public void setIdStruttura(long idStruttura) {
        this.idStruttura = idStruttura;
    }

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public long getIdOrgEnteConv() {
        return idOrgEnteConv;
    }

    public void setIdOrgEnteConv(long idOrgEnteConv) {
        this.idOrgEnteConv = idOrgEnteConv;
    }

    public boolean isVersatoreVerificato() {
        return versatoreVerificato;
    }

    public void setVersatoreVerificato(boolean versatoreVerificato) {
        this.versatoreVerificato = versatoreVerificato;
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

    public String getVersioneIndiceSipNonVerificata() {
        return versioneIndiceSipNonVerificata;
    }

    public void setVersioneIndiceSipNonVerificata(String versioneIndiceSipNonVerificata) {
        this.versioneIndiceSipNonVerificata = versioneIndiceSipNonVerificata;
    }

    public String getUrnPartChiaveUd() {
        return urnPartChiaveUd;
    }

    public void setUrnPartChiaveUd(String urnUD) {
        this.urnPartChiaveUd = urnUD;
    }

    public String getDescTipologiaUnitaDocumentariaNonVerificata() {
        return descTipologiaUnitaDocumentariaNonVerificata;
    }

    public void setDescTipologiaUnitaDocumentariaNonVerificata(String descTipologiaUnitaDocumentariaNonVerificata) {
        this.descTipologiaUnitaDocumentariaNonVerificata = descTipologiaUnitaDocumentariaNonVerificata;
    }

    public List<UpdUnitaDocColl> getUnitaDocCollegate() {
        if (unitaDocCollegate == null) {
            unitaDocCollegate = new ArrayList<UpdUnitaDocColl>(0);
        }
        return unitaDocCollegate;
    }

    public void setUnitaDocCollegate(List<UpdUnitaDocColl> unitaDocCollegate) {
        this.unitaDocCollegate = unitaDocCollegate;
    }

    public List<UpdDocumentoVers> getDocumentiAttesi() {
        if (documentiAttesi == null) {
            documentiAttesi = new ArrayList<UpdDocumentoVers>(0);
        }
        return documentiAttesi;
    }

    public void setDocumentiAttesi(List<UpdDocumentoVers> documentiAttesi) {
        this.documentiAttesi = documentiAttesi;
    }

    public HashMap<String, UpdComponenteVers> getComponentiAttesi() {
        if (componentiAttesi == null) {
            componentiAttesi = new HashMap<String, UpdComponenteVers>(0);
        }
        return componentiAttesi;
    }

    public void setComponentiAttesi(HashMap<String, UpdComponenteVers> sottoComponentiAttesi) {
        this.componentiAttesi = sottoComponentiAttesi;
    }

    public HashMap<String, UpdComponenteVers> getSottoComponentiAttesi() {
        if (sottoComponentiAttesi == null) {
            sottoComponentiAttesi = new HashMap<String, UpdComponenteVers>(0);
        }
        return componentiAttesi;
    }

    public void setSottoComponentiAttesi(HashMap<String, UpdComponenteVers> sottoComponentiAttesi) {
        this.sottoComponentiAttesi = sottoComponentiAttesi;
    }

    /*
     * public boolean isTrovatiIdCompDuplicati() { return trovatiIdCompDuplicati; } public void
     * setTrovatiIdCompDuplicati(boolean trovatiIdCompDuplicati) { this.trovatiIdCompDuplicati = trovatiIdCompDuplicati;
     * }
     */
    public boolean isTrovatiIdDocDuplicati() {
        return trovatiIdDocDuplicati;
    }

    public void setTrovatiIdDocDuplicati(boolean trovatiIdDocDuplicati) {
        this.trovatiIdDocDuplicati = trovatiIdDocDuplicati;
    }

    public Boolean isCorrAllegatiDichiarati() {
        return corrAllegatiDichiarati;
    }

    public void setCorrAllegatiDichiarati(Boolean corrAllegatiDichiarati) {
        this.corrAllegatiDichiarati = corrAllegatiDichiarati;
    }

    public Boolean isCorrAnnessiDichiarati() {
        return corrAnnessiDichiarati;
    }

    public void setCorrAnnessiDichiarati(Boolean corrAnnessiDichiarati) {
        this.corrAnnessiDichiarati = corrAnnessiDichiarati;
    }

    public Boolean isCorrAnnotazioniDichiarati() {
        return corrAnnotazioniDichiarati;
    }

    public void setCorrAnnotazioniDichiarati(Boolean corrAnnotazioniDichiarati) {
        this.corrAnnotazioniDichiarati = corrAnnotazioniDichiarati;
    }

    public String getSistemaDiMigrazione() {
        return sistemaDiMigrazione;
    }

    public void setSistemaDiMigrazione(String sistemaDiMigrazione) {
        this.sistemaDiMigrazione = sistemaDiMigrazione;
    }

    public String getTipoConservazione() {
        return tipoConservazione;
    }

    public void setTipoConservazione(String tipoConservazione) {
        this.tipoConservazione = tipoConservazione;
    }

    public String getNoteAggiornamento() {
        return noteAggiornamento;
    }

    public void setNoteAggiornamento(String noteAggiornamento) {
        this.noteAggiornamento = noteAggiornamento;
    }

    public String getHashIndiceSipXml() {
        return hashIndiceSipXml;
    }

    public void setHashIndiceSipXml(String hashIndiceSipXml) {
        this.hashIndiceSipXml = hashIndiceSipXml;
    }

    public String getAlgoritmoHashIndiceSIP() {
        return algoritmoHashIndiceSIP;
    }

    public void setAlgoritmoHashIndiceSIP(String algoritmoHashIndiceSIP) {
        this.algoritmoHashIndiceSIP = algoritmoHashIndiceSIP;
    }

    public String getEncodingHashIndiceSIP() {
        return encodingHashIndiceSIP;
    }

    public void setEncodingHashIndiceSIP(String encodingHashIndiceSIP) {
        this.encodingHashIndiceSIP = encodingHashIndiceSIP;
    }

    public long getIdTipologiaUnitaDocumentaria() {
        return idTipologiaUnitaDocumentaria;
    }

    public void setIdTipologiaUnitaDocumentaria(long idTipologiaUnitaDocumentaria) {
        this.idTipologiaUnitaDocumentaria = idTipologiaUnitaDocumentaria;
    }

    public String getDescTipologiaUnitaDocumentaria() {
        return descTipologiaUnitaDocumentaria;
    }

    public void setDescTipologiaUnitaDocumentaria(String descTipologiaUnitaDocumentaria) {
        this.descTipologiaUnitaDocumentaria = descTipologiaUnitaDocumentaria;
    }

    public String getDescTipologiaUnitaDocumentariaUnknown() {
        return descTipologiaUnitaDocumentariaUnknown;
    }

    public void setDescTipologiaUnitaDocumentariaUnknown(String descTipologiaUnitaDocumentariaUnknown) {
        this.descTipologiaUnitaDocumentariaUnknown = descTipologiaUnitaDocumentariaUnknown;
    }

    public CostantiDB.TipoSalvataggioFile getTipoSalvataggioFile() {
        return tipoSalvataggioFile;
    }

    public void setTipoSalvataggioFile(CostantiDB.TipoSalvataggioFile tipoSalvataggioFile) {
        this.tipoSalvataggioFile = tipoSalvataggioFile;
    }

    public long getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(long idRegistro) {
        this.idRegistro = idRegistro;
    }

    public FlControlliUpd getFlControlliUpd() {
        return flControlliUpd;
    }

    public void setFlControlliUpd(FlControlliUpd flControlliUpd) {
        this.flControlliUpd = flControlliUpd;
    }

    public CostantiDB.StatoConservazioneUnitaDoc getStatoConservazioneUnitaDoc() {
        return statoConservazioneUnitaDoc;
    }

    public void setStatoConservazioneUnitaDoc(CostantiDB.StatoConservazioneUnitaDoc statoConservazioneUnitaDoc) {
        this.statoConservazioneUnitaDoc = statoConservazioneUnitaDoc;
    }

    public AroUpdUDTiStatoUpdElencoVers getTiStatoUpdElencoVers() {
        return tiStatoUpdElencoVers;
    }

    public void setTiStatoUpdElencoVers(AroUpdUDTiStatoUpdElencoVers tiStatoUpdElencoVers) {
        this.tiStatoUpdElencoVers = tiStatoUpdElencoVers;
    }

    public Boolean isFlForzaAggiornamento() {
        return flForzaAggiornamento;
    }

    public void setFlForzaAggiornamento(Boolean flForzaAggiornamento) {
        this.flForzaAggiornamento = flForzaAggiornamento;
    }

    public Boolean isFlForzaCollegamento() {
        return flForzaCollegamento;
    }

    public void setFlForzaCollegamento(Boolean flForzaCollegamento) {
        this.flForzaCollegamento = flForzaCollegamento;
    }

    public long getIdTipoUDNonVerificata() {
        return idTipoUDNonVerificata;
    }

    public void setIdTipoUDNonVerificata(long idTipoUDNonVerificata) {
        this.idTipoUDNonVerificata = idTipoUDNonVerificata;
    }

    public long getIdTipoUDUnknown() {
        return idTipoUDUnknown;
    }

    public void setIdTipoUDUnknown(long idTipoUDUnknown) {
        this.idTipoUDUnknown = idTipoUDUnknown;
    }

    public long getIdTipoREGUnknown() {
        return idTipoREGUnknown;
    }

    public void setIdTipoREGUnknown(long idTipoREGUnknown) {
        this.idTipoREGUnknown = idTipoREGUnknown;
    }

    public long getIdTipoDOCPRINCUnknown() {
        return idTipoDOCPRINCUnknown;
    }

    public void setIdTipoDOCPRINCUnknown(long idTipoDOCPRINCUnknown) {
        this.idTipoDOCPRINCUnknown = idTipoDOCPRINCUnknown;
    }

    public String getDescTipoDocPrincipaleNonVerificato() {
        return descTipoDocPrincipaleNonVerificato;
    }

    public void setDescTipoDocPrincipaleNonVerificato(String descTipoDocPrincipaleNonVerificato) {
        this.descTipoDocPrincipaleNonVerificato = descTipoDocPrincipaleNonVerificato;
    }

    public String getUrnPartVersatore() {
        return urnPartVersatore;
    }

    public void setUrnPartVersatore(String urnPartVersatore) {
        this.urnPartVersatore = urnPartVersatore;
    }

    public long getIdTipoDocPrincipale() {
        return idTipoDocPrincipale;
    }

    public void setIdTipoDocPrincipale(long idTipoDocPrincipale) {
        this.idTipoDocPrincipale = idTipoDocPrincipale;
    }

    public String getTipoAggiornamento() {
        return tipoAggiornamento;
    }

    public void setTipoAggiornamento(String tipoAggiornamento) {
        this.tipoAggiornamento = tipoAggiornamento;
    }

    public String getCdKeyNormalized() {
        return cdKeyNormalized;
    }

    public void setCdKeyNormalized(String cdKeyNormalized) {
        this.cdKeyNormalized = cdKeyNormalized;
    }

    public Date getDtVersMax() {
        return dtVersMax;
    }

    public void setDtVersMax(Date dtVersMax) {
        this.dtVersMax = dtVersMax;
    }

    public Date getDtInizioCalcoloNewUrn() {
        return dtInizioCalcoloNewUrn;
    }

    public void setDtInizioCalcoloNewUrn(Date dtInizioCalcoloNewUrn) {
        this.dtInizioCalcoloNewUrn = dtInizioCalcoloNewUrn;
    }
}
