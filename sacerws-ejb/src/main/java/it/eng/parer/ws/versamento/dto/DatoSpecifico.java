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

package it.eng.parer.ws.versamento.dto;

/**
 *
 * @author Fioravanti_F
 */
public class DatoSpecifico implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2432324299565034042L;
    private long idDatoSpec;
    private String chiave = null;
    private String valore = null;

    /**
     * @return the idDatoSpec
     */
    public long getIdDatoSpec() {
        return idDatoSpec;
    }

    /**
     * @param idDatoSpec
     *            the idDatoSpec to set
     */
    public void setIdDatoSpec(long idDatoSpec) {
        this.idDatoSpec = idDatoSpec;
    }

    /**
     * @return the chiave
     */
    public String getChiave() {
        return chiave;
    }

    /**
     * @param chiave
     *            the chiave to set
     */
    public void setChiave(String chiave) {
        this.chiave = chiave;
    }

    /**
     * @return the valore
     */
    public String getValore() {
        return valore;
    }

    /**
     * @param valore
     *            the valore to set
     */
    public void setValore(String valore) {
        this.valore = valore;
    }
}
