/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versFascicoli.ejb;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.eng.parer.entity.DecModelloXsdFascicolo;
import it.eng.parer.entity.constraint.DecModelloXsdFascicolo.TiModelloXsd;
import it.eng.parer.entity.constraint.DecModelloXsdFascicolo.TiUsoModelloXsd;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.CSChiaveFasc;
import it.eng.parer.ws.dto.CSChiaveSottFasc;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versFascicoli.dto.DXPAFascicoloCollegato;
import it.eng.parer.ws.versFascicoli.dto.DXPAVoceClassificazione;
import it.eng.parer.ws.versFascicoli.dto.DXPGAmminPartecipante;
import it.eng.parer.ws.versFascicoli.dto.DXPGAmminTitolare;
import it.eng.parer.ws.versFascicoli.dto.DXPGProcAmmininistrativo;
import it.eng.parer.ws.versFascicoli.dto.DXPGRespFascicolo;
import it.eng.parer.ws.versFascicoli.dto.DXPGSoggettoCoinvolto;
import it.eng.parer.ws.versFascicoli.dto.DatiXmlProfiloArchivistico;
import it.eng.parer.ws.versFascicoli.dto.DatiXmlProfiloGenerale;
import it.eng.parer.ws.versFascicoli.dto.StrutturaVersFascicolo;
import it.eng.parer.ws.versFascicoli.dto.VersFascicoloExt;
import it.eng.parer.ws.xml.versfascicolo.IndiceSIPFascicolo;
import it.eng.parer.ws.xml.versfascicolo.ProfiloArchivisticoType;
import it.eng.parer.ws.xml.versfascicolo.ProfiloGeneraleType;
import it.eng.parer.ws.xml.versfascicolo.TipoConservazioneType;
import it.eng.parer.ws.xml.versfascicoloresp.ECFascicoloType;

/**
 *
 * @author fioravanti_f
 */
@SuppressWarnings("unchecked")
@Stateless(mappedName = "ControlliProfiliFascicolo")
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class ControlliProfiliFascicolo {

    private static final Logger log = LoggerFactory.getLogger(ControlliProfiliFascicolo.class);
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    protected ControlliSemantici controlliSemantici;

    public RispostaControlli verificaProfiloArchivistico(VersFascicoloExt versamento) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        IndiceSIPFascicolo parsedIndiceFasc = versamento.getVersamento();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();

        // verifico se esiste almeno un modello di tipo fascicolo utile
        long conta = 0;
        try {
            String queryStr = "select count(t) from DecUsoModelloXsdFasc t "
                    + "where t.decAaTipoFascicolo.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo "
                    + "and t.decAaTipoFascicolo.aaIniTipoFascicolo <= :annoIn "
                    + "and t.decAaTipoFascicolo.aaFinTipoFascicolo >= :annoIn "
                    + "and t.decModelloXsdFascicolo.tiModelloXsd = :segnatura "
                    + "and t.decModelloXsdFascicolo.tiUsoModelloXsd = :usoversamento "
                    + "and t.decModelloXsdFascicolo.dtIstituz <= :dataDiOggiIn "
                    + "and t.decModelloXsdFascicolo.dtSoppres > :dataDiOggiIn "; // da notare STRETTAMENTE MAGGIORE ;
            javax.persistence.Query query = entityManager.createQuery(queryStr);
            query.setParameter("idTipoFascicolo", svf.getIdTipoFascicolo());
            query.setParameter("annoIn", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
            query.setParameter("segnatura", TiModelloXsd.PROFILO_ARCHIVISTICO_FASCICOLO);
            query.setParameter("usoversamento", TiUsoModelloXsd.VERS);
            query.setParameter("dataDiOggiIn", new Date());
            conta = (Long) query.getSingleResult();
            //
            if (conta > 0) {
                // esiste un modello XSD, devo vedere se è dichiarato nell'XML e se va bene
                if (parsedIndiceFasc.getProfiloArchivistico() == null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_001_004);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_001_004,
                            svf.getUrnPartChiaveFascicolo(), svf.getTipoFascicoloNonverificato()));
                    return rispostaControlli;
                }
                //
                String versione = null;
                List<DecModelloXsdFascicolo> dmxfs;
                if (parsedIndiceFasc.getParametri().getVersioneProfiloArchivisticoFascicolo() != null) {
                    versione = parsedIndiceFasc.getParametri().getVersioneProfiloArchivisticoFascicolo();
                    // la versione è stata indicata
                    queryStr = "select t from DecModelloXsdFascicolo t " + "join t.decUsoModelloXsdFascs uf "
                            + "where t.tiModelloXsd = :segnatura " + "and t.tiUsoModelloXsd = :usoversamento "
                            + "and t.cdXsd = :cdXsd " + "and t.dtIstituz <= :dataDiOggiIn "
                            + "and t.dtSoppres > :dataDiOggiIn " // da notare STRETTAMENTE MAGGIORE
                            + "and uf.decAaTipoFascicolo.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo "
                            + "and uf.decAaTipoFascicolo.aaIniTipoFascicolo <= :annoIn "
                            + "and uf.decAaTipoFascicolo.aaFinTipoFascicolo >= :annoIn ";
                    query = entityManager.createQuery(queryStr, DecModelloXsdFascicolo.class);
                    query.setParameter("cdXsd", versione);
                    query.setParameter("idTipoFascicolo", svf.getIdTipoFascicolo());
                    query.setParameter("annoIn", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
                    query.setParameter("segnatura", TiModelloXsd.PROFILO_ARCHIVISTICO_FASCICOLO);
                    query.setParameter("usoversamento", TiUsoModelloXsd.VERS);
                    query.setParameter("dataDiOggiIn", new Date());
                    //
                    dmxfs = query.getResultList();
                    if (dmxfs.isEmpty()) {
                        rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_001_002);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_001_002,
                                svf.getUrnPartChiaveFascicolo(), versione));
                        return rispostaControlli;
                    }
                } else {
                    // versione standard
                    queryStr = "select t from DecModelloXsdFascicolo t " + "join t.decUsoModelloXsdFascs uf "
                            + "where t.tiModelloXsd = :segnatura " + "and t.tiUsoModelloXsd = :usoversamento "
                            + "and uf.flStandard = :flDefault " + "and t.dtIstituz <= :dataDiOggiIn "
                            + "and t.dtSoppres > :dataDiOggiIn " // da notare STRETTAMENTE MAGGIORE
                            + "and uf.decAaTipoFascicolo.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo "
                            + "and uf.decAaTipoFascicolo.aaIniTipoFascicolo <= :annoIn "
                            + "and uf.decAaTipoFascicolo.aaFinTipoFascicolo >= :annoIn ";
                    query = entityManager.createQuery(queryStr, DecModelloXsdFascicolo.class);
                    query.setParameter("flDefault", "1");
                    query.setParameter("idTipoFascicolo", svf.getIdTipoFascicolo());
                    query.setParameter("annoIn", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
                    query.setParameter("segnatura", TiModelloXsd.PROFILO_ARCHIVISTICO_FASCICOLO);
                    query.setParameter("usoversamento", TiUsoModelloXsd.VERS);
                    query.setParameter("dataDiOggiIn", new Date());
                    //
                    dmxfs = query.getResultList();
                    if (dmxfs.isEmpty()) {
                        rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_001_001);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_001_001,
                                svf.getUrnPartChiaveFascicolo(), svf.getTipoFascicoloNonverificato()));
                        return rispostaControlli;
                    }
                }
                // nota: questa logica (una variabile bool che mi indica se proseguire o meno)
                // in questo caso non serve a nulla perché questo metodo esce al primo errore
                // incontrato. Se in futuro sarà posibile rendere una lista di errori come
                // la verifica del profilo Generale, sarà già pronta.
                boolean prosegui = false;
                // ho recuperato l'XSD. salvo il riferimento per un futuro utilizzo
                svf.setIdRecXsdProfiloArchivistico(dmxfs.get(0).getIdModelloXsdFascicolo());
                // ho finalmente l'XSD del profilo archivistico, adesso lo verifico
                String paXsd = dmxfs.get(0).getBlXsd();
                RispostaControlli rc = this.verificaXsd(paXsd,
                        parsedIndiceFasc.getProfiloArchivistico().getValue().getAny());
                if (rc.isrBoolean()) {
                    prosegui = true;
                } else {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_002_001);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_002_001,
                            svf.getUrnPartChiaveFascicolo(), rc.getDsErr()));
                    return rispostaControlli;
                }
                DatiXmlProfiloArchivistico dxpa = null;
                if (prosegui) {
                    dxpa = this.recuperaDatiDaXmlPA(parsedIndiceFasc.getProfiloArchivistico().getValue());
                    if (dxpa == null) {
                        rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_002_001);
                        rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_002_001,
                                svf.getUrnPartChiaveFascicolo(),
                                "impossibile recuperare i metadati dal Profilo Atchivistico del fascicolo"));
                        return rispostaControlli;
                    }
                    // salvo i dati XML del profilo archivistico
                    svf.setDatiXmlProfiloArchivistico(dxpa);
                }
                if (prosegui) {
                    // Oggi è la mia giornata fortunata... la verifica è andata bene.
                    rispostaControlli.setrBoolean(true);
                }
            } else {
                if (parsedIndiceFasc.getProfiloArchivistico() != null) {
                    rispostaControlli.setCodErr(MessaggiWSBundle.FAS_PF_ARCH_001_003);
                    rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.FAS_PF_ARCH_001_003,
                            svf.getUrnPartChiaveFascicolo(), svf.getTipoFascicoloNonverificato()));
                    return rispostaControlli;
                }
                // il modello non c'è e non è presente nell'XSD... la verifica è andata bene.
                rispostaControlli.setrBoolean(true);
            }
        } catch (Exception e) {
            rispostaControlli.setCodErr(MessaggiWSBundle.ERR_666);
            rispostaControlli.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliProfiliFascicolo.verificaProfiloArchivistico: " + e.getMessage()));
            log.error("Eccezione nella verifica profilo archivistico ", e);
        }

        return rispostaControlli;
    }

    private DatiXmlProfiloArchivistico recuperaDatiDaXmlPA(ProfiloArchivisticoType pat) {
        DatiXmlProfiloArchivistico tmpDatiXml = new DatiXmlProfiloArchivistico();
        try {
            String tmpString;
            Node tmpDati = pat.getAny();
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            //
            XPathExpression expr = xpath
                    .compile("//SegnaturaArchivistica" + "/Classificazione" + "/IndiceClassificazione/text()");
            NodeList nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setIndiceClassificazione(tmpString);
            }
            //
            Node tmpnode;
            StringBuilder tmpSb = new StringBuilder();
            expr = xpath.compile("//SegnaturaArchivistica" + "/Classificazione" + "/DescrizioneIndiceClassificazione"
                    + "/VoceClassificazione");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpnode = nodes.item(i);
                if (tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpSb.append(this.recuperaDescVoceClassif(tmpnode));
                    tmpDatiXml.addVoceClassificazione(this.recuperVoceClassificazione(tmpnode));
                }
            }
            tmpDatiXml.setDescIndiceClassificazione(tmpSb.toString());
            //
            expr = xpath.compile("//SegnaturaArchivistica" + "/FascicoloDiAppartenenza");
            tmpnode = (Node) expr.evaluate(tmpDati, XPathConstants.NODE);
            if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                tmpDatiXml.setChiaveFascicoloDiAppartenenza(this.recuperaCsChiaveSottoFascDaNodo(tmpnode));
            }
            //
            expr = xpath.compile("//Collegamenti" + "/FascicoloCollegato");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                // FascicoloCollegato
                tmpnode = nodes.item(i);
                if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpDatiXml.addFascCollegato(this.recuperaFascCollegato(tmpnode));
                }
            }
        } catch (Exception ex) {
            log.error("errore recupero dati di profilo archivistico", ex);
            return null;
        }

        return tmpDatiXml;
    }

    private DXPAFascicoloCollegato recuperaFascCollegato(Node tmpnode) {
        DXPAFascicoloCollegato tmpFascCollegato = new DXPAFascicoloCollegato();
        NodeList tmpList;
        boolean continua = true;
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("DescrizioneCollegamento");
            if (tmpList.getLength() > 0) {
                tmpFascCollegato.setDescCollegamento(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("ChiaveCollegamento");
            if (tmpList.getLength() > 0) {
                tmpFascCollegato.setCsChiaveFasc(this.recuperaCsChiaveFascDaNodo(tmpList.item(0)));
            } else {
                continua = false;
            }
        }
        return continua ? tmpFascCollegato : null;
    }

    private String recuperaDescVoceClassif(Node tmpnode) {
        NodeList tmpList;
        String retVal = "/";
        tmpList = ((Element) tmpnode).getElementsByTagName("CodiceVoce");
        if (tmpList.getLength() > 0) {
            retVal += tmpList.item(0).getTextContent();
        }
        //
        tmpList = ((Element) tmpnode).getElementsByTagName("DescrizioneVoce");
        if (tmpList.getLength() > 0) {
            retVal += "/" + tmpList.item(0).getTextContent();
        }
        return retVal;
    }

    private DXPAVoceClassificazione recuperVoceClassificazione(Node tmpnode) {
        DXPAVoceClassificazione tmpVoceClassificazione = new DXPAVoceClassificazione();
        NodeList tmpList;
        boolean continua = true;
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("CodiceVoce");
            if (tmpList.getLength() > 0) {
                tmpVoceClassificazione.setCodiceVoce(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("DescrizioneVoce");
            if (tmpList.getLength() > 0) {
                tmpVoceClassificazione.setDescrizioneVoce(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        return continua ? tmpVoceClassificazione : null;
    }

    public RispostaControlli verificaProfiloGenerale(VersFascicoloExt versamento, ECFascicoloType fascicoloResp) {
        RispostaControlli rispostaControlli;
        rispostaControlli = new RispostaControlli();
        rispostaControlli.setrBoolean(false);

        IndiceSIPFascicolo parsedIndiceFasc = versamento.getVersamento();
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();

        try {
            String versione = null;
            List<DecModelloXsdFascicolo> dmxfs;

            if (parsedIndiceFasc.getParametri().getVersioneProfiloGeneraleFascicolo() != null) {
                versione = parsedIndiceFasc.getParametri().getVersioneProfiloGeneraleFascicolo();
                // la versione è stata indicata
                String queryStr = "select t from DecModelloXsdFascicolo t " + "join t.decUsoModelloXsdFascs uf "
                        + "where t.tiModelloXsd = :segnatura " + "and t.tiUsoModelloXsd = :usoversamento "
                        + "and t.cdXsd = :cdXsd " + "and t.dtIstituz <= :dataDiOggiIn "
                        + "and t.dtSoppres > :dataDiOggiIn " // da notare STRETTAMENTE MAGGIORE
                        + "and uf.decAaTipoFascicolo.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo "
                        + "and uf.decAaTipoFascicolo.aaIniTipoFascicolo <= :annoIn "
                        + "and uf.decAaTipoFascicolo.aaFinTipoFascicolo >= :annoIn ";
                javax.persistence.Query query = entityManager.createQuery(queryStr, DecModelloXsdFascicolo.class);
                query.setParameter("cdXsd", versione);
                query.setParameter("idTipoFascicolo", svf.getIdTipoFascicolo());
                query.setParameter("annoIn", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
                query.setParameter("segnatura", TiModelloXsd.PROFILO_GENERALE_FASCICOLO);
                query.setParameter("usoversamento", TiUsoModelloXsd.VERS);
                query.setParameter("dataDiOggiIn", new Date());
                //
                dmxfs = query.getResultList();
                if (dmxfs.isEmpty()) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_001_002,
                            svf.getUrnPartChiaveFascicolo(), versione);
                    return rispostaControlli;
                }
            } else {
                // versione standard
                String queryStr = "select t from DecModelloXsdFascicolo t " + "join t.decUsoModelloXsdFascs uf "
                        + "where t.tiModelloXsd = :segnatura " + "and t.tiUsoModelloXsd = :usoversamento "
                        + "and uf.flStandard = :flDefault " + "and t.dtIstituz <= :dataDiOggiIn "
                        + "and t.dtSoppres > :dataDiOggiIn " // da notare STRETTAMENTE MAGGIORE
                        + "and uf.decAaTipoFascicolo.decTipoFascicolo.idTipoFascicolo = :idTipoFascicolo "
                        + "and uf.decAaTipoFascicolo.aaIniTipoFascicolo <= :annoIn "
                        + "and uf.decAaTipoFascicolo.aaFinTipoFascicolo >= :annoIn ";
                javax.persistence.Query query = entityManager.createQuery(queryStr, DecModelloXsdFascicolo.class);
                query.setParameter("flDefault", "1");
                query.setParameter("idTipoFascicolo", svf.getIdTipoFascicolo());
                query.setParameter("annoIn", new BigDecimal(svf.getChiaveNonVerificata().getAnno()));
                query.setParameter("segnatura", TiModelloXsd.PROFILO_GENERALE_FASCICOLO);
                query.setParameter("usoversamento", TiUsoModelloXsd.VERS);
                query.setParameter("dataDiOggiIn", new Date());
                //
                dmxfs = query.getResultList();
                if (dmxfs.isEmpty()) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_001_001,
                            svf.getUrnPartChiaveFascicolo(), svf.getTipoFascicoloNonverificato());
                    return rispostaControlli;
                }
            }
            boolean prosegui = false;
            // ho recuperato l'XSD. salvo il riferimento per un futuro utilizzo
            svf.setIdRecxsdProfiloGenerale(dmxfs.get(0).getIdModelloXsdFascicolo());
            // ho finalmente l'XSD del profilo generale, adesso lo verifico
            String paXsd = dmxfs.get(0).getBlXsd();
            RispostaControlli rc = this.verificaXsd(paXsd, parsedIndiceFasc.getProfiloGenerale().getAny());
            if (rc.isrBoolean()) {
                prosegui = true;
            } else {
                versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_002_001,
                        svf.getUrnPartChiaveFascicolo(), rc.getDsErr());
                return rispostaControlli;
            }

            DatiXmlProfiloGenerale dxpg = null;
            if (prosegui) {
                dxpg = this.recuperaDatiDaXmlPG(parsedIndiceFasc.getProfiloGenerale());
                if (dxpg == null) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_002_001,
                            svf.getUrnPartChiaveFascicolo(),
                            "impossibile recuperare i metadati dal Profilo Generale del fascicolo");
                    return rispostaControlli;
                }
                // salvo i dati XML del profilo generale
                svf.setDatiXmlProfiloGenerale(dxpg);
                // imposto l'esito con i dati trovati
                fascicoloResp.setDataApertura(XmlDateUtility.dateToXMLGregorianCalendar(dxpg.getDataApertura()));
                fascicoloResp.setDataChiusura(XmlDateUtility.dateToXMLGregorianCalendar(dxpg.getDataChiusura()));
                fascicoloResp.setTempoConservazione(dxpg.getTempoConservazione().longValue());
                if (!this.verificaDateProfiloGen(versamento, dxpg)) {
                    // se ci sono errori sulle date proseguo, ma so di aver fallito il versamento.
                    // questo metodo scrive autonomamente gli errori nella lista errori
                    prosegui = false;
                }
                if (!this.verificaDocumentiProfiloGen(versamento, dxpg)) {
                    // se ci sono errori sui documenti proseguo, ma so di aver fallito il versamento.
                    // questo metodo scrive autonomamente gli errori nella lista errori
                    prosegui = false;
                }
                if (!this.verificaSoggettiProfiloGen(versamento, dxpg)) {
                    // se ci sono errori sui soggetti proseguo, ma so di aver fallito il versamento.
                    // questo metodo scrive autonomamente gli errori nella lista errori
                    prosegui = false;
                }
                if (!this.verificaResponsabiliProfiloGen(versamento, dxpg)) {
                    // se ci sono errori sui responsabile proseguo, ma so di aver fallito il versamento.
                    // questo metodo scrive autonomamente gli errori nella lista errori
                    prosegui = false;
                }
            }

            if (prosegui) {
                // Oggi è la mia giornata fortunata... la verifica è andata bene.
                rispostaControlli.setrBoolean(true);
            }
        } catch (Exception e) {
            versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.ERR_666,
                    "ControlliProfiliFascicolo.verificaProfiloGenerale: " + e.getMessage());
            log.error("Eccezione nella verifica profilo generale fascicolo ", e);
        }
        return rispostaControlli;
    }

    private boolean verificaDocumentiProfiloGen(VersFascicoloExt versamento, DatiXmlProfiloGenerale dxpg) {
        boolean retVal = true;
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        if (dxpg.getPrimoDocumento() != null) {
            RispostaControlli rc = controlliSemantici.checkChiave(dxpg.getPrimoDocumento(), svf.getIdStruttura(),
                    ControlliSemantici.TipiGestioneUDAnnullate.CARICA);
            if (rc.getrLong() == -1
                    || (svf.getUnitaDocElencate() != null && !svf.getUnitaDocElencate().contains(rc.getrLong()))) {
                // se non ha trovato la chiave tra quelle dichiarate
                // registro l'errore e proseguo con la ricerca dell'eventuale
                // ultimo documento
                String descChiaveUdColl = MessaggiWSFormat.formattaUrnPartUnitaDoc(dxpg.getPrimoDocumento());
                versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_003,
                        svf.getUrnPartChiaveFascicolo(), descChiaveUdColl);
                retVal = false;
            } else {
                dxpg.setIdPrimoDocumento(rc.getrLong());
            }
        }
        //
        if (dxpg.getUltimoDocumento() != null) {
            RispostaControlli rc = controlliSemantici.checkChiave(dxpg.getUltimoDocumento(), svf.getIdStruttura(),
                    ControlliSemantici.TipiGestioneUDAnnullate.CARICA);
            if (rc.getrLong() == -1
                    || (svf.getUnitaDocElencate() != null && !svf.getUnitaDocElencate().contains(rc.getrLong()))) {
                // se non ha trovato la chiave tra quelle dichiarate
                // registro l'errore
                String descChiaveUdColl = MessaggiWSFormat.formattaUrnPartUnitaDoc(dxpg.getUltimoDocumento());
                versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_004,
                        svf.getUrnPartChiaveFascicolo(), descChiaveUdColl);
                retVal = false;
            } else {
                dxpg.setIdUltimoDocumento(rc.getrLong());
            }
        }

        return retVal;
    }

    private boolean verificaDateProfiloGen(VersFascicoloExt versamento, DatiXmlProfiloGenerale dxpg) {
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        if (dxpg.getDataApertura() != null && dxpg.getDataChiusura() != null
                && dxpg.getDataApertura().after(dxpg.getDataChiusura())) {
            versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_001,
                    svf.getUrnPartChiaveFascicolo());
            return false;
        }
        if (versamento.getVersamento().getParametri().getTipoConservazione() == TipoConservazioneType.IN_ARCHIVIO
                && dxpg.getDataChiusura() == null) {
            versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_002,
                    svf.getUrnPartChiaveFascicolo());
            return false;
        }
        return true;

    }

    private boolean verificaSoggettiProfiloGen(VersFascicoloExt versamento, DatiXmlProfiloGenerale dxpg) {
        StrutturaVersFascicolo svf = versamento.getStrutturaComponenti();
        // verifica sul soggetto
        if (dxpg.getSoggettiCoinvolti().size() > 0) {
            Map<String, String> tipoRapportoUNQ = new HashMap<String, String>();
            for (DXPGSoggettoCoinvolto sogg : dxpg.getSoggettiCoinvolti()) {
                // 1. individuato come - nome/cognome oppure denominazione
                // 1.1 non esistono dati indicati per il soggetto den/cogn/nomne
                if (StringUtils.isBlank(sogg.getNome()) && StringUtils.isBlank(sogg.getCognome())
                        && StringUtils.isBlank(sogg.getDenominazione())) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_006,
                            svf.getUrnPartChiaveFascicolo());
                    return false;
                }
                // 1.2 se presente nome anche cognome (o viceversa)
                if (StringUtils.isNotBlank(sogg.getCognome()) && StringUtils.isBlank(sogg.getNome())
                        || StringUtils.isNotBlank(sogg.getNome()) && StringUtils.isBlank(sogg.getCognome())) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_007,
                            svf.getUrnPartChiaveFascicolo());
                    return false;
                }
                // 1.3 tutte presenti
                if (StringUtils.isNotBlank(sogg.getNome()) && StringUtils.isNotBlank(sogg.getCognome())
                        && StringUtils.isNotBlank(sogg.getDenominazione())) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_008,
                            svf.getUrnPartChiaveFascicolo());
                    return false;
                }
                // 2. per ogni soggetto (individuato come ...) tipo rapporto non ripetibile
                String key = StringUtils.isNotBlank(sogg.getDenominazione()) ? sogg.getDenominazione()
                        : sogg.getCognome() + "_" + sogg.getNome();
                String tipoRapporto = tipoRapportoUNQ.get(key);
                if (tipoRapporto == null) {
                    tipoRapportoUNQ.put(key, sogg.getTipoRapporto());
                    continue;
                } else if (tipoRapporto.equalsIgnoreCase(sogg.getTipoRapporto())) {
                    versamento.listErrAddError(svf.getUrnPartChiaveFascicolo(), MessaggiWSBundle.FAS_PF_GEN_003_009,
                            svf.getUrnPartChiaveFascicolo(), StringUtils.isNotBlank(sogg.getDenominazione())
                                    ? sogg.getDenominazione() : sogg.getCognome() + " " + sogg.getNome(),
                            sogg.getTipoRapporto());
                    return false;
                }
            }
        }

        return true;
    }

    private boolean verificaResponsabiliProfiloGen(VersFascicoloExt versamento, DatiXmlProfiloGenerale dxpg) {
        // TODO: al momento skippata in quanto il tipo responsabilità è FIXED a FASCICOLO
        if (dxpg.getResponsabili().size() > 0) {

        }
        return true;
    }

    private DatiXmlProfiloGenerale recuperaDatiDaXmlPG(ProfiloGeneraleType pgt) {
        DatiXmlProfiloGenerale tmpDatiXml = new DatiXmlProfiloGenerale();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //
            String tmpString;
            Node tmpDati = pgt.getAny();
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();

            XPathExpression expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/Oggetto/text()");
            NodeList nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setOggettoFascicolo(tmpString);
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/DataApertura/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setDataApertura(dateFormat.parse(tmpString));
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/DataChiusura/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setDataChiusura(dateFormat.parse(tmpString));
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/PrimoDocumentoNelFascicolo");
            Node tmpnode = (Node) expr.evaluate(tmpDati, XPathConstants.NODE);
            if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                tmpDatiXml.setPrimoDocumento(this.recuperaCsChiaveDaNodo(tmpnode));
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/UltimoDocumentoNelFascicolo");
            tmpnode = (Node) expr.evaluate(tmpDati, XPathConstants.NODE);
            if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                tmpDatiXml.setUltimoDocumento(this.recuperaCsChiaveDaNodo(tmpnode));
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/TempoConservazione/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                if (tmpString.matches("-?\\d+")) {
                    tmpDatiXml.setTempoConservazione(new BigDecimal(tmpString));
                }
            }
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/LivelloRiservatezza/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setLvlRiservatezza(tmpString);
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/Note/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.setNoteFascicolo(tmpString);
            }
            //
            expr = xpath.compile(
                    "//ProfiloGeneraleFascicolo" + "/AmministrazioniPartecipanti" + "/AmministrazionePartecipante");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                // AmministrazionePartecipante
                tmpnode = nodes.item(i);
                if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpDatiXml.addAmminPartecipante(this.recuperaAmmPartecipante(tmpnode));
                }
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/SoggettiCoinvolti" + "/SoggettoCoinvolto");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                // SoggettoCoinvolto
                tmpnode = nodes.item(i);
                if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpDatiXml.addSoggettoCoinvolto(this.recuperaSoggettoCoinvolto(tmpnode));
                }
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/Responsabili" + "/Responsabile");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                // Responsabile
                tmpnode = nodes.item(i);
                if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpDatiXml.addResponsabile(this.recuperaResponsabile(tmpnode));
                }
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/AmministrazioneTitolare");
            tmpnode = (Node) expr.evaluate(tmpDati, XPathConstants.NODE);
            if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                tmpDatiXml.setAmmtitolare(this.recuperaAmmTitolare(tmpnode));
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/UnitaOrganizzativeResponsabili"
                    + "/UnitaOrganizzativaResponsabile/text()");
            nodes = (NodeList) expr.evaluate(tmpDati, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                // UnitaOrganizzativaResponsabile
                /*
                 * Nota: per il momento è una lista di tipo String (come da XSD) essendo un unico campo TODO/EVO:
                 * eventualmente wrappare su oggetto
                 */
                tmpString = nodes.item(i).getNodeValue();
                tmpDatiXml.addUoOrgResponsabile(tmpString);
            }
            //
            expr = xpath.compile("//ProfiloGeneraleFascicolo" + "/ProcedimentoAmministrativo");
            tmpnode = (Node) expr.evaluate(tmpDati, XPathConstants.NODE);
            if (tmpnode != null && tmpnode.getNodeType() == Node.ELEMENT_NODE) {
                tmpDatiXml.setProcAmm(this.recuperaProcAmm(tmpnode));
            }
        } catch (Exception ex) {
            log.error("errore recupero dati di profilo generale", ex);
            return null;
        }

        return tmpDatiXml;

    }

    private DXPGProcAmmininistrativo recuperaProcAmm(Node tmpnode) {
        DXPGProcAmmininistrativo tmpProcAmmininistrativo = new DXPGProcAmmininistrativo();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("CodiceProcedimento");
        if (tmpList.getLength() > 0) {
            tmpProcAmmininistrativo.setCodice(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("DenominazioneProcedimento");
            if (tmpList.getLength() > 0) {
                tmpProcAmmininistrativo.setDenominazione(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        return continua ? tmpProcAmmininistrativo : null;
    }

    private DXPGRespFascicolo recuperaResponsabile(Node tmpnode) {
        DXPGRespFascicolo tmpResponsabile = new DXPGRespFascicolo();
        NodeList tmpList;
        boolean continua = true;
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Nome");
            if (tmpList.getLength() > 0) {
                tmpResponsabile.setNome(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Cognome");
            if (tmpList.getLength() > 0) {
                tmpResponsabile.setCognome(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Identificativo");
            if (tmpList.getLength() > 0) {
                tmpResponsabile.setCdIdentificativo(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("TipoIdentificativo");
            if (tmpList.getLength() > 0) {
                tmpResponsabile.setTiCdIdentificativo(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Responsabilita");
            if (tmpList.getLength() > 0) {
                tmpResponsabile.setResponsabilita(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        return continua ? tmpResponsabile : null;
    }

    private DXPGSoggettoCoinvolto recuperaSoggettoCoinvolto(Node tmpnode) {
        DXPGSoggettoCoinvolto tmpSoggCoinvolto = new DXPGSoggettoCoinvolto();
        NodeList tmpList;
        boolean continua = true;
        // can be null
        tmpList = ((Element) tmpnode).getElementsByTagName("Nome");
        if (tmpList.getLength() > 0) {
            tmpSoggCoinvolto.setNome(tmpList.item(0).getTextContent());
        }
        // can be null
        tmpList = ((Element) tmpnode).getElementsByTagName("Cognome");
        if (tmpList.getLength() > 0) {
            tmpSoggCoinvolto.setCognome(tmpList.item(0).getTextContent());
        }
        // can be null
        tmpList = ((Element) tmpnode).getElementsByTagName("Denominazione");
        if (tmpList.getLength() > 0) {
            tmpSoggCoinvolto.setDenominazione(tmpList.item(0).getTextContent());
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Identificativo");
            if (tmpList.getLength() > 0) {
                tmpSoggCoinvolto.setIdentificativo(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("TipoIdentificativo");
            if (tmpList.getLength() > 0) {
                tmpSoggCoinvolto.setTipoIdentificativo(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("TipoRapporto");
            if (tmpList.getLength() > 0) {
                tmpSoggCoinvolto.setTipoRapporto(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        return continua ? tmpSoggCoinvolto : null;
    }

    private DXPGAmminPartecipante recuperaAmmPartecipante(Node tmpnode) {
        DXPGAmminPartecipante tmpAmmPartecipante = new DXPGAmminPartecipante();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("Denominazione");
        if (tmpList.getLength() > 0) {
            tmpAmmPartecipante.setDenominazione(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Codice");
            if (tmpList.getLength() > 0) {
                tmpAmmPartecipante.setCodice(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("TipoCodice");
            if (tmpList.getLength() > 0) {
                tmpAmmPartecipante.setTipoCodice(tmpList.item(0).getTextContent());
            }
        } else {
            continua = false;
        }
        return continua ? tmpAmmPartecipante : null;
    }

    private DXPGAmminTitolare recuperaAmmTitolare(Node tmpnode) {
        DXPGAmminTitolare tmpAmminTitolare = new DXPGAmminTitolare();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("Denominazione");
        if (tmpList.getLength() > 0) {
            tmpAmminTitolare.setDenominazione(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Codice");
            if (tmpList.getLength() > 0) {
                tmpAmminTitolare.setCodice(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("TipoCodice");
            if (tmpList.getLength() > 0) {
                tmpAmminTitolare.setTiCodice(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        return continua ? tmpAmminTitolare : null;
    }

    private CSChiave recuperaCsChiaveDaNodo(Node tmpnode) {
        CSChiave tmpChiave = new CSChiave();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("Registro");
        if (tmpList.getLength() > 0) {
            tmpChiave.setTipoRegistro(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Numero");
            if (tmpList.getLength() > 0) {
                tmpChiave.setNumero(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Anno");
            if (tmpList.getLength() > 0) {
                String tmpString = tmpList.item(0).getTextContent();
                if (tmpString.matches("-?\\d+")) {
                    tmpChiave.setAnno(Long.parseLong(tmpString));
                }
            } else {
                continua = false;
            }
        }
        return continua ? tmpChiave : null;

    }

    private CSChiaveSottFasc recuperaCsChiaveSottoFascDaNodo(Node tmpnode) {
        CSChiaveSottFasc tmpChiave = new CSChiaveSottFasc();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("Oggetto");
        if (tmpList.getLength() > 0) {
            tmpChiave.setOggetto(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Numero");
            if (tmpList.getLength() > 0) {
                tmpChiave.setNumero(tmpList.item(0).getTextContent());
            } else {
                continua = false;
            }
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Anno");
            if (tmpList.getLength() > 0) {
                String tmpString = tmpList.item(0).getTextContent();
                if (tmpString.matches("-?\\d+")) {
                    tmpChiave.setAnno(Integer.parseInt(tmpString));
                }
            } else {
                continua = false;
            }
        }
        return continua ? tmpChiave : null;

    }

    private CSChiaveFasc recuperaCsChiaveFascDaNodo(Node tmpnode) {
        CSChiaveFasc tmpChiave = new CSChiaveFasc();
        NodeList tmpList;
        boolean continua = true;
        tmpList = ((Element) tmpnode).getElementsByTagName("Numero");
        if (tmpList.getLength() > 0) {
            tmpChiave.setNumero(tmpList.item(0).getTextContent());
        } else {
            continua = false;
        }
        if (continua) {
            tmpList = ((Element) tmpnode).getElementsByTagName("Anno");
            if (tmpList.getLength() > 0) {
                String tmpString = tmpList.item(0).getTextContent();
                if (tmpString.matches("-?\\d+")) {
                    tmpChiave.setAnno(Integer.parseInt(tmpString));
                }
            } else {
                continua = false;
            }
        }
        return continua ? tmpChiave : null;

    }

    private RispostaControlli verificaXsd(String xsd, Node xml) {
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

}
