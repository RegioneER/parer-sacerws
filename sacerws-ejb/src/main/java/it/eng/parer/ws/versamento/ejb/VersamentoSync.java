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

package it.eng.parer.ws.versamento.ejb;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 *
 * @author Fioravanti_F
 */
/**
 * Session Bean implementation class VersamentoSync
 */
@Stateless(mappedName = "VersamentoSync")
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class VersamentoSync extends VersamentoSyncBase {
    // questa classe è completamente vuota e deve restare così.
}
