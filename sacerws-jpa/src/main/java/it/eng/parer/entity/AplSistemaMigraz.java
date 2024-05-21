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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/**
 * The persistent class for the APL_SISTEMA_MIGRAZ database table.
 */
@Entity
@Cacheable(true)
@Table(name = "APL_SISTEMA_MIGRAZ")
public class AplSistemaMigraz implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idSistemaMigraz;

    private String dsSistemaMigraz;

    private String nmSistemaMigraz;

    public AplSistemaMigraz() {/* Hibernate */
    }

    @Id
    @XmlID
    @Column(name = "ID_SISTEMA_MIGRAZ")
    @GenericGenerator(name = "SAPL_SISTEMA_MIGRAZ_ID_SISTEMA_MIGRAZ_GENERATOR", strategy = "it.eng.sequences.hibernate.NonMonotonicSequenceGenerator", parameters = {
            @Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "SAPL_SISTEMA_MIGRAZ"),
            @Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1") })
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SAPL_SISTEMA_MIGRAZ_ID_SISTEMA_MIGRAZ_GENERATOR")
    public Long getIdSistemaMigraz() {
        return this.idSistemaMigraz;
    }

    public void setIdSistemaMigraz(Long idSistemaMigraz) {
        this.idSistemaMigraz = idSistemaMigraz;
    }

    @Column(name = "DS_SISTEMA_MIGRAZ")
    public String getDsSistemaMigraz() {
        return this.dsSistemaMigraz;
    }

    public void setDsSistemaMigraz(String dsSistemaMigraz) {
        this.dsSistemaMigraz = dsSistemaMigraz;
    }

    @Column(name = "NM_SISTEMA_MIGRAZ")
    public String getNmSistemaMigraz() {
        return this.nmSistemaMigraz;
    }

    public void setNmSistemaMigraz(String nmSistemaMigraz) {
        this.nmSistemaMigraz = nmSistemaMigraz;
    }
}
