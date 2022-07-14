/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.ejb;

import it.eng.parer.entity.VrsSessioneVers;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliRappVers")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliRappVers {

    private static final Logger log = LoggerFactory.getLogger(ControlliRappVers.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public RispostaControlli trovaVersSessUd(long idUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select t  from VrsSessioneVers t " + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                + "and t.tiStatoSessioneVers = 'CHIUSA_OK' " + "and t.tiSessioneVers = 'VERSAMENTO' ";

        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idUnitaDoc", idUd);

        List<VrsSessioneVers> vsv = query.getResultList();
        if (vsv.size() > 0) {
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setrLong(vsv.get(0).getIdSessioneVers());
        }

        return rispostaControlli;
    }

    public RispostaControlli trovaVersSessDoc(long idDoc, long idUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select t  from VrsSessioneVers t " + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                + "and t.aroDoc.idDoc = :idDoc " + "and t.tiStatoSessioneVers = 'CHIUSA_OK' "
                + "and t.tiSessioneVers = 'AGGIUNGI_DOCUMENTO' ";

        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idUnitaDoc", idUd);
        query.setParameter("idDoc", idDoc);

        List<VrsSessioneVers> vsvs = query.getResultList();
        if (vsvs.size() > 0) {
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setrLong(vsvs.get(0).getIdSessioneVers());
            // restituisco anche il tipo di sessione (versamento o aggiunta) che ha prodotto il doc
            rispostaControlli.setrString(vsvs.get(0).getTiSessioneVers());
        } else {
            queryStr = "select t  from VrsSessioneVers t " + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                    + "and t.tiStatoSessioneVers = 'CHIUSA_OK' " + "and t.tiSessioneVers = 'VERSAMENTO' ";

            query = entityManager.createQuery(queryStr);
            query.setParameter("idUnitaDoc", idUd);

            List<VrsSessioneVers> vsv = query.getResultList();
            if (vsv.size() > 0) {
                rispostaControlli.setrBoolean(true);
                rispostaControlli.setrLong(vsv.get(0).getIdSessioneVers());
                // restituisco anche il tipo di sessione (versamento o aggiunta) che ha prodotto il doc
                rispostaControlli.setrString(vsv.get(0).getTiSessioneVers());
            }
        }

        return rispostaControlli;
    }

    public RispostaControlli leggiXmlRappVers(long idSessVers) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select xml from VrsXmlDatiSessioneVers xml "
                + "where xml.vrsDatiSessioneVers.vrsSessioneVers.idSessioneVers = :idSessioneVers "
                + "and xml.vrsDatiSessioneVers.tiDatiSessioneVers = 'XML_DOC' " + "and xml.tiXmlDati = :tiXmlDati ";

        javax.persistence.Query query = entityManager.createQuery(queryStr);
        query.setParameter("idSessioneVers", idSessVers);
        query.setParameter("tiXmlDati", CostantiDB.TipiXmlDati.RAPP_VERS);

        List<VrsXmlDatiSessioneVers> vxdsv = query.getResultList();

        if (vxdsv.size() > 0) {
            rispostaControlli.setrString(vxdsv.get(0).getBlXml());
        } else {
            rispostaControlli.setrString(null);
        }

        return rispostaControlli;
    }

}
