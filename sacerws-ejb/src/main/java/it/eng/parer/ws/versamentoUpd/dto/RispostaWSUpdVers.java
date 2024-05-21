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

package it.eng.parer.ws.versamentoUpd.dto;

import it.eng.parer.ws.utils.AvanzamentoWs;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamentoUpd.utils.IRispostaUpdVersWS;
import it.eng.parer.ws.xml.versUpdResp.CodiceEsitoType;
import it.eng.parer.ws.xml.versUpdResp.ControlloFallitoType;
import it.eng.parer.ws.xml.versUpdResp.ErroreType;
import it.eng.parer.ws.xml.versUpdResp.WarningType;

/**
 *
 * @author sinatti_s
 */
public class RispostaWSUpdVers implements IRispostaUpdVersWS {

    private static final long serialVersionUID = 5904891240038140592L;
    private SeverityEnum severity = SeverityEnum.OK;
    private ErrorTypeEnum errorType = ErrorTypeEnum.NOERROR;

    private String errorMessage;
    private String errorCode;
    private AvanzamentoWs avanzamento;

    private ControlloWSResp controlloWs;

    CompRapportoUpdVers compRapportoUpdVers;

    private boolean erroreElementoDoppio;
    private long idElementoDoppio;
    private String urnPartDocumentoDoppio;

    private boolean isErroreDiSistema = false;

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

    public ControlloWSResp getControlloWs() {
        return controlloWs;
    }

    public void setControlloWs(ControlloWSResp controlloWs) {
        this.controlloWs = controlloWs;
    }

    @Override
    public AvanzamentoWs getAvanzamento() {
        return avanzamento;
    }

    @Override
    public void setAvanzamento(AvanzamentoWs avanzamento) {
        this.avanzamento = avanzamento;
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

    public String getUrnPartDocumentoDoppio() {
        return urnPartDocumentoDoppio;
    }

    public void setUrnPartDocumentoDoppio(String urnPartDocumentoDoppio) {
        this.urnPartDocumentoDoppio = urnPartDocumentoDoppio;
    }

    public CompRapportoUpdVers getCompRapportoUpdVers() {
        return compRapportoUpdVers;
    }

    public void setCompRapportoUpdVers(CompRapportoUpdVers compRapportoUpdVers) {
        this.compRapportoUpdVers = compRapportoUpdVers;
    }

    private StatiSessioneVersEnum statoSessioneVersamento = StatiSessioneVersEnum.ASSENTE;

    public StatiSessioneVersEnum getStatoSessioneVersamento() {
        return statoSessioneVersamento;
    }

    public void setStatoSessioneVersamento(StatiSessioneVersEnum statoSessioneVersamento) {
        this.statoSessioneVersamento = statoSessioneVersamento;
    }

    // Nota: per l'esito generale si gestisce la logica del "primo errore / warning" è riportato i successivi vengono
    // ignorati
    // dato che sono presenti nelle varie liste dei controlli ... il primo errore è quello da riportare nell'esito
    // generale

    //
    @Override
    public void setEsitoWsErrBundle(ControlloWSResp controlType, String errCode, Object... params) {
        // non ancora inserito (vedi init)
        if (canSetErrOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.NEGATIVO);

            ErroreType errore = buildErrorType(errCode, params);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            // TODO da verificare
            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    @Override
    public void setEsitoWsErrBundle(ControlloWSResp controlType, String errCode) {
        // non ancora inserito (vedi init)
        if (canSetErrOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.NEGATIVO);

            ErroreType errore = buildErrorType(errCode);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());// TODO: verificare
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    @Override
    public void setEsitoWsError(ControlloWSResp controlType, String errCode, String errMessage) {
        // non ancora inserito (vedi init)
        if (canSetErrOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.NEGATIVO);

            ErroreType errore = buildErrorType(errCode, errMessage);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());// TODO: verificare
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    @Override
    public void setEsitoWsWarning(ControlloWSResp controlType, String errCode, String errMessage) {
        // aggiorno entrambe le tipologie di rapporto di versamento. Alle fine
        // restituirò quella più adatta
        compRapportoUpdVers.getEsitoGeneralePositivo().setCodiceEsito(CodiceEsitoType.WARNING);

        // non ancora inserito (vedi init)
        if (canSetWarnOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.WARNING);

            ErroreType errore = buildErrorType(errCode, errMessage);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());// TODO: verificare
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    @Override
    public void setEsitoWsWarnBundle(ControlloWSResp controlType, String errCode, Object... params) {
        // aggiorno entrambe le tipologie di rapporto di versamento. Alle fine
        // restituirò quella più adatta
        compRapportoUpdVers.getEsitoGeneralePositivo().setCodiceEsito(CodiceEsitoType.WARNING);

        // non ancora inserito (vedi init)
        if (canSetWarnOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.WARNING);

            ErroreType errore = buildErrorType(errCode);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());// TODO: verificare
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    @Override
    public void setEsitoWsWarnBundle(ControlloWSResp controlType, String errCode) {
        // aggiorno entrambe le tipologie di rapporto di versamento. Alle fine
        // restituirò quella più adatta
        compRapportoUpdVers.getEsitoGeneralePositivo().setCodiceEsito(CodiceEsitoType.WARNING);

        // non ancora inserito (vedi init)
        if (canSetWarnOnEsito(controlType, errCode)) {
            //
            compRapportoUpdVers.getEsitoGeneraleNegativo().setCodiceEsito(CodiceEsitoType.WARNING);

            ErroreType errore = buildErrorType(errCode);

            //
            ControlloFallitoType controlloFallitoType = new ControlloFallitoType();
            controlloFallitoType.setTipoControllo(controlType.toString());// TODO: verificare
            compRapportoUpdVers.getEsitoGeneraleNegativo().setControlloFallito(controlloFallitoType);
            compRapportoUpdVers.getEsitoGeneraleNegativo().getControlloFallito().getErrore().add(errore);// add
                                                                                                         // errore
                                                                                                         // TODO:
                                                                                                         // da
                                                                                                         // verificare

            setEsitoGenerale(controlType, errCode, errore);
        }
    }

    /*
     * Si imposta come esito generale: 1- il PRIMO errore di SISTEMA 2- il PRIMO errore riscontrato 3- se esiste errore
     * di tipo WARNING lo "sovrascrivo" con l'errore riscontrato 4- si verifica ANCHE il codice in certi casi potrei
     * avere degli errori imprevisti 666 / 666P gestiti con diversa familiga e non come ERRORISISTEMA
     */
    private boolean canSetErrOnEsito(ControlloWSResp controlType, String errCode) {
        return compRapportoUpdVers.getEsitoGeneraleNegativo().getCodiceEsito().equals(CodiceEsitoType.NON_ATTIVATO)
                || compRapportoUpdVers.getEsitoGeneraleNegativo().getCodiceEsito().equals(CodiceEsitoType.WARNING)
                || controlType.getCdFamiglia()
                        .equalsIgnoreCase(ControlloEseguito.FamigliaControllo.ERRORISISTEMA.name())
                || isGenericOrPesistenceError(errCode);
    }

    /*
     * Si imposta come esito generale: 1- il PRIMO errore di SISTEMA 2- il PRIMO warning riscontrato 3- si verifica
     * ANCHE il codice in certi casi potrei avere degli errori imprevisti 666 / 666P gestiti con diversa familiga e non
     * come ERRORISISTEMA
     */
    private boolean canSetWarnOnEsito(ControlloWSResp controlType, String errCode) {
        return compRapportoUpdVers.getEsitoGeneraleNegativo().getCodiceEsito().equals(CodiceEsitoType.NON_ATTIVATO)
                || controlType.getCdFamiglia()
                        .equalsIgnoreCase(ControlloEseguito.FamigliaControllo.ERRORISISTEMA.name())
                || isGenericOrPesistenceError(errCode);
    }

    private void setEsitoGenerale(ControlloWSResp controlType, String errCode, ErroreType errore) {
        this.controlloWs = controlType;
        this.errorCode = errCode;
        this.errorMessage = errore.getMessaggio();

        // verifica se di sistema / 666 / 666P
        this.isErroreDiSistema = controlType.getCdFamiglia().equalsIgnoreCase(
                ControlloEseguito.FamigliaControllo.ERRORISISTEMA.name()) || isGenericOrPesistenceError(errCode);
    }

    private boolean isGenericOrPesistenceError(String errCode) {
        return errCode.equalsIgnoreCase(MessaggiWSBundle.ERR_666) || errCode.equalsIgnoreCase(MessaggiWSBundle.ERR_666P)
                || errCode.equalsIgnoreCase(MessaggiWSBundle.ERR_666N);
    }

    private ErroreType buildErrorType(String errCode, String errMessage) {
        ErroreType errore = new ErroreType();
        errore.setCodice(errCode);
        errore.setMessaggio(errMessage);
        return errore;
    }

    private ErroreType buildErrorType(String errCode, Object... params) {
        ErroreType errore = new ErroreType();
        errore.setCodice(errCode);
        errore.setMessaggio(
                params.length > 0 ? MessaggiWSBundle.getString(errCode, params) : MessaggiWSBundle.getString(errCode));
        return errore;
    }

    private WarningType buildWarningType(ControlloWSResp controlloWS, ErroreType errore) {
        WarningType warning = new WarningType();
        warning.setTipoControllo(controlloWS.toString());
        warning.getErrore().add(errore);
        return warning;
    }

    public boolean isErroreDiSistema() {
        return isErroreDiSistema;
    }

    public void setErroreDiSistema(boolean isErroreDiSistema) {
        this.isErroreDiSistema = isErroreDiSistema;
    }

}
