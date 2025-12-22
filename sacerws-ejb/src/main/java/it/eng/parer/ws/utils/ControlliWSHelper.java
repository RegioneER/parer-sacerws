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
package it.eng.parer.ws.utils;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecControlloWs;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;

@SuppressWarnings("unchecked")
@Stateless(mappedName = "ControlliWSHelper")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ControlliWSHelper {

    protected static final Logger log = LoggerFactory.getLogger(ControlliWSHelper.class);

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public List<DecControlloWs> caricaListaControlli() {
        String qlString = "SELECT e FROM DecControlloWs e ";
        Query query = entityManager.createQuery(qlString);

        return query.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public DecControlloWs caricaCdControlloWs(String cdControlloWs) {
        DecControlloWs tmperr = null;

        try {
            Query query = entityManager.createNamedQuery("DecControlloWs.findByCdControlloWs",
                    DecControlloWs.class);
            query.setParameter("cdControlloWs", cdControlloWs);
            tmperr = (DecControlloWs) query.getSingleResult();
        } catch (RuntimeException ex) {
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }

        return tmperr;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<DecControlloWs> caricaCdControlloFamiglia(String cdFamigliaControllo) {
        List<DecControlloWs> tmperr = null;

        try {
            Query query = entityManager.createNamedQuery("DecControlloWs.findByCdFamigliaControllo",
                    DecControlloWs.class);
            query.setParameter("cdFamigliaControllo", cdFamigliaControllo);
            tmperr = query.getResultList();
        } catch (RuntimeException ex) {
            // log.fatal("ControlliWSHelper.caricaControlloFamiglia fallita! ", ex);
            log.error("ControlliWSHelper.caricaControlloFamiglia fallita! ", ex);
            throw ex;
        }

        return tmperr;
    }

}
