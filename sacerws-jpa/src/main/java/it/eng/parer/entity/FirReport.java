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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the FIR_REPORT database table.
 */
@Entity
@Table(name = "FIR_REPORT")
public class FirReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idFirReport;

    private AroCompDoc aroCompDoc;

    private List<FirUrnReport> firUrnReports = new ArrayList<>();

    private byte[] blContenutoReport;

    private String cdKeyFile;

    private String nmBucket;

    private DecBackend decBackend;

    public FirReport() {
        // hibernate
    }

    @Id
    @Column(name = "ID_FIR_REPORT")
    @GenericGenerator(name = "SFIR_REPORT_ID_FIR_REPORT_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SFIR_REPORT"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SFIR_REPORT_ID_FIR_REPORT_GENERATOR")
    public Long getIdFirReport() {
        return this.idFirReport;
    }

    public void setIdFirReport(Long idFirReport) {
        this.idFirReport = idFirReport;
    }

    @Lob()
    @Column(name = "BL_CONTENUTO_REPORT")
    public byte[] getBlContenutoReport() {
        return blContenutoReport;
    }

    public void setBlContenutoReport(byte[] blContenutoReport) {
        this.blContenutoReport = blContenutoReport;
    }

    @Column(name = "CD_KEY_FILE")
    public String getCdKeyFile() {
        return this.cdKeyFile;
    }

    public void setCdKeyFile(String cdKeyFile) {
        this.cdKeyFile = cdKeyFile;
    }

    @Column(name = "NM_BUCKET")
    public String getNmBucket() {
        return this.nmBucket;
    }

    public void setNmBucket(String nmBucket) {
        this.nmBucket = nmBucket;
    }

    // bi-directional many-to-one association to AroCompDoc
    @ManyToOne
    @JoinColumn(name = "ID_COMP_DOC")
    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public void setAroCompDoc(AroCompDoc aroCompDoc) {
        this.aroCompDoc = aroCompDoc;
    }

    // bi-directional many-to-one association to DecReportServizioVerificaCompDoc
    @OneToMany(mappedBy = "firReport", cascade = {
            CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.REFRESH }, fetch = FetchType.LAZY)
    public List<FirUrnReport> getFirUrnReports() {
        return firUrnReports;
    }

    public void setFirUrnReports(List<FirUrnReport> firUrnReports) {
        this.firUrnReports = firUrnReports;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEC_BACKEND")
    public DecBackend getDecBackend() {
        return decBackend;
    }

    public void setDecBackend(DecBackend decBackend) {
        this.decBackend = decBackend;
    }
}
