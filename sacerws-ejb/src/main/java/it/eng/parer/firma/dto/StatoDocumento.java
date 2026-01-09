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

public class StatoDocumento {

    public boolean verFirmeCompCodiceEsitoNeg = false;
    public boolean verFirmeCompCodiceEsitoWar = false;
    public boolean docEsitoCompHasNeg = false;
    public boolean docEsitoCompHasWar = false;

    public StatoDocumento() {
        super();
    }

    public void reset() {
        verFirmeCompCodiceEsitoNeg = false;
        verFirmeCompCodiceEsitoWar = false;
        docEsitoCompHasNeg = false;
        docEsitoCompHasWar = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (docEsitoCompHasNeg ? 1231 : 1237);
        result = prime * result + (docEsitoCompHasWar ? 1231 : 1237);
        result = prime * result + (verFirmeCompCodiceEsitoNeg ? 1231 : 1237);
        result = prime * result + (verFirmeCompCodiceEsitoWar ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatoDocumento other = (StatoDocumento) obj;
        if (docEsitoCompHasNeg != other.docEsitoCompHasNeg)
            return false;
        if (docEsitoCompHasWar != other.docEsitoCompHasWar)
            return false;
        if (verFirmeCompCodiceEsitoNeg != other.verFirmeCompCodiceEsitoNeg)
            return false;
        if (verFirmeCompCodiceEsitoWar != other.verFirmeCompCodiceEsitoWar)
            return false;
        return true;
    }
}
