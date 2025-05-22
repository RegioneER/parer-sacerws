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

import java.math.BigDecimal;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import it.eng.parer.entity.ElvFascDaElabElenco;
import it.eng.parer.entity.FasFascicolo;
import it.eng.parer.entity.FasStatoConservFascicolo;
import it.eng.parer.entity.FasStatoFascicoloElenco;
import it.eng.parer.entity.constraint.ElvFascDaElabElenco.TiStatoFascDaElab;
import it.eng.parer.entity.constraint.FasStatoConservFascicolo.TiStatoConservazione;
import it.eng.parer.entity.constraint.FasStatoFascicoloElenco.TiStatoFascElenco;

/**
 *
 * @author sinatti_S
 */
@Stateless(mappedName = "ElencoVersamentoFascicoliHelper")
@LocalBean
public class ElencoVersamentoFascicoliHelper {

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager em;

    public void insertFascicoloOnCodaDaElab(FasFascicolo fascicolo, long idTipoFasc,
	    TiStatoFascDaElab status) {
	ElvFascDaElabElenco fascVersDaElab = new ElvFascDaElabElenco();
	fascVersDaElab.setFasFascicolo(fascicolo);
	fascVersDaElab.setIdTipoFascicolo(new BigDecimal(idTipoFasc));
	fascVersDaElab.setTiStatoFascDaElab(status);
	fascVersDaElab.setIdStrut(new BigDecimal(fascicolo.getOrgStrut().getIdStrut()));
	fascVersDaElab.setAaFascicolo(fascicolo.getAaFascicolo());
	fascVersDaElab.setTsVersFascicolo(fascicolo.getTsFineSes());

	fascicolo.getElvFascDaElabElencos().add(fascVersDaElab);
	em.persist(fascVersDaElab);
	em.flush();
    }

    public void insertFascicoloOnStatoCons(FasFascicolo fascicolo, TiStatoConservazione status) {
	FasStatoConservFascicolo statoConservFascicolo = new FasStatoConservFascicolo();
	statoConservFascicolo.setFasFascicolo(fascicolo);
	statoConservFascicolo.setIamUser(fascicolo.getIamUser());
	statoConservFascicolo.setTiStatoConservazione(status);
	statoConservFascicolo.setTsStato(fascicolo.getTsFineSes());

	fascicolo.getFasStatoConservFascicoloElencos().add(statoConservFascicolo);
	em.persist(statoConservFascicolo);
	em.flush();
    }

    public void insertFascicoloOnStatoElenco(FasFascicolo fascicolo, TiStatoFascElenco status) {
	FasStatoFascicoloElenco statoFascicoloElenco = new FasStatoFascicoloElenco();
	statoFascicoloElenco.setFasFascicolo(fascicolo);
	statoFascicoloElenco.setIamUser(fascicolo.getIamUser());
	statoFascicoloElenco.setTiStatoFascElencoVers(status);
	statoFascicoloElenco.setTsStato(fascicolo.getTsFineSes());

	fascicolo.getFasStatoFascicoloElencos().add(statoFascicoloElenco);
	em.persist(statoFascicoloElenco);
	em.flush();
    }
}
