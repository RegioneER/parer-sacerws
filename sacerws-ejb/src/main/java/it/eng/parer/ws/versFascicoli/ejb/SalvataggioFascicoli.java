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

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.VrsFascicoloKo;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "SalvataggioFascicoli")
@LocalBean
public class SalvataggioFascicoli {

    private static final Logger log = LoggerFactory.getLogger(SalvataggioFascicoli.class);
    //
    @EJB
    SalvataggioFascicoliHelper salvataggioFascicoliHelper;

    @EJB
    LogSessioneFascicoliHelper logSessioneFascicoliHelper;

    @EJB
    ElencoVersamentoFascicoli elencoVersamentoFascicoli;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean salvaFascicolo(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento, SyncFakeSessn sessione) {
        // salva i dati del fascicolo
        // eventualmente rimuove il fascicolo KO relativo a tentativi
        // precedenti di versamento
        RispostaControlli tmpRispostaControlli = null;
        FasFascicolo tmpFasFascicolo = null;
        VrsFascicoloKo tmpFascicoloKo = null;
        try {
            // cerco se esiste una precedente registrazione fallita dello
            // stesso fascicolo e se esiste, lo blocco in modo esclusivo: lo devo rimuovere
            // e devo riallocare tutte le sue sessioni al fascicolo che sto creando
            tmpRispostaControlli = logSessioneFascicoliHelper.cercaFascicoloKo(versamento);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            if (tmpRispostaControlli.getrObject() != null) {
                tmpFascicoloKo = (VrsFascicoloKo) tmpRispostaControlli.getrObject();
            }

            // salvo fascicolo
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviFascicolo(versamento, sessione, tmpFascicoloKo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            if (tmpRispostaControlli.getrObject() != null) {
                tmpFasFascicolo = (FasFascicolo) tmpRispostaControlli.getrObject();
            }

            // salvo ammin. partecipanti (se esistono) da profilo generale
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviAmmPartecipanti(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo soggetti coinvolti (se esistono) da profilo generale
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviSoggCoinvolti(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo responsabili (se esistono) da profilo generale
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviResponsabili(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo uo responsabili (se esistono) da profilo generale
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviUoOrgResponsabili(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo request e response
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviRequestResponseFascicolo(rispostaWs, versamento,
                    sessione, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo profili archivistico e generale
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviProfiliXMLFascicolo(versamento, sessione,
                    tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo unità documentarie
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviUnitaDocFascicolo(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo fascicoli collegati
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviLinkFascicolo(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo warning
            tmpRispostaControlli = salvataggioFascicoliHelper.scriviWarningFascicolo(versamento, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo warning aa
            if (versamento.getStrutturaComponenti().isWarningFormatoNumero()) {
                tmpRispostaControlli = salvataggioFascicoliHelper.salvaWarningAATipoFascicolo(versamento);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }

            // salvo su elenco coda elaborazione
            tmpRispostaControlli = elencoVersamentoFascicoli.scriviElvFascDaElabElenco(rispostaWs, versamento, sessione,
                    tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo su elenco stato conservazione
            tmpRispostaControlli = elencoVersamentoFascicoli.scriviStatoConservFascicolo(rispostaWs, versamento,
                    sessione, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo su elenco stato fascicolo
            tmpRispostaControlli = elencoVersamentoFascicoli.scriviStatoFascicoloElenco(rispostaWs, versamento,
                    sessione, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // rimuove fascicolo ko se necessario
            if (tmpFascicoloKo != null) {
                tmpRispostaControlli = salvataggioFascicoliHelper.ereditaVersamentiKoFascicolo(rispostaWs, versamento,
                        sessione, tmpFasFascicolo, tmpFascicoloKo);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
            }
            entityManager.flush();
        } catch (Exception e) {
            // l'errore di persistenza viene aggiunto alla pila
            // di errori esistenti e in seguito serializzato nell'xml
            // di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi dati.
            context.setRollbackOnly();
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio fascicolo: " + e.getMessage());
            versamento.aggiungErroreFatale(rispostaWs.getCompRapportoVersFascicolo().getEsitoGenerale());
            log.error("Errore interno nella fase di salvataggio fascicolo.", e);
            return false;
        }
        return true;
    }

    private void impostaErrore(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            RispostaControlli tmpRispostaControlli) {
        rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        rispostaWs.setEsitoWsError(tmpRispostaControlli.getCodErr(), tmpRispostaControlli.getDsErr());
        versamento.aggiungErroreFatale(rispostaWs.getCompRapportoVersFascicolo().getEsitoGenerale());
    }

}
