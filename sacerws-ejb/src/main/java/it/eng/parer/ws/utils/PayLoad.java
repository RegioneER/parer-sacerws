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
package it.eng.parer.ws.utils;

import java.io.Serializable;

/**
 *
 * @author DiLorenzo_F
 */
public class PayLoad implements Serializable {

    private static final long serialVersionUID = -5370296207518208965L;

    private long id;

    private long idStrut;

    private String tipoEntitaSacer;

    private String stato;

    private long aaKeyUnitaDoc;

    private long dtCreazione;

    public PayLoad() {
        // default
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(long idStrut) {
        this.idStrut = idStrut;
    }

    public String getTipoEntitaSacer() {
        return tipoEntitaSacer;
    }

    public void setTipoEntitaSacer(String tipoEntitaSacer) {
        this.tipoEntitaSacer = tipoEntitaSacer;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public long getAaKeyUnitaDoc() {
        return aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(long aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    public long getDtCreazione() {
        return dtCreazione;
    }

    public void setDtCreazione(long dtCreazione) {
        this.dtCreazione = dtCreazione;
    }

}
