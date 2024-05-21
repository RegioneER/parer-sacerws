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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import java.io.Serializable;

import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;

/**
 *
 * @author Fioravanti_F
 */
public class VoceDiErrore implements Serializable {

    private static final long serialVersionUID = 1L;

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
