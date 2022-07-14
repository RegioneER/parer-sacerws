package it.eng.parer.ws.versFascicoli.ejb;

import java.math.BigDecimal;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.ElvFascDaElabElenco;
import it.eng.parer.entity.FasStatoConservFascicolo;
import it.eng.parer.entity.FasStatoFascicoloElenco;
import it.eng.parer.entity.FasFascicolo;
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

    private static final Logger log = LoggerFactory.getLogger(ElencoVersamentoFascicoliHelper.class);

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager em;

    public void insertFascicoloOnCodaDaElab(long idFascicolo, long idTipoFasc, TiStatoFascDaElab status) {
        FasFascicolo fascicolo = em.find(FasFascicolo.class, idFascicolo);
        insertFascicoloOnCodaDaElab(fascicolo, idTipoFasc, status);
    }

    public void insertFascicoloOnCodaDaElab(FasFascicolo fascicolo, long idTipoFasc, TiStatoFascDaElab status) {
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

    public void deleteElencoVersDaElab(Long idElvFascDaElabElenco) {
        ElvFascDaElabElenco eevde = em.find(ElvFascDaElabElenco.class, idElvFascDaElabElenco);
        em.remove(eevde);
    }

    public void aggiornaElencoDaElabCorrente(long idElencoDaEleb, TiStatoFascDaElab stato) {
        ElvFascDaElabElenco elencoCorrenteDaElab = em.find(ElvFascDaElabElenco.class, idElencoDaEleb);
        elencoCorrenteDaElab.setTiStatoFascDaElab(stato);
        em.persist(elencoCorrenteDaElab);
    }

    //

    public void insertFascicoloOnStatoCons(long idFascicolo, TiStatoConservazione status) {
        FasFascicolo fascicolo = em.find(FasFascicolo.class, idFascicolo);
        insertFascicoloOnStatoCons(fascicolo, status);
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

    public void deleteStatoConservFascicolo(Long idStatoConservFascicolo) {
        FasStatoConservFascicolo eevde = em.find(FasStatoConservFascicolo.class, idStatoConservFascicolo);
        em.remove(eevde);
    }

    public void aggiornaStatoConservFascicoloCorrente(long idStatoConservFasc, TiStatoConservazione stato) {
        FasStatoConservFascicolo statoConservFascCorrente = em.find(FasStatoConservFascicolo.class, idStatoConservFasc);
        statoConservFascCorrente.setTiStatoConservazione(stato);
        em.persist(statoConservFascCorrente);
    }

    //

    public void insertFascicoloOnStatoElenco(long idFascicolo, TiStatoFascElenco status) {
        FasFascicolo fascicolo = em.find(FasFascicolo.class, idFascicolo);
        insertFascicoloOnStatoElenco(fascicolo, status);
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

    public void deleteStatoFascicoloElenco(Long idStatoFascicoloElenco) {
        FasStatoFascicoloElenco eevde = em.find(FasStatoFascicoloElenco.class, idStatoFascicoloElenco);
        em.remove(eevde);
    }

    public void aggiornaStatoFascicoloElencoCorrente(long idStatoFascicoloElenco, TiStatoFascElenco stato) {
        FasStatoFascicoloElenco statoFascicoloElenco = em.find(FasStatoFascicoloElenco.class, idStatoFascicoloElenco);
        statoFascicoloElenco.setTiStatoFascElencoVers(stato);
        em.persist(statoFascicoloElenco);
    }

}