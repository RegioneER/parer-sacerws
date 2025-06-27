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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.utils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author sinatti_s
 */
public class UpdCostanti {

    public static final int COLLEGAMENTO_DESC_MAX_SIZE = 256;
    public static final int MAXLEN_DATOSPEC = 4000;

    public static final String COLLEGAMENTO_DESC_SEP = ";";

    public static final String TIPOREG_SCONOSCIUTO = "Registro sconosciuto";
    public static final String TIPOUD_SCONOSCIUTO = "Tipo unita documentaria sconosciuta";
    public static final String TIPDOC_PRINCIPALE_SCONOSCIUTO = "Tipo documento principale sconosciuto";

    public enum StrutAaPartion {

	VERSINIDATISPEC("VERS_INI_DATI_SPEC"), UPDDATISPECUNITADOC("UPD_DATI_SPEC_UNITA_DOC"), /**/
	XMLUPDUNITADOC("XML_UPD_UNITA_DOC"), SESUPDUNITADOCKO("SES_UPD_UNITA_DOC_KO"), /**/
	XMLSESUPDUNITADOCKO("XML_SES_UPD_UNITA_DOC_KO"), UPDUNITADOCKO("UPD_UNITA_DOC_KO");

	private String partitionName;

	private StrutAaPartion(String partitionName) {
	    this.partitionName = partitionName;
	}

	public String getPartitionName() {
	    return partitionName;
	}

	public static StrutAaPartion getEnum(String value) {
	    if (value == null) {
		return null;
	    }
	    for (StrutAaPartion v : values()) {
		if (value.equalsIgnoreCase(v.getPartitionName())) {
		    return v;
		}
	    }
	    return null;
	}

	public static String printAll() {
	    return String.join(" , ", Arrays.asList(values()).stream()
		    .map(x -> x.getPartitionName()).collect(Collectors.toList()));
	}
    }

    //
    public enum TipoAggiornamento {
	METADATI
    }

    //
    public enum AggiornamentoEffettuato {

	HASPAFASCICOLOPRINCIPALETOUPD("Profilo archivistico – fascicolo principale",
		new BigDecimal(1)), /**/
	HASPAFASCICOLISECONDARITOUP("Profilo archivistico – fascicoli secondari",
		new BigDecimal(2)), /**/
	HASPROFILOUNITADOCUMENTARIATOUPD("Profilo unita' documentaria", new BigDecimal(3)), /**/
	HASPROFILONORMATIVOTOUPD("Profilo normativo", new BigDecimal(1)), /**/
	HASDOCUMENTICOLLEGATITOUPD("Collegamenti ad unita' documentarie", new BigDecimal(4)), /**/
	HASDATISPECIFICITOBEUPDATED("Dati specifici dell'unita' documentaria",
		new BigDecimal(5)), /**/
	HASDATISPECIFICIMIGRAZIONETOUPD("Dati specifici di migrazione dell’unità documentaria",
		new BigDecimal(6)), /**/
	HASDOCUMENTITOUPD("Almeno un documento dell'unita' documentaria", new BigDecimal(7)), /**/
	HASCOMPONENTITOUPD("Almeno un componente dell'unita' documentaria", new BigDecimal(8)), /**/
	// documenti
	HASDOCCPROFILO("Profilo documento", new BigDecimal(1)), /**/
	HASDOCDATISPEC("Dati specifici del documento", new BigDecimal(2)), /**/
	HASDOCDATISPECMIGRAZ("Dati specifici di migrazione del documento", new BigDecimal(3)), /**/
	HASDOCCOMPONENTI("Almeno un componente del documento", new BigDecimal(4)),
	// componenti
	HASCOMPONENTE(
		"Nome componente, urn del componente versato ed identificatore del componente versato",
		new BigDecimal(1)), /**/
	HASCOMPONENTEDATISPEC("Dati specifici del componente", new BigDecimal(2)), /**/
	HASCOMPONENTEDATISPECMIGRAZ("Dati specifici di migrazione del componente",
		new BigDecimal(3));

	private String label;
	private BigDecimal niOrd;

	private AggiornamentoEffettuato(String label, BigDecimal niOrd) {
	    this.label = label;
	    this.niOrd = niOrd;
	}

	public String getLabel() {
	    return label;
	}

	public BigDecimal getNiOrd() {
	    return niOrd;
	}

    }

}
