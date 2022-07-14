/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.dto;

import java.io.Serializable;

import it.eng.parer.ws.utils.AvanzamentoWs;

/**
 *
 * @author Fioravanti_F
 */
public interface IRispostaWS extends Serializable {

    public enum SeverityEnum {

        OK, WARNING, ERROR
    }

    public enum ErrorTypeEnum {

        NOERROR, WS_DATA, WS_SIGNATURE, DB_FATAL
    }

    public enum StatiSessioneVersEnum {
        /**
         * la sessione non può essere salvata per mancanza di elementi fondamentali
         */
        ASSENTE,
        /**
         * la sessione verrà salvata come ERRATA, mancanza di metadati identificativi
         */
        ERRATA,
        /**
         * la sessione è ERRATA ma si deve tentare un recupero per convertirla in FALLITA
         */
        DUBBIA,
        /**
         * il versamento è fallito ma ci sono i metadati identificativi di base
         */
        FALLITA,
        /**
         * il versamento è fallito con errore FATALE, verrà scritto sulle sessioni errate
         */
        FATALE,
        /**
         * il versamento è andato a buon fine.
         */
        OK
    }

    String getErrorCode();

    String getErrorMessage();

    ErrorTypeEnum getErrorType();

    SeverityEnum getSeverity();

    AvanzamentoWs getAvanzamento();

    void setErrorCode(String errorCode);

    void setErrorMessage(String errorMessage);

    void setErrorType(ErrorTypeEnum errorType);

    void setSeverity(SeverityEnum severity);

    void setAvanzamento(AvanzamentoWs avanzamento);

    void setEsitoWsErrBundle(String errCode, Object... params);

    void setEsitoWsErrBundle(String errCode);

    void setEsitoWsWarnBundle(String errCode, Object... params);

    void setEsitoWsWarnBundle(String errCode);

    void setEsitoWsError(String errCode, String errMessage);

    void setEsitoWsWarning(String errCode, String errMessage);

}
