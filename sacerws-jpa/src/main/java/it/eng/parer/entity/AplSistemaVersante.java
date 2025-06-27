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

package it.eng.parer.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import it.eng.parer.granted_entity.UsrUser;

/**
 * The persistent class for the APL_SISTEMA_VERSANTE database table.
 */
@Entity
@Table(schema = "SACER_IAM", name = "APL_SISTEMA_VERSANTE")
public class AplSistemaVersante implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSistemaVersante;

    private String cdVersione;

    private String dsSistemaVersante;

    private String nmSistemaVersante;

    private List<UsrUser> usrUsers = new ArrayList<>();

    public AplSistemaVersante() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_SISTEMA_VERSANTE")
    public Long getIdSistemaVersante() {
	return this.idSistemaVersante;
    }

    public void setIdSistemaVersante(Long idSistemaVersante) {
	this.idSistemaVersante = idSistemaVersante;
    }

    @XmlTransient
    @Column(name = "CD_VERSIONE")
    public String getCdVersione() {
	return this.cdVersione;
    }

    public void setCdVersione(String cdVersione) {
	this.cdVersione = cdVersione;
    }

    @XmlTransient
    @Column(name = "DS_SISTEMA_VERSANTE")
    public String getDsSistemaVersante() {
	return this.dsSistemaVersante;
    }

    public void setDsSistemaVersante(String dsSistemaVersante) {
	this.dsSistemaVersante = dsSistemaVersante;
    }

    @Column(name = "NM_SISTEMA_VERSANTE")
    public String getNmSistemaVersante() {
	return this.nmSistemaVersante;
    }

    public void setNmSistemaVersante(String nmSistemaVersante) {
	this.nmSistemaVersante = nmSistemaVersante;
    }

    // bi-directional many-to-one association to UsrUser
    @OneToMany(mappedBy = "aplSistemaVersante")
    @XmlTransient
    public List<UsrUser> getUsrUsers() {
	return this.usrUsers;
    }

    public void setUsrUsers(List<UsrUser> usrUsers) {
	this.usrUsers = usrUsers;
    }

}
