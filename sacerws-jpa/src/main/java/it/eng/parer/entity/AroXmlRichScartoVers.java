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

/**
 * The persistent class for the ARO_XML_RICH_SCARTO_VERS database table.
 *
 */
@Entity
@Table(name = "ARO_XML_RICH_SCARTO_VERS")
@NamedQuery(name = "AroXmlRichScartoVers.findAll", query = "SELECT a FROM AroXmlRichScartoVers a")
public class AroXmlRichScartoVers implements Serializable {
    private static final long serialVersionUID = 1L;
    private long idXmlRichScartoVers;
    private String blXmlRichScartoVers;
    private String cdVersioneXml;
    private String tiXmlRichScartoVers;
    private AroRichScartoVers aroRichScartoVers;

    public AroXmlRichScartoVers() {
        /* Hibernate */
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_XML_RICH_SCARTO_VERS")
    public long getIdXmlRichScartoVers() {
        return this.idXmlRichScartoVers;
    }

    public void setIdXmlRichScartoVers(long idXmlRichScartoVers) {
        this.idXmlRichScartoVers = idXmlRichScartoVers;
    }

    @Lob
    @Column(name = "BL_XML_RICH_SCARTO_VERS")
    public String getBlXmlRichScartoVers() {
        return this.blXmlRichScartoVers;
    }

    public void setBlXmlRichScartoVers(String blXmlRichScartoVers) {
        this.blXmlRichScartoVers = blXmlRichScartoVers;
    }

    @Column(name = "CD_VERSIONE_XML")
    public String getCdVersioneXml() {
        return this.cdVersioneXml;
    }

    public void setCdVersioneXml(String cdVersioneXml) {
        this.cdVersioneXml = cdVersioneXml;
    }

    @Column(name = "TI_XML_RICH_SCARTO_VERS")
    public String getTiXmlRichScartoVers() {
        return this.tiXmlRichScartoVers;
    }

    public void setTiXmlRichScartoVers(String tiXmlRichScartoVers) {
        this.tiXmlRichScartoVers = tiXmlRichScartoVers;
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

}
