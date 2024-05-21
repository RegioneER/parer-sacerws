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
import it.eng.parer.entity.VrsSesFascicoloErr;
import it.eng.parer.entity.VrsSesFascicoloKo;
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
@Stateless(mappedName = "LogSessioneFascicoli")
@LocalBean
public class LogSessioneFascicoli {

    private static final Logger log = LoggerFactory.getLogger(LogSessioneFascicoli.class);
    //

    @EJB
    LogSessioneFascicoliHelper logSessioneFascicoliHelper;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean registraSessioneErrata(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione) {
        // nota l'errore critico di persistenza viene contrassegnato con la lettera P
        // per dare la possibilità all'eventuale chiamante di ripetere il tentativo
        // quando possibile (non è infatti un errore "definitivo" dato dall'input, ma bensì
        // un errore interno provocato da problemi al database)
        RispostaControlli tmpRispostaControlli = null;
        VrsSesFascicoloErr tmpSesFascicoloErr = null;

        try {
            // verifica se posso salvare la sessione errata
            tmpRispostaControlli = logSessioneFascicoliHelper.verificaPartizioneFascicoloErr();
            if (!tmpRispostaControlli.isrBoolean()) {
                // c'è un problema del partizionamento. Si tratta di un'evenienza rara ma
                // possibile. In questo caso non posso salvare la sessione errata.
                // Mi limito a restituire un errore al chiamante e spero che qualcuno lo legga.
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo sessione errata:
            tmpRispostaControlli = logSessioneFascicoliHelper.scriviFascicoloErr(rispostaWs, versamento, sessione);
            if (!tmpRispostaControlli.isrBoolean()) {
                // probabilmente è inutile visto che se arrivo qui è per una runtimeexception
                // che forza il rollback in ogni caso, ma meglio andare sul sicuro, dal momento
                // che non posso escludere di terminare male un salvataggio anche senza una
                // eccezione (magari in altri metodi invocati con lo stesso pattern)
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo gli xml di request & response
            tmpSesFascicoloErr = (VrsSesFascicoloErr) tmpRispostaControlli.getrObject();
            tmpRispostaControlli = logSessioneFascicoliHelper.scriviXmlFascicoloErr(rispostaWs, versamento, sessione,
                    tmpSesFascicoloErr);
            if (!tmpRispostaControlli.isrBoolean()) {
                // probabilmente è inutile visto che se arrivo qui è per una runtimeexception
                // che forza il rollback in ogni caso, ma meglio andare sul sicuro, dal momento
                // che non posso escludere di terminare male un salvataggio anche senza una
                // eccezione (magari in altri metodi invocati con lo stesso pattern)
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            entityManager.flush();
        } catch (Exception e) {
            // l'errore di persistenza viene aggiunto alla pila
            // di errori esistenti e in seguito serializzato nell'xml
            // di risposta.
            context.setRollbackOnly();
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio sessione errata: " + e.getMessage());
            versamento.aggiungErroreFatale(rispostaWs.getCompRapportoVersFascicolo().getEsitoGenerale());
            log.error("Errore interno nella fase di salvataggio sessione errata.", e);
            return false;
        }
        return true;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean registraSessioneFallita(RispostaWSFascicolo rispostaWs, VersFascicoloExt versamento,
            SyncFakeSessn sessione) {
        // nota l'errore critico di persistenza viene contrassegnato con la lettera P
        // per dare la possibilità all'eventuale chiamante di ripetere il tentativo
        // quando possibile (non è infatti un errore "definitivo" dato dall'input, ma bensì
        // un errore interno provocato da problemi al database)
        //
        // gli errori di persistenza in questa fase vengono aggiunti alla pila
        // di errori esistenti e in seguito serializzati nell'xml
        // di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi dati.
        RispostaControlli tmpRispostaControlli = null;
        FasFascicolo tmpFasFascicolo = null;
        VrsFascicoloKo tmpFascicoloKo = null;
        VrsSesFascicoloKo tmpSesFascicoloKo = null;
        try {
            // se sono in errore di fascicolo doppio -> cerco il fascicolo originale
            if (rispostaWs.isErroreElementoDoppio()) {
                tmpFasFascicolo = entityManager.find(FasFascicolo.class, rispostaWs.getIdElementoDoppio());
            } else {
                // altrimenti -> cerco se esiste una precedente registrazione fallita dello
                // stesso fascicolo e se esiste, lo blocco in modo esclusivo: lo devo modificare
                tmpRispostaControlli = logSessioneFascicoliHelper.cercaFascicoloKo(versamento);
                if (!tmpRispostaControlli.isrBoolean()) {
                    context.setRollbackOnly();
                    this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                    return false;
                }
                if (tmpRispostaControlli.getrObject() != null) {
                    tmpFascicoloKo = (VrsFascicoloKo) tmpRispostaControlli.getrObject();
                } else {
                    // verifica se posso salvare la sessione fallita
                    tmpRispostaControlli = logSessioneFascicoliHelper
                            .verificaPartizioneFascicoloByAaStrutKo(versamento);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        // c'è un problema del partizionamento. Si tratta di un'evenienza rara ma
                        // possibile. In questo caso non posso salvare la sessione fallita.
                        // Mi limito a restituire un errore al chiamante e spero che qualcuno lo legga.
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }
                    // se non ho fascicoli buoni o cattivi già creati -> creo fascicolo fallito
                    tmpRispostaControlli = logSessioneFascicoliHelper.scriviFascicoloKo(rispostaWs, versamento,
                            sessione);
                    if (!tmpRispostaControlli.isrBoolean()) {
                        context.setRollbackOnly();
                        this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                        return false;
                    }
                    tmpFascicoloKo = (VrsFascicoloKo) tmpRispostaControlli.getrObject();
                }
            }

            // salvo sessione fallita
            tmpRispostaControlli = logSessioneFascicoliHelper.scriviSessioneFascicoloKo(rispostaWs, versamento,
                    sessione, tmpFascicoloKo, tmpFasFascicolo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            tmpSesFascicoloKo = (VrsSesFascicoloKo) tmpRispostaControlli.getrObject();
            // salvo xml di request & response
            tmpRispostaControlli = logSessioneFascicoliHelper.scriviXmlFascicoloKo(rispostaWs, versamento, sessione,
                    tmpSesFascicoloKo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }

            // salvo i warning e gli errori ulteriori
            tmpRispostaControlli = logSessioneFascicoliHelper.scriviErroriFascicoloKo(rispostaWs, versamento,
                    tmpSesFascicoloKo);
            if (!tmpRispostaControlli.isrBoolean()) {
                context.setRollbackOnly();
                this.impostaErrore(rispostaWs, versamento, tmpRispostaControlli);
                return false;
            }
            entityManager.flush();
        } catch (Exception e) {
            // l'errore di persistenza viene aggiunto alla pila
            // di errori esistenti e in seguito serializzato nell'xml
            // di risposta. Inoltre tenterò di salvare una sessione errata con questi stessi dati.
            context.setRollbackOnly();
            rispostaWs.setSeverity(IRispostaWS.SeverityEnum.ERROR);
            rispostaWs.setEsitoWsErrBundle(MessaggiWSBundle.ERR_666P,
                    "Errore interno nella fase di salvataggio sessione fallita: " + e.getMessage());
            versamento.aggiungErroreFatale(rispostaWs.getCompRapportoVersFascicolo().getEsitoGenerale());
            log.error("Errore interno nella fase di salvataggio sessione fallita.", e);
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
