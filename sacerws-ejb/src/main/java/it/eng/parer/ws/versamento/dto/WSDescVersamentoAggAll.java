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

import it.eng.parer.ws.dto.IWSDesc;
import it.eng.parer.ws.utils.Costanti;

/**
 *
 * @author Fioravanti_F
 */
public class WSDescVersamentoAggAll implements IWSDesc {

    @Override
    public String getVersione() {
        return Costanti.WS_AGGIUNTA_VRSN;
    }

    @Override
    public String getNomeWs() {
        return Costanti.WS_AGGIUNTA_DOC_NOME;
    }

    // @Override
    // public String[] getCompatibilitaWS() {
    // return Costanti.WS_AGGIUNTA_COMP;
    // }
}
