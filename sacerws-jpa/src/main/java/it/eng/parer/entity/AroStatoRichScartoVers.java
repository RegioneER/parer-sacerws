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
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the ARO_STATO_RICH_SCARTO_VERS database table.
 *
 */
@Entity
@Table(name = "ARO_STATO_RICH_SCARTO_VERS")
@NamedQuery(name = "AroStatoRichScartoVers.findAll", query = "SELECT a FROM AroStatoRichScartoVers a")
public class AroStatoRichScartoVers implements Serializable {
    private static final long serialVersionUID = 1L;
    private long idStatoRichScartoVers;
    private String dsNotaRichScartoVers;
    private Date dtRegStatoRichScartoVers;
    private BigDecimal pgStatoRichScartoVers;
    private String tiStatoRichScartoVers;
    private AroRichScartoVers aroRichScartoVers;
    private IamUser iamUser;

    public AroStatoRichScartoVers() {
        /* Hibernate */
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_STATO_RICH_SCARTO_VERS")
    public long getIdStatoRichScartoVers() {
        return this.idStatoRichScartoVers;
    }

    public void setIdStatoRichScartoVers(long idStatoRichScartoVers) {
        this.idStatoRichScartoVers = idStatoRichScartoVers;
    }

    @Column(name = "DS_NOTA_RICH_SCARTO_VERS")
    public String getDsNotaRichScartoVers() {
        return this.dsNotaRichScartoVers;
    }

    public void setDsNotaRichScartoVers(String dsNotaRichScartoVers) {
        this.dsNotaRichScartoVers = dsNotaRichScartoVers;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_REG_STATO_RICH_SCARTO_VERS")
    public Date getDtRegStatoRichScartoVers() {
        return this.dtRegStatoRichScartoVers;
    }

    public void setDtRegStatoRichScartoVers(Date dtRegStatoRichScartoVers) {
        this.dtRegStatoRichScartoVers = dtRegStatoRichScartoVers;
    }

    @Column(name = "PG_STATO_RICH_SCARTO_VERS")
    public BigDecimal getPgStatoRichScartoVers() {
        return this.pgStatoRichScartoVers;
    }

    public void setPgStatoRichScartoVers(BigDecimal pgStatoRichScartoVers) {
        this.pgStatoRichScartoVers = pgStatoRichScartoVers;
    }

    @Column(name = "TI_STATO_RICH_SCARTO_VERS")
    public String getTiStatoRichScartoVers() {
        return this.tiStatoRichScartoVers;
    }

    public void setTiStatoRichScartoVers(String tiStatoRichScartoVers) {
        this.tiStatoRichScartoVers = tiStatoRichScartoVers;
    }

    // bi-directional many-to-one association to AroRichScartoVers
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SCARTO_VERS")
    public AroRichScartoVers getAroRichScartoVers() {
        return this.aroRichScartoVers;
    }

    public void setAroRichScartoVers(AroRichScartoVers aroRichScartoVers) {
        this.aroRichScartoVers = aroRichScartoVers;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

}
