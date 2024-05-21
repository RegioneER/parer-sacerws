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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.ws.xml.versReq.ChiaveType;

/**
 *
 * @author Fioravanti_F
 */
public class UnitaDocColl implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_LEN_DESCRIZIONE = 254;

    private ChiaveType chiave;
    private long idUnitaDocLink;
    /*
     * questa variabile contiene l'insieme in ordine di inserimento di tutte le descrizioni dei collegamenti che puntano
     * ad una stessa ud. dal momento che l'implementazione di LinkedHashSet è un set, le descrizioni duplicate vengono
     * automaticamente ignorate
     */
    private Set<String> descrizioni = new LinkedHashSet<>();

    /**
     * @return the chiave
     */
    public ChiaveType getChiave() {
        return chiave;
    }

    /**
     * @param chiave
     *            the chiave to set
     */
    public void setChiave(ChiaveType chiave) {
        this.chiave = chiave;
    }

    /**
     * @return the idUnitaDocLink
     */
    public long getIdUnitaDocLink() {
        return idUnitaDocLink;
    }

    /**
     * @param idUnitaDocLink
     *            the idUnitaDocLink to set
     */
    public void setIdUnitaDocLink(long idUnitaDocLink) {
        this.idUnitaDocLink = idUnitaDocLink;
    }

    //
    public void aggiungiDescrizioneUnivoca(String descrizione) {
        descrizioni.add(descrizione);
    }

    public String generaDescrizione() {
        // la descrizione del collegamento è la somma delle descrizioni uniche
        // di tutti i riferimenti alla UD oggetto del collegamento.
        return StringUtils.join(descrizioni.toArray(), ";");
    }

}
