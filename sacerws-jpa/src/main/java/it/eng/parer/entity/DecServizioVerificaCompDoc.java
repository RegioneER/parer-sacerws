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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;

/**
 * The persistent class for the DEC_SERVIZIO_VERIFICA_COMP_DOC database table.
 *
 */
@Entity
@Table(name = "DEC_SERVIZIO_VERIFICA_COMP_DOC")
public class DecServizioVerificaCompDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idServizioVerificaCompDoc;
    private CdServizioVerificaCompDoc cdServizio;
    private String nmVersione;
    private String dsServizioVerificaCompDoc;
    private List<DecReportServizioVerificaCompDoc> decReportServizioCompDocs = new ArrayList<>();

    public DecServizioVerificaCompDoc() {
        // hibernate
    }

    @Id
    @SequenceGenerator(name = "DEC_SERVIZIO_VERIFICA_COMP_DOC_ID_SERVIZIO_VERIFICA_COMP_DOC_GENERATOR", sequenceName = "SDEC_SERVIZIO_VERIFICA_COMP_DOC", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEC_SERVIZIO_VERIFICA_COMP_DOC_ID_SERVIZIO_VERIFICA_COMP_DOC_GENERATOR")
    @Column(name = "ID_SERVIZIO_VERIFICA_COMP_DOC")
    public Long getIdServizioVerificaCompDoc() {
        return this.idServizioVerificaCompDoc;
    }

    public void setIdServizioVerificaCompDoc(Long idServizioVerificaCompDoc) {
        this.idServizioVerificaCompDoc = idServizioVerificaCompDoc;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "CD_SERVIZIO")
    public CdServizioVerificaCompDoc getCdServizio() {
        return cdServizio;
    }

    public void setCdServizio(CdServizioVerificaCompDoc cdServizio) {
        this.cdServizio = cdServizio;
    }

    @Column(name = "NM_VERSIONE")
    public String getNmVersione() {
        return nmVersione;
    }

    public void setNmVersione(String nmVersione) {
        this.nmVersione = nmVersione;
    }

    @Column(name = "DS_SERVIZIO_VERIFICA_COMP_DOC")
    public String getDsServizioVerificaCompDoc() {
        return dsServizioVerificaCompDoc;
    }

    public void setDsServizioVerificaCompDoc(String dsServizioVerificaCompDoc) {
        this.dsServizioVerificaCompDoc = dsServizioVerificaCompDoc;
    }

    // bi-directional many-to-one association to DecReportServizioVerificaCompDoc
    @OneToMany(mappedBy = "decServizioVerificaCompDoc")
    public List<DecReportServizioVerificaCompDoc> getDecReportServizioVerificaCompDocs() {
        return decReportServizioCompDocs;
    }

    public void setDecReportServizioVerificaCompDocs(
            List<DecReportServizioVerificaCompDoc> decReportServizioCompDocs) {
        this.decReportServizioCompDocs = decReportServizioCompDocs;
    }
}
