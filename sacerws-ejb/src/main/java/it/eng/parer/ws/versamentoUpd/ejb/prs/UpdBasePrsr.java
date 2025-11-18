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
package it.eng.parer.ws.versamentoUpd.ejb.prs;

import it.eng.parer.ws.dto.IRispostaWS.SeverityEnum;
import it.eng.parer.ws.utils.ControlliWSBundle;
import it.eng.parer.ws.versamentoUpd.utils.IRispostaUpdVersWS;

public abstract class UpdBasePrsr {

    protected void setEsitoControlloErr(String ctrlws, String codErr, String dsErr,
            IRispostaUpdVersWS rispostaWs) {
        // è un errore grave ...
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        // esito generale
        rispostaWs.setEsitoWsError(ControlliWSBundle.getControllo(ctrlws), codErr, dsErr);

    }

    protected void setEsitoControlloErrBundle(String ctrlws, String codErr,
            IRispostaUpdVersWS rispostaWs) {
        // è un errore grave ...
        rispostaWs.setSeverity(SeverityEnum.ERROR);
        // esito generale
        rispostaWs.setEsitoWsErrBundle(ControlliWSBundle.getControllo(ctrlws), codErr);

    }

    protected void setEsitoControlloWarn(String ctrlws, String codErr, String dsErr,
            IRispostaUpdVersWS rispostaWs) {
        // warning (solo se severity diverso da ERROR)
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setSeverity(SeverityEnum.WARNING);
            // esito generale
            rispostaWs.setEsitoWsWarning(ControlliWSBundle.getControllo(ctrlws), codErr, dsErr);
        }

    }

    protected void setEsitoControlloWarnBundle(String ctrlws, String codErr,
            IRispostaUpdVersWS rispostaWs) {
        // warning (solo se severity diverso da ERROR)
        if (rispostaWs.getSeverity() != SeverityEnum.ERROR) {
            rispostaWs.setSeverity(SeverityEnum.WARNING);
            // esito generale
            rispostaWs.setEsitoWsWarnBundle(ControlliWSBundle.getControllo(ctrlws), codErr);
        }
    }

}
