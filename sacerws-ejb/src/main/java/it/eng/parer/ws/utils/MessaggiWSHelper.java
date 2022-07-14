/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.utils;

import it.eng.parer.entity.DecErrSacer;
import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;
import it.eng.parer.exception.SacerWsRuntimeException;

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

/**
 *
 * @author fioravanti_f
 */
@Stateless(mappedName = "MessaggiWSHelper")
@LocalBean
@TransactionManagement(TransactionManagementType.CONTAINER)
public class MessaggiWSHelper {

    protected static final Logger log = LoggerFactory.getLogger(MessaggiWSHelper.class);

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public List<DecErrSacer> caricaListaErrori() {
        String qlString = "SELECT e FROM DecErrSacer e ";
        Query query = entityManager.createQuery(qlString);

        return query.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public DecErrSacer caricaDecErrore(String cdErrore) {
        DecErrSacer tmperr = null;

        try {
            String qlString = "SELECT e FROM DecErrSacer e " + "where e.cdErr = :cdErr";
            Query query = entityManager.createQuery(qlString);
            query.setParameter("cdErr", cdErrore);
            tmperr = (DecErrSacer) query.getSingleResult();
        } catch (RuntimeException ex) {
            throw new SacerWsRuntimeException(ex, SacerWsErrorCategory.INTERNAL_ERROR);
        }

        return tmperr;
    }

}
