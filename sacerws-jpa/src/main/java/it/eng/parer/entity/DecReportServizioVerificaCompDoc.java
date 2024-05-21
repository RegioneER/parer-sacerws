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

package it.eng.parer.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.eng.parer.entity.constraint.DecReportServizioVerificaCompDoc.TiFormatoServizioVerificaCompDoc;
import it.eng.parer.entity.constraint.DecReportServizioVerificaCompDoc.TiReportServizioVerificaCompDoc;

/**
 * The persistent class for the DEC_REPORT_SERVIZIO_VERIFICA_COMP_DOC database table.
 *
 */
@Entity
@Table(name = "DEC_REPORT_SERVIZIO_VERIFICA_COMP_DOC")
public class DecReportServizioVerificaCompDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idReportServizioVerificaCompDoc;
    private DecServizioVerificaCompDoc decServizioVerificaCompDoc;
    private String blXslt;
    private TiReportServizioVerificaCompDoc tiReport;
    private TiFormatoServizioVerificaCompDoc tiFormato;

    public DecReportServizioVerificaCompDoc() {/* Hibernate */
    }

    @Id
    @SequenceGenerator(name = "DEC_REPORT_SERVIZIO_VERIFICA_COMP_DOC_ID_REPORT_SERVIZIO_VERIFICA_COMP_DOC_GENERATOR", sequenceName = "SDEC_REPORT_SERVIZIO_VERIFICA_COMP_DOC", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEC_REPORT_SERVIZIO_VERIFICA_COMP_DOC_ID_REPORT_SERVIZIO_VERIFICA_COMP_DOC_GENERATOR")
    @Column(name = "ID_REPORT_SERVIZIO_VERIFICA_COMP_DOC")
    public Long getIdReportServizioVerificaCompDoc() {
        return this.idReportServizioVerificaCompDoc;
    }

    public void setIdReportServizioVerificaCompDoc(Long idReportServizioVerificaCompDoc) {
        this.idReportServizioVerificaCompDoc = idReportServizioVerificaCompDoc;
    }

    @Lob
    @Column(name = "BL_XSLT")
    public String getBlXsltReport() {
        return this.blXslt;
    }

    public void setBlXsltReport(String blXslt) {
        this.blXslt = blXslt;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_REPORT")
    public TiReportServizioVerificaCompDoc getTiReport() {
        return this.tiReport;
    }

    public void setTiReport(TiReportServizioVerificaCompDoc tiReport) {
        this.tiReport = tiReport;
    }

    @ManyToOne
    @JoinColumn(name = "ID_SERVIZIO_VERIFICA_COMP_DOC")
    public DecServizioVerificaCompDoc getDecServizioVerificaCompDoc() {
        return this.decServizioVerificaCompDoc;
    }

    public void setDecServizioVerificaCompDoc(DecServizioVerificaCompDoc decServizioVerificaCompDoc) {
        this.decServizioVerificaCompDoc = decServizioVerificaCompDoc;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_FORMATO")
    public TiFormatoServizioVerificaCompDoc getTiFormato() {
        return tiFormato;
    }

    public void setTiFormato(TiFormatoServizioVerificaCompDoc tiFormato) {
        this.tiFormato = tiFormato;
    }
}
