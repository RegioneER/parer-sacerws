/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.xml.versResp.ECEsitoExtType;
import it.eng.parer.ws.xml.versResp.EsitoVersamento;

/**
 *
 * @author Fioravanti_F
 */
public class RispostaWS implements IRispostaVersWS {

    private static final long serialVersionUID = 5904891240038140592L;
    private SeverityEnum severity = SeverityEnum.OK;
    private ErrorTypeEnum errorType = ErrorTypeEnum.NOERROR;
    private String errorMessage;
    private String errorCode;
    private AvanzamentoWs avanzamento;
    private EsitoVersamento istanzaEsito;
    private boolean erroreElementoDoppio;
    private long idElementoDoppio;

    @Override
    public SeverityEnum getSeverity() {
	return severity;
    }

    @Override
    public void setSeverity(SeverityEnum severity) {
	this.severity = severity;
    }

    @Override
    public ErrorTypeEnum getErrorType() {
	return errorType;
    }

    @Override
    public void setErrorType(ErrorTypeEnum errorType) {
	this.errorType = errorType;
    }

    @Override
    public String getErrorMessage() {
	return errorMessage;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
	return errorCode;
    }

    @Override
    public void setErrorCode(String errorCode) {
	this.errorCode = errorCode;
    }

    @Override
    public AvanzamentoWs getAvanzamento() {
	return avanzamento;
    }

    @Override
    public void setAvanzamento(AvanzamentoWs avanzamento) {
	this.avanzamento = avanzamento;
    }

    /**
     * @return the istanzaEsito
     */
    public EsitoVersamento getIstanzaEsito() {
	return istanzaEsito;
    }

    /**
     * @param istanzaEsito the istanzaEsito to set
     */
    public void setIstanzaEsito(EsitoVersamento istanzaEsito) {
	this.istanzaEsito = istanzaEsito;
    }

    @Override
    public void setEsitoWsErrBundle(String errCode, Object... params) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.NEGATIVO);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale()
		.setMessaggioErrore(MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void setEsitoWsErrBundle(String errCode) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.NEGATIVO);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale().setMessaggioErrore(MessaggiWSBundle.getString(errCode));
    }

    @Override
    public void setEsitoWsWarnBundle(String errCode, Object... params) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.WARNING);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale()
		.setMessaggioErrore(MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void setEsitoWsWarnBundle(String errCode) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.WARNING);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale().setMessaggioErrore(MessaggiWSBundle.getString(errCode));
    }

    @Override
    public void setEsitoWsError(String errCode, String errMessage) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.NEGATIVO);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale().setMessaggioErrore(errMessage);
    }

    @Override
    public void setEsitoWsWarning(String errCode, String errMessage) {
	istanzaEsito.getEsitoGenerale().setCodiceEsito(ECEsitoExtType.WARNING);
	istanzaEsito.getEsitoGenerale().setCodiceErrore(errCode);
	istanzaEsito.getEsitoGenerale().setMessaggioErrore(errMessage);
    }

    @Override
    public boolean isErroreElementoDoppio() {
	return erroreElementoDoppio;
    }

    @Override
    public void setErroreElementoDoppio(boolean erroreElementoDoppio) {
	this.erroreElementoDoppio = erroreElementoDoppio;
    }

    @Override
    public long getIdElementoDoppio() {
	return idElementoDoppio;
    }

    @Override
    public void setIdElementoDoppio(long idElementoDoppio) {
	this.idElementoDoppio = idElementoDoppio;
    }

}
