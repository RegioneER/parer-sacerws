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
package it.eng.parer.ws.versamento.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import it.eng.parer.entity.VrsSessioneVers;
import it.eng.parer.entity.VrsXmlDatiSessioneVers;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import static it.eng.parer.ws.utils.CostantiDB.TiStatoSesioneVers.CHIUSA_OK;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliRappVers")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliRappVers {

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ObjectStorageService objectStorageService;

    public RispostaControlli trovaVersSessUd(long idUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select t  from VrsSessioneVers t "
                + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc " + "and t.tiStatoSessioneVers = '"
                + CHIUSA_OK + "' " + "and t.tiSessioneVers = 'VERSAMENTO' ";

        TypedQuery<VrsSessioneVers> query = entityManager.createQuery(queryStr,
                VrsSessioneVers.class);
        query.setParameter("idUnitaDoc", idUd);

        List<VrsSessioneVers> vsv = query.getResultList();
        if (!vsv.isEmpty()) {
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setrLong(vsv.get(0).getIdSessioneVers());
        }

        return rispostaControlli;
    }

    public RispostaControlli trovaVersSessDoc(long idDoc, long idUd) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select t  from VrsSessioneVers t "
                + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc " + "and t.aroDoc.idDoc = :idDoc "
                + "and t.tiStatoSessioneVers = '" + CHIUSA_OK + "' "
                + "and t.tiSessioneVers = 'AGGIUNGI_DOCUMENTO' ";

        TypedQuery<VrsSessioneVers> query = entityManager.createQuery(queryStr,
                VrsSessioneVers.class);
        query.setParameter("idUnitaDoc", idUd);
        query.setParameter("idDoc", idDoc);

        List<VrsSessioneVers> vsvs = query.getResultList();
        if (!vsvs.isEmpty()) {
            rispostaControlli.setrBoolean(true);
            rispostaControlli.setrLong(vsvs.get(0).getIdSessioneVers());
            // restituisco anche il tipo di sessione (versamento o aggiunta) che ha prodotto
            // il doc
            rispostaControlli.setrString(vsvs.get(0).getTiSessioneVers());
        } else {
            queryStr = "select t  from VrsSessioneVers t "
                    + "where t.aroUnitaDoc.idUnitaDoc = :idUnitaDoc "
                    + "and t.tiStatoSessioneVers = '" + CHIUSA_OK + "' "
                    + "and t.tiSessioneVers = 'VERSAMENTO' ";

            query = entityManager.createQuery(queryStr, VrsSessioneVers.class);
            query.setParameter("idUnitaDoc", idUd);

            List<VrsSessioneVers> vsv = query.getResultList();
            if (!vsv.isEmpty()) {
                rispostaControlli.setrBoolean(true);
                rispostaControlli.setrLong(vsv.get(0).getIdSessioneVers());
                // restituisco anche il tipo di sessione (versamento o aggiunta) che ha prodotto
                // il doc
                rispostaControlli.setrString(vsv.get(0).getTiSessioneVers());
            }
        }

        return rispostaControlli;
    }

    public RispostaControlli leggiXmlRappVersFromUd(long idSessVers, long idUd) {
        // 1. find from os
        Map<String, String> xmlVersamentoOs = objectStorageService.getObjectSipUnitaDoc(idUd);
        // 2. find from db
        return leggiXmlRappVers(idSessVers, xmlVersamentoOs);
    }

    public RispostaControlli leggiXmlRappVersFromDoc(long idSessVers, long idDoc) {
        // 1. find from os
        Map<String, String> xmlVersamentoOs = objectStorageService.getObjectSipDoc(idDoc);
        // 2. find from db
        return leggiXmlRappVers(idSessVers, xmlVersamentoOs);
    }

    private RispostaControlli leggiXmlRappVers(long idSessVers,
            Map<String, String> xmlVersamentoOs) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        String queryStr = "select xml from VrsXmlDatiSessioneVers xml "
                + "where xml.vrsDatiSessioneVers.vrsSessioneVers.idSessioneVers = :idSessioneVers "
                + "and xml.vrsDatiSessioneVers.tiDatiSessioneVers = 'XML_DOC' "
                + "and xml.tiXmlDati = :tiXmlDati ";

        TypedQuery<VrsXmlDatiSessioneVers> query = entityManager.createQuery(queryStr,
                VrsXmlDatiSessioneVers.class);
        query.setParameter("idSessioneVers", idSessVers);
        query.setParameter("tiXmlDati", CostantiDB.TipiXmlDati.RAPP_VERS);

        List<VrsXmlDatiSessioneVers> vxdsv = query.getResultList();

        if (!xmlVersamentoOs.isEmpty()) {
            rispostaControlli.setrString(xmlVersamentoOs.get(CostantiDB.TipiXmlDati.RAPP_VERS));
        } else if (!vxdsv.isEmpty()) {
            rispostaControlli.setrString(vxdsv.get(0).getBlXml());
        } else {
            rispostaControlli.setrString(null);
        }

        return rispostaControlli;
    }

}
