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
package it.eng.parer.ws.versFascicoli.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.eng.parer.ws.dto.IErroriMultipli;
import it.eng.parer.ws.dto.IRispostaWS;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import it.eng.parer.ws.xml.versfascicoloresp.ECErroreType;
import it.eng.parer.ws.xml.versfascicoloresp.ECErroriUlterioriType;
import it.eng.parer.ws.xml.versfascicoloresp.ECWarningType;
import it.eng.parer.ws.xml.versfascicoloresp.ECWarningUlterioriType;
import it.eng.parer.ws.xml.versfascicoloresp.EsitoGeneraleType;
import it.eng.spagoLite.security.User;

/**
 *
 * @author fioravanti_f
 */
public abstract class AbsVersFascicoloExt implements IVersFascicoliExt, IErroriMultipli {

    private static final long serialVersionUID = 1L;
    private User utente;
    private String versioneWsChiamata;
    private String loginName;
    //
    // private HashMap<String, String> xmlDefaults;
    //
    private StrutturaVersFascicolo strutturaComponenti;
    private List<VoceDiErrore> erroriTrovati = new ArrayList<VoceDiErrore>();
    //
    private int numErrori = 0;
    private int numWarning = 0;
    //
    private HashMap<String, String> xmlDefaults;
    //
    private HashMap<String, String> wsVersions;

    @Override
    public String getLoginName() {
        return loginName;
    }

    @Override
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public User getUtente() {
        return utente;
    }

    @Override
    public void setUtente(User utente) {
        this.utente = utente;
    }

    @Override
    public String getVersioneWsChiamata() {
        return versioneWsChiamata;
    }

    @Override
    public void setVersioneWsChiamata(String versioneWsChiamata) {
        this.versioneWsChiamata = versioneWsChiamata;
    }

    public HashMap<String, String> getXmlDefaults() {
        return xmlDefaults;
    }

    public void setXmlDefaults(HashMap<String, String> xmlDefaults) {
        this.xmlDefaults = xmlDefaults;
    }

    @Override
    public StrutturaVersFascicolo getStrutturaComponenti() {
        return strutturaComponenti;
    }

    @Override
    public void setStrutturaComponenti(StrutturaVersFascicolo strutturaComponenti) {
        this.strutturaComponenti = strutturaComponenti;
    }

    @Override
    public List<VoceDiErrore> getErroriTrovati() {
        return erroriTrovati;
    }

    @Override
    public boolean isTrovatiErrori() {
        return numErrori > 0;
    }

    @Override
    public boolean isTrovatiWarning() {
        return numWarning > 0;
    }

    @Override
    public VoceDiErrore calcolaErrorePrincipale() {
        numErrori = 0;
        numWarning = 0;
        VoceDiErrore tmpVdEErr = null;
        VoceDiErrore tmpVdEWarn = null;
        for (VoceDiErrore tmpVdE : erroriTrovati) {
            if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                tmpVdEErr = tmpVdE;
                numErrori++;
            }
            if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                tmpVdEWarn = tmpVdE;
                numWarning++;
            }
        }
        if (tmpVdEErr != null) {
            this.impostaErrorePrincipale(tmpVdEErr);
            return tmpVdEErr;
        } else if (tmpVdEWarn != null) {
            this.impostaErrorePrincipale(tmpVdEWarn);
            return tmpVdEWarn;
        } else {
            return null;
        }
    }

    public void aggiungErroreFatale(EsitoGeneraleType esito) {
        VoceDiErrore tmpErrore;
        tmpErrore = this.addError("", esito.getCodiceErrore(), esito.getMessaggioErrore());
        this.impostaErrorePrincipale(tmpErrore);
    }

    @Override
    public void aggiungErroreFatale(String errCode, String errMessage) {
        VoceDiErrore tmpErrore;
        tmpErrore = this.addError("", errCode, errMessage);
        this.impostaErrorePrincipale(tmpErrore);
    }

    @Override
    public void listErrAddWarning(String descElemento, String errCode, Object... params) {
        this.addWarning(descElemento, errCode, MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void listErrAddWarning(String descElemento, String errCode) {
        this.addWarning(descElemento, errCode, MessaggiWSBundle.getString(errCode));
    }

    @Override
    public void listErrAddError(String descElemento, String errCode, Object... params) {
        this.addError(descElemento, errCode, MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void listErrAddError(String descElemento, String errCode) {
        this.addError(descElemento, errCode, MessaggiWSBundle.getString(errCode));
    }

    @Override
    public VoceDiErrore addWarning(String descElemento, String errCode, String errMessage) {
        VoceDiErrore tmpErrore = new VoceDiErrore();
        tmpErrore.setSeverity(IRispostaWS.SeverityEnum.WARNING);
        tmpErrore.setCodiceEsito(VoceDiErrore.TipiEsitoErrore.WARNING);
        tmpErrore.setElementoResponsabile(VoceDiErrore.ResponsabilitaErrore.UNI_DOC);
        tmpErrore.setErrorCode(errCode);
        tmpErrore.setErrorMessage(errMessage);
        tmpErrore.setDescElementoErr(descElemento);
        erroriTrovati.add(tmpErrore);
        numWarning++;
        return tmpErrore;
    }

    @Override
    public VoceDiErrore addError(String descElemento, String errCode, String errMessage) {
        VoceDiErrore tmpErrore = new VoceDiErrore();
        tmpErrore.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        tmpErrore.setCodiceEsito(VoceDiErrore.TipiEsitoErrore.NEGATIVO);
        tmpErrore.setElementoResponsabile(VoceDiErrore.ResponsabilitaErrore.UNI_DOC);
        tmpErrore.setErrorCode(errCode);
        tmpErrore.setErrorMessage(errMessage);
        tmpErrore.setDescElementoErr(descElemento);
        erroriTrovati.add(tmpErrore);
        numErrori++;
        return tmpErrore;
    }

    @Override
    public ECErroriUlterioriType produciEsitoErroriSec() {
        ECErroriUlterioriType erroriSecondariType = null;
        if (numErrori > 1) {
            erroriSecondariType = new ECErroriUlterioriType();
            for (VoceDiErrore tmpVdE : erroriTrovati) {
                if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO
                        && !tmpVdE.isElementoPrincipale()) {
                    ECErroreType tmpErrore = new ECErroreType();
                    tmpErrore.setCodiceErrore(tmpVdE.getErrorCode());
                    tmpErrore.setMessaggioErrore(tmpVdE.getErrorMessage());
                    erroriSecondariType.getErrore().add(tmpErrore);
                }
            }
        }

        return erroriSecondariType;
    }

    @Override
    public ECWarningUlterioriType produciEsitoWarningSec() {
        ECWarningUlterioriType warningSecondariType = null;
        if ((numErrori > 0 && numWarning > 0) || (numWarning > 1)) {
            warningSecondariType = new ECWarningUlterioriType();
            for (VoceDiErrore tmpVdE : erroriTrovati) {
                if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING && !tmpVdE.isElementoPrincipale()) {
                    ECWarningType tmpWarning = new ECWarningType();
                    tmpWarning.setCodiceWarning(tmpVdE.getErrorCode());
                    tmpWarning.setMessaggioWarning(tmpVdE.getErrorMessage());
                    warningSecondariType.getWarning().add(tmpWarning);
                }
            }
        }

        return warningSecondariType;
    }

    private void impostaErrorePrincipale(VoceDiErrore vde) {
        for (VoceDiErrore tmpVdE : erroriTrovati) {
            tmpVdE.setElementoPrincipale(false);
        }
        vde.setElementoPrincipale(true);
    }

    public HashMap<String, String> getWsVersions() {
        return wsVersions;
    }

    public void setWsVersions(HashMap<String, String> wsVersions) {
        this.wsVersions = wsVersions;
    }
}
