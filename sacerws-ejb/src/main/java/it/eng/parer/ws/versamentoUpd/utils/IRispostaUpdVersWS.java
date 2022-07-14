/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.IRispostaVersWS;
import it.eng.parer.ws.versamentoUpd.dto.ControlloWSResp;

/**
 *
 * @author sinatti_s
 */
public interface IRispostaUpdVersWS extends IRispostaVersWS {

    // esito generale
    void setEsitoWsErrBundle(ControlloWSResp controlType, String errCode, Object... params);

    void setEsitoWsErrBundle(ControlloWSResp controlType, String errCode);

    void setEsitoWsWarnBundle(ControlloWSResp controlType, String errCode, Object... params);

    void setEsitoWsWarnBundle(ControlloWSResp controlType, String errCode);

    void setEsitoWsError(ControlloWSResp controlType, String errCode, String errMessage);

    void setEsitoWsWarning(ControlloWSResp controlType, String errCode, String errMessage);

    // per compatibilità con precedente gestione si settano degli errori generali di tipo "generici"

    default void setEsitoWsErrBundle(String errCode, Object... params) {
        setEsitoWsErrBundle(genericCtrlWsResp(errCode), errCode, params);
    }

    default void setEsitoWsErrBundle(String errCode) {
        setEsitoWsErrBundle(genericCtrlWsResp(errCode), errCode);
    }

    default void setEsitoWsWarnBundle(String errCode, Object... params) {
        setEsitoWsWarnBundle(genericCtrlWsResp(errCode), errCode, params);
    }

    default void setEsitoWsWarnBundle(String errCode) {
        setEsitoWsWarnBundle(genericCtrlWsResp(errCode), errCode);
    }

    default void setEsitoWsError(String errCode, String errMessage) {
        setEsitoWsError(genericCtrlWsResp(errCode), errCode, errMessage);
    }

    default void setEsitoWsWarning(String errCode, String errMessage) {
        setEsitoWsWarning(genericCtrlWsResp(errCode), errCode, errMessage);
    }

    /* gestione controlli famiglia "erroriSistema" */
    default ControlloWSResp genericCtrlWsResp(String errCode) {
        ControlloWSResp ctrlWSResp = null;
        switch (errCode) {
        case MessaggiWSBundle.ERR_666P:
            ctrlWSResp = ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_ERRORIDB);
            break;

        default:
            ctrlWSResp = ControlliWSBundle.getControllo(ControlliWSBundle.CTRL_GENERIC_ERROR);
            break;
        }
        return ctrlWSResp;
    }
}
