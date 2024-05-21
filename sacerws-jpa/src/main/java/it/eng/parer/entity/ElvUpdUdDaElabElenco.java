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

import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import it.eng.parer.entity.constraint.ElvUpdUdDaElabElenco.ElvUpdUdDaElabTiStatoUpdElencoVers;
import it.eng.parer.entity.inheritance.oop.ElvUdDocUpdDaElabElenco;

/**
 * The persistent class for the ELV_UPD_UD_DA_ELAB_ELENCO database table.
 */
@Entity
@Table(name = "ELV_UPD_UD_DA_ELAB_ELENCO")
@AssociationOverride(name = "udDocUpdObj", joinColumns = @JoinColumn(name = "ID_UPD_UNITA_DOC", insertable = false, updatable = false))
public class ElvUpdUdDaElabElenco extends ElvUdDocUpdDaElabElenco<AroUpdUnitaDoc> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUpdUdDaElabElenco;

    private OrgStrut orgStrut;

    private OrgSubStrut orgSubStrut;

    private AroUpdUnitaDoc aroUpdUnitaDoc;

    private ElvUpdUdDaElabTiStatoUpdElencoVers tiStatoUpdElencoVers;

    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    private DecTipoUnitaDoc decTipoUnitaDoc;

    private DecTipoDoc decTipoDocPrinc;

    public ElvUpdUdDaElabElenco() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UPD_UD_DA_ELAB_ELENCO")
    @GenericGenerator(name = "SELV_UPD_UD_DA_ELAB_ELENCO_ID_UPD_UD_DA_ELAB_ELENCO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SELV_UPD_UD_DA_ELAB_ELENCO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SELV_UPD_UD_DA_ELAB_ELENCO_ID_UPD_UD_DA_ELAB_ELENCO_GENERATOR")
    public Long getIdUpdUdDaElabElenco() {
        return this.idUpdUdDaElabElenco;
    }

    public void setIdUpdUdDaElabElenco(Long idUpdUdDaElabElenco) {
        this.idUpdUdDaElabElenco = idUpdUdDaElabElenco;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to OrgSubStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUB_STRUT")
    public OrgSubStrut getOrgSubStrut() {
        return this.orgSubStrut;
    }

    public void setOrgSubStrut(OrgSubStrut orgSubStrut) {
        this.orgSubStrut = orgSubStrut;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_UNITA_DOC")
    public AroUpdUnitaDoc getAroUpdUnitaDoc() {
        return this.aroUpdUnitaDoc;
    }

    public void setAroUpdUnitaDoc(AroUpdUnitaDoc aroUpdUnitaDoc) {
        this.aroUpdUnitaDoc = aroUpdUnitaDoc;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO_UPD_ELENCO_VERS")
    public ElvUpdUdDaElabTiStatoUpdElencoVers getTiStatoUpdElencoVers() {
        return this.tiStatoUpdElencoVers;
    }

    public void setTiStatoUpdElencoVers(ElvUpdUdDaElabTiStatoUpdElencoVers tiStatoUpdElencoVers) {
        this.tiStatoUpdElencoVers = tiStatoUpdElencoVers;
    }

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC")
    public DecRegistroUnitaDoc getDecRegistroUnitaDoc() {
        return this.decRegistroUnitaDoc;
    }

    public void setDecRegistroUnitaDoc(DecRegistroUnitaDoc decRegistroUnitaDoc) {
        this.decRegistroUnitaDoc = decRegistroUnitaDoc;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
        return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
        this.decTipoUnitaDoc = decTipoUnitaDoc;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC_PRINC")
    public DecTipoDoc getDecTipoDocPrinc() {
        return this.decTipoDocPrinc;
    }

    public void setDecTipoDocPrinc(DecTipoDoc decTipoDocPrinc) {
        this.decTipoDocPrinc = decTipoDocPrinc;
    }
}
