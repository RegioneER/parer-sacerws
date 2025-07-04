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

package it.eng.parer.firma.dto.input;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.spagoCore.util.UUIDMdcLogUtil;
import java.time.ZonedDateTime;

/**
 * @author sinatti_s
 */
public class InvokeVerificaInput implements Serializable {
    private static final long serialVersionUID = 1878413280654785734L;

    private ComponenteVers componenteVers;

    private List<ComponenteVers> sottoComponentiFirma;

    private List<ComponenteVers> sottoComponentiMarca;

    private ZonedDateTime dataDiRiferimento;

    private Map<String, Boolean> abilitazioni;

    private boolean verificaAllaDataDiFirma;

    private String uuid;

    public InvokeVerificaInput(ComponenteVers componenteVers,
	    List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
	    ZonedDateTime dataDiRiferimento, Map<String, Boolean> abilitazioni,
	    boolean verificaAllaDataDiFirma) {
	super();
	this.componenteVers = componenteVers;
	this.sottoComponentiFirma = sottoComponentiFirma;
	this.sottoComponentiMarca = sottoComponentiMarca;
	this.dataDiRiferimento = dataDiRiferimento;
	this.abilitazioni = abilitazioni;
	this.verificaAllaDataDiFirma = verificaAllaDataDiFirma;
	// reperito dal contesto se non presente generato
	this.uuid = UUIDMdcLogUtil.getUuid();
    }

    /**
     * @return the componenteVers
     */
    public ComponenteVers getComponenteVers() {
	return componenteVers;
    }

    /**
     * @param componenteVers the componenteVers to set
     */
    public void setComponenteVers(ComponenteVers componenteVers) {
	this.componenteVers = componenteVers;
    }

    /**
     * @return the sottoComponentiFirma
     */
    public List<ComponenteVers> getSottoComponentiFirma() {
	return sottoComponentiFirma;
    }

    /**
     * @param sottoComponentiFirma the sottoComponentiFirma to set
     */
    public void setSottoComponentiFirma(List<ComponenteVers> sottoComponentiFirma) {
	this.sottoComponentiFirma = sottoComponentiFirma;
    }

    /**
     * @return the sottoComponentiMarca
     */
    public List<ComponenteVers> getSottoComponentiMarca() {
	return sottoComponentiMarca;
    }

    /**
     * @param sottoComponentiMarca the sottoComponentiMarca to set
     */
    public void setSottoComponentiMarca(List<ComponenteVers> sottoComponentiMarca) {
	this.sottoComponentiMarca = sottoComponentiMarca;
    }

    /**
     * @return the dataVersamento
     */
    public ZonedDateTime getDataDiRiferimento() {
	return dataDiRiferimento;
    }

    /**
     * @param dataDiRiferimento the dataVersamento to set
     */
    public void setDataDiRiferimento(ZonedDateTime dataDiRiferimento) {
	this.dataDiRiferimento = dataDiRiferimento;
    }

    /**
     * @return the abilitazioni
     */
    public Map<String, Boolean> getAbilitazioni() {
	return abilitazioni;
    }

    /**
     * @param abilitazioni the abilitazioni to set
     */
    public void setAbilitazioni(Map<String, Boolean> abilitazioni) {
	this.abilitazioni = abilitazioni;
    }

    /**
     * @return the verificaAllaDataDiFirma
     */
    public boolean isVerificaAllaDataDiFirma() {
	return verificaAllaDataDiFirma;
    }

    /**
     * @param verificaAllaDataDiFirma the verificaAllaDataDiFirma to set
     */
    public void setVerificaAllaDataDiFirma(boolean verificaAllaDataDiFirma) {
	this.verificaAllaDataDiFirma = verificaAllaDataDiFirma;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

}
