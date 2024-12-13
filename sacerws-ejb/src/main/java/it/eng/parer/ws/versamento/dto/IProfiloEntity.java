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

public interface IProfiloEntity {

    /**
     * Logica che si è possibile esndere aggiungendo eventuali altri id di profili censiti. (e.g.
     * getIdRecXsdProfiloNormativo != null and getIdRecXsdProfiloGenerale != null and ....etc.)
     *
     * @return true se presente almeno un profilo
     */
    public default boolean hasXsdProfile() {
        return getIdRecUsoXsdProfiloNormativo() != null;
    }

    /* id "tipizzati" per i profili esistenti */
    public Long getIdRecUsoXsdProfiloNormativo();

    public void setIdRecUsoXsdProfiloNormativo(Long idRecUsoXsdProfiloNormativo);

    /* xml canonicalizzati del profilo generato ai controlli */
    public String getDatiC14NProfNormXml();

    public void setDatiC14NProfNormXml(String datiC14NProfNormXml);

}
