/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ext;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.Costanti.VersioneWS;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.versamentoUpd.dto.ControlloEseguito;
import it.eng.parer.ws.versamentoUpd.dto.RispostaWSUpdVers;
import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.AggiornamentoEffettuato;
import it.eng.parer.ws.xml.versUpdReq.IndiceSIPAggiornamentoUnitaDocumentaria;
import it.eng.parer.ws.xml.versUpdResp.AggiornamentiEffettuatiType;
import it.eng.parer.ws.xml.versUpdResp.ControlliFallitiUlterioriType;
import it.eng.parer.ws.xml.versUpdResp.ControlliType;
import it.eng.parer.ws.xml.versUpdResp.ControlloFallitoType;
import it.eng.parer.ws.xml.versUpdResp.ErroreType;
import it.eng.parer.ws.xml.versUpdResp.WarningType;
import it.eng.parer.ws.xml.versUpdResp.WarningsType;

/**
 *
 * @author sinatti_s
 */
public class UpdVersamentoExt extends AbsUpdVersamentoExt {

    private static final long serialVersionUID = 5261426459498072293L;

    private String datiXml;
    private VersioneWS versioneCalc = null;

    private IndiceSIPAggiornamentoUnitaDocumentaria versamento;
    private IWSDesc descrizione;
    private EnumSet<Costanti.ModificatoriWS> modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

    private HashMap<String, String> xmlDefaults;
    private HashMap<String, String> wsVersions;

    private boolean simulaScrittura;

    // to be updated !
    private boolean hasProfiloArchivisticoToUpd = false;
    private boolean hasPAFascicoloPrincipaleToUpd = hasProfiloArchivisticoToUpd && false;
    private boolean hasPAFascicoliSecondariToUp = hasProfiloArchivisticoToUpd && false;
    private boolean hasProfiloUnitaDocumentariaToUpd = false;
    private boolean hasProfiloNormativoToUpd = false;
    private boolean hasDatiSpecificiToBeUpdated = false;
    private boolean hasDatiSpecificiMigrazioneToUpd = false;
    private boolean hasDocumentiCollegatiToUpd = false;// NumeroAllegati, NumeroAnnessi, NumeroAnnotazioni

    // to be updated NULL
    private boolean hasProfiloArchivisticoToUpdNull = false;

    private boolean hasDocumentoPrincipaleToUpd = false; // DocumentoPrinc
    private boolean hasDocPStrutturaOriginaleToUpd = hasDocumentoPrincipaleToUpd && false;// StrutturaOriginale
    private boolean hasAllegatiToUpd = false;// Allegati
    private boolean hasAllProfiloDocumentoToUpd = hasAllegatiToUpd && false;// ProfiloDocumento
    private boolean hasAllDatiSpecificiToUpd = hasAllegatiToUpd && false;// DatiSpecifici
    private boolean hasAllDatiSpecificiMigrazioneToUpd = hasAllegatiToUpd && false;// DatiSpecificiMigrazione
    private boolean hasAllDatiFiscaliToUpd = hasAllegatiToUpd && false;// DatiFiscali
    private boolean hasAllStrutturaOriginaleToUpd = hasAllegatiToUpd && false;// StrutturaOriginale

    private boolean hasAnnotazioniToUpd = false;// Annotazioni
    private boolean hasAnnessiToUpd = false;// Annessi

    @Override
    public void setDatiXml(String datiXml) {
        this.datiXml = datiXml;
    }

    @Override
    public String getDatiXml() {
        return datiXml;
    }

    @Override
    public boolean isSimulaScrittura() {
        return simulaScrittura;
    }

    @Override
    public void setSimulaScrittura(boolean simulaScrittura) {
        this.simulaScrittura = simulaScrittura;
    }

    @Override
    public IWSDesc getDescrizione() {
        return descrizione;
    }

    @Override
    public void setDescrizione(IWSDesc descrizione) {
        this.descrizione = descrizione;
    }

    public IndiceSIPAggiornamentoUnitaDocumentaria getVersamento() {
        return versamento;
    }

    public void setVersamento(IndiceSIPAggiornamentoUnitaDocumentaria versamento) {
        this.versamento = versamento;
    }

    @Override
    public RispostaControlli checkVersioneRequest(String versione) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(true);

        versioneCalc = VersioneWS.evalute(versione);
        modificatoriWS = EnumSet.noneOf(Costanti.ModificatoriWS.class);

        switch (versioneCalc) {
        case V1_5:
            // MEV#23176
            this.modificatoriWS.add(Costanti.ModificatoriWS.TAG_URN_SIP_1_5);
            // end MEV#23176
            // MEV#28309
            this.modificatoriWS.add(Costanti.ModificatoriWS.TAG_DATISPEC_DEL_1_6);
            // MEV#26423
            this.modificatoriWS.add(Costanti.ModificatoriWS.TAG_PROFILI_UPD_1_6);
            break;
        }
        return rispostaControlli;
    }

    @Override
    public EnumSet<Costanti.ModificatoriWS> getModificatoriWSCalc() {
        return this.modificatoriWS;
    }

    @Override
    public String getVersioneCalc() {
        return this.versioneCalc.getVersion();
    }

    public EnumSet<Costanti.ModificatoriWS> getModificatoriWS() {
        return modificatoriWS;
    }

    public void setModificatoriWS(EnumSet<Costanti.ModificatoriWS> modificatoriWS) {
        this.modificatoriWS = modificatoriWS;
    }

    public HashMap<String, String> getXmlDefaults() {
        return xmlDefaults;
    }

    public void setXmlDefaults(HashMap<String, String> xmlDefaults) {
        this.xmlDefaults = xmlDefaults;
    }

    public boolean hasProfiloArchivisticoToUpd() {
        return hasProfiloArchivisticoToUpd;
    }

    public void setHasProfiloArchivisticoToUpd(boolean hasProfiloArchivisticoToUpd) {
        this.hasProfiloArchivisticoToUpd = hasProfiloArchivisticoToUpd;
    }

    public boolean hasProfiloUnitaDocumentariaToUpd() {
        return hasProfiloUnitaDocumentariaToUpd;
    }

    public void setHasProfiloUnitaDocumentariaToUpd(boolean hasProfiloUnitaDocumentariaToUpd) {
        this.hasProfiloUnitaDocumentariaToUpd = hasProfiloUnitaDocumentariaToUpd;
    }

    public boolean hasDatiSpecificiToBeUpdated() {
        return hasDatiSpecificiToBeUpdated;
    }

    public void setHasDatiSpecificiToBeUpdated(boolean hasDatiSpecificiToBeUpdated) {
        this.hasDatiSpecificiToBeUpdated = hasDatiSpecificiToBeUpdated;
    }

    public boolean hasDatiSpecificiMigrazioneToUpd() {
        return hasDatiSpecificiMigrazioneToUpd;
    }

    public void setHasDatiSpecificiMigrazioneToUpd(boolean hasDatiSpecificiMigrazioneToUpd) {
        this.hasDatiSpecificiMigrazioneToUpd = hasDatiSpecificiMigrazioneToUpd;
    }

    public boolean hasDocumentiCollegatiToUpd() {
        return hasDocumentiCollegatiToUpd;
    }

    public void setHasDocumentiCollegatiToUpd(boolean hasDocumentiCollegatiToUpd) {
        this.hasDocumentiCollegatiToUpd = hasDocumentiCollegatiToUpd;
    }

    public boolean hasDocumentoPrincipaleToUpd() {
        return hasDocumentoPrincipaleToUpd;
    }

    public void setHasDocumentoPrincipaleToUpd(boolean hasDocumentoPrincipaleToUpd) {
        this.hasDocumentoPrincipaleToUpd = hasDocumentoPrincipaleToUpd;
    }

    public boolean hasDocPStrutturaOriginaleToUpd() {
        return hasDocPStrutturaOriginaleToUpd;
    }

    public void setHasDocPStrutturaOriginaleToUpd(boolean hasDocPStrutturaOriginaleToUpd) {
        this.hasDocPStrutturaOriginaleToUpd = hasDocPStrutturaOriginaleToUpd;
    }

    public boolean hasPAFascicoloPrincipaleToUpd() {
        return hasPAFascicoloPrincipaleToUpd;
    }

    public void setHasPAFascicoloPrincipaleToUpd(boolean hasPAFascicoloPrincipaleToUpd) {
        this.hasPAFascicoloPrincipaleToUpd = hasPAFascicoloPrincipaleToUpd;
    }

    public boolean hasPAFascicoliSecondariToUp() {
        return hasPAFascicoliSecondariToUp;
    }

    public void setHasPAFascicoliSecondariToUp(boolean hasPAFascicoliSecondariToUp) {
        this.hasPAFascicoliSecondariToUp = hasPAFascicoliSecondariToUp;
    }

    public boolean hasAllegatiToUpd() {
        return hasAllegatiToUpd;
    }

    public void setHasAllegatiToUpd(boolean hasAllegatiToUpd) {
        this.hasAllegatiToUpd = hasAllegatiToUpd;
    }

    public boolean hasAnnotazioniToUpd() {
        return hasAnnotazioniToUpd;
    }

    public void setHasAnnotazioniToUpd(boolean hasAnnotazioniToUpd) {
        this.hasAnnotazioniToUpd = hasAnnotazioniToUpd;
    }

    public boolean hasAnnessiToUpd() {
        return hasAnnessiToUpd;
    }

    public void setHasAnnessiToUpd(boolean hasAnnessiToUpd) {
        this.hasAnnessiToUpd = hasAnnessiToUpd;
    }

    // has doc principale/annessi/allegati/annotazioni
    public boolean hasDocumentiToUpd() {
        return this.hasDocumentoPrincipaleToUpd() || this.hasAnnessiToUpd() || this.hasAnnotazioniToUpd()
                || this.hasAllegatiToUpd();
    }

    public boolean hasAllProfiloDocumentoToUpd() {
        return hasAllProfiloDocumentoToUpd;
    }

    public void setHasAllProfiloDocumentoToUpd(boolean hasAllProfiloDocumentoToUpd) {
        this.hasAllProfiloDocumentoToUpd = hasAllProfiloDocumentoToUpd;
    }

    public boolean hasProfiloNormativoToUpd() {
        return hasProfiloNormativoToUpd;
    }

    public void setHasProfiloNormativoToUpd(boolean hasProfiloNormativoToUpd) {
        this.hasProfiloNormativoToUpd = hasProfiloNormativoToUpd;
    }

    public boolean hasAllDatiSpecificiToUpd() {
        return hasAllDatiSpecificiToUpd;
    }

    public void setHasAllDatiSpecificiToUpd(boolean hasAllDatiSpecificiToUpd) {
        this.hasAllDatiSpecificiToUpd = hasAllDatiSpecificiToUpd;
    }

    public boolean hasAllDatiSpecificiMigrazioneToUpd() {
        return hasAllDatiSpecificiMigrazioneToUpd;
    }

    public void setHasAllDatiSpecificiMigrazioneToUpd(boolean hasAllDatiSpecificiMigrazioneToUpd) {
        this.hasAllDatiSpecificiMigrazioneToUpd = hasAllDatiSpecificiMigrazioneToUpd;
    }

    public boolean hasAllDatiFiscaliToUpd() {
        return hasAllDatiFiscaliToUpd;
    }

    public void setHasAllDatiFiscaliToUpd(boolean hasAllDatiFiscaliToUpd) {
        this.hasAllDatiFiscaliToUpd = hasAllDatiFiscaliToUpd;
    }

    public boolean hasAllStrutturaOriginaleToUpd() {
        return hasAllStrutturaOriginaleToUpd;
    }

    public void setHasAllStrutturaOriginaleToUpd(boolean hasAllStrutturaOriginaleToUpd) {
        this.hasAllStrutturaOriginaleToUpd = hasAllStrutturaOriginaleToUpd;
    }

    public boolean hasProfiloArchivisticoToUpdNull() {
        return hasProfiloArchivisticoToUpdNull;
    }

    public void setHasProfiloArchivisticoToUpdNull(boolean hasProfiloArchivisticoToUpdNull) {
        this.hasProfiloArchivisticoToUpdNull = hasProfiloArchivisticoToUpdNull;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#produciControlliAllegato (java.lang.String)
     */
    @Override
    public ControlliType produciControlliAllegato(String idDocumento) {
        return produciElencoControlliType(getControlliAllegato(idDocumento));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#produciControlliAnnesso( java.lang.String)
     */
    @Override
    public ControlliType produciControlliAnnesso(String idDocumento) {
        return produciElencoControlliType(getControlliAnnesso(idDocumento));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliAnnotazione(java.lang.String)
     */
    @Override
    public ControlliType produciControlliAnnotazione(String idDocumento) {
        return produciElencoControlliType(getControlliAnnotazione(idDocumento));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * produciControlliCollegamento(it.eng.parer.ws.dto.CSChiave)
     */
    @Override
    public ControlliType produciControlliCollegamento(CSChiave csChiave) {
        return produciElencoControlliType(getControlliCollegamento(csChiave));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliComponenteAllegati(java.lang.String)
     */
    @Override
    public ControlliType produciControlliComponenteAllegati(String idComponente) {
        return produciElencoControlliType(getControlliComponenteAllegati(idComponente));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliComponenteAnnessi(java.lang.String)
     */
    @Override
    public ControlliType produciControlliComponenteAnnessi(String idComponente) {
        return produciElencoControlliType(getControlliComponenteAnnessi(idComponente));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * produciControlliComponenteAnnotazioni(java.lang.String)
     */
    @Override
    public ControlliType produciControlliComponenteAnnotazioni(String idComponente) {
        return produciElencoControlliType(getControlliComponenteAnnotazioni(idComponente));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#
     * produciControlliComponenteDocPrincipale(java.lang.String)
     */
    @Override
    public ControlliType produciControlliComponenteDocPrincipale(String idComponente) {
        return produciElencoControlliType(getControlliComponenteDocPrincipale(idComponente));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliDocPrincipale(java.lang.String)
     */
    @Override
    public ControlliType produciControlliDocPrincipale(String idDocumento) {
        return produciElencoControlliType(getControlliDocPrincipale(idDocumento));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliFallitiUlteriori()
     */
    @Override
    public ControlliFallitiUlterioriType produciControlliFallitiUlteriori(RispostaWSUpdVers rispostaWs) {
        ControlliFallitiUlterioriType controlliFallitiUlterioriType = null;

        // clean first (in quanto sono dinamici ciò significa che ogni volta che vengono invocati dovranno essere
        // ri-formulati sulla base degli errori percepiti)
        this.getControlliFallitiUlteriori().clear();

        this.produciControlliFallitiFromGenUD(rispostaWs);

        if (!getControlliFallitiUlteriori().isEmpty()) {
            controlliFallitiUlterioriType = new ControlliFallitiUlterioriType();
            // sort control list
            List<ControlloEseguito> sorted = sortControlli(getControlliFallitiUlteriori());
            for (ControlloEseguito controllo : sorted) {
                ControlloFallitoType ctype = buildControlloFallitoType(controllo.getEsito().name(),
                        controllo.getDsControllo());
                for (VoceDiErrore errore : controllo.getErrori()) {
                    ErroreType etype = buildErrorType(errore.getErrorCode(), errore.getErrorMessage());
                    ctype.getErrore().add(etype);
                }
                controlliFallitiUlterioriType.getControlloFallito().add(ctype);
            }
        }
        return controlliFallitiUlterioriType;
    }

    @Override
    public ControlliType produciControlliGenerali() {
        return produciElencoControlliType(getControlliGenerali());
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli# produciControlliUnitaDocumentaria()
     */
    @Override
    public ControlliType produciControlliUnitaDocumentaria() {
        return produciElencoControlliType(getControlliUnitaDocumentaria());
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#produciWarnings()
     */
    @Override
    public WarningsType produciWarnings() {
        WarningsType warningsType = null;
        if (!getWarnings().isEmpty()) {
            warningsType = new WarningsType();
            // sort control list
            List<ControlloEseguito> sorted = sortControlli(getWarnings());
            for (ControlloEseguito controllo : sorted) {
                WarningType wtype = buildWarningType(controllo.getDsControllo());
                for (VoceDiErrore errore : controllo.getErrori()) {
                    ErroreType etype = buildErrorType(errore.getErrorCode(), errore.getErrorMessage());
                    wtype.getErrore().add(etype);
                }
                warningsType.getWarning().add(wtype);
            }
        }
        return warningsType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.parer.ws.versamentoUpd.dto.IControlliMultipli#resetControlli()
     */
    @Override
    public void resetControlli() {
        // clear list
        this.controlliGenerali.clear();
        this.warnings.clear();
        this.controlliDocumento.clear();
        this.controlliFallitiUlteriori.clear();
        this.controlliDocPrincipale.clear();
        this.controlliAllegati.clear();
        this.controlliAnnotazioni.clear();
        this.controlliComponenteDocPrincipale.clear();
        this.controlliComponenteAllegati.clear();
        this.controlliComponenteAnnessi.clear();
        this.controlliComponenteAnnotazioni.clear();
    }

    @Override
    public void addAggiornamentoDocumenti(AggiornamentoEffettuato aggiornamento, CategoriaDocumento catDoc,
            String key) {
        switch (catDoc) {
        case Principale:
            addAggiornamentoDocPrincipale(aggiornamento, key);
            break;
        case Allegato:
            addAggiornamentoAllegati(aggiornamento, key);
            break;
        case Annesso:
            addAggiornamentoAnnessi(aggiornamento, key);
            break;
        case Annotazione:
            addAggiornamentoAnnotazioni(aggiornamento, key);
            break;
        }
    }

    public void addAggiornamentoCompDocumenti(AggiornamentoEffettuato aggiornamento, CategoriaDocumento catDoc,
            String key) {
        switch (catDoc) {
        case Principale:
            addAggiornamentoCompDocPrincipale(aggiornamento, key);
            break;
        case Allegato:
            addAggiornamentoCompAllegati(aggiornamento, key);
            break;
        case Annesso:
            addAggiornamentoCompAnnessi(aggiornamento, key);
            break;
        case Annotazione:
            addAggiornamentoCompAnnotazioni(aggiornamento, key);
            break;
        }
    }

    public AggiornamentiEffettuatiType produciAggiornamentiUnitaDocumentaria() {
        return produciAggiornamentiEffettuatiType(getAggiornamentiUnitaDocumentaria());
    }

    public AggiornamentiEffettuatiType produciAggiornamentiDocPrincipale(String idDocumento) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiDocPrincipale(idDocumento));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiCompDocPrincipale(String idComponente) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiCompDocPrincipale(idComponente));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiAllegato(String idDocumento) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiAllegato(idDocumento));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiCompAllegato(String idComponente) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiCompAllegato(idComponente));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiAnnesso(String idDocumento) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiAnnesso(idDocumento));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiCompAnnesso(String idComponente) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiCompAnnesso(idComponente));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiAnnotazione(String idDocumento) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiAnnotazione(idDocumento));
    }

    public AggiornamentiEffettuatiType produciAggiornamentiCompAnnotazione(String idComponente) {
        return produciAggiornamentiEffettuatiType(getAggiornamentiCompAnnotazione(idComponente));
    }

    public HashMap<String, String> getWsVersions() {
        return wsVersions;
    }

    public void setWsVersions(HashMap<String, String> wsVersions) {
        this.wsVersions = wsVersions;
    }

}
