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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.DecModelloXsdUd.TiModelloXsdUd;

/**
 * The persistent class for the DEC_MODELLO_XSD_UD database table.
 *
 */
@Entity
@Table(name = "DEC_MODELLO_XSD_UD")
public class DecModelloXsdUd implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idModelloXsdUd;
    private AplSistemaMigraz aplSistemaMigraz;
    private OrgAmbiente orgAmbiente;
    private String tiUsoModelloXsd;
    private TiModelloXsdUd tiModelloXsd;
    private String dsXsd;
    private String cdXsd;
    private String blXsd;
    private Date dtIstituz;
    private Date dtSoppres;
    private String flDefault;
    private List<DecUsoModelloXsdUniDoc> decUsoModelloXsdUniDocs = new ArrayList<>();
    private List<DecUsoModelloXsdDoc> decUsoModelloXsdDocs = new ArrayList<>();
    private List<DecUsoModelloXsdCompDoc> decUsoModelloXsdCompDocs = new ArrayList<>();

    public DecModelloXsdUd() {
        // hibernate
    }

    @Id
    @GenericGenerator(name = "DEC_MODELLO_XSD_UD_IDMODELLOXSDUD_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_MODELLO_XSD_UD"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEC_MODELLO_XSD_UD_IDMODELLOXSDUD_GENERATOR")
    @Column(name = "ID_MODELLO_XSD_UD")
    public Long getIdModelloXsdUd() {
        return this.idModelloXsdUd;
    }

    public void setIdModelloXsdUd(Long idModelloXsdUd) {
        this.idModelloXsdUd = idModelloXsdUd;
    }

    // bi-directional many-to-one association to AplSistemaMigraz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SISTEMA_MIGRAZ")
    public AplSistemaMigraz getAplSistemaMigraz() {
        return this.aplSistemaMigraz;
    }

    public void setAplSistemaMigraz(AplSistemaMigraz aplSistemaMigraz) {
        this.aplSistemaMigraz = aplSistemaMigraz;
    }

    // bi-directional many-to-one association to OrgAmbiente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AMBIENTE")
    public OrgAmbiente getOrgAmbiente() {
        return this.orgAmbiente;
    }

    public void setOrgAmbiente(OrgAmbiente orgAmbiente) {
        this.orgAmbiente = orgAmbiente;
    }

    @Column(name = "TI_USO_MODELLO_XSD")
    public String getTiUsoModelloXsd() {
        return this.tiUsoModelloXsd;
    }

    public void setTiUsoModelloXsd(String tiUsoModelloXsd) {
        this.tiUsoModelloXsd = tiUsoModelloXsd;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_ISTITUZ")
    public Date getDtIstituz() {
        return this.dtIstituz;
    }

    public void setDtIstituz(Date dtIstituz) {
        this.dtIstituz = dtIstituz;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_MODELLO_XSD")
    public TiModelloXsdUd getTiModelloXsd() {
        return this.tiModelloXsd;
    }

    public void setTiModelloXsd(TiModelloXsdUd tiModelloXsd) {
        this.tiModelloXsd = tiModelloXsd;
    }

    @Column(name = "DS_XSD")
    public String getDsXsd() {
        return this.dsXsd;
    }

    public void setDsXsd(String dsXsd) {
        this.dsXsd = dsXsd;
    }

    @Column(name = "CD_XSD")
    public String getCdXsd() {
        return this.cdXsd;
    }

    public void setCdXsd(String cdXsd) {
        this.cdXsd = cdXsd;
    }

    @Lob
    @Column(name = "BL_XSD")
    public String getBlXsd() {
        return this.blXsd;
    }

    public void setBlXsd(String blXsd) {
        this.blXsd = blXsd;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_SOPPRES")
    public Date getDtSoppres() {
        return this.dtSoppres;
    }

    public void setDtSoppres(Date dtSoppres) {
        this.dtSoppres = dtSoppres;
    }

    @Column(name = "FL_DEFAULT", columnDefinition = "CHAR")
    public String getFlDefault() {
        return this.flDefault;
    }

    public void setFlDefault(String flDefault) {
        this.flDefault = flDefault;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decModelloXsdUd", cascade = CascadeType.PERSIST)
    public List<DecUsoModelloXsdUniDoc> getDecUsoModelloXsdUniDocs() {
        return this.decUsoModelloXsdUniDocs;
    }

    public void setDecUsoModelloXsdUniDocs(List<DecUsoModelloXsdUniDoc> decUsoModelloXsdUniDocs) {
        this.decUsoModelloXsdUniDocs = decUsoModelloXsdUniDocs;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decModelloXsdUd", cascade = CascadeType.PERSIST)
    public List<DecUsoModelloXsdDoc> getDecUsoModelloXsdDocs() {
        return this.decUsoModelloXsdDocs;
    }

    public void setDecUsoModelloXsdDocs(List<DecUsoModelloXsdDoc> decUsoModelloXsdDocs) {
        this.decUsoModelloXsdDocs = decUsoModelloXsdDocs;
    }

    // bi-directional many-to-one association to DecTipoRapprAmmesso
    @OneToMany(mappedBy = "decModelloXsdUd", cascade = CascadeType.PERSIST)
    public List<DecUsoModelloXsdCompDoc> getDecUsoModelloXsdCompDocs() {
        return this.decUsoModelloXsdCompDocs;
    }

    public void setDecUsoModelloXsdCompDocs(List<DecUsoModelloXsdCompDoc> decUsoModelloXsdCompDocs) {
        this.decUsoModelloXsdCompDocs = decUsoModelloXsdCompDocs;
    }

}
