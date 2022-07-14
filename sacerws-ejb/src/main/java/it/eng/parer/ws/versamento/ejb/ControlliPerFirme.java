package it.eng.parer.ws.versamento.ejb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecEstensioneFile;
import it.eng.parer.entity.DecFormatoFileDoc;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.FirCertifCa;
import it.eng.parer.entity.FirCertifFirmatario;
import it.eng.parer.entity.FirCertifOcsp;
import it.eng.parer.entity.FirCrl;
import it.eng.parer.entity.FirOcsp;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.entity.converter.NeverendingDateConverter;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.MessaggiWSBundle;

/**
 *
 * @author Gilioli_P ... edited by Francesco Fioravanti
 *
 */
@Stateless(mappedName = "ControlliPerFirme")
@LocalBean
public class ControlliPerFirme {

    private static final Logger LOG = LoggerFactory.getLogger(ControlliPerFirme.class);
    // logging base messages
    private static final String LOG_BASEMSG_ERROR_ON_DECTABLE = "Eccezione nella lettura della tabella di decodifica";

    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    public RispostaControlli getOrgStrutt(long idStrutVers) {
        RispostaControlli rs;
        rs = new RispostaControlli();
        rs.setrLong(-1);
        OrgStrut os = null;

        try {
            os = entityManager.find(OrgStrut.class, idStrutVers);
            rs.setrLong(0);
            rs.setrObject(os);
        } catch (Exception e) {
            rs.setCodErr(MessaggiWSBundle.ERR_666);
            rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliPerFirme.getOrgStrutt: " + e.getMessage()));
            LOG.error(LOG_BASEMSG_ERROR_ON_DECTABLE, e);
        }
        return rs;
    }

    /**
     * Ottieni la struttura.
     *
     * @param idStrutVers
     *            identificativo della struttura versante
     *
     * @return entity della tabella ORG_STRUT
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public OrgStrut getOrgStruttAsEntity(long idStrutVers) throws VerificaFirmaException {

        try {
            return entityManager.find(OrgStrut.class, idStrutVers);
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getOrgStruttAsEntity: " + e.getMessage()), e);
        }
    }

    /**
     * Ottieni il formato file standard per l'id passato in input.
     *
     * @param idDecFormatoFileStandard
     *            pk della tabella DEC_FORMATO_FILE_STANDARD
     *
     * @return entity DecFormatoFileStandard
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public DecFormatoFileStandard getDecFormatoFileStandardAsEntity(long idDecFormatoFileStandard)
            throws VerificaFirmaException {

        try {
            return entityManager.find(DecFormatoFileStandard.class, idDecFormatoFileStandard);
        } catch (Exception e) {
            LOG.error("Eccezione nella lettura  della tabella di decodifica " + e);
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getDecFormatoFileStandard: " + e.getMessage()), e);
        }

    }

    public RispostaControlli confrontaFormati(long idFormatoStd, long idFormatoVers) {
        String formatoVers;
        String formatoRappr;
        RispostaControlli rs;
        rs = new RispostaControlli();
        rs.setrLong(-1);
        rs.setrBoolean(false);

        try {
            DecFormatoFileDoc formatoVersato = entityManager.find(DecFormatoFileDoc.class, idFormatoVers);
            formatoVers = formatoVersato.getNmFormatoFileDoc();
            DecFormatoFileStandard tmpFileStandard = entityManager.find(DecFormatoFileStandard.class, idFormatoStd);
            formatoRappr = tmpFileStandard.getNmFormatoFileStandard();
            rs.setrLong(0);
            rs.setrObject(tmpFileStandard);
            if (formatoRappr.equals(formatoVers)) {
                rs.setrBoolean(true);
            } else {
                StringBuilder esitoNegativo = new StringBuilder();
                esitoNegativo.append("Il formato di rappresentazione calcolato ");
                esitoNegativo.append(formatoRappr);
                esitoNegativo.append(" è diverso da quello versato ");
                esitoNegativo.append(formatoVers);
                rs.setrString(esitoNegativo.toString());
            }
        } catch (Exception e) {
            rs.setCodErr(MessaggiWSBundle.ERR_666);
            rs.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliPerFirme.confrontaFormati: " + e.getMessage()));
            LOG.error(LOG_BASEMSG_ERROR_ON_DECTABLE, e);
        }
        return rs;
    }

    /**
     * Ottieni la lista dei formati standard filtrati per l'estensione standard.
     *
     * @param formato
     *            estensione standard del file
     *
     * @return Lista dei formati compatibili con l'estensione standard in input.
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<DecEstensioneFile> getDecEstensioneFiles(String formato) throws VerificaFirmaException {

        try {
            String queryStr = "SELECT ext FROM DecFormatoFileStandard dec JOIN dec.decEstensioneFiles ext"
                    + " WHERE dec.nmFormatoFileStandard = :formato ";
            TypedQuery<DecEstensioneFile> q = entityManager.createQuery(queryStr, DecEstensioneFile.class);
            q.setParameter("formato", formato);
            return q.getResultList();
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getDecEstensioneFiles: " + e.getMessage()), e);
        }
    }

    /**
     * Ottieni la lista dei formati standard filtrati per l'estensione dichiarata.
     *
     * @param formatoVersato
     *            estensione dichiarata
     *
     * @return Lista dei formati compatibili con l'estensione dichiarata in input.
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<DecFormatoFileStandard> getDecFmtFileStdFromEstensioneFiles(String formatoVersato)
            throws VerificaFirmaException {
        try {
            String queryStr = "SELECT dec FROM DecFormatoFileStandard dec " + "JOIN dec.decEstensioneFiles ext "
                    + "WHERE UPPER(ext.cdEstensioneFile) = :estensioneFile ";
            Query q = entityManager.createQuery(queryStr);
            q.setParameter("estensioneFile", formatoVersato);
            return q.getResultList();
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "ControlliPerFirme.getDecFmtFileStdFromEstensioneFiles: " + e.getMessage()),
                    e);
        }
    }

    /**
     * Ottieni i formati standard filtrati per il mimetype passato in input.
     *
     * @param tikaMime
     *            mime type del file sbustato
     *
     * @return lista di formati file standard filtrati per mime type
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<DecFormatoFileStandard> getDecFmtFileStandardFromTikaMimes(String tikaMime)
            throws VerificaFirmaException {
        try {
            String queryStr = "SELECT ffs FROM DecFormatoFileStandard ffs "
                    + " WHERE ffs.nmMimetypeFile = :formatoTika";
            TypedQuery<DecFormatoFileStandard> q = entityManager.createQuery(queryStr, DecFormatoFileStandard.class);
            q.setParameter("formatoTika", tikaMime);
            return q.getResultList();
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "ControlliPerFirme.getDecFmtFileStandardFromTikaMimes: " + e.getMessage()),
                    e);
        }
    }

    /**
     * Ottieni la lista dei formati standard configurati per i formati busta passati in input.
     *
     * @param tiFormatoFirmaMarca
     *            lista di formati per filtrare
     *
     * @return Lista dei formati file standard filtrati
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<DecFormatoFileStandard> getDecFmtFileStandardFromFmtMarcas(Set<String> tiFormatoFirmaMarca)
            throws VerificaFirmaException {

        try {
            String queryStr = "SELECT DISTINCT dec FROM DecFormatoFileStandard dec JOIN dec.decFormatoFileBustas bus"
                    + " WHERE bus.tiFormatoFirmaMarca IN :formati ";
            TypedQuery<DecFormatoFileStandard> q = entityManager.createQuery(queryStr, DecFormatoFileStandard.class);
            q.setParameter("formati", tiFormatoFirmaMarca);
            return q.getResultList();
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "ControlliPerFirme.getDecFmtFileStandardFromFmtMarcas: " + e.getMessage()),
                    e);
        }
    }

    /**
     * Lista dei formati standard per i documenti.
     *
     * @param idFormatoFileDoc
     *            id formato documento
     * @param chiaveComp
     *            chiave del componente
     * @param nmFormatoFileDoc
     *            nome formato file
     *
     * @return lista di formati standard
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<DecFormatoFileStandard> getDecFmtFileStandardFromFmtDocs(long idFormatoFileDoc, String chiaveComp,
            String nmFormatoFileDoc) throws VerificaFirmaException {
        List<DecFormatoFileStandard> dffs = null;
        try {
            String queryStr = "SELECT ffs FROM DecFormatoFileStandard ffs "
                    + "JOIN ffs.decUsoFormatoFileStandards usoFF " + "JOIN usoFF.decFormatoFileDoc ff "
                    + "WHERE ff.idFormatoFileDoc = :idFormatoVersato " + "AND usoFF.niOrdUso = 1";
            TypedQuery<DecFormatoFileStandard> q = entityManager.createQuery(queryStr, DecFormatoFileStandard.class);
            q.setParameter("idFormatoVersato", idFormatoFileDoc);
            dffs = q.getResultList();
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getDecFmtFileStandardFromFmtDocs: " + e.getMessage()));
        }
        // configuration error (666 blocking error code)
        if (dffs == null || dffs.isEmpty()) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "Componente " + chiaveComp + ": il Formato file riconosciuto come " + nmFormatoFileDoc
                                    + " non ha una associazione su registro formati standard"));
        }

        return dffs;
    }

    /**
     * Formato dei file di tipo "documento"
     *
     * @param idFormatoFileDoc
     *            chiave della DecFormatoFileDoc
     *
     * @return entity per il documento identificato da idFormatoFileDoc
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public DecFormatoFileDoc getDecFormatoFileDoc(long idFormatoFileDoc) throws VerificaFirmaException {
        try {
            return entityManager.find(DecFormatoFileDoc.class, idFormatoFileDoc);
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getDecFormatoFileDoc: " + e.getMessage()), e);
        }
    }

    /**
     * Recupero del certificato per il firmatario della CA. Eccezione altrimenti.
     *
     * @param niSerialCertifCa
     *            serial number
     * @param dlDnIssuerCertifCa
     *            distinguished name della CA
     *
     * @return lista di entity
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public FirCertifCa getFirCertifCa(BigDecimal niSerialCertifCa, String dlDnIssuerCertifCa)
            throws VerificaFirmaException {

        // result
        FirCertifCa firCertifCa = null;

        // lancio query di controllo
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<FirCertifCa> criteriaQuery = criteriaBuilder.createQuery(FirCertifCa.class);
            Root<FirCertifCa> root = criteriaQuery.from(FirCertifCa.class);
            criteriaQuery.select(root)
                    .where(criteriaBuilder.and(criteriaBuilder.equal(root.get("niSerialCertifCa"), niSerialCertifCa),
                            criteriaBuilder.equal(root.get("dlDnIssuerCertifCa"), dlDnIssuerCertifCa)));
            List<FirCertifCa> firCertifCas = entityManager.createQuery(criteriaQuery).getResultList();

            if (firCertifCas != null && !firCertifCas.isEmpty()) {
                firCertifCa = firCertifCas.get(0);
            }
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle
                    .getString(MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirCertifCa: " + e.getMessage()));
        }
        // result
        return firCertifCa;

    }

    /**
     * Ottieni l'id della CRL a partire dalla CA passata e i parametri della crl stessa.
     *
     * @param firCertifCa
     *            entity della CA
     * @param niSerialCrl
     *            numero di serie della CRL (può essere null)
     * @param dtIniCrl
     *            data inizio della CRL
     * @param dtScadCrl
     *            data di scadenza della CRL
     *
     * @return null oppure l'identificativo dell'entity della CRL
     *
     * @throws it.eng.parer.firma.exception.VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public FirCrl getFirCrl(FirCertifCa firCertifCa, BigDecimal niSerialCrl, Date dtIniCrl, Date dtScadCrl)
            throws VerificaFirmaException {
        FirCrl firCrl = null;

        try {
            String queryStr = "SELECT c FROM FirCrl c WHERE c.firCertifCa = :firCertifCa AND c.dtIniCrl = :dtIniCrl AND c.dtScadCrl = :dtScadCrl";

            if (niSerialCrl != null) {
                queryStr += " AND c.niSerialCrl = :niSerialCrl";
            } else {
                queryStr += " AND c.niSerialCrl is null";
            }

            TypedQuery<FirCrl> query = entityManager.createQuery(queryStr, FirCrl.class);
            query.setParameter("firCertifCa", firCertifCa).setParameter("dtIniCrl", dtIniCrl).setParameter("dtScadCrl",
                    NeverendingDateConverter.verifyOverZoneId(dtScadCrl, TimeZone.getTimeZone("UTC").toZoneId()));

            if (niSerialCrl != null) {
                query.setParameter("niSerialCrl", niSerialCrl);
            }

            List<FirCrl> resultList = query.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                firCrl = resultList.get(0);
            }
            return firCrl;
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle
                    .getString(MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirCrl: " + e.getMessage()));
        }
    }

    public FirCertifOcsp getFirCertifOcsp(FirCertifCa firCertifCa, BigDecimal niSerialCertifOcsp)
            throws VerificaFirmaException {
        FirCertifOcsp firCertifOcsp = null;
        try {
            final String findFirCertifOcpByAlternateKey = "Select f from FirCertifOcsp f Where f.firCertifCa = :firCertifCa And f.niSerialCertifOcsp = :niSerialCertifOcsp";
            TypedQuery<FirCertifOcsp> query = entityManager
                    .createQuery(findFirCertifOcpByAlternateKey, FirCertifOcsp.class)
                    .setParameter("firCertifCa", firCertifCa).setParameter("niSerialCertifOcsp", niSerialCertifOcsp);
            List<FirCertifOcsp> resultList = query.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                firCertifOcsp = resultList.get(0);
            }

            return firCertifOcsp;
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle
                    .getString(MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirCertifOcsp: " + e.getMessage()));
        }
    }

    public FirOcsp getFirOcsp(FirCertifOcsp firCertifOcsp, String dsCertifIssuername, String dsCertifSerialBase64,
            String dsCertifSkiBase64) throws VerificaFirmaException {
        FirOcsp firOcsp = null;

        try {
            String queryStr = "SELECT c FROM FirOcsp c WHERE c.firCertifOcsp = :firCertifOcsp";

            if (dsCertifIssuername != null) {
                queryStr += " AND c.dsCertifIssuername = :dsCertifIssuername";
            }

            if (dsCertifSerialBase64 != null) {
                queryStr += " AND c.dsCertifSerialBase64 = :dsCertifSerialBase64";
            }

            if (dsCertifSkiBase64 != null) {
                queryStr += " AND c.dsCertifSkiBase64 = :dsCertifSkiBase64";
            }

            TypedQuery<FirOcsp> query = entityManager.createQuery(queryStr, FirOcsp.class);
            query.setParameter("firCertifOcsp", firCertifOcsp);

            if (dsCertifIssuername != null) {
                query.setParameter("dsCertifIssuername", dsCertifIssuername);
            }

            if (dsCertifSerialBase64 != null) {
                query.setParameter("dsCertifSerialBase64", dsCertifSerialBase64);
            }

            if (dsCertifSkiBase64 != null) {
                query.setParameter("dsCertifSkiBase64", dsCertifSkiBase64);
            }

            List<FirOcsp> resultList = query.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                firOcsp = resultList.get(0);
            }
            return firOcsp;
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle
                    .getString(MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirOcsp: " + e.getMessage()));
        }
    }

    /**
     * Ottieni il firmatario tramite la chiave alternativa
     *
     * @param firCertifCa
     *            (id) della ca
     * @param niSerialCertifFirmatario
     *            seriale del firmatario
     *
     * @return il firmatario oppure null
     *
     * @throws it.eng.parer.firma.exception.VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public FirCertifFirmatario getFirCertifFirmatario(FirCertifCa firCertifCa, BigDecimal niSerialCertifFirmatario)
            throws VerificaFirmaException {
        FirCertifFirmatario firFirCertifFirmatario = null;
        try {
            final String findFirCertifFirmatarioByAlternateKey = "Select f from FirCertifFirmatario f Where f.firCertifCa = :firCertifCa And f.niSerialCertifFirmatario = :niSerialCertifFirmatario";
            TypedQuery<FirCertifFirmatario> query = entityManager
                    .createQuery(findFirCertifFirmatarioByAlternateKey, FirCertifFirmatario.class)
                    .setParameter("firCertifCa", firCertifCa)
                    .setParameter("niSerialCertifFirmatario", niSerialCertifFirmatario);
            List<FirCertifFirmatario> resultList = query.getResultList();
            if (resultList != null && !resultList.isEmpty()) {
                firFirCertifFirmatario = resultList.get(0);
            }

            return firFirCertifFirmatario;
        } catch (Exception e) {
            LOG.error("Eccezione nella lettura della tabella FirCertifFirmatario ", e);
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirCertifFirmatario: " + e.getMessage()));
        }
    }

    /**
     * Ritorna la lista dei certificati del firmatario. Eccezione altrimenti.
     *
     * @param niSerialCertifFirmatario
     *            serial number del firmatario
     * @param firCertifCa
     *            CA
     *
     * @return Lista di Cerificati del firmatario
     *
     * @throws VerificaFirmaException
     *             in caso di errore nell'accesso al DB
     */
    public List<Long> getFirCertifFirmatarioIds(BigDecimal niSerialCertifFirmatario, FirCertifCa firCertifCa)
            throws VerificaFirmaException {
        // lista entity JPA ritornate dalle Query
        List<Long> firFirCertifFirmatarios = new ArrayList<>();

        // lancio query di controllo
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<FirCertifFirmatario> root = criteriaQuery.from(FirCertifFirmatario.class);
            criteriaQuery.select(root.get("idCertifFirmatario"))
                    .where(criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("niSerialCertifFirmatario"), niSerialCertifFirmatario),
                            criteriaBuilder.equal(root.get("firCertifCa"), firCertifCa)));

            firFirCertifFirmatarios = entityManager.createQuery(criteriaQuery).getResultList();
            return firFirCertifFirmatarios;
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getFirCertifFirmatarioIds: " + e.getMessage()), e);
        }
    }

    public DecServizioVerificaCompDoc getDecServizioVerificaCompDoc(String cdService, String cdLibrary)
            throws VerificaFirmaException {
        try {
            String queryStr = "SELECT s FROM DecServizioVerificaCompDoc s"
                    + " WHERE s.nmVersione = :cdLibrary AND s.cdServizio = :cdService";
            TypedQuery<DecServizioVerificaCompDoc> q = entityManager.createQuery(queryStr,
                    DecServizioVerificaCompDoc.class);
            q.setParameter("cdService", CdServizioVerificaCompDoc.valueOf(cdService));
            q.setParameter("cdLibrary", cdLibrary);

            List<DecServizioVerificaCompDoc> dec = q.getResultList();

            // default metadati (nessuna versione)
            if (dec.isEmpty()) {
                q.setParameter("cdLibrary", Costanti.VERIFICA_FIRMA_METADATI_REPORT_NOVERSION);
                dec = q.getResultList();
            }
            return dec.get(0);
        } catch (Exception e) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
                    MessaggiWSBundle.ERR_666, "ControlliPerFirme.getDecServizioVerificaCompDoc: " + e.getMessage()), e);
        }
    }
}
