/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.prsr.strategy;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.versamentoUpd.utils.IRispostaUpdVersWS;

public abstract class UpdBasePrsr implements IUpdStrategyPrsr {

    protected IRispostaUpdVersWS rispostaWs;

    protected void setEsitoControlloErr(String ctrlws, String codErr, String dsErr) {
        // è un errore grave ...
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        // esito generale
        rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ctrlws), // TODO: il
                                                                           // controllo
                                                                           // che è
                                                                           // fallito
                                                                           // (verificare
                                                                           // se
                                                                           // esplicitarlo
                                                                           // direttamente
                                                                           // e non
                                                                           // inserirlo
                                                                           // come
                                                                           // parte
                                                                           // della
                                                                           // risposta)
                codErr, dsErr);

    }

    protected void setEsitoControlloErrBundle(String ctrlws, String codErr) {
        // è un errore grave ...
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        // esito generale
        rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ctrlws), // TODO: il
                                                                               // controllo
                                                                               // che è
                                                                               // fallito
                                                                               // (verificare
                                                                               // se
                                                                               // esplicitarlo
                                                                               // direttamente
                                                                               // e non
                                                                               // inserirlo
                                                                               // come
                                                                               // parte
                                                                               // della
                                                                               // risposta)
                codErr);

    }

    protected void setEsitoControlloWarn(String ctrlws, String codErr, String dsErr) {
        // warning (solo se severity diverso da ERROR)
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setSeverity(SeverityEnum.WARNING);
            // esito generale
            rispostaWs.setEsitoWsWarning(ControlliWSBundle.getControllo(ctrlws), // TODO: il
                                                                                 // controllo
                                                                                 // che è
                                                                                 // fallito
                                                                                 // (verificare
                                                                                 // se
                                                                                 // esplicitarlo
                                                                                 // direttamente
                                                                                 // e non
                                                                                 // inserirlo
                                                                                 // come
                                                                                 // parte
                                                                                 // della
                                                                                 // risposta)
                    codErr, dsErr);
        }

    }

    protected void setEsitoControlloWarnBundle(String ctrlws, String codErr) {
        // warning (solo se severity diverso da ERROR)
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setSeverity(SeverityEnum.WARNING);
            // esito generale
            rispostaWs.setEsitoWsWarnBundle(ControlliWSBundle.getControllo(ctrlws), // TODO: il
                    // controllo
                    // che è
                    // fallito
                    // (verificare
                    // se
                    // esplicitarlo
                    // direttamente
                    // e non
                    // inserirlo
                    // come
                    // parte
                    // della
                    // risposta)
                    codErr);
        }
    }

}