/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import java.io.Serializable;

/**
 *
 * @author Fioravanti_F
 */
public class VoceDiErrore implements Serializable {

    public enum ResponsabilitaErrore {
        NON_APPLICABILE, UNI_DOC, DOC
    }

    //
    public enum TipiEsitoErrore {
        POSITIVO, NEGATIVO, WARNING, NON_ATTIVATO
    }

    //
    private ResponsabilitaErrore elementoResponsabile;
    //
    private IRispostaWS.SeverityEnum severity;
    private TipiEsitoErrore codiceEsito;
    private String errorCode;
    private String errorMessage;
    private String descElementoErr;
    //
    private DocumentoVers rifDocumentoVers;
    //
    private boolean elementoPrincipale;

    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    public ResponsabilitaErrore getElementoResponsabile() {
        return elementoResponsabile;
    }

    public void setElementoResponsabile(ResponsabilitaErrore elementoResp) {
        this.elementoResponsabile = elementoResp;
    }

    public TipiEsitoErrore getCodiceEsito() {
        return codiceEsito;
    }

    public void setCodiceEsito(TipiEsitoErrore severity) {
        this.codiceEsito = severity;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDescElementoErr() {
        return descElementoErr;
    }

    public void setDescElementoErr(String descElementoErr) {
        this.descElementoErr = descElementoErr;
    }

    public DocumentoVers getRifDocumentoVers() {
        return rifDocumentoVers;
    }

    public void setRifDocumentoVers(DocumentoVers rifDocumentoVers) {
        this.rifDocumentoVers = rifDocumentoVers;
    }

    public boolean isElementoPrincipale() {
        return elementoPrincipale;
    }

    public void setElementoPrincipale(boolean elementoPrincipale) {
        this.elementoPrincipale = elementoPrincipale;
    }
}
