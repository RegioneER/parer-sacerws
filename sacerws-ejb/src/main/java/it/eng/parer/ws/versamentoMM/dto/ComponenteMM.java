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

package it.eng.parer.ws.versamentoMM.dto;

import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.xml.versReqMultiMedia.ComponenteType;

/**
 *
 * @author Fioravanti_F
 */
public class ComponenteMM {

    // tag <id>
    private String id;
    private ComponenteVers rifComponenteVers;
    // private FileBinario rifFileBinario;
    private ComponenteType myComponenteMM;
    private boolean forzaFirmeFormati = false;
    private boolean forzaHash = false;
    private long idFormatoFileCalc;
    private byte[] hashForzato;

    public boolean isForzaFirmeFormati() {
	return forzaFirmeFormati;
    }

    public void setForzaFirmeFormati(boolean forzaFirmeFormati) {
	this.forzaFirmeFormati = forzaFirmeFormati;
    }

    public boolean isForzaHash() {
	return forzaHash;
    }

    public void setForzaHash(boolean forzaHash) {
	this.forzaHash = forzaHash;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public long getIdFormatoFileCalc() {
	return idFormatoFileCalc;
    }

    public void setIdFormatoFileCalc(long idFormatoFileCalc) {
	this.idFormatoFileCalc = idFormatoFileCalc;
    }

    public ComponenteType getMyComponenteMM() {
	return myComponenteMM;
    }

    public void setMyComponenteMM(ComponenteType myComponenteMM) {
	this.myComponenteMM = myComponenteMM;
    }

    public ComponenteVers getRifComponenteVers() {
	return rifComponenteVers;
    }

    public void setRifComponenteVers(ComponenteVers rifComponenteVers) {
	this.rifComponenteVers = rifComponenteVers;
    }

    public byte[] getHashForzato() {
	return hashForzato;
    }

    public void setHashForzato(byte[] hashForzato) {
	this.hashForzato = hashForzato;
    }
}
