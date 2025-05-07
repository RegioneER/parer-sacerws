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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the DEC_PARTE_NUMERO_REGISTRO database table.
 */
@Entity
@Table(name = "DEC_PARTE_NUMERO_REGISTRO")
public class DecParteNumeroRegistro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idParteNumeroRegistro;

    private String dlValoriParte;

    private String dsParteNumeroRegistro;

    private BigDecimal niMaxCharParte;

    private BigDecimal niMinCharParte;

    private BigDecimal niParteNumeroRegistro;

    private String nmParteNumeroRegistro;

    private String tiCharParte;

    private String tiCharSep;

    private String tiPadSxParte;

    private String tiParte;

    private DecAaRegistroUnitaDoc decAaRegistroUnitaDoc;

    public DecParteNumeroRegistro() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_PARTE_NUMERO_REGISTRO")
    @GenericGenerator(name = "SDEC_PARTE_NUMERO_REGISTRO_ID_PARTE_NUMERO_REGISTRO_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SDEC_PARTE_NUMERO_REGISTRO"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SDEC_PARTE_NUMERO_REGISTRO_ID_PARTE_NUMERO_REGISTRO_GENERATOR")
    public Long getIdParteNumeroRegistro() {
        return this.idParteNumeroRegistro;
    }

    public void setIdParteNumeroRegistro(Long idParteNumeroRegistro) {
        this.idParteNumeroRegistro = idParteNumeroRegistro;
    }

    @Column(name = "DL_VALORI_PARTE")
    public String getDlValoriParte() {
        return this.dlValoriParte;
    }

    public void setDlValoriParte(String dlValoriParte) {
        this.dlValoriParte = dlValoriParte;
    }

    @Column(name = "DS_PARTE_NUMERO_REGISTRO")
    public String getDsParteNumeroRegistro() {
        return this.dsParteNumeroRegistro;
    }

    public void setDsParteNumeroRegistro(String dsParteNumeroRegistro) {
        this.dsParteNumeroRegistro = dsParteNumeroRegistro;
    }

    @Column(name = "NI_MAX_CHAR_PARTE")
    public BigDecimal getNiMaxCharParte() {
        return this.niMaxCharParte;
    }

    public void setNiMaxCharParte(BigDecimal niMaxCharParte) {
        this.niMaxCharParte = niMaxCharParte;
    }

    @Column(name = "NI_MIN_CHAR_PARTE")
    public BigDecimal getNiMinCharParte() {
        return this.niMinCharParte;
    }

    public void setNiMinCharParte(BigDecimal niMinCharParte) {
        this.niMinCharParte = niMinCharParte;
    }

    @Column(name = "NI_PARTE_NUMERO_REGISTRO")
    public BigDecimal getNiParteNumeroRegistro() {
        return this.niParteNumeroRegistro;
    }

    public void setNiParteNumeroRegistro(BigDecimal niParteNumeroRegistro) {
        this.niParteNumeroRegistro = niParteNumeroRegistro;
    }

    @Column(name = "NM_PARTE_NUMERO_REGISTRO")
    public String getNmParteNumeroRegistro() {
        return this.nmParteNumeroRegistro;
    }

    public void setNmParteNumeroRegistro(String nmParteNumeroRegistro) {
        this.nmParteNumeroRegistro = nmParteNumeroRegistro;
    }

    @Column(name = "TI_CHAR_PARTE")
    public String getTiCharParte() {
        return this.tiCharParte;
    }

    public void setTiCharParte(String tiCharParte) {
        this.tiCharParte = tiCharParte;
    }

    @Column(name = "TI_CHAR_SEP", columnDefinition = "char")
    public String getTiCharSep() {
        return this.tiCharSep;
    }

    public void setTiCharSep(String tiCharSep) {
        this.tiCharSep = tiCharSep;
    }

    @Column(name = "TI_PAD_SX_PARTE")
    public String getTiPadSxParte() {
        return this.tiPadSxParte;
    }

    public void setTiPadSxParte(String tiPadSxParte) {
        this.tiPadSxParte = tiPadSxParte;
    }

    @Column(name = "TI_PARTE")
    public String getTiParte() {
        return tiParte;
    }

    public void setTiParte(String tiParte) {
        this.tiParte = tiParte;
    }

    // bi-directional many-to-one association to DecAaRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AA_REGISTRO_UNITA_DOC")
    public DecAaRegistroUnitaDoc getDecAaRegistroUnitaDoc() {
        return this.decAaRegistroUnitaDoc;
    }

    public void setDecAaRegistroUnitaDoc(DecAaRegistroUnitaDoc decAaRegistroUnitaDoc) {
        this.decAaRegistroUnitaDoc = decAaRegistroUnitaDoc;
    }
}
