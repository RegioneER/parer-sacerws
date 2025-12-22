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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the IAM_ABIL_ORGANIZ database table.
 */
@Entity
@Table(name = "IAM_ABIL_ORGANIZ")
public class IamAbilOrganiz implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idAbilOrganiz;

    private String flOrganizDefault;

    private BigDecimal idOrganizApplic;

    private IamUser iamUser;

    private List<IamAbilTipoDato> iamAbilTipoDatos = new ArrayList<>();

    private List<IamAutorServ> iamAutorServs = new ArrayList<>();

    public IamAbilOrganiz() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_ABIL_ORGANIZ")
    @GenericGenerator(name = "SIAM_ABIL_ORGANIZ_ID_ABIL_ORGANIZ_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SIAM_ABIL_ORGANIZ"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SIAM_ABIL_ORGANIZ_ID_ABIL_ORGANIZ_GENERATOR")
    public Long getIdAbilOrganiz() {
        return this.idAbilOrganiz;
    }

    public void setIdAbilOrganiz(Long idAbilOrganiz) {
        this.idAbilOrganiz = idAbilOrganiz;
    }

    @Column(name = "FL_ORGANIZ_DEFAULT", columnDefinition = "char(1)")
    public String getFlOrganizDefault() {
        return this.flOrganizDefault;
    }

    public void setFlOrganizDefault(String flOrganizDefault) {
        this.flOrganizDefault = flOrganizDefault;
    }

    @Column(name = "ID_ORGANIZ_APPLIC")
    public BigDecimal getIdOrganizApplic() {
        return this.idOrganizApplic;
    }

    public void setIdOrganizApplic(BigDecimal idOrganizApplic) {
        this.idOrganizApplic = idOrganizApplic;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

    // bi-directional many-to-one association to IamAbilTipoDato
    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
            CascadeType.REMOVE }, mappedBy = "iamAbilOrganiz")
    public List<IamAbilTipoDato> getIamAbilTipoDatos() {
        return this.iamAbilTipoDatos;
    }

    public void setIamAbilTipoDatos(List<IamAbilTipoDato> iamAbilTipoDatos) {
        this.iamAbilTipoDatos = iamAbilTipoDatos;
    }

    // bi-directional many-to-one association to IamAutorServ
    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
            CascadeType.REMOVE }, mappedBy = "iamAbilOrganiz")
    public List<IamAutorServ> getIamAutorServs() {
        return this.iamAutorServs;
    }

    public void setIamAutorServs(List<IamAutorServ> iamAutorServs) {
        this.iamAutorServs = iamAutorServs;
    }
}
