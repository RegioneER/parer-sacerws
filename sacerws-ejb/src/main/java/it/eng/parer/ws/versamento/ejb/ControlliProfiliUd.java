/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.ejb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import it.eng.parer.entity.DecModelloXsdUd;
import it.eng.parer.entity.constraint.DecModelloXsdUd.TiModelloXsdUd;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.CostantiDB.TiUsoModelloXsd;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.XmlUtils;
import it.eng.parer.ws.xml.versReq.ProfiloNormativoType;

@Stateless(mappedName = "ControlliProfiliUd")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliProfiliUd {

    private static final Logger LOG = LoggerFactory.getLogger(ControlliProfiliUd.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    protected ControlliSemantici controlliSemantici;

    public RispostaControlli checkProfiloNormativo(ProfiloNormativoType pfNormType, TipiEntitaSacer tiEntita,
            long idTiEntita, String descrEntita, String descrTiEntita) {
        //
        final int IDX_DEC_MODELLO_XSD = 0;
        final int IDX_DEC_USO_MODELLO_XSD = 1;
        //
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            // verifico se esiste almeno un USO di modello sul tiEntita / idTiEntita
            List<Long> idUsoModelloXsds = checkXsdProfileExistence(idTiEntita, tiEntita,
                    TiModelloXsdUd.PROFILO_NORMATIVO_UNITA_DOC);
            if (!idUsoModelloXsds.isEmpty()) {
                // esiste un modello XSD, devo vedere se è dichiarato nell'XML e se va bene
                if (pfNormType == null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.PROFNORM_001_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PROFNORM_001_001,
                            tiEntita.descrivi(), descrEntita, descrTiEntita));
                    return rispostaControlli;
                }
                //
                Object[] dmxud = getXsdProfileByVersion(idUsoModelloXsds, tiEntita,
                        TiModelloXsdUd.PROFILO_NORMATIVO_UNITA_DOC, pfNormType.getVersione());
                if (dmxud == null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.PROFNORM_001_002);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PROFNORM_001_002,
                            tiEntita.descrivi(), descrEntita, pfNormType.getVersione(), descrTiEntita));
                    return rispostaControlli;
                }

                // Object[] = 1 pk uso
                long idRecUsoXsdProfiloNormativo = ((Long) (dmxud[IDX_DEC_USO_MODELLO_XSD])).longValue();
                // Object[] = 0 DecModelloXsdUd
                String paXsd = ((DecModelloXsdUd) dmxud[IDX_DEC_MODELLO_XSD]).getBlXsd();
                RispostaControlli rc = validateXmlProfileOnXsd(paXsd, pfNormType.getAny());
                if (!rc.isrBoolean()) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.PROFNORM_001_003);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PROFNORM_001_003,
                            tiEntita.descrivi(), descrEntita, descrTiEntita, rc.getDsErr()));
                    return rispostaControlli;
                }
                // generate xml + canonicalize
                String xml = generaXmlProfilo(pfNormType.getAny());
                rispostaControlli.setrString(XmlUtils.doCanonicalizzazioneXml(xml, false));
                rispostaControlli.setrLong(idRecUsoXsdProfiloNormativo);
                rispostaControlli.setrBoolean(true);
            } else {
                if (pfNormType != null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.PROFNORM_001_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.PROFNORM_001_004,
                            tiEntita.descrivi(), descrEntita, descrTiEntita));
                    return rispostaControlli;
                }
                // il modello non c'è e non è presente nell'XSD... la verifica è andata bene.
                rispostaControlli.setrBoolean(true);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliProfiliUd.checkProfiloNormativo: " + e.getMessage()));
            LOG.error("Eccezione nella verifica profilo normativo ", e);
        }

        return rispostaControlli;
    }

    private List<Long> checkXsdProfileExistence(long idTiEntita, TipiEntitaSacer tiEntitaSacer,
            TiModelloXsdUd tiModelloXsdUd) {
        //
        StringBuilder queryStr = new StringBuilder();

        if (tiEntitaSacer.equals(TipiEntitaSacer.UNI_DOC)) {
            queryStr.append("select uso.idUsoModelloXsdUniDoc " + "from DecUsoModelloXsdUniDoc uso ");
            queryStr.append("where uso.decTipoUnitaDoc.idTipoUnitaDoc = :idTiEntita ");
        } else if (tiEntitaSacer.equals(TipiEntitaSacer.DOC)) {
            queryStr.append("select uso.idUsoModelloXsdDoc " + "from DecUsoModelloXsdDoc uso ");
            queryStr.append("where uso.decTipoUnitaDoc.idTipoUnitaDoc = :idTiEntita ");
        } else if (tiEntitaSacer.equals(TipiEntitaSacer.COMP) || tiEntitaSacer.equals(TipiEntitaSacer.SUB_COMP)) {
            queryStr.append("select uso.idUsoModelloXsdCompDoc " + "from DecUsoModelloXsdCompDoc uso ");
            queryStr.append("where uso.decTipoCompDoc.idTipoCompDoc = :idTiEntita ");
        }
        // common part
        queryStr.append("and uso.decModelloXsdUd.tiModelloXsd = :tiModelloXsdUd "
                + "and uso.decModelloXsdUd.tiUsoModelloXsd = :tiUsoModelloXsd "
                + "and uso.decModelloXsdUd.dtIstituz <= :dataDiOggiIn "
                + "and uso.decModelloXsdUd.dtSoppres > :dataDiOggiIn " + "and uso.dtIstituz <= :dataDiOggiIn "
                + "and uso.dtSoppres > :dataDiOggiIn ");

        javax.persistence.Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter("idTiEntita", new BigDecimal(idTiEntita));
        query.setParameter("tiModelloXsdUd", tiModelloXsdUd);
        query.setParameter("tiUsoModelloXsd", TiUsoModelloXsd.VERS.name());
        query.setParameter("dataDiOggiIn", new Date());
        return query.getResultList();
    }

    /*
     * Nota: restituisce modello+pk uso del modello
     */
    private Object[] getXsdProfileByVersion(List<Long> idUsoModelloXsds, TipiEntitaSacer tiEntitaSacer,
            TiModelloXsdUd tiModelloXsdUd, String version) {
        Object[] result = null;

        StringBuilder queryStr = new StringBuilder();
        //
        queryStr.append("select uso.decModelloXsdUd "); // model

        if (tiEntitaSacer.equals(TipiEntitaSacer.UNI_DOC)) {
            queryStr.append(", uso.idUsoModelloXsdUniDoc "); // pk
            queryStr.append("from DecUsoModelloXsdUniDoc uso ");
            queryStr.append("where uso.idUsoModelloXsdUniDoc in :idUsoModelloXsds ");
        } else if (tiEntitaSacer.equals(TipiEntitaSacer.DOC)) {
            queryStr.append(", uso.idUsoModelloXsdDoc "); // pk
            queryStr.append("from DecUsoModelloXsdDoc uso ");
            queryStr.append("where uso.idUsoModelloXsdDoc in :idUsoModelloXsds ");
        } else if (tiEntitaSacer.equals(TipiEntitaSacer.COMP) || tiEntitaSacer.equals(TipiEntitaSacer.SUB_COMP)) {
            queryStr.append(", uso.idUsoModelloXsdCompDoc "); // pk
            queryStr.append("from DecUsoModelloXsdCompDoc uso ");
            queryStr.append("where uso.idUsoModelloXsdCompDoc in :idUsoModelloXsds ");
        }
        //
        queryStr.append("and uso.decModelloXsdUd.tiModelloXsd = :tiModelloXsdUd "
                + "and uso.decModelloXsdUd.tiUsoModelloXsd = :tiUsoModelloXsd "
                + "and UPPER(uso.decModelloXsdUd.cdXsd) = :cdXsd "
                + "and uso.decModelloXsdUd.dtIstituz <= :dataDiOggiIn "
                + "and uso.decModelloXsdUd.dtSoppres > :dataDiOggiIn ");

        javax.persistence.Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter("idUsoModelloXsds", idUsoModelloXsds);
        query.setParameter("tiModelloXsdUd", tiModelloXsdUd);
        query.setParameter("cdXsd", version.toUpperCase().trim()); // normalize version
        query.setParameter("tiUsoModelloXsd", TiUsoModelloXsd.VERS.name());
        query.setParameter("dataDiOggiIn", new Date());
        //
        try {
            result = (Object[]) query.getSingleResult();
        } catch (NoResultException e) {
            // caso possibile = versione non presente o disattivata
            LOG.warn(
                    "ControlliProfiliUd.getXsdProfileByVersion: nessun risultato trovato per {} con versione {} su tipo entita {}",
                    tiModelloXsdUd.name(), version, tiEntitaSacer.name(), e);
        }
        return result;
    }

    private RispostaControlli validateXmlProfileOnXsd(String xsd, Node xml) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        try {
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            Schema schema = factory.newSchema(new StreamSource(new StringReader(xsd)));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(xml));
            rispostaControlli.setrBoolean(true);
        } catch (IOException | SAXException e) {
            rispostaControlli.setDsErr(e.getLocalizedMessage());
        }

        return rispostaControlli;
    }

    private String generaXmlProfilo(Node profilo) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(profilo);
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

}
