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
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the FIR_CERTIF_FIRMATARIO database table.
 */
@Entity
@Cacheable(true)
@Table(name = "FIR_CERTIF_FIRMATARIO")
public class FirCertifFirmatario implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCertifFirmatario;

    private Date dtFinValCertifFirmatario;

    private Date dtIniValCertifFirmatario;

    private String dsSerialCertifFirmatario;

    private FirCertifCa firCertifCa;

    private FirFilePerFirma firFilePerFirma;

    public FirCertifFirmatario() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_CERTIF_FIRMATARIO")
    @GenericGenerator(name = "SFIR_CERTIF_FIRMATARIO_ID_CERTIF_FIRMATARIO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_CERTIF_FIRMATARIO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_CERTIF_FIRMATARIO_ID_CERTIF_FIRMATARIO_GENERATOR")
    public Long getIdCertifFirmatario() {
        return this.idCertifFirmatario;
    }

    public void setIdCertifFirmatario(Long idCertifFirmatario) {
        this.idCertifFirmatario = idCertifFirmatario;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_FIN_VAL_CERTIF_FIRMATARIO")
    public Date getDtFinValCertifFirmatario() {
        return this.dtFinValCertifFirmatario;
    }

    public void setDtFinValCertifFirmatario(Date dtFinValCertifFirmatario) {
        this.dtFinValCertifFirmatario = dtFinValCertifFirmatario;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INI_VAL_CERTIF_FIRMATARIO")
    public Date getDtIniValCertifFirmatario() {
        return this.dtIniValCertifFirmatario;
    }

    public void setDtIniValCertifFirmatario(Date dtIniValCertifFirmatario) {
        this.dtIniValCertifFirmatario = dtIniValCertifFirmatario;
    }

    @Column(name = "DS_SERIAL_CERTIF_FIRMATARIO")
    public String getDsSerialCertifFirmatario() {
        return this.dsSerialCertifFirmatario;
    }

    public void setDsSerialCertifFirmatario(String dsSerialCertifFirmatario) {
        this.dsSerialCertifFirmatario = dsSerialCertifFirmatario;
    }

    // bi-directional many-to-one association to FirCertifCa
    @ManyToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CERTIF_CA")
    public FirCertifCa getFirCertifCa() {
        return this.firCertifCa;
    }

    public void setFirCertifCa(FirCertifCa firCertifCa) {
        this.firCertifCa = firCertifCa;
    }

    // bi-directional one-to-one association to FirFilePerFirma
    @OneToOne(mappedBy = "firCertifFirmatario", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public FirFilePerFirma getFirFilePerFirma() {
        return this.firFilePerFirma;
    }

    public void setFirFilePerFirma(FirFilePerFirma firFilePerFirma) {
        this.firFilePerFirma = firFilePerFirma;
    }

}
