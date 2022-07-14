/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.ejb;

import it.eng.parer.entity.AplParamApplic;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.ParametroApplDB;

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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fioravanti_F
 */
@Stateless(mappedName = "ControlliTpi")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliTpi {

    private static final Logger log = LoggerFactory.getLogger(ControlliTpi.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    ConfigurationHelper config;

    public RispostaControlli caricaRootPath() {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        Properties props = new Properties();
        boolean useDb = false;
        boolean prosegui = true;
        InputStream tmpStream = null;
        String rootftp = null;

        try {
            tmpStream = this.getClass().getClassLoader().getResourceAsStream("/sacerEjb.properties");
            props.load(tmpStream);
            if (props.getProperty("tpiFs.overrideDbUrl").equals("true")) {
                rootftp = props.getProperty("tpiFs.url." + ParametroApplDB.TPI_ROOT_SACER);
                rispostaControlli.setrString(rootftp);
                rispostaControlli.setrBoolean(true);
            } else {
                useDb = true;
            }
        } catch (IOException ex) {
            prosegui = false;
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliTpi.caricaRootPath - properties: " + ex.getMessage()));
            log.error("Eccezione nella lettura del file di properties ", ex);
        } finally {
            IOUtils.closeQuietly(tmpStream);
        }

        if (prosegui && useDb) {
            try {
                // carico i parametri applicativi
                String queryStr = "select tpa " + "from AplParamApplic tpa " + "where "
                        + "tpa.tiParamApplic = :tiParamApplicIn " + "and tpa.nmParamApplic = :nmParamAppliciN ";

                javax.persistence.Query query = entityManager.createQuery(queryStr, AplParamApplic.class);
                query.setParameter("tiParamApplicIn", ParametroApplDB.TipoParametroAppl.TPI);
                query.setParameter("nmParamAppliciN", ParametroApplDB.TPI_ROOT_SACER);

                AplParamApplic tud = (AplParamApplic) query.getSingleResult();
                rispostaControlli.setrString(config.getParamApplicValue(tud.getNmParamApplic()));
                rispostaControlli.setrBoolean(true);
            } catch (NoResultException e) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666, "ControlliTpi.caricaRootPath - "
                                + "Applicativo chiamante non correttamente configurato nella tabella AplParamApplic"));
            } catch (Exception e) {
                rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
                rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliTpi.caricaRootPath - AplParamApplic: " + e.getMessage()));
                log.error("Eccezione nella lettura  della tabella AplParamApplic " + e);
            }
        }
        return rispostaControlli;
    }

    public RispostaControlli verificaAbilitazioneTpi() {
        /*
         * se il TPI non è stato installato, vuol dire che tutta la gestione asincrona del versamento basata su TIVOLI è
         * inutilizabile. In questo caso lo storage dei documenti avviene su una tabella di blob dedicata chiamata
         * ARO_FILE_COMP con struttura identica a ARO_CONTENUTO_COMP
         */
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        Properties props = new Properties();
        InputStream tmpStream = null;

        try {
            tmpStream = this.getClass().getClassLoader().getResourceAsStream("/sacerEjb.properties");
            props.load(tmpStream);
            if (props.getProperty("tpi.TPIAbilitato").equals("true")) {
                rispostaControlli.setrBoolean(true);
            } else {
                rispostaControlli.setrBoolean(false);
            }
        } catch (IOException ex) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliTpi.verificaAbilitazioneTpi - properties: " + ex.getMessage()));
            log.error("Eccezione nella lettura del file di properties ", ex);
        } finally {
            IOUtils.closeQuietly(tmpStream);
        }

        return rispostaControlli;
    }
}
