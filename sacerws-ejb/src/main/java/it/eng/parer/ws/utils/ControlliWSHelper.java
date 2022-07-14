/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author sinatti_s
 */
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
            Query query = entityManager.createNamedQuery("DecControlloWs.findByCdControlloWs", DecControlloWs.class);
            query.setParameter("cdControlloWs", cdControlloWs);
            tmperr = (DecControlloWs) query.getSingleResult();
        } catch (RuntimeException ex) {
            // log.fatal("ControlliWSHelper.caricaDecControlloWs fallita! ", ex);
            log.error("ControlliWSHelper.caricaDecControlloWs fallita! ", ex);
            throw ex;
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
