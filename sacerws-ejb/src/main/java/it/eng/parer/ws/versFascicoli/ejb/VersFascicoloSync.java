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
package it.eng.parer.ws.versFascicoli.ejb;

import java.util.Date;
import java.util.HashMap;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.ejb.ControlliWS;
import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.TipiWSPerControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versFascicoli.dto.CompRapportoVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;
import it.eng.parer.ws.xml.versfascicoloresp.CodiceEsitoType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoChiamataWSType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoPosNegType;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoGeneraleType;
import it.eng.spagoLite.security.User;

/**
 *
 * @author fioravanti_f
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "VersamentoSync")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class VersFascicoloSync {

    //
    private static final Logger logger = LoggerFactory.getLogger(VersFascicoloSync.class);
    @EJB
    private ControlliWS myControlliWs;
    @EJB
    private SalvataggioFascicoli mySalvataggioFascicoli;
    @EJB
    private LogSessioneFascicoli myLogSessioneFascicoli;
    @EJB
    private RecupSessDubbieFasc myRecupSessDubbieFasc;
    @EJB
    private ControlliSemantici myControlliSemantici;
    @EJB
    private VersFascicoloExtPrsr tmpPrsr;

    public void init(RispostaWSFascicolo rispostaWs, AvanzamentoWs avanzamento, VersFascicoloExt versamento) {
        logger.debug("sono nel metodo init");
        rispostaWs.setSeverity(IRispostaWS.SeverityEnum.OK);
        rispostaWs.setErrorCode("");
        rispostaWs.setErrorMessage("");

        RispostaControlli rs = this.loadWsVersions(versamento);

        // prepara la classe composita esito e la aggancia alla rispostaWS
        CompRapportoVersFascicolo myEsitoComposito = new CompRapportoVersFascicolo();
        rispostaWs.setCompRapportoVersFascicolo(myEsitoComposito);
        // aggancia alla rispostaWS
        rispostaWs.setAvanzamento(avanzamento);
        //
        myEsitoComposito
                .setVersioneRapportoVersamento(versamento.getDescrizione().getVersione(versamento.getWsVersions()));
        //
        myEsitoComposito.setDataRapportoVersamento(XmlDateUtility.dateToXMLGregorianCalendar(new Date()));
        //

        //
        if (!rs.isrBoolean()) {
            rispostaWs.setSeverity(SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(rs.getCodErr(), rs.getDsErr());
        } else {
            myEsitoComposito.setEsitoGenerale(new EsitoGeneraleType());
            myEsitoComposito.getEsitoGenerale().setCodiceEsito(CodiceEsitoType.POSITIVO);
            myEsitoComposito.getEsitoGenerale().setCodiceErrore("");
            myEsitoComposito.getEsitoGenerale().setMessaggioErrore("");
            //
            myEsitoComposito.setEsitoChiamataWS(new ECEsitoChiamataWSType());
            myEsitoComposito.getEsitoChiamataWS().setCodiceEsito(ECEsitoPosNegType.POSITIVO);
            myEsitoComposito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.POSITIVO);
            myEsitoComposito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.POSITIVO);
        }
    }

    protected RispostaControlli loadWsVersions(VersFascicoloExt versamento) {
        RispostaControlli rs = myControlliWs.loadWsVersions(versamento.getDescrizione());
        // if positive ...
        if (rs.isrBoolean()) {
            versamento.setWsVersions((HashMap<String, String>) rs.getrObject());
        }
        return rs;
    }

    public void verificaVersione(String versione, RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento) {
        logger.debug("sono nel metodo verificaVersione");
        CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
        versamento.setVersioneWsChiamata(versione);
        RispostaControlli tmpRispostaControlli;
        myEsito.setVersioneIndiceSIPFascicolo(versione);
        tmpRispostaControlli = myControlliWs.checkVersione(versione, versamento.getDescrizione().getNomeWs(),
                versamento.getWsVersions(), Costanti.TipiWSPerControlli.VERSAMENTO_FASCICOLO);
        if (!tmpRispostaControlli.isrBoolean()) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());
            myEsito.getEsitoChiamataWS().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
            myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
            myEsito.getEsitoChiamataWS().setVersioneWSCorretta(ECEsitoPosNegType.NEGATIVO);
            // se la versione è sbagliata o inesistente, tento comunque di produrre una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
        } else {
            // imposto la versione dell'xml di versamento in via provvisioria al valore del ws;
            // se riuscirò a leggere l'XML imposterò il valore effettivo
            versamento.getStrutturaComponenti().setVersioneIndiceSipNonVerificata(versione);
            versamento.checkVersioneRequest(versione);
            myEsito.setVersioneRapportoVersamento(versamento.getVersioneCalc());
        }
    }

    public void verificaCredenziali(String loginName, String password, String indirizzoIp,
            RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento) {
        logger.debug("sono nel metodo verificaCredenziali");
        CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
        RispostaControlli tmpRispostaControlli = null;
        User tmpUser = null;
        tmpRispostaControlli = myControlliWs.checkCredenziali(loginName, password, indirizzoIp,
                TipiWSPerControlli.VERSAMENTO_FASCICOLO);
        if (tmpRispostaControlli.isrBoolean()) {
            tmpUser = (User) tmpRispostaControlli.getrObject();
            tmpRispostaControlli = myControlliWs.checkAuthWSNoOrg(tmpUser, versamento.getDescrizione(),
                    TipiWSPerControlli.VERSAMENTO_FASCICOLO);
        }
        if (!tmpRispostaControlli.isrBoolean()) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());
            myEsito.getEsitoChiamataWS().setCodiceEsito(ECEsitoPosNegType.NEGATIVO);
            myEsito.getEsitoChiamataWS().setCredenzialiOperatore(ECEsitoPosNegType.NEGATIVO);
            // se le credenziali sono sbagliate, tento comunque di produrre una sessione fallita
            rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.DUBBIA);
        }

        versamento.setLoginName(loginName);
        versamento.setUtente(tmpUser);
    }

    public void parseXML(SyncFakeSessn sessione, RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento) {
        logger.debug("sono nel metodo parseXML");
        CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
        AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

        if (versamento.getUtente() == null) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666, "Errore: l'utente non è autenticato.");
            return;
        }

        try {
            tmpPrsr.parseXML(sessione, rispostaWs, versamento);
        } catch (Exception e) {
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            if (ExceptionUtils.getRootCause(e) instanceof ParamApplicNotFoundException) {
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.FAS_CONFIG_006_001,
                        ((ParamApplicNotFoundException) ExceptionUtils.getRootCause(e)).getNmParamApplic());
            } else {
                rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666,
                        "Errore nella fase di parsing dell'XML del EJB " + e.getMessage());
            }
            logger.error("Eccezione nella fase di parsing dell'XML del EJB ", e);
        }
    }

    public void salvaTutto(SyncFakeSessn sessione, RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento) {
        logger.debug("sono nel metodo salvaTutto");
        CompRapportoVersFascicolo myEsito = rispostaWs.getCompRapportoVersFascicolo();
        AvanzamentoWs tmpAvanzamentoWs = rispostaWs.getAvanzamento();

        // questo strano codice serve a includere nella colletion dei messaggi di errore
        // l'errore inserito in modo diretto tramite i metodi rispostaWs#setEsitoWsErr*
        // Se l'errore fa parte di una serie, inserita tramite versamento#listErrAddError oppure
        // versamento#addError non è necessario effettuare questa aggiunta.
        if (myEsito.getEsitoGenerale().getCodiceEsito() == CodiceEsitoType.NEGATIVO && !versamento.isTrovatiErrori()) {
            versamento.aggiungErroreFatale(myEsito.getEsitoGenerale());
        }
        myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
        myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
        // verifico se è simulazione di salvataggio e ho qualcosa da salvare
        if (!versamento.isSimulaScrittura()
                && rispostaWs.getStatoSessioneVersamento() != IRispostaWS.StatiSessioneVersEnum.ASSENTE) {
            if (rispostaWs.getSeverity() != IRispostaWS.SeverityEnum.ERROR
                    && rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.OK) {
                // salva fascicolo - se versamento è ok o warning
                if (!mySalvataggioFascicoli.salvaFascicolo(rispostaWs, versamento, sessione)) {
                    rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                }
            }
            // non uso un costrutto if ... else,
            // perché dovrei poter salvare una sessione fallita/errata
            // dopo aver tentato di scrivere il fascicolo sul db.
            // In pratica è possibile dover eseguire sia il salvataggio fascicolo
            // che il salvataggio sessione fallita nella stessa chiamata.
            if (rispostaWs.getSeverity() == IRispostaWS.SeverityEnum.ERROR) {
                if (rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.DUBBIA) {
                    // recupero sessione (da dubbia a fallita, se possibile, altrimenti diventa errata)
                    myRecupSessDubbieFasc.recuperaSessioneErrata(rispostaWs, versamento);
                }
                // salva sessione
                if (rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.FALLITA) {
                    myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
                    myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
                    if (!myLogSessioneFascicoli.registraSessioneFallita(rispostaWs, versamento, sessione)) {
                        // se fallisco il salvataggio della sessione fallita, ci riprovo salvando una
                        // sessione errata con gli stessi dati. Anche in questo caso non ho usato un
                        // costrutto if ... else
                        rispostaWs.setStatoSessioneVersamento(IRispostaWS.StatiSessioneVersEnum.ERRATA);
                    }
                }
                if (rispostaWs.getStatoSessioneVersamento() == IRispostaWS.StatiSessioneVersEnum.ERRATA) {
                    myEsito.setErroriUlteriori(versamento.produciEsitoErroriSec());
                    myEsito.setWarningUlteriori(versamento.produciEsitoWarningSec());
                    myLogSessioneFascicoli.registraSessioneErrata(rispostaWs, versamento, sessione);
                }

            }

        }

    }

}
