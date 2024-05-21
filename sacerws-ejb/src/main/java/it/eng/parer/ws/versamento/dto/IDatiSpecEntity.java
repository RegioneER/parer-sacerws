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

import java.util.Map;

/**
 *
 * @author Fioravanti_F
 */
public interface IDatiSpecEntity {

    public long getIdRecXsdDatiSpec();

    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec);

    public Map<String, DatoSpecifico> getDatiSpecifici();

    public void setDatiSpecifici(Map<String, DatoSpecifico> datiSpecifici);

    public long getIdRecXsdDatiSpecMigrazione();

    public void setIdRecXsdDatiSpecMigrazione(long idRecXsdDatiSpec);

    public Map<String, DatoSpecifico> getDatiSpecificiMigrazione();

    public void setDatiSpecificiMigrazione(Map<String, DatoSpecifico> datiSpecificiMigrazione);
}
