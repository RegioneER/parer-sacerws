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
package it.eng.parer.ws.dto;

/**
 *
 * @author Fioravanti_F
 */
public class CSChiaveFasc implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String numero;
    private Integer anno;

    public Integer getAnno() {
	return anno;
    }

    public void setAnno(Integer anno) {
	this.anno = anno;
    }

    public String getNumero() {
	return numero;
    }

    public void setNumero(String numero) {
	this.numero = numero;
    }

    @Override
    public String toString() {

	return anno + "-" + numero;

    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((anno == null) ? 0 : anno.hashCode());
	result = prime * result + ((numero == null) ? 0 : numero.hashCode());
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
	CSChiaveFasc other = (CSChiaveFasc) obj;
	if (anno == null) {
	    if (other.anno != null)
		return false;
	} else if (!anno.equals(other.anno))
	    return false;
	if (numero == null) {
	    if (other.numero != null)
		return false;
	} else if (!numero.equals(other.numero))
	    return false;
	return true;
    }

}
