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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.ejb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.versFascicoli.dto.DXPAFascicoloCollegato;
import it.eng.parer.ws.versFascicoli.dto.FascicoloLink;
import it.eng.parer.ws.versFascicoli.dto.FlControlliFasc;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versFascicoli.utils.CostantiFasc;
import it.eng.parer.ws.xml.versfascicolo.ChiaveUDType;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicoloresp.ContenutoSinteticoType;
import it.eng.parer.ws.xml.versfascicoloresp.ECEsitoContenutoFascicoloType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType;
import it.eng.parer.ws.xml.versfascicoloresp.SCChiaveUDType;
import it.eng.parer.ws.xml.versfascicoloresp.SCUDTypeNonPresenti;
import it.eng.parer.ws.xml.versfascicoloresp.SCUDTypePresenti;

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "ControlliCollFascicolo")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliCollFascicolo {

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    protected ControlliSemantici controlliSemantici;

    @EJB
    protected ControlliFascicoli controlliFascicoli;

    public RispostaControlli verificaUdFascicolo(VersFascicoloExt versamento,
	    ECFascicoloType fascicoloResp) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);

	IndiceSIPFascicolo parsedIndiceFasc = versamento.getVersamento();
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();

	int numeroUd = parsedIndiceFasc.getContenutoSintetico().getNumeroUnitaDocumentarie();
	// popolo il tag di esito ContenutoSintetico
	fascicoloResp.setContenutoSintetico(new ContenutoSinteticoType());
	fascicoloResp.getContenutoSintetico().setNumeroUnitaDocumentarie(numeroUd);

	// verifico che esista il tag ContenutoAnaliticoUnitaDocumentarie
	if (parsedIndiceFasc.getContenutoAnaliticoUnitaDocumentarie() == null) {
	    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(),
		    MessaggiWSBundle.FASC_003_002, svf.getUrnPartChiaveFascicolo());
	    return rispostaControlli;
	}

	// verifico che il numero dichiarato e il numero di tag UnitaDocumentaria coincidano
	if (parsedIndiceFasc.getContenutoAnaliticoUnitaDocumentarie().getUnitaDocumentaria()
		.size() != numeroUd) {
	    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(),
		    MessaggiWSBundle.FASC_003_001, svf.getUrnPartChiaveFascicolo(), numeroUd);
	    return rispostaControlli;
	}

	// verifico ogni singolo collegamento
	List<Long> unitaDocs = new ArrayList<>();
	CSChiave tmpChiaveUd = new CSChiave();
	boolean trovatiErrori = false;
	// prima preparo il tag ControlliContenutoFascicolo
	ECEsitoContenutoFascicoloType ececft = new ECEsitoContenutoFascicoloType();
	SCUDTypePresenti presenti = new SCUDTypePresenti();
	SCUDTypeNonPresenti nonPresenti = new SCUDTypeNonPresenti();
	ececft.setUnitaDocumentariePresenti(presenti);
	ececft.setUnitaDocumentarieNonPresenti(nonPresenti);
	fascicoloResp.setControlliContenutoFascicolo(ececft);

	for (ChiaveUDType cudt : parsedIndiceFasc.getContenutoAnaliticoUnitaDocumentarie()
		.getUnitaDocumentaria()) {
	    tmpChiaveUd.setTipoRegistro(cudt.getRegistro());
	    tmpChiaveUd.setAnno(Long.valueOf(cudt.getAnno()));
	    tmpChiaveUd.setNumero(cudt.getNumero());
	    String descChiaveUdColl = MessaggiWSFormat.formattaUrnPartUnitaDoc(tmpChiaveUd);
	    // preparo la chiave UD da rendere nell'esito di ControlliContenutoFascicolo
	    SCChiaveUDType sCChiaveUDType = new SCChiaveUDType();
	    sCChiaveUDType.setRegistro(cudt.getRegistro());
	    sCChiaveUDType.setAnno(cudt.getAnno());
	    sCChiaveUDType.setNumero(cudt.getNumero());

	    RispostaControlli rc = controlliSemantici.checkChiave(tmpChiaveUd,
		    versamento.getStrutturaComponenti().getIdStruttura(),
		    ControlliSemantici.TipiGestioneUDAnnullate.CONSIDERA_ASSENTE);
	    if (rc.getrLong() == -1 || rc.getCodErr().equals(MessaggiWSBundle.UD_012_002)) { // se
											     // non
											     // ha
											     // trovato
											     // la
											     // chiave...oppure
											     // esiste
											     // ma
											     // ANNULLATA
		trovatiErrori = true;
		if (rc.isrBoolean()) { // se in ogni caso la query è andata a buon fine
		    // creo il nuovo errore da aggiungere alla lista
		    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(),
			    MessaggiWSBundle.FASC_003_003, svf.getUrnPartChiaveFascicolo(),
			    descChiaveUdColl);
		    // inoltre la aggiungo alla lista di esito delle UD non presenti
		    nonPresenti.getUnitaDocumentaria().add(sCChiaveUDType);
		} else {
		    // allora è un errore di database..
		    // non ha trovato la chiave ma non ha impostato a true
		    // il bool di rispostacontrolli
		    versamento.addError(svf.getUrnPartChiaveFascicolo(), rc.getCodErr(),
			    rc.getDsErr());
		    break;
		}
	    } else {
		// se l'ha trovata, la aggiunge alla lista delle UD trovate
		unitaDocs.add(rc.getrLong());
		presenti.getUnitaDocumentaria().add(sCChiaveUDType);
	    }
	}
	//
	presenti.setNumeroUnitaDocumentariePresenti(
		BigInteger.valueOf(presenti.getUnitaDocumentaria().size()));
	nonPresenti.setNumeroUnitaDocumentarieNonPresenti(
		BigInteger.valueOf(nonPresenti.getUnitaDocumentaria().size()));
	//
	if (!trovatiErrori) {
	    rispostaControlli.setrBoolean(true);
	    svf.setUnitaDocElencate(unitaDocs);
	}
	return rispostaControlli;
    }

    /*
     * Restituisce una lista (nel peggiore dei casi vuota) di fascicoli da inserire (non
     * controllati)
     */
    public RispostaControlli buildCollegamentiFascicolo(VersFascicoloExt versamento,
	    ECFascicoloType.EsitoControlliFascicolo myControlliFascicolo) {

	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);

	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	// recupero flag per risposta
	FlControlliFasc flContrFasc = svf.getFlControlliFasc();

	// composizione dei fascicoli da collegare non verificati (no fk su id fascicolo)
	List<FascicoloLink> fascicoliToBeLinked = new ArrayList<FascicoloLink>(0);

	/*
	 * vengono costruite due liste: duplicated -> chiavi duplicate notDuplicated -> chiavi non
	 * duplicate
	 *
	 * obiettivo: costrutire un'unica lista da processare successivamente ossia
	 * fascicoliToBeLinked
	 */

	Set<CSChiaveFasc> setUniqueCsFKeys = new HashSet<>(0); // utile per verificare i duplicati
	Set<CSChiaveFasc> setDuplicateCsFKeys = new HashSet<>(0); // utile per verificare i
								  // duplicati

	// build dei duplicati
	for (DXPAFascicoloCollegato el : svf.getDatiXmlProfiloArchivistico().getFascCollegati()) {
	    CSChiaveFasc csKey = el.getCsChiaveFasc();
	    if (!setUniqueCsFKeys.add(csKey)) { // se già presente in lista
		// add on duplicates
		setDuplicateCsFKeys.add(csKey);
	    }
	}

	// sui duplicati si ottiene una descrizione completa (con separatore ";")
	for (Iterator<CSChiaveFasc> it = setDuplicateCsFKeys.iterator(); it.hasNext();) {
	    CSChiaveFasc csKey = it.next();
	    StringBuilder sb = new StringBuilder(0);
	    Set<String> setUniqueDesc = new HashSet<String>(0);
	    for (DXPAFascicoloCollegato coll : svf.getDatiXmlProfiloArchivistico()
		    .getFascCollegati()) {
		if (coll.getCsChiaveFasc().equals(csKey)) {
		    if (StringUtils.isNotBlank(coll.getDescCollegamento())) {
			// tutte le descrizioni diverse (a parità di chiave) vengono inserite con il
			// carattere ";"
			if (setUniqueDesc.add(coll.getDescCollegamento())) {
			    sb.append(coll.getDescCollegamento());
			    sb.append(CostantiFasc.COLLEGAMENTO_DESC_SEP);
			}
		    }
		}
	    }
	    // se ha inserito qualcosa come descrizione finale per quella chiave
	    if (sb.length() > 0) {
		// rimuove ultimo separatore ";" se esiste
		int ind = sb.lastIndexOf(CostantiFasc.COLLEGAMENTO_DESC_SEP);
		if (ind >= 0) {
		    sb = sb.deleteCharAt(ind);
		}
	    }
	    // finally .... create a new one and add on list
	    FascicoloLink coll = new FascicoloLink();
	    coll.setCsChiaveFasc(csKey);
	    coll.setDescCollegamento(sb.toString());

	    // verifica lunghezza massima descrizione
	    if (coll.getDescCollegamento().length() > CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE) {
		rispostaControlli.setCodErr(MessaggiWSBundle.FASC_006_002);
	    }

	    //// !!DA NON FARE -> dovrà restituire un esito in ERRORE troncamento descrizione ->
	    //// eseguito se controllo
	    //// non abilitato in modo da poter effettuare l'inserimento in ogni caso
	    /*
	     * if(!flContrFasc.isFlAbilitaContrColleg()) { if(coll.getDescCollegamento().length() >
	     * CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE) {
	     * coll.setDescCollegamento(coll.getDescCollegamento().substring(0,
	     * CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE)); } }
	     */
	    // aggiunge in lista
	    fascicoliToBeLinked.add(coll);

	    // remove from unique
	    if (setUniqueCsFKeys.contains(csKey)) {
		setUniqueCsFKeys.remove(csKey);
	    }
	}

	// inserisce i collegamenti con chiave univoca (nessun problema sulla descrizione "doppia")
	// "ripescandoli" dalla
	// lista dei collegamenti
	for (Iterator<CSChiaveFasc> it = setUniqueCsFKeys.iterator(); it.hasNext();) {
	    CSChiaveFasc csKeyUnique = it.next();
	    for (DXPAFascicoloCollegato coll : svf.getDatiXmlProfiloArchivistico()
		    .getFascCollegati()) {
		if (coll.getCsChiaveFasc().equals(csKeyUnique)) {
		    // !!DA NON FARE -> dovrà restituire un esito in ERRORE troncamento descrizione
		    // -> eseguito se
		    // controllo non abilitato in modo da poter effettuare l'inserimento in ogni
		    // caso
		    /*
		     * if(!flContrFasc.isFlAbilitaContrColleg()) {
		     * if(coll.getDescCollegamento().length() >
		     * CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE) {
		     * coll.setDescCollegamento(coll.getDescCollegamento().substring(0,
		     * CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE)); } }
		     */
		    // aggiunge in lista
		    FascicoloLink link = new FascicoloLink();
		    link.setCsChiaveFasc(coll.getCsChiaveFasc());
		    link.setDescCollegamento(coll.getDescCollegamento());
		    fascicoliToBeLinked.add(link);

		    // verifica lunghezza massima descrizione
		    if (coll.getDescCollegamento()
			    .length() > CostantiFasc.COLLEGAMENTO_DESC_MAX_SIZE) {
			rispostaControlli.setCodErr(MessaggiWSBundle.FASC_006_002);
		    }
		}
	    }
	}

	rispostaControlli.setrObject(fascicoliToBeLinked);// lo setto in ogni caso
	if (!fascicoliToBeLinked.isEmpty()) {
	    rispostaControlli.setrBoolean(true);
	}

	return rispostaControlli;
    }

}
