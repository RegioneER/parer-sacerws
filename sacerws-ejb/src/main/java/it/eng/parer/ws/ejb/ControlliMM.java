/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.AplParamApplic;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliMM")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliMM {

    private static final Logger log = LoggerFactory.getLogger(ControlliMM.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    ConfigurationHelper config;

    public enum TipiRootPath {

        In, Out
    }

    public RispostaControlli caricaRootPath(String appVersante, TipiRootPath tipoRootPath) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        Properties props = new Properties();
        boolean useDb = false;
        boolean prosegui = true;
        String rootftp = null;

        try (InputStream tmpStream = this.getClass().getClassLoader().getResourceAsStream("/sacerEjb.properties")) {
            props.load(tmpStream);
            if (props.getProperty("rootFtp.overrideDbUrl").equals("true")) {
                switch (tipoRootPath) {
                case In:
                    rootftp = props.getProperty("rootFtp.url." + ParametroApplDB.PATH_MM_IN_ + appVersante);
                    break;
                case Out:
                    rootftp = props.getProperty("rootFtp.url." + ParametroApplDB.PATH_MM_OUT_ + appVersante);
                    break;
                default:
                    break;
                }
                rispostaControlli.setrString(rootftp);
                rispostaControlli.setrBoolean(true);
            } else {
                useDb = true;
            }
        } catch (IOException ex) {
            prosegui = false;
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliMM.caricaRootPath - properties: " + ex.getMessage()));
            log.error("Eccezione nella lettura del file di properties ", ex);
        }

        if (prosegui && useDb) {
            try {
                // carico i parametri applicativi
                String queryStr = "select tpa " + "from AplParamApplic tpa "
                        + "where tpa.tiParamApplic = :tiParamApplicIn " + "and tpa.nmParamApplic = :nmParamAppliciN ";

                javax.persistence.Query query = entityManager.createQuery(queryStr, AplParamApplic.class);
                query.setParameter("tiParamApplicIn", ParametroApplDB.TipoParametroAppl.PATH);
                switch (tipoRootPath) {
                case In:
                    query.setParameter("nmParamAppliciN", ParametroApplDB.PATH_MM_IN_ + appVersante);
                    break;
                case Out:
                    query.setParameter("nmParamAppliciN", ParametroApplDB.PATH_MM_OUT_ + appVersante);
                    break;
                default:
                    break;
                }
                AplParamApplic tud = (AplParamApplic) query.getSingleResult();
                rispostaControlli.setrString(config.getParamApplicValue(tud.getNmParamApplic()));
                rispostaControlli.setrBoolean(true);
            } catch (NoResultException e) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666, "ControlliMM.caricaRootPath - "
                                + "Applicativo chiamante non correttamente configurato nella tabella AplParamApplic"));
            } catch (Exception e) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliMM.caricaRootPath - AplParamApplic: " + e.getMessage()));
                log.error("Eccezione nella lettura  della tabella AplParamApplic", e);
            }
        }

        return rispostaControlli;
    }
}
