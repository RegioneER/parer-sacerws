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

package it.eng.parer.ws.versamento.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.eng.parer.ws.utils.Costanti.CategoriaDocumento;
import it.eng.parer.ws.utils.ParametroApplDB;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.xml.versReq.ComponenteType;
import it.eng.parer.ws.xml.versReq.DocumentoType;
import it.eng.parer.ws.xml.versReq.SottoComponenteType;
import it.eng.parer.ws.xml.versReq.TipoSupportoType;
import it.eng.parer.ws.xml.versReq.UnitaDocAggAllegati;
import it.eng.parer.ws.xml.versReq.UnitaDocumentaria;

/**
 *
 * @author Fioravanti_F
 */
public class Conteggi {

    private HashMap<String, ComponenteVers> fileAttesi;
    private List<DocumentoVers> documentiAttesi;
    private Map<String, DocumentoVers> tmpDocumentiMap;
    private boolean trovatiIdCompDuplicati;
    private boolean trovatiIdDocDuplicati;
    private String trovataDataNullaIn;
    private StrutturaVersamento strutturaVersamentoAtteso;
    //
    Map<String, String> xmlDefaults;

    /**
     * @return dto strutturaVersamentoAtteso
     */
    public StrutturaVersamento getStrutturaVersamentoAtteso() {
	return strutturaVersamentoAtteso;
    }

    /**
     * @param strutturaVersamentoAtteso strutturaVersamentoAtteso
     */
    public void setStrutturaVersamentoAtteso(StrutturaVersamento strutturaVersamentoAtteso) {
	this.strutturaVersamentoAtteso = strutturaVersamentoAtteso;
    }

    public Conteggi() {
	this.init();
    }

    public final void init() {
	fileAttesi = new HashMap<>();
	documentiAttesi = new ArrayList<>();
	tmpDocumentiMap = new HashMap<>();
	trovatiIdCompDuplicati = false;
	trovatiIdDocDuplicati = false;
	trovataDataNullaIn = null;
	strutturaVersamentoAtteso = new StrutturaVersamento();
	strutturaVersamentoAtteso.setUnitaDocCollegate(new ArrayList<>());
    }

    /**
     * Questa versione della verifica serve per il versamento sincrono. Analizza ed eventualmente
     * integra dei default l'intera unità documentaria versata
     *
     * @param versamento unita doc versata
     * @param defaults   parametri di default
     */
    public void verifica(UnitaDocumentaria versamento, Map<String, String> defaults) {
	int progressivo = 0;
	//
	int progressivoDoc = 0;
	long contaAllegati = 0;
	long contaAnnessi = 0;
	long contaAnnotazioni = 0;
	boolean corrAllegatiDichiarati;
	boolean corrAnnessiDichiarati;
	boolean corrAnnotazioniDichiarati;
	corrAllegatiDichiarati = true;
	corrAnnessiDichiarati = true;
	corrAnnotazioniDichiarati = true;

	this.xmlDefaults = defaults;

	progressivo = 1;
	//
	progressivoDoc = 1;
	this.aggiungiVerificaDocumento(versamento.getDocumentoPrincipale(), progressivo,
		CategoriaDocumento.Principale, progressivoDoc);

	progressivo = 0;
	if (versamento.getAllegati() != null) {
	    Iterator<? extends DocumentoType> tmpEnumDoc = versamento.getAllegati().getAllegato()
		    .iterator();
	    while (tmpEnumDoc.hasNext()) {
		DocumentoType tmpDocumentoType = tmpEnumDoc.next();
		progressivo += 1;
		progressivoDoc += 1; // globale
		contaAllegati += 1;
		this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
			CategoriaDocumento.Allegato, progressivoDoc);
	    }
	}

	progressivo = 0;
	if (versamento.getAnnessi() != null) {
	    Iterator<? extends DocumentoType> tmpEnumDoc = versamento.getAnnessi().getAnnesso()
		    .iterator();
	    while (tmpEnumDoc.hasNext()) {
		DocumentoType tmpDocumentoType = tmpEnumDoc.next();
		progressivo += 1;
		progressivoDoc += 1; // globale
		contaAnnessi += 1;
		this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
			CategoriaDocumento.Annesso, progressivoDoc);
	    }
	}

	progressivo = 0;
	if (versamento.getAnnotazioni() != null) {
	    Iterator<? extends DocumentoType> tmpEnumDoc = versamento.getAnnotazioni()
		    .getAnnotazione().iterator();
	    while (tmpEnumDoc.hasNext()) {
		DocumentoType tmpDocumentoType = tmpEnumDoc.next();
		progressivo += 1;
		progressivoDoc += 1; // globale
		contaAnnotazioni += 1;
		this.aggiungiVerificaDocumento(tmpDocumentoType, progressivo,
			CategoriaDocumento.Annotazione, progressivoDoc);
	    }
	}

	if ((versamento.getNumeroAllegati() != null)
		&& (versamento.getNumeroAllegati() != contaAllegati)
		|| (versamento.getNumeroAllegati() == null && contaAllegati != 0)/* MAC#13460 */) {
	    corrAllegatiDichiarati = false;
	}
	if ((versamento.getNumeroAnnessi() != null)
		&& (versamento.getNumeroAnnessi() != contaAnnessi)
		|| (versamento.getNumeroAnnessi() == null && contaAnnessi != 0)/* MAC#13460 */) {
	    corrAnnessiDichiarati = false;
	}
	if ((versamento.getNumeroAnnotazioni() != null)
		&& (versamento.getNumeroAnnotazioni() != contaAnnotazioni)
		|| ((versamento.getNumeroAnnotazioni() == null
			&& contaAnnotazioni != 0))/* MAC#13460 */) {
	    corrAnnotazioniDichiarati = false;
	}

	strutturaVersamentoAtteso.setDocumentiAttesi(documentiAttesi);
	strutturaVersamentoAtteso.setFileAttesi(fileAttesi);
	strutturaVersamentoAtteso.setTrovatiIdCompDuplicati(trovatiIdCompDuplicati);
	strutturaVersamentoAtteso.setTrovatiIdDocDuplicati(trovatiIdDocDuplicati);
	strutturaVersamentoAtteso.setTrovataDataNullaIn(trovataDataNullaIn);
	strutturaVersamentoAtteso.setCorrAllegatiDichiarati(corrAllegatiDichiarati);
	strutturaVersamentoAtteso.setCorrAnnessiDichiarati(corrAnnessiDichiarati);
	strutturaVersamentoAtteso.setCorrAnnotazioniDichiarati(corrAnnotazioniDichiarati);
    }

    /**
     * Questa versione della verifica serve per l'aggiunta allegati. Dal momento che i dati relativi
     * all'unità documentaria vengono versati durante il versamento, questa analizza soltanto il
     * songolo documento che viene versato dal servizio. (che può essere un Allegato, un Annesso,
     * un'Annotazione)
     *
     * @param versamento unita doc versata
     * @param defaults   parametri di default
     */
    public void verifica(UnitaDocAggAllegati versamento, Map<String, String> defaults) {
	boolean corrispondenzaAllegati = false;

	this.xmlDefaults = defaults;

	if (versamento.getAllegato() != null) {
	    this.aggiungiVerificaDocumento(versamento.getAllegato(), 1, CategoriaDocumento.Allegato,
		    1);
	    corrispondenzaAllegati = true;
	}
	if (versamento.getAnnesso() != null) {
	    this.aggiungiVerificaDocumento(versamento.getAnnesso(), 1, CategoriaDocumento.Annesso,
		    1);
	    corrispondenzaAllegati = true;
	}
	if (versamento.getAnnotazione() != null) {
	    this.aggiungiVerificaDocumento(versamento.getAnnotazione(), 1,
		    CategoriaDocumento.Annotazione, 1);
	    corrispondenzaAllegati = true;
	}
	strutturaVersamentoAtteso.setDocumentiAttesi(documentiAttesi);
	strutturaVersamentoAtteso.setFileAttesi(fileAttesi);
	strutturaVersamentoAtteso.setTrovatiIdCompDuplicati(trovatiIdCompDuplicati);
	strutturaVersamentoAtteso.setTrovatiIdDocDuplicati(trovatiIdDocDuplicati);
	strutturaVersamentoAtteso.setTrovataDataNullaIn(trovataDataNullaIn);
	strutturaVersamentoAtteso.setCorrAllegatiDichiarati(corrispondenzaAllegati);
	strutturaVersamentoAtteso.setCorrAnnessiDichiarati(corrispondenzaAllegati);
	strutturaVersamentoAtteso.setCorrAnnotazioniDichiarati(corrispondenzaAllegati);
    }

    private void aggiungiVerificaDocumento(DocumentoType doc, int progressivo,
	    CategoriaDocumento categoria, int progressivoDoc) {
	DocumentoVers tmpDocumentoVers;
	tmpDocumentoVers = new DocumentoVers();
	tmpDocumentoVers.setProgressivo(progressivo);
	//
	tmpDocumentoVers.setNiOrdDoc(progressivoDoc);
	tmpDocumentoVers.setCategoriaDoc(categoria);
	tmpDocumentoVers.setRifDocumento(doc);
	tmpDocumentoVers.setFileAttesi(new ArrayList<>());

	// impostazione del valore di default per il tipo struttura documento
	if (doc.getStrutturaOriginale().getTipoStruttura() == null) {
	    doc.getStrutturaOriginale()
		    .setTipoStruttura(xmlDefaults.get(ParametroApplDB.TIPO_STRUT_DOC));
	}

	/*
	 * verifica se le date dei dati fiscali sono state scritte male: il parser xml di Castor
	 * presenta un fastidioso bug per cui accetta un tag di tipo xs:date anche se è vuoto.
	 * Questo si traduce in un campo Datetime Java completamente sballato e in un dato salvato
	 * sul database con valori altrettanto sballati.
	 */
	if (doc.getDatiFiscali() != null) {
	    if (doc.getDatiFiscali().getDataEmissione() == null) {
		trovataDataNullaIn = "DatiFiscali.DataEmissione del documento <ID> "
			+ doc.getIDDocumento();
	    }
	    if (doc.getDatiFiscali().getDataTermineEmissione() == null) {
		trovataDataNullaIn = "DatiFiscali.DataTermineEmissione del documento <ID> "
			+ doc.getIDDocumento();
	    }
	}

	// test di duplicazione dell'id documento all'interno dell'unità documentaria versata
	if (doc.getIDDocumento() == null || doc.getIDDocumento().length() == 0) {
	    trovatiIdDocDuplicati = true;
	}
	if (tmpDocumentiMap.get(doc.getIDDocumento()) != null) {
	    trovatiIdDocDuplicati = true;
	}
	tmpDocumentiMap.put(doc.getIDDocumento(), tmpDocumentoVers);

	documentiAttesi.add(tmpDocumentoVers);
	this.verificaComponenti(tmpDocumentoVers);
    }

    private void verificaComponenti(DocumentoVers documentoVersato) {
	ComponenteVers tmpComponenteVers;
	ComponenteVers tmpSottoComponenteVers;
	DocumentoType documento = documentoVersato.getRifDocumento();

	Iterator<? extends ComponenteType> tmpEnumCompo = documento.getStrutturaOriginale()
		.getComponenti().getComponente().iterator();
	while (tmpEnumCompo.hasNext()) {
	    ComponenteType tmpComponente = tmpEnumCompo.next();

	    // impostazione valori di default per il componente
	    if (tmpComponente.getTipoComponente() == null) {
		tmpComponente.setTipoComponente(xmlDefaults.get(ParametroApplDB.TIPO_COMP_DOC));
	    }
	    if (tmpComponente.getTipoSupportoComponente() == null) {
		tmpComponente.setTipoSupportoComponente(TipoSupportoType
			.valueOf(xmlDefaults.get(ParametroApplDB.TIPO_SUPPORTO_COMP)));
	    }
	    Boolean bool = tmpComponente.isUtilizzoDataFirmaPerRifTemp();
	    if ((bool != null) && (bool == false)) {
		tmpComponente.setUtilizzoDataFirmaPerRifTemp(
			Boolean.parseBoolean(xmlDefaults.get(ParametroApplDB.USO_DATA_FIRMA)));
	    }

	    if (fileAttesi.get(tmpComponente.getID()) != null) {
		trovatiIdCompDuplicati = true;
	    }
	    tmpComponenteVers = new ComponenteVers();
	    if (tmpComponente.getID() == null || tmpComponente.getID().length() == 0) {
		trovatiIdCompDuplicati = true;
	    }
	    tmpComponenteVers.setId(tmpComponente.getID());
	    tmpComponenteVers.setTipoSupporto(ComponenteVers.TipiSupporto
		    .valueOf(tmpComponente.getTipoSupportoComponente().value()));
	    tmpComponenteVers.setPresenteRifMeta(tmpComponente.getRiferimento() != null);
	    tmpComponenteVers.setDatiLetti(false);
	    tmpComponenteVers.setMyComponente(tmpComponente);
	    tmpComponenteVers.setRifDocumentoVers(documentoVersato);
	    documentoVersato.getFileAttesi().add(tmpComponenteVers);
	    fileAttesi.put(tmpComponente.getID(), tmpComponenteVers);
	    if (tmpComponente.getSottoComponenti() != null) {
		Iterator<? extends SottoComponenteType> tmpEnumSottoCompo = tmpComponente
			.getSottoComponenti().getSottoComponente().iterator();
		while (tmpEnumSottoCompo.hasNext()) {
		    SottoComponenteType tmpSottoComponente = tmpEnumSottoCompo.next();

		    // impostazione valori di default per il sottocomponente
		    if (tmpSottoComponente.getTipoSupportoComponente() == null) {
			tmpSottoComponente.setTipoSupportoComponente(TipoSupportoType
				.valueOf(xmlDefaults.get(ParametroApplDB.TIPO_SUPPORTO_COMP)));
		    }

		    if (fileAttesi.get(tmpSottoComponente.getID()) != null) {
			trovatiIdCompDuplicati = true;
		    }
		    tmpSottoComponenteVers = new ComponenteVers();
		    if (tmpSottoComponente.getID() == null
			    || tmpSottoComponente.getID().length() == 0) {
			trovatiIdCompDuplicati = true;
		    }
		    tmpSottoComponenteVers.setId(tmpSottoComponente.getID());
		    tmpSottoComponenteVers.setTipoSupporto(ComponenteVers.TipiSupporto
			    .valueOf(tmpSottoComponente.getTipoSupportoComponente().value()));
		    tmpSottoComponenteVers
			    .setPresenteRifMeta(tmpSottoComponente.getRiferimento() != null);
		    tmpSottoComponenteVers.setDatiLetti(false);
		    tmpSottoComponenteVers.setMyComponente(tmpComponente);
		    tmpSottoComponenteVers.setMySottoComponente(tmpSottoComponente);
		    tmpSottoComponenteVers.setRifDocumentoVers(documentoVersato);
		    tmpSottoComponenteVers.setRifComponenteVersPadre(tmpComponenteVers);
		    documentoVersato.getFileAttesi().add(tmpSottoComponenteVers);
		    fileAttesi.put(tmpSottoComponente.getID(), tmpSottoComponenteVers);
		}
	    }
	}
    }
}
