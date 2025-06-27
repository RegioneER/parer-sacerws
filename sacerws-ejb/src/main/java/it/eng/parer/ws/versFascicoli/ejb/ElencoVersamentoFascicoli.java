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

package it.eng.parer.ws.versFascicoli.ejb;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.constraint.ElvFascDaElabElenco.TiStatoFascDaElab;
import it.eng.parer.entity.constraint.FasStatoConservFascicolo.TiStatoConservazione;
import it.eng.parer.entity.constraint.FasStatoFascicoloElenco.TiStatoFascElenco;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.versFascicoli.dto.RispostaWSFascicolo;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.versamento.dto.SyncFakeSessn;

/**
 *
 * @author sinatti_S
 */
@Stateless(mappedName = "ElencoVersamentoFascicoli")
@LocalBean
public class ElencoVersamentoFascicoli {

    //
    @EJB
    ElencoVersamentoFascicoliHelper elencoVersamentoFascicoliHelper;

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @Resource
    private EJBContext context;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public RispostaControlli scriviElvFascDaElabElenco(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione, FasFascicolo fascicolo) {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	tmpRispostaControlli.setrBoolean(false);
	try {
	    elencoVersamentoFascicoliHelper.insertFascicoloOnCodaDaElab(fascicolo,
		    svf.getIdTipoFascicolo(), TiStatoFascDaElab.IN_ATTESA_SCHED);
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio coda da elaborare: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviStatoConservFascicolo(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione, FasFascicolo fascicolo) {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	tmpRispostaControlli.setrBoolean(false);
	try {
	    elencoVersamentoFascicoliHelper.insertFascicoloOnStatoCons(fascicolo,
		    TiStatoConservazione.PRESA_IN_CARICO);
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio stato conservazione fascicolo: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

    public RispostaControlli scriviStatoFascicoloElenco(RispostaWSFascicolo rispostaWs,
	    VersFascicoloExt versamento, SyncFakeSessn sessione, FasFascicolo fascicolo) {

	RispostaControlli tmpRispostaControlli = new RispostaControlli();
	// StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
	tmpRispostaControlli.setrBoolean(false);
	try {
	    elencoVersamentoFascicoliHelper.insertFascicoloOnStatoElenco(fascicolo,
		    TiStatoFascElenco.IN_ATTESA_SCHED);
	    tmpRispostaControlli.setrBoolean(true);
	} catch (Exception e) {
	    tmpRispostaControlli.setCodErr(MessaggiWSBundle.ERR_666P);
	    tmpRispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666P,
		    "Errore interno nella fase di salvataggio stato fascicolo elenco: "
			    + e.getMessage()));
	    tmpRispostaControlli.setrBoolean(false);
	}

	return tmpRispostaControlli;
    }

}
