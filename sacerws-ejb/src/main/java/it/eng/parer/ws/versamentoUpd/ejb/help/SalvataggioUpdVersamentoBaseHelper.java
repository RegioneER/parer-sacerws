/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ejb.help;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.eng.parer.entity.AroUsoXsdDatiSpec;
import it.eng.parer.entity.AroValoreAttribDatiSpec;
import it.eng.parer.entity.DecXsdAttribDatiSpec;
import it.eng.parer.util.ejb.AppServerInstance;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.XmlUpdVersCache;
import it.eng.parer.ws.ejb.XmlVersCache;
import it.eng.parer.ws.utils.ControlliWSHelper;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.CostantiDB.TipiEntitaSacer;
import it.eng.parer.ws.utils.CostantiDB.TipiUsoDatiSpec;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSHelper;

public abstract class SalvataggioUpdVersamentoBaseHelper {

    @EJB
    protected MessaggiWSHelper messaggiWSHelper;

    @EJB
    protected ControlliWSHelper controlliWSHelper;

    @EJB
    XmlUpdVersCache xmlUpdVersCache;

    @EJB
    XmlVersCache xmlVersCache;

    @EJB
    protected AppServerInstance appServerInstance;

    @EJB
    protected LogSessioneUpdVersamentoHelper updLogSessioneHelper;

    @PersistenceContext(unitName = "ParerJPA")
    protected EntityManager entityManager;

    public abstract Logger getLogger();

    public RispostaControlli checkUsoXsdDatiSpecifici(long idAroUdOrDocOrComp, TipiUsoDatiSpec tiUsoXsd,
            TipiEntitaSacer tiEntitaSacer) {

        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        List<AroUsoXsdDatiSpec> usoXsdDatiSpecs = null;

        try {
            String queryStr = "select ud from AroUsoXsdDatiSpec ud " + "where ud.tiUsoXsd = :tiUsoXsd  "
                    + "and ud.tiEntitaSacer = :tiEntitaSacer ";

            switch (tiEntitaSacer) {
            case UNI_DOC:
                queryStr += "and ud.aroUnitaDoc.idUnitaDoc = :idUnitaDoc ";
                break;
            case DOC:
                queryStr += "and ud.aroDoc.idDoc = :idDoc ";
                break;
            case COMP:
                queryStr += "and ud.aroCompDoc.idCompDoc = :idCompDoc ";
                break;
            default:
                break;
            }

            // ud.aroUnitaDoc.idUnitaDoc = :idUnitaDoc
            javax.persistence.Query query = entityManager.createQuery(queryStr, AroUsoXsdDatiSpec.class);
            query.setParameter("tiUsoXsd", tiUsoXsd.name());
            query.setParameter("tiEntitaSacer", tiEntitaSacer.name());

            switch (tiEntitaSacer) {
            case UNI_DOC:
                query.setParameter("idUnitaDoc", idAroUdOrDocOrComp);
                break;
            case DOC:
                query.setParameter("idDoc", idAroUdOrDocOrComp);
                break;
            case COMP:
                query.setParameter("idCompDoc", idAroUdOrDocOrComp);
                break;
            default:
                break;
            }

            usoXsdDatiSpecs = query.getResultList();

            // se esiste ...
            if (usoXsdDatiSpecs.size() == 1) {
                // livello attuale
                rispostaControlli.setrObject(usoXsdDatiSpecs.get(0));
                rispostaControlli.setrLong(0);
            }

            rispostaControlli.setrBoolean(true); // query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "SalvataggioUpdVersamentoBaseHelper.checkUsoXsdDatiSpecifici: " + e.getMessage()));
            getLogger().error("Eccezione nella lettura  della tabella dati specifici ", e);
        }

        return rispostaControlli;
    }

    public RispostaControlli getAroValoreAttribDatiSpecs(long idUsoXsdDatiSpec, long idAttribDatiSpec, long idStrut) {

        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrLong(-1);
        rispostaControlli.setrBoolean(false);

        List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpec = null;

        try {
            String queryStr = "select ud from AroValoreAttribDatiSpec ud "
                    + "where ud.aroUsoXsdDatiSpec.idUsoXsdDatiSpec = :idUsoXsdDatiSpec  "
                    + "and ud.decAttribDatiSpec.idAttribDatiSpec = :idAttribDatiSpec " + "and ud.idStrut = :idStrut ";

            javax.persistence.Query query = entityManager.createQuery(queryStr, AroValoreAttribDatiSpec.class);
            query.setParameter("idUsoXsdDatiSpec", idUsoXsdDatiSpec);
            query.setParameter("idAttribDatiSpec", idAttribDatiSpec);
            query.setParameter("idStrut", idStrut);

            aroValoreAttribDatiSpec = query.getResultList();

            // se esiste ...
            if (aroValoreAttribDatiSpec.size() >= 1) {
                // livello attuale
                rispostaControlli.setrObject(aroValoreAttribDatiSpec);
                rispostaControlli.setrLong(0);
            }

            rispostaControlli.setrBoolean(true); // query is good
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "SalvataggioUpdVersamentoBaseHelper.getAroValoreAttribDatiSpecs: " + e.getMessage()));
            getLogger().error("Eccezione nella lettura  dei valori dei dati specifici ", e);
        }

        return rispostaControlli;
    }

    protected RispostaControlli generaXmlDatiSpecFromAroUnitaDoc(String versione, String tiUsoXsd,
            List<DecXsdAttribDatiSpec> attrDs, long idAroUsoXsdDatiSpec, long idStrut)
            throws JAXBException, ParserConfigurationException {

        RispostaControlli tmpRispostaControlli = new RispostaControlli();
        tmpRispostaControlli.setrBoolean(true);

        //
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();

        it.eng.parer.ws.xml.versReq.DatiSpecificiType datiSpec = new it.eng.parer.ws.xml.versReq.DatiSpecificiType();
        // build object
        datiSpec.setVersioneDatiSpecifici(versione);

        /*
         * // per ogni attributo dei dati specifici costruiscta <tag>value</tag> // ordinando per getNiOrdAttrib
         * List<DecXsdAttribDatiSpec> sorted = new ArrayList<>(attrDs); sorted.sort(new
         * Comparator<DecXsdAttribDatiSpec>() {
         * 
         * @Override public int compare(DecXsdAttribDatiSpec d1, DecXsdAttribDatiSpec d2) { return
         * d1.getNiOrdAttrib().compareTo(d2.getNiOrdAttrib()); } });
         */
        for (DecXsdAttribDatiSpec ds : attrDs) {
            String attr = ds.getDecAttribDatiSpec().getNmAttribDatiSpec();

            List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs = new ArrayList<>(0);
            // recupero il valore per quell'attributo
            tmpRispostaControlli = this.getAroValoreAttribDatiSpecs(idAroUsoXsdDatiSpec,
                    ds.getDecAttribDatiSpec().getIdAttribDatiSpec(), idStrut);

            // query is not good ..
            if (!tmpRispostaControlli.isrBoolean()) {
                return tmpRispostaControlli;
            }

            if (tmpRispostaControlli.getrLong() != -1) {
                aroValoreAttribDatiSpecs = (List<AroValoreAttribDatiSpec>) tmpRispostaControlli.getrObject();
            }

            /*
             * List<AroValoreAttribDatiSpec> aroValoreAttribDatiSpecs = new ArrayList<>(ds.getDecAttribDatiSpec()
             * .getAroValoreAttribDatiSpecs()).stream() .filter(d -> d.getAroUsoXsdDatiSpec().getIdUsoXsdDatiSpec() ==
             * idAroUsoXsdDatiSpec).collect(Collectors.toList());
             */

            for (AroValoreAttribDatiSpec tmpAroValoreAttribDatiSpec : aroValoreAttribDatiSpecs) {
                // create the element
                Element element = doc.createElement(attr);
                // gestito anche il caso di valore nullo </EmptyTag>
                element.appendChild(doc.createTextNode(StringUtils.isBlank(tmpAroValoreAttribDatiSpec.getDlValore())
                        ? "" : tmpAroValoreAttribDatiSpec.getDlValore()));
                //
                datiSpec.getAny().add(element);
            }
        }

        // TODO: non dovrebbe occorrere la validazione dato che è stata già fatta in
        // fase di controllo !
        // vedi UpdGestioneDatiSpec
        //
        // Root element
        JAXBElement<it.eng.parer.ws.xml.versReq.DatiSpecificiType> root;
        if (tiUsoXsd.equalsIgnoreCase(CostantiDB.TipiUsoDatiSpec.VERS.name())) {
            root = new it.eng.parer.ws.xml.versReq.ObjectFactory().createUnitaDocumentariaDatiSpecifici(datiSpec);
        } else {
            root = new it.eng.parer.ws.xml.versReq.ObjectFactory()
                    .createUnitaDocumentariaDatiSpecificiMigrazione(datiSpec);
        }
        Marshaller tmpMarshaller = xmlVersCache.getVersReqCtxforUD().createMarshaller();
        StringWriter sw = new StringWriter();
        tmpMarshaller.marshal(root, sw);
        //
        // return sw.toString();

        tmpRispostaControlli.setrString(sw.toString());

        return tmpRispostaControlli;
    }
}
