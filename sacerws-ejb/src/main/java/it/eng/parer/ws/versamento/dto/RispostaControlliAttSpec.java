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
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import java.util.HashMap;

import it.eng.parer.ws.dto.RispostaControlli;

/**
 *
 * @author Fioravanti_F
 */
public class RispostaControlliAttSpec extends RispostaControlli {

    private static final long serialVersionUID = 1L;
    private long idRecXsdDatiSpec;
    private HashMap<String, DatoSpecifico> datiSpecifici;

    public long getIdRecXsdDatiSpec() {
        return idRecXsdDatiSpec;
    }

    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    public HashMap<String, DatoSpecifico> getDatiSpecifici() {
        return datiSpecifici;
    }

    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
    }
}
