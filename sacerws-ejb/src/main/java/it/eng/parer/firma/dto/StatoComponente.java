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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.firma.dto;

public class StatoComponente {
    // variabili di controllo

    public boolean ctrlConfMarcheHasFormatoNonConosciuto = false;

    public boolean ctrlMarcheCrittNegativo = false;
    public boolean ctrlMarcheCatenaNegativo = false;
    public boolean ctrlMarcheCertificatoNegativo = false;
    public boolean ctrlMarcheCrlNegativo = false;
    public boolean ctrlMarcheOcspNegativo = false;

    public boolean componenteMarcato = false;

    public boolean ctrlConfFirmeHasFormatoNonConosciuto = false;
    public boolean ctrlConfFirmeHasFormatoNonConforme = false;
    public boolean ctrlConfFirmeHasNonAmmessoDelib45 = false;

    public boolean componenteFirmato = false;

    public boolean ctrlFirmeCrittWarning = false;
    public boolean ctrlFirmeCrittErrore = false;
    public boolean ctrlFirmeCrittNegativo = false;
    public boolean ctrlFirmeCatenaWarning = false;
    public boolean ctrlFirmeCatenaErrore = false;
    public boolean ctrlFirmeCatenaNegativo = false;
    public boolean ctrlFirmeCertificatoWarning = false;
    public boolean ctrlFirmeCertificatoErrore = false;
    public boolean ctrlFirmeCertificatoScadRev = false;
    public boolean ctrlFirmeCertificatoNoValid = false;
    public boolean ctrlFirmeCertificatoErrato = false;
    public boolean ctrlFirmeCRLWarning = false;
    public boolean ctrlFirmeCRLErrore = false;
    public boolean ctrlFirmeCRLCertRev = false;
    public boolean ctrlFirmeCRLScad = false;
    public boolean ctrlFirmeCRLNoValid = false;
    public boolean ctrlFirmeCRLNoScaric = false;

    public boolean ctrlFirmeOCSPWarning = false;
    public boolean ctrlFirmeOCSPNoValid = false;
    public boolean ctrlFirmeOCSPRev = false;
    public boolean ctrlFirmeOCSPNoScaric = false;

    public boolean compEsitoVerFirmeHasWarn = false;
    public boolean compEsitoVerFirmeHasErr = false;

    public StatoComponente() {
        super();
    }

    public void reset() {
        ctrlConfMarcheHasFormatoNonConosciuto = false;

        ctrlMarcheCrittNegativo = false;
        ctrlMarcheCatenaNegativo = false;
        ctrlMarcheCertificatoNegativo = false;
        ctrlMarcheCrlNegativo = false;
        ctrlMarcheOcspNegativo = false;

        componenteMarcato = false;

        ctrlConfFirmeHasFormatoNonConosciuto = false;
        ctrlConfFirmeHasFormatoNonConforme = false;
        ctrlConfFirmeHasNonAmmessoDelib45 = false;

        componenteFirmato = false;

        ctrlFirmeCrittWarning = false;
        ctrlFirmeCrittErrore = false;
        ctrlFirmeCrittNegativo = false;

        ctrlFirmeCatenaWarning = false;
        ctrlFirmeCatenaErrore = false;
        ctrlFirmeCatenaNegativo = false;

        ctrlFirmeCertificatoWarning = false;
        ctrlFirmeCertificatoErrore = false;
        ctrlFirmeCertificatoScadRev = false;
        ctrlFirmeCertificatoNoValid = false;
        ctrlFirmeCertificatoErrato = false;

        ctrlFirmeCRLWarning = false;
        ctrlFirmeCRLErrore = false;
        ctrlFirmeCRLCertRev = false;
        ctrlFirmeCRLScad = false;
        ctrlFirmeCRLNoValid = false;
        ctrlFirmeCRLNoScaric = false;

        ctrlFirmeOCSPWarning = false;
        ctrlFirmeOCSPNoValid = false;
        ctrlFirmeOCSPNoScaric = false;
        ctrlFirmeOCSPRev = false;

        compEsitoVerFirmeHasWarn = false;
        compEsitoVerFirmeHasErr = false;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (compEsitoVerFirmeHasErr ? 1231 : 1237);
        result = prime * result + (compEsitoVerFirmeHasWarn ? 1231 : 1237);
        result = prime * result + (componenteFirmato ? 1231 : 1237);
        result = prime * result + (componenteMarcato ? 1231 : 1237);
        result = prime * result + (ctrlConfFirmeHasFormatoNonConforme ? 1231 : 1237);
        result = prime * result + (ctrlConfFirmeHasFormatoNonConosciuto ? 1231 : 1237);
        result = prime * result + (ctrlConfFirmeHasNonAmmessoDelib45 ? 1231 : 1237);
        result = prime * result + (ctrlConfMarcheHasFormatoNonConosciuto ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLCertRev ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLErrore ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLNoScaric ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLNoValid ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLScad ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCRLWarning ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCatenaErrore ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCatenaNegativo ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCatenaWarning ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCertificatoErrato ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCertificatoErrore ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCertificatoNoValid ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCertificatoScadRev ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCertificatoWarning ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCrittErrore ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCrittNegativo ? 1231 : 1237);
        result = prime * result + (ctrlFirmeCrittWarning ? 1231 : 1237);
        result = prime * result + (ctrlFirmeOCSPNoScaric ? 1231 : 1237);
        result = prime * result + (ctrlFirmeOCSPNoValid ? 1231 : 1237);
        result = prime * result + (ctrlFirmeOCSPRev ? 1231 : 1237);
        result = prime * result + (ctrlFirmeOCSPWarning ? 1231 : 1237);
        result = prime * result + (ctrlMarcheCatenaNegativo ? 1231 : 1237);
        result = prime * result + (ctrlMarcheCertificatoNegativo ? 1231 : 1237);
        result = prime * result + (ctrlMarcheCrittNegativo ? 1231 : 1237);
        result = prime * result + (ctrlMarcheCrlNegativo ? 1231 : 1237);
        result = prime * result + (ctrlMarcheOcspNegativo ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatoComponente other = (StatoComponente) obj;
        if (compEsitoVerFirmeHasErr != other.compEsitoVerFirmeHasErr)
            return false;
        if (compEsitoVerFirmeHasWarn != other.compEsitoVerFirmeHasWarn)
            return false;
        if (componenteFirmato != other.componenteFirmato)
            return false;
        if (componenteMarcato != other.componenteMarcato)
            return false;
        if (ctrlConfFirmeHasFormatoNonConforme != other.ctrlConfFirmeHasFormatoNonConforme)
            return false;
        if (ctrlConfFirmeHasFormatoNonConosciuto != other.ctrlConfFirmeHasFormatoNonConosciuto)
            return false;
        if (ctrlConfFirmeHasNonAmmessoDelib45 != other.ctrlConfFirmeHasNonAmmessoDelib45)
            return false;
        if (ctrlConfMarcheHasFormatoNonConosciuto != other.ctrlConfMarcheHasFormatoNonConosciuto)
            return false;
        if (ctrlFirmeCRLCertRev != other.ctrlFirmeCRLCertRev)
            return false;
        if (ctrlFirmeCRLErrore != other.ctrlFirmeCRLErrore)
            return false;
        if (ctrlFirmeCRLNoScaric != other.ctrlFirmeCRLNoScaric)
            return false;
        if (ctrlFirmeCRLNoValid != other.ctrlFirmeCRLNoValid)
            return false;
        if (ctrlFirmeCRLScad != other.ctrlFirmeCRLScad)
            return false;
        if (ctrlFirmeCRLWarning != other.ctrlFirmeCRLWarning)
            return false;
        if (ctrlFirmeCatenaErrore != other.ctrlFirmeCatenaErrore)
            return false;
        if (ctrlFirmeCatenaNegativo != other.ctrlFirmeCatenaNegativo)
            return false;
        if (ctrlFirmeCatenaWarning != other.ctrlFirmeCatenaWarning)
            return false;
        if (ctrlFirmeCertificatoErrato != other.ctrlFirmeCertificatoErrato)
            return false;
        if (ctrlFirmeCertificatoErrore != other.ctrlFirmeCertificatoErrore)
            return false;
        if (ctrlFirmeCertificatoNoValid != other.ctrlFirmeCertificatoNoValid)
            return false;
        if (ctrlFirmeCertificatoScadRev != other.ctrlFirmeCertificatoScadRev)
            return false;
        if (ctrlFirmeCertificatoWarning != other.ctrlFirmeCertificatoWarning)
            return false;
        if (ctrlFirmeCrittErrore != other.ctrlFirmeCrittErrore)
            return false;
        if (ctrlFirmeCrittNegativo != other.ctrlFirmeCrittNegativo)
            return false;
        if (ctrlFirmeCrittWarning != other.ctrlFirmeCrittWarning)
            return false;
        if (ctrlFirmeOCSPNoScaric != other.ctrlFirmeOCSPNoScaric)
            return false;
        if (ctrlFirmeOCSPNoValid != other.ctrlFirmeOCSPNoValid)
            return false;
        if (ctrlFirmeOCSPRev != other.ctrlFirmeOCSPRev)
            return false;
        if (ctrlFirmeOCSPWarning != other.ctrlFirmeOCSPWarning)
            return false;
        if (ctrlMarcheCatenaNegativo != other.ctrlMarcheCatenaNegativo)
            return false;
        if (ctrlMarcheCertificatoNegativo != other.ctrlMarcheCertificatoNegativo)
            return false;
        if (ctrlMarcheCrittNegativo != other.ctrlMarcheCrittNegativo)
            return false;
        if (ctrlMarcheCrlNegativo != other.ctrlMarcheCrlNegativo)
            return false;
        if (ctrlMarcheOcspNegativo != other.ctrlMarcheOcspNegativo)
            return false;
        return true;
    }

}
