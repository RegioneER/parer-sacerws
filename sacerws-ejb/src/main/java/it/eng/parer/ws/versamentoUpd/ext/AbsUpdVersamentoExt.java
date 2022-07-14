/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.AggiornamentoEffettuato;
import it.eng.parer.ws.xml.versUpdResp.AggiornamentiEffettuatiType;
import it.eng.parer.ws.xml.versUpdResp.ControlliType;
import it.eng.parer.ws.xml.versUpdResp.ControlloType;
import it.eng.parer.ws.xml.versUpdResp.ErroreType;
import it.eng.spagoLite.security.User;

/**
 *
 * @author sinatti_s
 */
public abstract class AbsUpdVersamentoExt implements IUpdVersamentoExt, IControlliWsMultipli, IAggiornamentiWsMultipli {

    /**
     * 
     */
    static final long serialVersionUID = -3946879392676891033L;

    private User utente;
    private String versioneWsChiamata;
    private String loginName;
    //
    // HashMap<String, String> xmlDefaults;
    //
    StrutturaUpdVers strutturaUpdVers;

    protected Set<ControlloEseguito> controlliDiSistema = new HashSet<ControlloEseguito>(0);
    // liste suddive per macro-tipo
    protected Set<ControlloEseguito> controlliGenerali = new HashSet<ControlloEseguito>(0);
    protected Set<ControlloEseguito> warnings = new HashSet<ControlloEseguito>(0);
    protected Set<ControlloEseguito> controlliDocumento = new HashSet<ControlloEseguito>(0);
    protected Set<ControlloEseguito> controlliFallitiUlteriori = new HashSet<ControlloEseguito>(0);

    protected Map<String, Set<ControlloEseguito>> controlliDocPrincipale = new HashMap<String, Set<ControlloEseguito>>(
            0);
    protected Map<String, Set<ControlloEseguito>> controlliAllegati = new HashMap<String, Set<ControlloEseguito>>(0);
    protected Map<String, Set<ControlloEseguito>> controlliAnnessi = new HashMap<String, Set<ControlloEseguito>>(0);
    protected Map<String, Set<ControlloEseguito>> controlliAnnotazioni = new HashMap<String, Set<ControlloEseguito>>(0);

    // componenti
    protected Map<String, Set<ControlloEseguito>> controlliComponenteDocPrincipale = new HashMap<String, Set<ControlloEseguito>>(
            0);
    protected Map<String, Set<ControlloEseguito>> controlliComponenteAllegati = new HashMap<String, Set<ControlloEseguito>>(
            0);
    protected Map<String, Set<ControlloEseguito>> controlliComponenteAnnessi = new HashMap<String, Set<ControlloEseguito>>(
            0);
    protected Map<String, Set<ControlloEseguito>> controlliComponenteAnnotazioni = new HashMap<String, Set<ControlloEseguito>>(
            0);

    // chiave -> Controlli
    protected Map<CSChiave, Set<ControlloEseguito>> controlliCollegamenti = new HashMap<CSChiave, Set<ControlloEseguito>>(
            0);

    //
    protected Set<AggiornamentoEffettuato> aggiornamentiUnitaDocumentaria = new HashSet<AggiornamentoEffettuato>(0);
    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiDocPrincipale = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);
    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiCompDocPrincipale = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);

    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiAllegati = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);
    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiCompAllegati = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);

    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiAnnessi = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);
    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiCompAnnessi = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);

    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiAnnotazioni = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);
    //
    protected Map<String, Set<AggiornamentoEffettuato>> aggiornamentiCompAnnotazioni = new HashMap<String, Set<AggiornamentoEffettuato>>(
            0);

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.dto.IRestWSBase#getLoginName()
     */
    @Override
    public String getLoginName() {
        return loginName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.dto.IRestWSBase#setLoginName(java.lang.String)
     */
    @Override
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IUpdVersExt#getUtente()
     */
    @Override
    public User getUtente() {
        return utente;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IUpdVersExt#setUtente(it.eng.spagoLite. security.User)
     */
    @Override
    public void setUtente(User utente) {
        this.utente = utente;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.dto.IRestWSBase#getVersioneWsChiamata()
     */
    @Override
    public String getVersioneWsChiamata() {
        return versioneWsChiamata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.dto.IRestWSBase#setVersioneWsChiamata(java.lang.String)
     */
    @Override
    public void setVersioneWsChiamata(String versioneWsChiamata) {
        this.versioneWsChiamata = versioneWsChiamata;
    }

    // public HashMap<String, String> getXmlDefaults() {
    // return xmlDefaults;
    // }
    //
    // public void setXmlDefaults(HashMap<String, String> xmlDefaults) {
    // this.xmlDefaults = xmlDefaults;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IUpdVersExt#getStrutturaUpdVers()
     */
    @Override
    public StrutturaUpdVers getStrutturaUpdVers() {
        if (strutturaUpdVers == null) {
            return new StrutturaUpdVers();
        }
        return strutturaUpdVers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IUpdVersExt#setStrutturaUpdVers(it.eng.
     * parer.ws.versamentoUpd.dto.StrutturaUpdVers)
     */
    @Override
    public void setStrutturaUpdVers(StrutturaUpdVers strutturaUpdVers) {
        this.strutturaUpdVers = strutturaUpdVers;
    }

    ////

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnGenerali(it.eng.parer.ws.versamentoUpd.dto.ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnSistema(ControlloWSResp ctlrTypeWS, String errCode, String errMessage,
            Object... params) {
        ControlloEseguito cft = buildControlloEseguito(getControlliGenerali(), IRispostaWS.SeverityEnum.ERROR,
                VoceDiErrore.TipiEsitoErrore.NEGATIVO, ctlrTypeWS, errCode, errMessage, params);
        getControlliDiSistema().add(cft);
    }

    // OK
    public void addControlloOkOnGenerali(ControlloWSResp ctlrTypeWS) {
        addEsitoControlloOnGeneraliBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.POSITIVO, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnGenerali(ControlloWSResp ctlrTypeWS) {
        addEsitoControlloOnGeneraliBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnGenerali(it.eng.parer.ws.versamentoUpd.dto.ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnGenerali(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String errCode, String errMessage, Object... params) {
        ControlloEseguito cft = buildControlloEseguito(getControlliGenerali(), severity, esito, ctlrTypeWS, errCode,
                errMessage, params);
        getControlliGenerali().add(cft);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnFallitiUlteriori(it.eng.parer.ws.versamentoUpd.dto. ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnFallitiUlteriori(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String errCode, String errMessage, Object... params) {
        ControlloEseguito cft = buildControlloEseguito(getControlliFallitiUlteriori(), severity, esito, ctlrTypeWS,
                errCode, errMessage, params);
        getControlliFallitiUlteriori().add(cft);
    }

    // OK
    public void addControlloOkOnControlliUnitaDocumentaria(ControlloWSResp ctlrTypeWS) {
        addEsitoControlloOnControlliUnitaDocumentariaBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliUnitaDocumentaria(ControlloWSResp ctlrTypeWS) {
        addEsitoControlloOnControlliUnitaDocumentariaBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliUnitaDocumentaria(it.eng.parer.ws.versamentoUpd. dto.ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliUnitaDocumentaria(ControlloWSResp ctlrTypeWS,
            IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String errCode, String errMessage,
            Object... params) {
        ControlloEseguito cft = buildControlloEseguito(getControlliUnitaDocumentaria(), severity, esito, ctlrTypeWS,
                errCode, errMessage, params);
        getControlliUnitaDocumentaria().add(cft);
    }

    // OK
    public void addControlloOkOnControlliDocPrincipale(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliDocPrincipaleBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idDocumento, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliDocPrincipale(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliDocPrincipaleBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                idDocumento, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliDocPrincipale(it.eng.parer.ws.versamentoUpd.dto. ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliDocPrincipale(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String idDocumento, String errCode, String errMessage,
            Object... params) {
        Set<ControlloEseguito> cftS = getControlliDocPrincipale(idDocumento);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliDocPrincipale().put(idDocumento, cftS);
    }

    // OK
    public void addControlloOkOnControlliAllegati(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAllegatiBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idDocumento, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliAllegati(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAllegatiBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                idDocumento, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliAllegati(it.eng.parer.ws.versamentoUpd.dto. ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliAllegati(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String idDocumento, String errCode, String errMessage,
            Object... params) {
        Set<ControlloEseguito> cftS = getControlliAllegato(idDocumento);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliAllegati().put(idDocumento, cftS);
    }

    // OK
    public void addControlloOkOnControlliAnnessi(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAnnessiBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idDocumento, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliAnnessi(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAnnessiBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                idDocumento, null);
    }

    @Override
    public void addEsitoControlloOnControlliAnnessi(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String idDocumento, String errCode, String errMessage,
            Object... params) {
        Set<ControlloEseguito> cftS = getControlliAnnesso(idDocumento);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliAnnessi().put(idDocumento, cftS);
    }

    // OK
    public void addControlloOkOnControlliAnnotazioni(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAnnotazioniBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idDocumento, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliAnnotazioni(ControlloWSResp ctlrTypeWS, String idDocumento) {
        addEsitoControlloOnControlliAnnotazioniBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                idDocumento, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliAnnotazioni(it.eng.parer.ws.versamentoUpd.dto. ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliAnnotazioni(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, String idDocumento, String errCode, String errMessage,
            Object... params) {
        Set<ControlloEseguito> cftS = getControlliAnnotazione(idDocumento);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliAnnotazioni().put(idDocumento, cftS);
    }

    // OK
    public void addControlloOkOnControlliComponentiDocPrincipale(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiDocPrincipaleBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idComponente, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliComponentiDocPrincipale(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiDocPrincipaleBundle(ctlrTypeWS, null,
                VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO, idComponente, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliComponentiDocPrincipale(it.eng.parer.ws. versamentoUpd.dto.ControlloWSResp,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliComponentiDocPrincipale(ControlloWSResp ctlrTypeWS,
            IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String idComponente, String errCode,
            String errMessage, Object... params) {
        Set<ControlloEseguito> cftS = getControlliComponenteDocPrincipale(idComponente);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliComponentiDocPrincipale().put(idComponente, cftS);
    }

    //
    // OK
    public void addControlloOkOnControlliComponentiAllegati(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAllegatiBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idComponente, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliComponentiAllegati(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAllegatiBundle(ctlrTypeWS, null,
                VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO, idComponente, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliComponentiDocPrincipale(it.eng.parer.ws. versamentoUpd.dto.ControlloWSResp,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliComponentiAllegati(ControlloWSResp ctlrTypeWS,
            IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String idComponente, String errCode,
            String errMessage, Object... params) {
        Set<ControlloEseguito> cftS = getControlliComponenteAllegati(idComponente);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliComponentiAllegati().put(idComponente, cftS);
    }

    //
    // OK
    public void addControlloOkOnControlliComponentiAnnessi(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAnnessiBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idComponente, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliComponentiAnnessi(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAnnessiBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                idComponente, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliComponentiDocPrincipale(it.eng.parer.ws. versamentoUpd.dto.ControlloWSResp,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliComponentiAnnessi(ControlloWSResp ctlrTypeWS,
            IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String idComponente, String errCode,
            String errMessage, Object... params) {
        Set<ControlloEseguito> cftS = getControlliComponenteAnnessi(idComponente);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliComponentiAnnessi().put(idComponente, cftS);
    }

    //
    // OK
    public void addControlloOkOnControlliComponentiAnnotazioni(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAnnotazioniBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, idComponente, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliComponentiAnnotazioni(ControlloWSResp ctlrTypeWS, String idComponente) {
        addEsitoControlloOnControlliComponentiAnnotazioniBundle(ctlrTypeWS, null,
                VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO, idComponente, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliComponentiDocPrincipale(it.eng.parer.ws. versamentoUpd.dto.ControlloWSResp,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliComponentiAnnotazioni(ControlloWSResp ctlrTypeWS,
            IRispostaWS.SeverityEnum severity, VoceDiErrore.TipiEsitoErrore esito, String idComponente, String errCode,
            String errMessage, Object... params) {
        Set<ControlloEseguito> cftS = getControlliComponenteAnnotazioni(idComponente);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliComponentiAnnotazioni().put(idComponente, cftS);
    }

    // OK
    public void addControlloOkOnControlliCollegamento(ControlloWSResp ctlrTypeWS, CSChiave csChiave) {
        addEsitoControlloOnControlliCollegamentoBundle(ctlrTypeWS, IRispostaWS.SeverityEnum.OK,
                VoceDiErrore.TipiEsitoErrore.POSITIVO, csChiave, null);
    }

    // NON_ATTIVATO
    public void addControlloNAOnControlliCollegamento(ControlloWSResp ctlrTypeWS, CSChiave csChiave) {
        addEsitoControlloOnControlliCollegamentoBundle(ctlrTypeWS, null, VoceDiErrore.TipiEsitoErrore.NON_ATTIVATO,
                csChiave, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * addEsitoControlloOnControlliCollegamento(it.eng.parer.ws.versamentoUpd.dto. ControlloWS,
     * it.eng.parer.ws.dto.IRispostaWS.SeverityEnum, it.eng.parer.ws.versamento.dto.VoceDiErrore.TipiEsitoErrore,
     * it.eng.parer.ws.dto.CSChiave, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addEsitoControlloOnControlliCollegamento(ControlloWSResp ctlrTypeWS, IRispostaWS.SeverityEnum severity,
            VoceDiErrore.TipiEsitoErrore esito, CSChiave csChiave, String errCode, String errMessage,
            Object... params) {
        Set<ControlloEseguito> cftS = getControlliCollegamento(csChiave);
        ControlloEseguito cft = buildControlloEseguito(cftS, severity, esito, ctlrTypeWS, errCode, errMessage, params);
        cftS.add(cft);
        getControlliCollegamenti().put(csChiave, cftS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#addControlloOnWarnings(
     * it.eng.parer.ws.versamentoUpd.dto.ControlloWS, java.lang.String, java.lang.String, java.lang.Object[])
     */
    @Override
    public void addControlloOnWarnings(ControlloWSResp ctlrTypeWS, String errCode, String errMessage,
            Object... params) {
        ControlloEseguito cft = buildControlloEseguito(getWarnings(), IRispostaWS.SeverityEnum.WARNING,
                VoceDiErrore.TipiEsitoErrore.WARNING, ctlrTypeWS, errCode, errMessage, params);
        getWarnings().add(cft);
    }

    protected void produciControlliFallitiFromGenUD(RispostaWSUpdVers rispostaWs) {
        // exclude ESITO GENERALE
        /*
         * Tutti i controlli con esisto NEGATIVO da controlligenerali+controlliud
         */
        if (!getControlliGenerali().isEmpty()) {
            for (ControlloEseguito controllo : getControlliGenerali().stream()
                    .filter(c -> c.getEsito().equals(VoceDiErrore.TipiEsitoErrore.NEGATIVO))
                    .collect(Collectors.toList())) {
                for (VoceDiErrore errore : controllo.getErrori()) {
                    if (!verificaErrorePrinc(rispostaWs, controllo, errore)) {
                        addEsitoControlloOnFallitiUlteriori(ControlliWSBundle.getControllo(controllo.getCdControllo()),
                                errore.getSeverity(), errore.getCodiceEsito(), errore.getErrorCode(),
                                errore.getErrorMessage());
                    }
                }
            }
        }

        if (!getControlliUnitaDocumentaria().isEmpty()) {
            for (ControlloEseguito controllo : getControlliUnitaDocumentaria().stream()
                    .filter(c -> c.getEsito().equals(VoceDiErrore.TipiEsitoErrore.NEGATIVO))
                    .collect(Collectors.toList())) {
                for (VoceDiErrore errore : controllo.getErrori()) {
                    if (!verificaErrorePrinc(rispostaWs, controllo, errore)) {
                        addEsitoControlloOnFallitiUlteriori(ControlliWSBundle.getControllo(controllo.getCdControllo()),
                                errore.getSeverity(), errore.getCodiceEsito(), errore.getErrorCode(),
                                errore.getErrorMessage());
                    }
                }
            }
        }
    }

    protected ControlliType produciElencoControlliType(Set<ControlloEseguito> controlli) {
        ControlliType controlliType = null;
        if (!controlli.isEmpty()) {
            controlliType = new ControlliType();
            // sort control list
            List<ControlloEseguito> sorted = sortControlli(controlli);
            for (ControlloEseguito controllo : sorted) {
                ControlloType ctype = buildControlloType(controllo.getEsito().name(), controllo.getDsControllo());
                for (VoceDiErrore errore : controllo.getErrori()) {
                    ErroreType etype = buildErrorType(errore.getErrorCode(), errore.getErrorMessage());
                    ctype.getErrore().add(etype);
                }
                controlliType.getControllo().add(ctype);
            }
        }
        return controlliType;
    }

    protected AggiornamentiEffettuatiType produciAggiornamentiEffettuatiType(
            Set<AggiornamentoEffettuato> aggiornamenti) {
        AggiornamentiEffettuatiType aggiornamentiEffettuatiType = null;

        if (!aggiornamenti.isEmpty()) {
            aggiornamentiEffettuatiType = new AggiornamentiEffettuatiType();
            List<AggiornamentoEffettuato> aggiornamentiSorted = sortAggiornamentiEffettuati(aggiornamenti);
            for (AggiornamentoEffettuato aggiornamento : aggiornamentiSorted) {
                aggiornamentiEffettuatiType.getAggiornamento().add(aggiornamento.getLabel());
            }
        }
        return aggiornamentiEffettuatiType;
    }

    // sort aggiornamenti da effettuare
    protected List<AggiornamentoEffettuato> sortAggiornamentiEffettuati(Set<AggiornamentoEffettuato> aggiornamenti) {
        List<AggiornamentoEffettuato> sortedList = new ArrayList<AggiornamentoEffettuato>(aggiornamenti);
        //
        Collections.sort(sortedList, (c1, c2) -> c1.getNiOrd().compareTo(c2.getNiOrd()));

        // return sorted
        return sortedList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliGenerali()
     */
    @Override
    public Set<ControlloEseguito> getControlliDiSistema() {
        // TODO Auto-generated method stub
        return this.controlliDiSistema;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliGenerali()
     */
    @Override
    public Set<ControlloEseguito> getControlliGenerali() {
        // TODO Auto-generated method stub
        return this.controlliGenerali;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliFallitiUlteriori()
     */
    @Override
    public Set<ControlloEseguito> getControlliFallitiUlteriori() {
        // TODO Auto-generated method stub
        return this.controlliFallitiUlteriori;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliCollegamenti ()
     */
    @Override
    public Map<CSChiave, Set<ControlloEseguito>> getControlliCollegamenti() {
        return this.controlliCollegamenti;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliCollegamento (it.eng.parer.ws.dto.CSChiave)
     */
    @Override
    public Set<ControlloEseguito> getControlliCollegamento(CSChiave csChiave) {
        return getControlliCollegamenti().containsKey(csChiave) ? getControlliCollegamenti().get(csChiave)
                : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliDocPrincipale()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliDocPrincipale() {
        return this.controlliDocPrincipale;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliDocPrincipale(java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliDocPrincipale(String idDocumento) {
        return getControlliDocPrincipale().containsKey(idDocumento) ? getControlliDocPrincipale().get(idDocumento)
                : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAllegati()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliAllegati() {
        return this.controlliAllegati;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAllegato( java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliAllegato(String idDocumento) {
        return getControlliAllegati().containsKey(idDocumento) ? getControlliAllegati().get(idDocumento)
                : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAnnessi()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliAnnessi() {
        return this.controlliAnnessi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAnnesso(java .lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliAnnesso(String idDocumento) {
        return getControlliAnnessi().containsKey(idDocumento) ? getControlliAnnessi().get(idDocumento)
                : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAnnotazioni( )
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliAnnotazioni() {
        return this.controlliAnnotazioni;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getControlliAnnotazione( java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliAnnotazione(String idDocumento) {
        return getControlliAnnotazioni().containsKey(idDocumento) ? getControlliAnnotazioni().get(idDocumento)
                : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponentiDocPrincipale()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliComponentiDocPrincipale() {
        return this.controlliComponenteDocPrincipale;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponenteDocPrincipale(java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliComponenteDocPrincipale(String idComponente) {
        return getControlliComponentiDocPrincipale().containsKey(idComponente)
                ? getControlliComponentiDocPrincipale().get(idComponente) : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponentiDocPrincipale()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliComponentiAllegati() {
        return this.controlliComponenteAllegati;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponenteAllegati(java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliComponenteAllegati(String idComponente) {
        return getControlliComponentiAllegati().containsKey(idComponente)
                ? getControlliComponentiAllegati().get(idComponente) : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponentiAllegati()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliComponentiAnnessi() {
        return this.controlliComponenteAnnessi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponenteAnnessi(java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliComponenteAnnessi(String idComponente) {
        return getControlliComponentiAnnessi().containsKey(idComponente)
                ? getControlliComponentiAnnessi().get(idComponente) : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponentiAllegati()
     */
    @Override
    public Map<String, Set<ControlloEseguito>> getControlliComponentiAnnotazioni() {
        return this.controlliComponenteAnnotazioni;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliComponenteAnnotazioni(java.lang.String)
     */
    @Override
    public Set<ControlloEseguito> getControlliComponenteAnnotazioni(String idComponente) {
        return getControlliComponentiAnnotazioni().containsKey(idComponente)
                ? getControlliComponentiAnnotazioni().get(idComponente) : new HashSet<ControlloEseguito>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# getControlliUnitaDocumentaria()
     */
    @Override
    public Set<ControlloEseguito> getControlliUnitaDocumentaria() {
        // TODO Auto-generated method stub
        return this.controlliDocumento;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#getWarnings()
     */
    @Override
    public Set<ControlloEseguito> getWarnings() {
        // TODO Auto-generated method stub
        return this.warnings;
    }

    @Override
    public void addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato aggiornamento) {
        getAggiornamentiUnitaDocumentaria().add(aggiornamento);
    }

    public abstract void addAggiornamentoDocumenti(AggiornamentoEffettuato aggiornamento, CategoriaDocumento catDoc,
            String key);

    public abstract void addAggiornamentoCompDocumenti(AggiornamentoEffettuato aggiornamento, CategoriaDocumento catDoc,
            String key);

    @Override
    public void addAggiornamentoDocPrincipale(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiDocPrincipale().containsKey(key)
                ? getAggiornamentiDocPrincipale(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiDocPrincipale().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoCompDocPrincipale(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiCompDocPrincipale().containsKey(key)
                ? getAggiornamentiCompDocPrincipale(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiCompDocPrincipale().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoAllegati(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiAllegati().containsKey(key)
                ? getAggiornamentiAllegato(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiAllegati().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoCompAllegati(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiCompAllegati().containsKey(key)
                ? getAggiornamentiCompAllegato(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiCompAllegati().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoAnnessi(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiAnnessi().containsKey(key)
                ? getAggiornamentiAnnesso(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiAnnessi().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoCompAnnessi(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiCompAnnessi().containsKey(key)
                ? getAggiornamentiCompAnnesso(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiCompAnnessi().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoAnnotazioni(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiAnnotazioni().containsKey(key)
                ? getAggiornamentiAnnotazione(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiAnnotazioni().put(key, aggiornamenti);
    }

    @Override
    public void addAggiornamentoCompAnnotazioni(AggiornamentoEffettuato aggiornamento, String key) {
        // recupero aggiornamenti
        Set<AggiornamentoEffettuato> aggiornamenti = getAggiornamentiCompAnnotazioni().containsKey(key)
                ? getAggiornamentiCompAnnotazione(key) : new HashSet<AggiornamentoEffettuato>(0);
        // aggiungo in lista
        aggiornamenti.add(aggiornamento);
        // aggiungo su mappa
        getAggiornamentiCompAnnotazioni().put(key, aggiornamenti);
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiUnitaDocumentaria() {
        // TODO Auto-generated method stub
        return this.aggiornamentiUnitaDocumentaria;
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiDocPrincipale() {
        // TODO Auto-generated method stub
        return this.aggiornamentiDocPrincipale;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiDocPrincipale(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiDocPrincipale.get(key);
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompDocPrincipale() {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompDocPrincipale;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiCompDocPrincipale(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompDocPrincipale.get(key);
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAllegati() {
        // TODO Auto-generated method stub
        return this.aggiornamentiAllegati;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiAllegato(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiAllegati.get(key);
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAllegati() {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiCompAllegato(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati.get(key);
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAnnotazioni() {
        // TODO Auto-generated method stub
        return this.aggiornamentiAnnotazioni;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiAnnotazione(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiAnnotazioni.get(key);
    }

    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAnnotazioni() {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiCompAnnotazione(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati.get(key);
    }

    //
    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAnnessi() {
        // TODO Auto-generated method stub
        return this.aggiornamentiAnnessi;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiAnnesso(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiAnnessi.get(key);
    }

    @Override
    public Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAnnessi() {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati;
    }

    @Override
    public Set<AggiornamentoEffettuato> getAggiornamentiCompAnnesso(String key) {
        // TODO Auto-generated method stub
        return this.aggiornamentiCompAllegati.get(key);
    }

}
