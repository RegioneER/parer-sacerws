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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the ARO_VALORE_ATTRIB_DATI_SPEC_RIC_DS database table.
 *
 * La PK (ID_VALORE_ATTRIB_DATI_SPEC) viene impostata manualmente con lo stesso valore usato per il
 * corrispondente record in ARO_VALORE_ATTRIB_DATI_SPEC (doppio binario). Non usa @GeneratedValue.
 *
 * DL_VALORE e' una colonna virtuale GENERATED ALWAYS AS (UPPER(DL_VALORE_ORI)), quindi non e'
 * inseribile/aggiornabile da JPA. Il valore originale (case-sensitive) va in DL_VALORE_ORI.
 *
 * Sono esclusi i record con DL_VALORE_ORI null o vuoto.
 */
@Entity
@Table(name = "ARO_VALORE_ATTRIB_DATI_SPEC_RIC_DS")
public class AroValoreAttribDatiSpecRicDs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * PK: stesso valore del corrispondente record in ARO_VALORE_ATTRIB_DATI_SPEC. Non
     * auto-generata.
     */
    private Long idValoreAttribDatiSpec;

    /** Valore originale (case-sensitive). Colonna DL_VALORE_ORI. NOT NULL. */
    private String dlValoreOri;

    /**
     * Colonna virtuale GENERATED ALWAYS AS (UPPER(DL_VALORE_ORI)). Non inseribile/aggiornabile.
     */
    private String dlValore;

    /** Colonna di partizionamento. NOT NULL. */
    private BigDecimal idStrut;

    /** Anno chiave unita doc - colonna di partizionamento. NOT NULL. */
    private BigDecimal aaKeyUnitaDoc;

    /** FK verso ARO_UNITA_DOC. NOT NULL. */
    private Long idUnitaDoc;

    /** FK verso ARO_USO_XSD_DATI_SPEC. */
    private Long idUsoXsdDatiSpec;

    /** FK verso ARO_DOC o ARO_COMP_DOC. Nullable (null per UNI_DOC). */
    private Long idDoc;

    /** VERS o MIGRAZ. NOT NULL. */
    private String tiUsoXsd;

    /** UNI_DOC, DOC o COMP. NOT NULL. */
    private String tiEntitaSacer;

    /** Versione XSD per UNI_DOC. Nullable. */
    private String cdVersioneXsdUd;

    /** Versione XSD per DOC/COMP. Nullable. */
    private String cdVersioneXsdDoc;

    private DecAttribDatiSpec decAttribDatiSpec;

    public AroValoreAttribDatiSpecRicDs() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_VALORE_ATTRIB_DATI_SPEC")
    public Long getIdValoreAttribDatiSpec() {
        return idValoreAttribDatiSpec;
    }

    public void setIdValoreAttribDatiSpec(Long idValoreAttribDatiSpec) {
        this.idValoreAttribDatiSpec = idValoreAttribDatiSpec;
    }

    @Column(name = "DL_VALORE_ORI", nullable = false)
    public String getDlValoreOri() {
        return dlValoreOri;
    }

    public void setDlValoreOri(String dlValoreOri) {
        this.dlValoreOri = dlValoreOri;
    }

    @Column(name = "DL_VALORE", insertable = false, updatable = false)
    public String getDlValore() {
        return dlValore;
    }

    public void setDlValore(String dlValore) {
        this.dlValore = dlValore;
    }

    @Column(name = "ID_STRUT", nullable = false)
    public BigDecimal getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "AA_KEY_UNITA_DOC", nullable = false)
    public BigDecimal getAaKeyUnitaDoc() {
        return aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "ID_UNITA_DOC", nullable = false)
    public Long getIdUnitaDoc() {
        return idUnitaDoc;
    }

    public void setIdUnitaDoc(Long idUnitaDoc) {
        this.idUnitaDoc = idUnitaDoc;
    }

    @Column(name = "ID_USO_XSD_DATI_SPEC")
    public Long getIdUsoXsdDatiSpec() {
        return idUsoXsdDatiSpec;
    }

    public void setIdUsoXsdDatiSpec(Long idUsoXsdDatiSpec) {
        this.idUsoXsdDatiSpec = idUsoXsdDatiSpec;
    }

    @Column(name = "ID_DOC")
    public Long getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(Long idDoc) {
        this.idDoc = idDoc;
    }

    @Column(name = "TI_USO_XSD", nullable = false)
    public String getTiUsoXsd() {
        return tiUsoXsd;
    }

    public void setTiUsoXsd(String tiUsoXsd) {
        this.tiUsoXsd = tiUsoXsd;
    }

    @Column(name = "TI_ENTITA_SACER", nullable = false)
    public String getTiEntitaSacer() {
        return tiEntitaSacer;
    }

    public void setTiEntitaSacer(String tiEntitaSacer) {
        this.tiEntitaSacer = tiEntitaSacer;
    }

    @Column(name = "CD_VERSIONE_XSD_UD")
    public String getCdVersioneXsdUd() {
        return cdVersioneXsdUd;
    }

    public void setCdVersioneXsdUd(String cdVersioneXsdUd) {
        this.cdVersioneXsdUd = cdVersioneXsdUd;
    }

    @Column(name = "CD_VERSIONE_XSD_DOC")
    public String getCdVersioneXsdDoc() {
        return cdVersioneXsdDoc;
    }

    public void setCdVersioneXsdDoc(String cdVersioneXsdDoc) {
        this.cdVersioneXsdDoc = cdVersioneXsdDoc;
    }

    // many-to-one association to DecAttribDatiSpec
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ATTRIB_DATI_SPEC", nullable = false)
    public DecAttribDatiSpec getDecAttribDatiSpec() {
        return decAttribDatiSpec;
    }

    public void setDecAttribDatiSpec(DecAttribDatiSpec decAttribDatiSpec) {
        this.decAttribDatiSpec = decAttribDatiSpec;
    }

}
