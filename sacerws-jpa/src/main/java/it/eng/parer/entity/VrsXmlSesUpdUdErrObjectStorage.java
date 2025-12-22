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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "VRS_XML_SES_UPD_UD_ERR_OBJECT_STORAGE")
public class VrsXmlSesUpdUdErrObjectStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlSesUpdUdErrObjectStorage;
    private VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr;
    private DecBackend decBackend;
    private String nmTenant;
    private String nmBucket;
    private String cdKeyFile;
    private BigDecimal idStrut;

    public VrsXmlSesUpdUdErrObjectStorage() {
        // hibernate constructor
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_XML_SES_UPD_UD_ERR_OBJECT_STORAGE")
    public Long getIdXmlSesUpdUdErrObjectStorage() {
        return idXmlSesUpdUdErrObjectStorage;
    }

    public void setIdXmlSesUpdUdErrObjectStorage(Long idXmlSesUpdUdErrObjectStorage) {
        this.idXmlSesUpdUdErrObjectStorage = idXmlSesUpdUdErrObjectStorage;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SES_UPD_UNITA_DOC_ERR")
    public VrsSesUpdUnitaDocErr getVrsSesUpdUnitaDocErr() {
        return vrsSesUpdUnitaDocErr;
    }

    public void setVrsSesUpdUnitaDocErr(VrsSesUpdUnitaDocErr vrsSesUpdUnitaDocErr) {
        this.vrsSesUpdUnitaDocErr = vrsSesUpdUnitaDocErr;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEC_BACKEND")
    public DecBackend getDecBackend() {
        return decBackend;
    }

    public void setDecBackend(DecBackend decBackend) {
        this.decBackend = decBackend;
    }

    @Column(name = "NM_TENANT")
    public String getNmTenant() {
        return nmTenant;
    }

    public void setNmTenant(String nmTenant) {
        this.nmTenant = nmTenant;
    }

    @Column(name = "NM_BUCKET")
    public String getNmBucket() {
        return nmBucket;
    }

    public void setNmBucket(String nmBucket) {
        this.nmBucket = nmBucket;
    }

    @Column(name = "CD_KEY_FILE")
    public String getCdKeyFile() {
        return cdKeyFile;
    }

    public void setCdKeyFile(String cdKeyFile) {
        this.cdKeyFile = cdKeyFile;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }
}
