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

package it.eng.parer.ws.versamento.ejb;

import static it.eng.parer.util.DateUtilsConverter.convert;

import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecAttribDatiSpec;
import it.eng.parer.entity.OrgCampoValSubStrut;
import it.eng.parer.entity.OrgRegolaValSubStrut;
import it.eng.parer.entity.OrgSubStrut;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versamento.dto.DatoSpecifico;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliSubStrut")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliSubStrut {

    private static final Logger log = LoggerFactory.getLogger(ControlliSubStrut.class);
    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    private enum TipiDatiSpecCalcSubStrut {

	UNITA_DOC, DOCUMENTO_PRINCIPALE
    }

    @SuppressWarnings("unchecked")
    public RispostaControlli calcolaSubStrut(StrutturaVersamento strutV) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);
	String queryStr;
	javax.persistence.Query query;

	try {
	    // trova la regola di calcolo per la substruttura in base al tipo UD e al tipo doc
	    // principale
	    queryStr = "select tud " + "from OrgRegolaValSubStrut tud "
		    + "where tud.decTipoUnitaDoc.idTipoUnitaDoc = :idTipoUnitaDocIn "
		    + " and tud.decTipoDoc.idTipoDoc = :idTipoDocIn "
		    + " and tud.dtIstituz <= :dataDiOggiIn "
		    + " and tud.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE
							     // della data di
	    // riferimento!!!
	    query = entityManager.createQuery(queryStr, OrgRegolaValSubStrut.class);
	    query.setParameter("idTipoUnitaDocIn", strutV.getIdTipologiaUnitaDocumentaria());
	    query.setParameter("idTipoDocIn",
		    strutV.getDocumentiAttesi().get(0).getIdTipoDocumentoDB());
	    query.setParameter("dataDiOggiIn", convert(strutV.getDataVersamento()));
	    List<OrgRegolaValSubStrut> orvsses = query.getResultList();
	    if (orvsses.size() != 1) {
		rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
			strutV.getUrnPartChiaveUd(),
			"la regola di calcolo della substruttura non è stata definita"));
		return rispostaControlli;
	    }
	    OrgRegolaValSubStrut orvss = orvsses.get(0);
	    if (orvss.getOrgCampoValSubStruts() == null
		    || orvss.getOrgCampoValSubStruts().isEmpty()) {
		rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
			strutV.getUrnPartChiaveUd(),
			"la regola di calcolo della substruttura non ha campi definiti"));
		return rispostaControlli;
	    }

	    // questa è una verifica che dovrà essere modificata
	    if (orvss.getOrgCampoValSubStruts().size() > 1) {
		rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
			strutV.getUrnPartChiaveUd(),
			"è stato definito più di un campo per la regola di calcolo della substruttura"));
		return rispostaControlli;
	    }
	    // fine verifica che dovrà essere modificata

	    OrgCampoValSubStrut campoValSubStrut = orvss.getOrgCampoValSubStruts().get(0);
	    CostantiDB.TipoCampo tcvs = CostantiDB.TipoCampo.valueOf(campoValSubStrut.getTiCampo());
	    switch (tcvs) {
	    case SUB_STRUT:
		if (campoValSubStrut.getOrgSubStrut() != null) {
		    rispostaControlli.setrLong(campoValSubStrut.getOrgSubStrut().getIdSubStrut());
		    strutV.setIdSubStruttura(campoValSubStrut.getOrgSubStrut().getIdSubStrut());
		    strutV.setDescSubStruttura(campoValSubStrut.getOrgSubStrut().getNmSubStrut());
		    rispostaControlli.setrBoolean(true);
		} else {
		    rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
		    rispostaControlli.setDsErr(MessaggiWSBundle.getString(
			    MessaggiWSBundle.UD_011_001, strutV.getUrnPartChiaveUd(),
			    "il campo individuato dalla regola di calcolo della substruttura non ha riferimenti ad una substruttura"));
		    return rispostaControlli;
		}
		break;
	    case DATO_SPEC_UNI_DOC:
		rispostaControlli = this.calcolaSubStrutDaDatoSpec(strutV,
			TipiDatiSpecCalcSubStrut.UNITA_DOC,
			campoValSubStrut.getDecAttribDatiSpec());
		break;
	    case DATO_SPEC_DOC_PRINC:
		rispostaControlli = this.calcolaSubStrutDaDatoSpec(strutV,
			TipiDatiSpecCalcSubStrut.DOCUMENTO_PRINCIPALE,
			campoValSubStrut.getDecAttribDatiSpec());
		break;
	    default:
		rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
			strutV.getUrnPartChiaveUd(),
			"il tipo campo definisce una logica di calcolo della substruttura non supportata"));
		return rispostaControlli;
	    }

	} catch (Exception ex) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "Errore nel calcolo della sottostruttura  " + ex.getMessage()));
	    log.error("Errore nel calcolo della sottostruttura ", ex);
	}

	return rispostaControlli;
    }

    @SuppressWarnings("unchecked")
    private RispostaControlli calcolaSubStrutDaDatoSpec(StrutturaVersamento strutV,
	    TipiDatiSpecCalcSubStrut tipiDatiSpecCalcSubStrut, DecAttribDatiSpec dads) {
	RispostaControlli rispostaControlli;
	rispostaControlli = new RispostaControlli();
	rispostaControlli.setrBoolean(false);
	String descElemento = null;
	Map<String, DatoSpecifico> datiSpecifici = null;
	String queryStr;
	javax.persistence.Query query;

	if (dads == null) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
		    strutV.getUrnPartChiaveUd(),
		    "il campo individuato dalla regola di calcolo della substruttura non ha riferimenti a un tipo dato specifico"));
	    return rispostaControlli;
	}

	long idAttribDatoSpec = dads.getIdAttribDatiSpec();
	String descAttribDatoSpec = dads.getNmAttribDatiSpec();

	String coderrDatoSpecNullo = null;
	String codErrDatoSpecErrato = null;

	switch (tipiDatiSpecCalcSubStrut) {
	case UNITA_DOC:
	    descElemento = "l'Unità documentaria";
	    coderrDatoSpecNullo = MessaggiWSBundle.UD_011_002;
	    codErrDatoSpecErrato = MessaggiWSBundle.UD_011_004;
	    datiSpecifici = strutV.getDatiSpecifici();
	    break;
	case DOCUMENTO_PRINCIPALE:
	    descElemento = "il Documento principale dell'Unità documentaria";
	    coderrDatoSpecNullo = MessaggiWSBundle.UD_011_003;
	    codErrDatoSpecErrato = MessaggiWSBundle.UD_011_005;
	    datiSpecifici = strutV.getDocumentiAttesi().get(0).getDatiSpecifici();
	    break;
	}

	if (datiSpecifici == null || datiSpecifici.isEmpty()) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.UD_011_001);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_011_001,
		    strutV.getUrnPartChiaveUd(), "non sono presenti dati specifici per "
			    + descElemento + " utili a determinare la substruttura"));
	    return rispostaControlli;
	}

	String tmpValSubStrut = null;
	for (DatoSpecifico tmpds : datiSpecifici.values()) {
	    if (tmpds.getIdDatoSpec() == idAttribDatoSpec) {
		tmpValSubStrut = tmpds.getValore();
		break;
	    }
	}

	if (tmpValSubStrut == null || tmpValSubStrut.isEmpty()) {
	    rispostaControlli.setCodErr(coderrDatoSpecNullo);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(coderrDatoSpecNullo,
		    strutV.getUrnPartChiaveUd(), descAttribDatoSpec));
	    return rispostaControlli;
	}

	try {
	    queryStr = "select tud " + "from OrgSubStrut tud "
		    + "where tud.nmSubStrut = :nmSubStrutIn "
		    + " and tud.orgStrut.idStrut = :idStrutIn ";
	    query = entityManager.createQuery(queryStr, OrgSubStrut.class);
	    query.setParameter("nmSubStrutIn", tmpValSubStrut);
	    query.setParameter("idStrutIn", strutV.getIdStruttura());
	    List<OrgSubStrut> orgss = query.getResultList();
	    if (orgss.isEmpty()) {
		rispostaControlli.setCodErr(codErrDatoSpecErrato);
		rispostaControlli.setDsErr(MessaggiWSBundle.getString(codErrDatoSpecErrato,
			strutV.getUrnPartChiaveUd(), descAttribDatoSpec));
		return rispostaControlli;
	    } else {
		rispostaControlli.setrLong(orgss.get(0).getIdSubStrut());
		strutV.setIdSubStruttura(orgss.get(0).getIdSubStrut());
		strutV.setDescSubStruttura(orgss.get(0).getNmSubStrut());
		rispostaControlli.setrBoolean(true);
	    }
	} catch (Exception ex) {
	    rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
	    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
		    "Errore nel calcolo della sottostruttura  " + ex.getMessage()));
	    log.error("Errore nel calcolo della sottostruttura ", ex);
	}

	return rispostaControlli;
    }
}
