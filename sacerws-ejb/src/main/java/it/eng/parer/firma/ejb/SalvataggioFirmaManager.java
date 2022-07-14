/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.firma.ejb;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.PutObjectResult;

import it.eng.parer.crypto.model.CryptoEnums.EsitoControllo;
import it.eng.parer.crypto.model.CryptoEnums.TipoControlli;
import it.eng.parer.crypto.model.CryptoEnums.TipoControlliMarca;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.entity.AroBustaCrittog;
import it.eng.parer.entity.AroCompDoc;
import it.eng.parer.entity.AroCompUrnCalc;
import it.eng.parer.entity.AroContrFirmaComp;
import it.eng.parer.entity.AroContrMarcaComp;
import it.eng.parer.entity.AroControfirmaFirma;
import it.eng.parer.entity.AroFirmaComp;
import it.eng.parer.entity.AroMarcaComp;
import it.eng.parer.entity.AroUsoCertifCaContrComp;
import it.eng.parer.entity.AroUsoCertifCaContrMarca;
import it.eng.parer.entity.AroVerifFirmaDtVer;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.DecReportServizioVerificaCompDoc;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.FirCertifCa;
import it.eng.parer.entity.FirCertifFirmatario;
import it.eng.parer.entity.FirCertifOcsp;
import it.eng.parer.entity.FirCrl;
import it.eng.parer.entity.FirFilePerFirma;
import it.eng.parer.entity.FirOcsp;
import it.eng.parer.entity.FirReport;
import it.eng.parer.entity.FirUrlDistribCrl;
import it.eng.parer.entity.FirUrlDistribOcsp;
import it.eng.parer.entity.FirUrnReport;
import it.eng.parer.entity.constraint.AroCompUrnCalc.TiUrn;
import it.eng.parer.entity.constraint.DecReportServizioVerificaCompDoc.TiReportServizioVerificaCompDoc;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc.CdServizioVerificaCompDoc;
import it.eng.parer.entity.constraint.FiUrnReport.TiUrnReport;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.util.VerificaFirmaEnums;
import it.eng.parer.firma.xml.VFCertifCaType;
import it.eng.parer.firma.xml.VFCertifFirmatarioType;
import it.eng.parer.firma.xml.VFContrFirmaCompType;
import it.eng.parer.firma.xml.VFContrMarcaCompType;
import it.eng.parer.firma.xml.VFCrlType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VFOcspType;
import it.eng.parer.firma.xml.VFTipoControlloType;
import it.eng.parer.firma.xml.VFUrlDistribCrlType;
import it.eng.parer.firma.xml.VFUrlDistribOcspType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.Costanti.GenReportVerificaFirma;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.XmlDateUtility;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;
import it.eng.parer.ws.versamento.ejb.oracleBlb.WriteCompBlbOracle;

/**
 * Parser (base) per la gestione del result wrapper post verifica documento firmato
 *
 * @author sinatti_s
 */
@Stateless(mappedName = "SalvataggioFirmaManager")
@LocalBean
public class SalvataggioFirmaManager {

    private static final Logger LOG = LoggerFactory.getLogger(SalvataggioFirmaManager.class);

    //
    @PersistenceContext(unitName = "ParerJPA")
    private EntityManager entityManager;

    @EJB
    private ControlliPerFirme controlliPerFirme;

    @EJB
    private ConfigurationHelper configHelper;

    @EJB
    private VerificaFirmaReportHelper verificaFirmaReportHelper;

    @EJB
    private VerificaFirmaReportAwsClient verificaFirmaReportAwsClient;

    @EJB
    private WriteCompBlbOracle writeCompBlbOracle;

    /**
     * Note : 1 - popola le entità secondo le logiche previste (vedi FirmeFormatiVers.extractVerifyInfo) 1.1 - si
     * sostituisce alle logiche di questa API utilizzando quanto estratto in fase di verifica firma sul wrapper 2 - si
     * prevede che il metodo sotto venga invocato nell'ambito di SalvataggioSync.salvaComponenti
     *
     *
     * @param risposta
     *            risposta controlli
     * @param idCompVers
     *            id componente
     * @param wrapper
     *            wrapper verifica della firma
     * @param tmpTabCDComponente
     *            componente
     * @param tmpTabCDSottoComp
     *            sotto componente
     *
     * @return boolean risultato salvataggio true/falase
     */
    public boolean salvaBustaCrittografica(RispostaControlli risposta, String idCompVers, VerificaFirmaWrapper wrapper,
            AroCompDoc tmpTabCDComponente, AroCompDoc tmpTabCDSottoComp) {
        //
        boolean result = true;

        try {
            // forEach busta
            for (VFBusta busta : wrapper.getVFBusta()) {
                // check (esistono firme/marche per il componente <ID> analizzato?)
                if (busta.getFirmaComps().stream().filter(f -> f.getId().equals(idCompVers)).count() != 0
                        || busta.getMarcaComps().stream().filter(m -> m.getId().equals(idCompVers)).count() != 0) {

                    // create AroBustaCrittog componente
                    AroBustaCrittog tmpBustaCrittogComp = buildAroBustaCrittog(tmpTabCDComponente, busta.getPgBusta(),
                            busta.getAdditionalInfo().getIdFormatoFileStandard());

                    // create AroBustaCrittog sotto componente
                    AroBustaCrittog tmpBustaCrittogSottoComp = null;
                    if (tmpTabCDSottoComp != null) {
                        // create empty list
                        if (tmpTabCDSottoComp.getAroBustaCrittogs() == null) {
                            tmpTabCDSottoComp.setAroBustaCrittogs(new ArrayList<AroBustaCrittog>());
                        }

                        tmpBustaCrittogSottoComp = buildAroBustaCrittog(tmpTabCDSottoComp, busta.getPgBusta(),
                                busta.getAdditionalInfo().getIdFormatoFileStandard());

                    }

                    // FIRMA
                    List<VFFirmaCompType> firmaCompById = busta.getFirmaComps().stream()
                            .filter(f -> f.getId().equals(idCompVers)).collect(Collectors.toList());
                    //
                    for (VFFirmaCompType firmaCompType : firmaCompById) {
                        // FIRMA
                        AroFirmaComp tmpFirmaComp = this.buildAroFirmaComp(wrapper, busta, firmaCompType,
                                tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp, null);

                        // MARCHE
                        buildAroMarcaCompFromFirma(idCompVers, wrapper, busta, firmaCompType, tmpTabCDComponente,
                                tmpBustaCrittogComp, tmpBustaCrittogSottoComp, tmpFirmaComp);

                        // CONTRO FIRME
                        for (VFFirmaCompType firmaControCompType : firmaCompType.getControfirmaFirmaFiglios()) {
                            // FIRMA
                            AroFirmaComp tmpControFirmaComp = buildAroFirmaComp(wrapper, busta, firmaControCompType,
                                    tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp, tmpFirmaComp);

                            // MARCHE
                            buildAroMarcaCompFromFirma(idCompVers, wrapper, busta, firmaControCompType,
                                    tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
                                    tmpControFirmaComp);
                        }

                    }

                    // MARCHE
                    for (VFMarcaCompType marcaCompType : busta.getMarcaComps().stream()
                            .filter(m -> m.getId().equals(idCompVers)).collect(Collectors.toList())) {

                        // MARCA
                        AroMarcaComp tmpMarcaComp = this.buildAroMarcaComp(busta, marcaCompType, tmpTabCDComponente,
                                tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
                                wrapper.getAdditionalInfo().isIsDetached());
                        // persist
                        entityManager.persist(tmpMarcaComp);

                    }
                }
            }
            // flush
            entityManager.flush();
        } catch (VerificaFirmaException ex) {
            LOG.error("Errore durante il popolamento della busta");
            risposta.setrBoolean(false);
            risposta.setCodErr(ex.getCodiceErrore());
            risposta.setDsErr(ex.getDescrizioneErrore());
            result = false;
        } catch (Exception ex) {
            LOG.error("Errore durante il popolamento della busta");
            risposta.setrBoolean(false);
            risposta.setCodErr(MessaggiWSBundle.ERR_666P);
            risposta.setDsErr("Errore generico durante il popolamento della busta " + ExceptionUtils.getMessage(ex));
            result = false;
        }

        return result;
    }

    /**
     * Gestione della persistenza del report di verifica firma (object storage vs database)
     *
     * @param risposta
     *            riposta dei controlli effettuati (vedi {@link RispostaControlli})
     * @param wrapper
     *            wrapper standard verifica firma
     * @param strutV
     *            oggetto standard con dati di versamento
     * @param tmpAroCompDoc
     *            componente
     *
     * @return true/false con risultato dell'operazione
     */
    public boolean salvaReportVerificaCompDoc(RispostaControlli risposta, VerificaFirmaWrapper wrapper,
            StrutturaVersamento strutV, AroCompDoc tmpAroCompDoc) {
        boolean result = true;

        try {
            /*
             * Persisto servizio di verifica e report per il componente firmato. Tipicamente dovrebbe essere sempre il
             * componente principale.
             */
            // identifica il servizio di verifica sul componente
            // verifica compdoc
            DecServizioVerificaCompDoc servizioVerificaCompDoc = entityManager.find(DecServizioVerificaCompDoc.class,
                    wrapper.getAdditionalInfo().getIdServizioFirmaCompDoc());
            tmpAroCompDoc.setDecServizioVerificaCompDoc(servizioVerificaCompDoc);

            // result
            if (StringUtils.isNotBlank(strutV.getGenerazioneRerortVerificaFirma())
                    && !strutV.getGenerazioneRerortVerificaFirma().equalsIgnoreCase(GenReportVerificaFirma.OFF.name())
                    && wrapper.getAdditionalInfo().getReportContent() != null) {
                buildFirReport(tmpAroCompDoc, wrapper, strutV);
            }
            // flush
            entityManager.flush();
        } catch (VerificaFirmaException ex) {
            LOG.error("Errore durante la creazione del report verifica firma compdoc");
            risposta.setrBoolean(false);
            risposta.setCodErr(ex.getCodiceErrore());
            risposta.setDsErr(ex.getDescrizioneErrore());
            result = false;
        } catch (Exception ex) {
            LOG.error("Errore imprevisto durante la creazione del report verifica firma compdoc");
            risposta.setCodErr(MessaggiWSBundle.ERR_666P);
            risposta.setDsErr("Errore generico durante la creazione del report verifica firma compdoc "
                    + ExceptionUtils.getMessage(ex));
            result = false;
        }

        return result;
    }

    /**
     *
     * @param idCompVers
     * @param wrapper
     * @param busta
     * @param firmaCompType
     * @param tmpTabCDComponente
     * @param tmpBustaCrittogComp
     * @param tmpBustaCrittogSottoComp
     * @param tmpFirmaComp
     *
     * @throws VerificaFirmaException
     */
    private void buildAroMarcaCompFromFirma(String idCompVers, VerificaFirmaWrapper wrapper, VFBusta busta,
            VFFirmaCompType firmaCompType, AroCompDoc tmpTabCDComponente, AroBustaCrittog tmpBustaCrittogComp,
            AroBustaCrittog tmpBustaCrittogSottoComp, AroFirmaComp tmpFirmaComp) throws VerificaFirmaException {

        List<VFMarcaCompType> marcaCompTypeById = firmaCompType.getMarcaComps().stream()
                .filter(m -> m.getId().equals(idCompVers)).collect(Collectors.toList());
        //
        for (VFMarcaCompType marcaCompType : marcaCompTypeById) {

            // MARCA
            AroMarcaComp tmpMarcaComp = buildAroMarcaComp(busta, marcaCompType, tmpTabCDComponente, tmpBustaCrittogComp,
                    tmpBustaCrittogSottoComp, wrapper.getAdditionalInfo().isIsDetached());
            // aggiungo la marca alla firma
            tmpFirmaComp.setAroMarcaComp(tmpMarcaComp);

        }
    }

    private AroBustaCrittog buildAroBustaCrittog(AroCompDoc tmpAroCompDoc, BigDecimal pgBusta,
            long idFormatoFileStandard) {
        // FIXME FIXME FIXME PLEASE
        if (tmpAroCompDoc.getAroBustaCrittogs() != null && !tmpAroCompDoc.getAroBustaCrittogs().isEmpty()) {
            for (AroBustaCrittog aroBustaCrittog : tmpAroCompDoc.getAroBustaCrittogs()) {
                if (aroBustaCrittog.getPgBustaCrittog().equals(pgBusta)) {
                    return aroBustaCrittog;
                }

            }
        }

        AroBustaCrittog tmpBustaCrittog = new AroBustaCrittog();
        tmpBustaCrittog.setAroCompDoc(tmpAroCompDoc);
        tmpBustaCrittog.setAroFirmaComps(new ArrayList<AroFirmaComp>());
        tmpBustaCrittog.setAroMarcaComps(new ArrayList<AroMarcaComp>());
        tmpBustaCrittog.setPgBustaCrittog(pgBusta);
        tmpBustaCrittog.setIdStrut(tmpAroCompDoc.getIdStrut());

        // FIXME: questo è brutto
        if (idFormatoFileStandard > 0L) {
            // additional info (ottenute in precedenza -> vedi check formati)
            tmpBustaCrittog
                    .setDecFormatoFileStandard(entityManager.find(DecFormatoFileStandard.class, idFormatoFileStandard));
        }
        tmpAroCompDoc.getAroBustaCrittogs().add(tmpBustaCrittog);

        return tmpBustaCrittog;
    }

    /*
     * Popola, all'interno dei componenti, le buste per le firme.
     */
    private AroFirmaComp buildAroFirmaComp(VerificaFirmaWrapper wrapper, VFBusta busta, VFFirmaCompType firmaCompType,
            AroCompDoc tmpTabCDComponente, AroBustaCrittog tmpBustaCrittogComp,
            AroBustaCrittog tmpBustaCrittogSottoComp, AroFirmaComp tmpFirmaCompPadre) throws VerificaFirmaException {

        // certifCaType
        VFCertifCaType certifCaType = firmaCompType.getCertifFirmatario().getCertifCaFirmatario();
        // CERTIFICATO CA + BLOB
        FirCertifCa certificatoCa = buildFirCertifCa(certifCaType);

        // CERTIFICATO FIRMATARIO + BLOB (necessario ottenerlo prima di creare la nuova
        // entity AroFirmaComp!)
        FirCertifFirmatario tmpCertifFirmatario = buildFirCertifFirmatario(certificatoCa, firmaCompType);

        // CA
        VFCertifCaType firCertifCa = firmaCompType.getCertifCa();
        FirCertifCa caUso = buildFirCertifCa(firCertifCa);

        // CA CRL (se presente)
        FirCrl crlUso = null;
        VFCrlType firCrlCa = firmaCompType.getCrlCertifCa();
        if (firCrlCa != null) {
            crlUso = buildFirCrl(firCrlCa);
        }

        // CRL (se presente)
        FirCrl crlContr = null;
        VFCrlType firCrl = firmaCompType.getCrl();
        if (firCrl != null) {
            crlContr = buildFirCrl(firCrl);
        }

        // CA OCSP (se presente)
        FirOcsp ocspUso = null;
        VFOcspType firOcspCa = firmaCompType.getOcspCertifCa();
        if (firOcspCa != null) {
            ocspUso = buildFirOcspWithCertif(firOcspCa);
        }

        // OCSP (se presente)
        FirOcsp ocspContr = null;
        VFOcspType firOcsp = firmaCompType.getOcsp();
        if (firOcsp != null) {
            ocspContr = buildFirOcspWithCertif(firOcsp);
        }

        // FIRMA
        AroFirmaComp tmpFirmaComp = new AroFirmaComp();
        tmpFirmaComp.setAroContrFirmaComps(new ArrayList<AroContrFirmaComp>());
        tmpFirmaComp.setAroControfirmaFirmaFiglios(new ArrayList<AroControfirmaFirma>());
        tmpFirmaComp.setAroControfirmaFirmaPadres(new ArrayList<AroControfirmaFirma>());
        tmpFirmaComp.setAroVerifFirmaDtVers(new ArrayList<AroVerifFirmaDtVer>());

        // Creo una mappa di CONTR_FIRMA_COMP identificati dalla costante enumerata in
        // VFTipoControlloType
        Map<VFTipoControlloType, VFContrFirmaCompType> controlliFirma = firmaCompType.getContrFirmaComps().stream()
                .collect(Collectors.toMap(VFContrFirmaCompType::getTiContr, t -> t));

        // CONTROLLI FIRMA - CRITTOGRAFICO
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CRITTOGRAFICO);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - CRITTOGRAFICO ABILITATO
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CRITTOGRAFICO_ABILITATO);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO_ABILITATO.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - CATENA TRUSTED
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CATENA_TRUSTED);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CATENA_TRUSTED.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

            ArrayList<AroUsoCertifCaContrComp> usoCerifiCaContrComp = new ArrayList<>();

            AroUsoCertifCaContrComp usoCertifCatena = new AroUsoCertifCaContrComp();
            usoCertifCatena.setPgCertifCa(BigDecimal.ONE);
            usoCertifCatena.setAroContrFirmaComp(controllo);

            // CA
            usoCertifCatena.setFirCertifCa(caUso);
            // CRL (se presente)
            usoCertifCatena.setFirCrl(crlUso);
            // OCSP (se presente)
            usoCertifCatena.setFirOcsp(ocspUso);
            //
            usoCerifiCaContrComp.add(usoCertifCatena);
            entityManager.persist(usoCertifCatena);
            controllo.setAroUsoCertifCaContrComps(usoCerifiCaContrComp);

            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - CATENA TRUSTED ABILITATO
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CATENA_TRUSTED_ABILITATO);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CATENA_TRUSTED_ABILITATO.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - CRL
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CRL);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CRL.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());
            controllo.setFirCrl(crlContr);
            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - OCSP
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.OCSP);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.OCSP.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());
            controllo.setFirOcsp(ocspContr);

            // USO CONTR
            if (ocspContr != null) {
                ArrayList<AroUsoCertifCaContrComp> usoCerifiCaContrComp = new ArrayList<>();

                AroUsoCertifCaContrComp usoCertifOcsp = new AroUsoCertifCaContrComp();
                usoCertifOcsp.setPgCertifCa(BigDecimal.ONE);
                usoCertifOcsp.setAroContrFirmaComp(controllo);

                // CA
                usoCertifOcsp.setFirCertifCa(ocspContr.getFirCertifOcsp().getFirCertifCa());
                usoCertifOcsp.setFirOcsp(ocspContr);

                //
                usoCerifiCaContrComp.add(usoCertifOcsp);
                entityManager.persist(usoCertifOcsp);
                //
                controllo.setAroUsoCertifCaContrComps(usoCerifiCaContrComp);
            }
            //
            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }
        // CONTROLLI FIRMA - CERTIFICATO
        {
            VFContrFirmaCompType vfControllo = controlliFirma.get(VFTipoControlloType.CERTIFICATO);
            AroContrFirmaComp controllo = new AroContrFirmaComp();
            controllo.setAroFirmaComp(tmpFirmaComp);
            controllo.setTiContr(VFTipoControlloType.CERTIFICATO.name());
            controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
            controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

            tmpFirmaComp.getAroContrFirmaComps().add(controllo);
            // persist
            entityManager.persist(controllo);
        }

        //
        tmpFirmaComp.setCdFirmatario(firmaCompType.getCdFirmatario());
        tmpFirmaComp.setNmCognomeFirmatario(firmaCompType.getNmCognomeFirmatario());
        tmpFirmaComp.setNmFirmatario(firmaCompType.getNmFirmatario());
        tmpFirmaComp.setDlDnFirmatario(firmaCompType.getDlDnFirmatario());
        tmpFirmaComp.setBlFirmaBase64(firmaCompType.getDsFirmaBase64());
        tmpFirmaComp.setDsAlgoFirma(firmaCompType.getDsAlgoFirma());
        tmpFirmaComp.setTiFormatoFirma(firmaCompType.getTiFormatoFirma());
        tmpFirmaComp.setTiFirma(firmaCompType.getTiFirma().name());

        tmpFirmaComp.setFirCertifFirmatario(tmpCertifFirmatario);
        //
        if (firmaCompType.getDtFirma() != null) {
            tmpFirmaComp.setDtFirma(XmlDateUtility.xmlGregorianCalendarToDate(firmaCompType.getDtFirma()));
        }
        if (firmaCompType.getTmRifTempUsato() != null) {
            tmpFirmaComp
                    .setTmRifTempUsato(XmlDateUtility.xmlGregorianCalendarToDate(firmaCompType.getTmRifTempUsato()));
        }
        //
        tmpFirmaComp.setTiRifTempUsato(firmaCompType.getTipoRiferimentoTemporaleUsato());

        tmpFirmaComp.setTiEsitoContrConforme(firmaCompType.getTiEsitoContrConforme());
        tmpFirmaComp.setDsMsgEsitoContrConforme(firmaCompType.getDsMsgEsitoContrConforme());
        //
        tmpFirmaComp.setTiEsitoVerifFirma(firmaCompType.getTiEsitoVerifFirma());
        tmpFirmaComp.setDsMsgEsitoVerifFirma(firmaCompType.getDsMsgEsitoVerifFirma());

        tmpFirmaComp.setPgFirma(firmaCompType.getPgFirma());
        tmpFirmaComp.setPgBusta(busta.getPgBusta());

        boolean isDetachedSignature = wrapper.getAdditionalInfo().isIsDetached();
        if (isDetachedSignature) {
            tmpBustaCrittogSottoComp.getAroFirmaComps().add(tmpFirmaComp);
            tmpFirmaComp.setAroBustaCrittog(tmpBustaCrittogSottoComp);
        } else {
            tmpBustaCrittogComp.getAroFirmaComps().add(tmpFirmaComp);
            tmpFirmaComp.setAroBustaCrittog(tmpBustaCrittogComp);
        }
        // Aggiungo la firma al componente e viceversa
        tmpFirmaComp.setAroCompDoc(tmpTabCDComponente);
        // Setto id struttura
        tmpFirmaComp.setIdStrut(tmpTabCDComponente.getIdStrut());
        tmpTabCDComponente.getAroFirmaComps().add(tmpFirmaComp);
        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */

        //
        if (tmpFirmaCompPadre != null) {
            AroControfirmaFirma tmpControfirmaFirma = new AroControfirmaFirma();
            tmpControfirmaFirma.setAroFirmaFiglio(tmpFirmaComp);
            tmpControfirmaFirma.setAroFirmaPadre(tmpFirmaCompPadre);

            tmpFirmaComp.getAroControfirmaFirmaPadres().add(tmpControfirmaFirma);
            tmpFirmaCompPadre.getAroControfirmaFirmaFiglios().add(tmpControfirmaFirma);
            // persist
            entityManager.persist(tmpControfirmaFirma);
        }

        return tmpFirmaComp;
    }

    /**
     * Costruisci l'entity Marca
     *
     * @param busta
     * @param marcaCompType
     * @param tmpTabCDComponente
     * @param tmpBustaCrittogComp
     * @param tmpBustaCrittogSottoComp
     * @param isDetached
     *
     * @return entity AroMarcaComp
     *
     * @throws VerificaFirmaException
     */
    private AroMarcaComp buildAroMarcaComp(VFBusta busta, VFMarcaCompType marcaCompType, AroCompDoc tmpTabCDComponente,
            AroBustaCrittog tmpBustaCrittogComp, AroBustaCrittog tmpBustaCrittogSottoComp, boolean isDetached)
            throws VerificaFirmaException {
        //
        AroMarcaComp tmpMarcaComp = new AroMarcaComp();
        tmpMarcaComp.setAroContrMarcaComps(new ArrayList<AroContrMarcaComp>());

        // Creo una mappa di CONTR_MARCA_COMP identificati dalla costante enumerata in
        // VFTipoControlloType
        Map<VFTipoControlloType, VFContrMarcaCompType> controlliMarca = marcaCompType.getContrMarcaComps().stream()
                .collect(Collectors.toMap(VFContrMarcaCompType::getTiContr, t -> t));

        // TOFIX : corretto ?!
        if (!marcaCompType.getTiEsitoContrConforme()
                .equals(VerificaFirmaEnums.EsitoControllo.FORMATO_NON_CONOSCIUTO.name())) {

            VFCertifCaType certifTsaType = marcaCompType.getCertifTsa();
            // CERTIFICATO TSA + BLOB
            FirCertifCa certificatoTsa = buildFirCertifCa(certifTsaType);
            // CRL TSA (se presente)
            FirCrl crlTsa = null;
            VFCrlType firCrl = marcaCompType.getCrlTsa();
            if (firCrl != null) {
                crlTsa = buildFirCrl(marcaCompType.getCrlTsa());
            }
            // OCSP TSA (se presente)
            FirOcsp ocspTsa = null;
            VFOcspType firOcsp = marcaCompType.getOcspTsa();
            if (firOcsp != null) {
                ocspTsa = buildFirOcspWithCertif(marcaCompType.getOcspTsa());
            }
            // CRL TSA + BLOB
            {
                VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.CRITTOGRAFICO);
                // CONTROLLI MARCA - CRITTOGRAFICO signatureValidations
                AroContrMarcaComp controllo = new AroContrMarcaComp();
                controllo.setAroMarcaComp(tmpMarcaComp);
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO.name());
                controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
                controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());
                entityManager.persist(controllo);
            }

            // CONTROLLI MARCA - CATENA_TRUSTED CertificateAssociation &&
            // CertificateReliability
            {
                VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.CATENA_TRUSTED);
                AroContrMarcaComp controllo = new AroContrMarcaComp();
                controllo.setAroUsoCertifCaContrMarcas(new ArrayList<AroUsoCertifCaContrMarca>());
                controllo.setAroMarcaComp(tmpMarcaComp);
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                controllo.setTiContr(VFTipoControlloType.CATENA_TRUSTED.name());
                controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
                controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());

                ArrayList<AroUsoCertifCaContrMarca> usoCerifiCaContrComp = new ArrayList<>();

                AroUsoCertifCaContrMarca usoCertifCatena = new AroUsoCertifCaContrMarca();
                usoCertifCatena.setPgCertifCa(BigDecimal.ONE);
                usoCertifCatena.setAroContrMarcaComp(controllo);

                // CA
                usoCertifCatena.setFirCertifCa(certificatoTsa);
                // CRL (se presente)
                usoCertifCatena.setFirCrl(crlTsa);
                // OCSP (se presente)
                usoCertifCatena.setFirOcsp(ocspTsa);
                //
                usoCerifiCaContrComp.add(usoCertifCatena);
                // persist
                entityManager.persist(usoCertifCatena);
                controllo.getAroUsoCertifCaContrMarcas().add(usoCertifCatena);

                // persist
                entityManager.persist(controllo);

            }

            // CONTROLLI MARCA - CERTIFICATO CertificateExpiration
            {
                VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.CERTIFICATO);
                AroContrMarcaComp controllo = new AroContrMarcaComp();
                controllo.setAroMarcaComp(tmpMarcaComp);
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                controllo.setTiContr(VFTipoControlloType.CERTIFICATO.name());
                controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
                controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());
                entityManager.persist(controllo);
            }

            // CONTROLLI MARCA - CRL CertificateRevocation
            {
                VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.CRL);
                AroContrMarcaComp controllo = new AroContrMarcaComp();
                controllo.setAroMarcaComp(tmpMarcaComp);
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                controllo.setTiContr(VFTipoControlloType.CRL.name());
                controllo.setFirCrl(crlTsa);
                controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
                controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());
                entityManager.persist(controllo);
            }

            // CONTROLLI MARCA - OCSP CertificateRevocation
            {
                VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.OCSP);
                AroContrMarcaComp controllo = new AroContrMarcaComp();
                controllo.setAroMarcaComp(tmpMarcaComp);
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                controllo.setTiContr(VFTipoControlloType.OCSP.name());
                controllo.setFirOcsp(ocspTsa);
                controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
                controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());

                // USO CONTR
                if (ocspTsa != null) {
                    ArrayList<AroUsoCertifCaContrMarca> usoCerifiCaContrComp = new ArrayList<>();

                    AroUsoCertifCaContrMarca usoCertifOcsp = new AroUsoCertifCaContrMarca();
                    usoCertifOcsp.setPgCertifCa(BigDecimal.ONE);
                    usoCertifOcsp.setAroContrMarcaComp(controllo);

                    // CA
                    usoCertifOcsp.setFirCertifCa(ocspTsa.getFirCertifOcsp().getFirCertifCa());
                    usoCertifOcsp.setFirOcsp(ocspTsa);

                    //
                    usoCerifiCaContrComp.add(usoCertifOcsp);
                    entityManager.persist(usoCertifOcsp);
                    //
                    controllo.setAroUsoCertifCaContrMarcas(usoCerifiCaContrComp);
                }
                //
                tmpMarcaComp.getAroContrMarcaComps().add(controllo);
                // persist
                entityManager.persist(controllo);
            }

            tmpMarcaComp.setFirCertifCa(certificatoTsa);

            // } // crl
        }

        tmpMarcaComp.setDsMarcaBase64(marcaCompType.getDsMarcaBase64());
        tmpMarcaComp.setDsAlgoMarca(marcaCompType.getDsAlgoMarca());
        tmpMarcaComp.setTmMarcaTemp(XmlDateUtility.xmlGregorianCalendarToDate(marcaCompType.getTmMarcaTemp()));
        tmpMarcaComp.setTiFormatoMarca(marcaCompType.getTiFormatoMarca());
        tmpMarcaComp.setDtScadMarca(XmlDateUtility.xmlGregorianCalendarToDate(marcaCompType.getDtScadMarca()));

        tmpMarcaComp.setTiEsitoVerifMarca(marcaCompType.getTiEsitoVerifMarca());
        tmpMarcaComp.setDsMsgEsitoVerifMarca(marcaCompType.getDsMsgEsitoVerifMarca());

        tmpMarcaComp.setTiEsitoContrConforme(marcaCompType.getTiEsitoContrConforme());
        tmpMarcaComp.setDsMsgEsitoContrConforme(marcaCompType.getDsMsgEsitoContrConforme());

        tmpMarcaComp.setPgBusta(busta.getPgBusta());
        tmpMarcaComp.setPgMarca(marcaCompType.getPgMarca());

        if (isDetached) {
            tmpBustaCrittogSottoComp.getAroMarcaComps().add(tmpMarcaComp);
            tmpMarcaComp.setAroBustaCrittog(tmpBustaCrittogSottoComp);
        } else {
            tmpBustaCrittogComp.getAroMarcaComps().add(tmpMarcaComp);
            tmpMarcaComp.setAroBustaCrittog(tmpBustaCrittogComp);
        }
        // Aggiungo la marca al componente e viceversa

        tmpMarcaComp.setAroCompDoc(tmpTabCDComponente);

        tmpTabCDComponente.getAroMarcaComps().add(tmpMarcaComp);

        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */
        return tmpMarcaComp;
    }

    /*
     * Ottiene dal DB con la PK ottenuta dal wrapper o tenta di ricercare l'entità già creata nella stessa transazione
     * (caso in cui non esiste su DB, PK non presente su additional info, e quindi già inserita precedentemente (stessa
     * transaction) o crea l'entity per il certificato della CA
     *
     */
    private FirCertifCa buildFirCertifCa(VFCertifCaType certifCaType) throws VerificaFirmaException {

        // FirCertifCa
        FirCertifCa tmpCertificatoCa = null;
        if (certifCaType.getAdditionalInfo() != null && certifCaType.getAdditionalInfo().getIdCertifCa() != null) {
            tmpCertificatoCa = entityManager.find(FirCertifCa.class,
                    certifCaType.getAdditionalInfo().getIdCertifCa().longValue());
        } else {
            /* entità inserita in ciclo precedente */
            tmpCertificatoCa = controlliPerFirme.getFirCertifCa(certifCaType.getNiSerialCertifCa(),
                    certifCaType.getDlDnIssuerCertifCa());
        }

        // FIX MAC 26836, compilo il subject mancante e correggo il subject keyId:
        if (tmpCertificatoCa != null) {
            correggiInformazioniCA(tmpCertificatoCa, certifCaType);
        }

        // not found
        if (tmpCertificatoCa == null) {
            tmpCertificatoCa = new FirCertifCa();
            tmpCertificatoCa.setFirUrlDistribCrls(new ArrayList<FirUrlDistribCrl>());
            tmpCertificatoCa.setFirUrlDistribOcsps(new ArrayList<FirUrlDistribOcsp>());
            tmpCertificatoCa.setFirCertifFirmatarios(new ArrayList<FirCertifFirmatario>());
            tmpCertificatoCa.setFirCrls(new ArrayList<FirCrl>());

            tmpCertificatoCa.setNiSerialCertifCa(certifCaType.getNiSerialCertifCa());
            tmpCertificatoCa.setDsSubjectKeyId(certifCaType.getDsSubjectKeyId());
            tmpCertificatoCa
                    .setDtFinValCertifCa(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtFinValCertifCa()));
            tmpCertificatoCa
                    .setDtIniValCertifCa(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()));
            tmpCertificatoCa.setDlDnIssuerCertifCa(certifCaType.getDlDnIssuerCertifCa());
            tmpCertificatoCa.setDlDnSubjectCertifCa(certifCaType.getDlDnSubjectCertifCa());
            // urls crl
            int idx = 1;
            for (VFUrlDistribCrlType url : certifCaType.getUrlDistribCrls()) {
                FirUrlDistribCrl firUrl = new FirUrlDistribCrl();
                firUrl.setDlUrlDistribCrl(url.getDlUrlDistribCrl());
                firUrl.setNiOrdUrlDistribCrl(new BigDecimal(idx));
                firUrl.setFirCertifCa(tmpCertificatoCa);
                tmpCertificatoCa.getFirUrlDistribCrls().add(firUrl);
                idx++;
            }

            // urls ocsp
            idx = 1;
            for (VFUrlDistribOcspType url : certifCaType.getUrlDistribOcsps()) {
                FirUrlDistribOcsp firUrl = new FirUrlDistribOcsp();
                firUrl.setDlUrlDistribOcsp(url.getDlUrlDistribOcsp());
                firUrl.setNiOrdUrlDistribOcsp(new BigDecimal(idx));
                firUrl.setFirCertifCa(tmpCertificatoCa);
                tmpCertificatoCa.getFirUrlDistribOcsps().add(firUrl);
                idx++;
            }

            // se il file non presente -> non lo si persiste
            if (certifCaType.getFilePerFirma() != null) {
                FirFilePerFirma blobCertCa = new FirFilePerFirma();
                blobCertCa.setTiFilePerFirma(certifCaType.getFilePerFirma().getTiFilePerFirma().name());
                blobCertCa.setBlFilePerFirma(certifCaType.getFilePerFirma().getBlFilePerFirma());
                blobCertCa.setFirCertifCa(tmpCertificatoCa);
                tmpCertificatoCa.setFirFilePerFirma(blobCertCa);
            } else {
                LOG.warn("File per firma non presente per CA serial {} Issuer {} Subject {}",
                        tmpCertificatoCa.getNiSerialCertifCa(), tmpCertificatoCa.getDlDnIssuerCertifCa(),
                        tmpCertificatoCa.getDlDnSubjectCertifCa());
            }

            // persist
            entityManager.persist(tmpCertificatoCa);
        } else {
            // pregresso URL OCSP
            if (tmpCertificatoCa.getFirUrlDistribOcsps().isEmpty()) {
                // urls ocsp
                int idx = 1;
                for (VFUrlDistribOcspType url : certifCaType.getUrlDistribOcsps()) {
                    FirUrlDistribOcsp firUrl = new FirUrlDistribOcsp();
                    firUrl.setDlUrlDistribOcsp(url.getDlUrlDistribOcsp());
                    firUrl.setNiOrdUrlDistribOcsp(new BigDecimal(idx));
                    firUrl.setFirCertifCa(tmpCertificatoCa);
                    tmpCertificatoCa.getFirUrlDistribOcsps().add(firUrl);
                    // persist
                    entityManager.persist(firUrl);
                    idx++;
                }
            }
        }
        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */
        return tmpCertificatoCa;
    }

    /**
     * Assumo che la CA trovata sul DB non sia correttamente censita; se ci sono differenze aggiorno con le informazioni
     * prevenienti dalla verifica delle firme.
     *
     * @param tmpCertificatoCa,
     *            entity esistente
     * @param certifCaType,
     *            oggetto che identifica la CA proveniente dalla verifica firme
     */
    private void correggiInformazioniCA(FirCertifCa tmpCertificatoCa, VFCertifCaType certifCaType) {
        boolean pleasePersist = false;
        // Controllo su DL_DN_SUBJECT
        if (tmpCertificatoCa.getDlDnSubjectCertifCa() == null
                || !tmpCertificatoCa.getDlDnSubjectCertifCa().equals(certifCaType.getDlDnSubjectCertifCa())) {
            tmpCertificatoCa.setDlDnSubjectCertifCa(certifCaType.getDlDnSubjectCertifCa());
            pleasePersist = true;
        }
        boolean isSubjectKeyIdEsistenteNullOrNonValorizzato = tmpCertificatoCa.getDsSubjectKeyId() == null
                || tmpCertificatoCa.getDsSubjectKeyId().equals("NON_VALORIZZATO");
        boolean isSubjectKeyIdNuovoNotNullAndValorizzato = certifCaType.getDsSubjectKeyId() != null
                && !certifCaType.getDsSubjectKeyId().equals("NON_VALORIZZATO");
        // Controllo su SUBJECT_KEY_ID
        if (isSubjectKeyIdEsistenteNullOrNonValorizzato && isSubjectKeyIdNuovoNotNullAndValorizzato) {
            tmpCertificatoCa.setDsSubjectKeyId(certifCaType.getDsSubjectKeyId());
            pleasePersist = true;
        }
        // controllo su DT_INI_VAL
        if (!tmpCertificatoCa.getDtIniValCertifCa()
                .equals(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()))) {
            tmpCertificatoCa
                    .setDtIniValCertifCa(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()));
            pleasePersist = true;
        }
        // controllo su DT_FIN_VAL
        if (!tmpCertificatoCa.getDtFinValCertifCa()
                .equals(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtFinValCertifCa()))) {
            tmpCertificatoCa
                    .setDtFinValCertifCa(XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()));
            pleasePersist = true;
        }

        if (pleasePersist) {
            entityManager.persist(tmpCertificatoCa);
        }
    }

    private FirCrl buildFirCrl(VFCrlType crl) throws VerificaFirmaException {

        FirCrl tmpFirCrl = null;

        FirCertifCa tmpCertifCa = buildFirCertifCa(crl.getCertifCa());

        if (crl.getAdditionalInfo() != null && crl.getAdditionalInfo().getIdCrl() != null) {
            tmpFirCrl = entityManager.find(FirCrl.class, crl.getAdditionalInfo().getIdCrl().longValue());
        } else {
            /* entità inserita in ciclo precedente */
            tmpFirCrl = controlliPerFirme.getFirCrl(tmpCertifCa, crl.getNiSerialCrl(),
                    XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtIniCrl()),
                    XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtScadCrl()));
        }

        if (tmpFirCrl == null) {
            tmpFirCrl = new FirCrl();
            tmpFirCrl.setDtIniCrl(XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtIniCrl()));
            tmpFirCrl.setDtScadCrl(XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtScadCrl()));
            tmpFirCrl.setNiSerialCrl(crl.getNiSerialCrl());
            tmpFirCrl.setFirCertifCa(tmpCertifCa);

            // add on list parent
            tmpCertifCa.getFirCrls().add(tmpFirCrl);

            // se il file non presente -> non lo si persiste
            if (crl.getFilePerFirma() != null) {
                FirFilePerFirma tmpFilePerFirma = new FirFilePerFirma();
                tmpFilePerFirma.setTiFilePerFirma(crl.getFilePerFirma().getTiFilePerFirma().name());// TipoFileEnum.CRL.name()
                tmpFilePerFirma.setBlFilePerFirma(crl.getFilePerFirma().getBlFilePerFirma());
                tmpFilePerFirma.setFirCrl(tmpFirCrl);
                tmpFirCrl.setFirFilePerFirma(tmpFilePerFirma);
            } else {
                LOG.warn("File per firma non presente per CRL serial {}", tmpFirCrl.getNiSerialCrl());
            }

            // persist
            entityManager.persist(tmpFirCrl);
        }

        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */
        return tmpFirCrl;

    }

    private FirOcsp buildFirOcspWithCertif(VFOcspType ocsp) throws VerificaFirmaException {

        FirCertifOcsp tmpFirCertifOcsp = null;

        FirCertifCa tmpCertifCa = buildFirCertifCa(ocsp.getCertifOcsp().getCertifCa());

        if (ocsp.getCertifOcsp().getAdditionalInfo() != null
                && ocsp.getCertifOcsp().getAdditionalInfo().getIdCertifOcsp() != null) {
            tmpFirCertifOcsp = entityManager.find(FirCertifOcsp.class,
                    ocsp.getCertifOcsp().getAdditionalInfo().getIdCertifOcsp().longValue());
        } else {
            /* entità inserita in ciclo precedente */
            tmpFirCertifOcsp = controlliPerFirme.getFirCertifOcsp(tmpCertifCa,
                    ocsp.getCertifOcsp().getNiSerialCertifOcsp());
        }

        if (tmpFirCertifOcsp == null) {
            tmpFirCertifOcsp = new FirCertifOcsp();
            tmpFirCertifOcsp.setNiSerialCertifOcsp(ocsp.getCertifOcsp().getNiSerialCertifOcsp());
            tmpFirCertifOcsp.setDtIniValCertifOcsp(
                    XmlDateUtility.xmlGregorianCalendarToDate(ocsp.getCertifOcsp().getDtIniValCertifOcsp()));
            tmpFirCertifOcsp.setDtFinValCertifOcsp(
                    XmlDateUtility.xmlGregorianCalendarToDate(ocsp.getCertifOcsp().getDtFinValCertifOcsp()));
            tmpFirCertifOcsp.setDlDnSubjectCertifOcsp(ocsp.getCertifOcsp().getDlDnSubject());
            tmpFirCertifOcsp.setFirCertifCa(tmpCertifCa);

            // add on list parent
            tmpCertifCa.getFirCertifOcsps().add(tmpFirCertifOcsp);

            // se il file non presente -> non lo si persiste
            if (ocsp.getCertifOcsp().getFilePerFirma() != null) {
                FirFilePerFirma tmpFilePerFirma = new FirFilePerFirma();
                tmpFilePerFirma.setTiFilePerFirma(ocsp.getCertifOcsp().getFilePerFirma().getTiFilePerFirma().name());// TipoFileEnum.CERTIF_OCSP.name()
                tmpFilePerFirma.setBlFilePerFirma(ocsp.getCertifOcsp().getFilePerFirma().getBlFilePerFirma());
                tmpFilePerFirma.setFirCertifOcsp(tmpFirCertifOcsp);
                tmpFirCertifOcsp.setFirFilePerFirma(tmpFilePerFirma);
            } else {
                LOG.warn("File per firma non presente per OCSP serial {} issuer {}",
                        tmpFirCertifOcsp.getNiSerialCertifOcsp(), tmpFirCertifOcsp.getDlDnSubjectCertifOcsp());
            }

            // persist
            entityManager.persist(tmpFirCertifOcsp);
        }

        FirOcsp tmpFirOcsp = null;

        if (ocsp.getAdditionalInfo() != null && ocsp.getAdditionalInfo().getIdOcsp() != null) {
            tmpFirOcsp = entityManager.find(FirOcsp.class, ocsp.getAdditionalInfo().getIdOcsp().longValue());
        } else {
            /* entità inserita in ciclo precedente */
            tmpFirOcsp = controlliPerFirme.getFirOcsp(tmpFirCertifOcsp, ocsp.getDsCertifIssuername(),
                    ocsp.getDsCertifSerialBase64(), ocsp.getDsCertifSkiBase64());
        }

        if (tmpFirOcsp == null) {
            tmpFirOcsp = new FirOcsp();
            tmpFirOcsp.setDsCertifIssuername(ocsp.getDsCertifIssuername());
            tmpFirOcsp.setDsCertifSerialBase64(ocsp.getDsCertifSerialBase64());
            tmpFirOcsp.setDsCertifSkiBase64(ocsp.getDsCertifSkiBase64());
            tmpFirOcsp.setFirCertifOcsp(tmpFirCertifOcsp);

            // add parent
            tmpFirCertifOcsp.getFirOcsps().add(tmpFirOcsp);

            FirFilePerFirma tmpFilePerFirma = new FirFilePerFirma();
            tmpFilePerFirma.setTiFilePerFirma(ocsp.getFilePerFirma().getTiFilePerFirma().name());// TipoFileEnum.OCSP.name()
            tmpFilePerFirma.setBlFilePerFirma(ocsp.getFilePerFirma().getBlFilePerFirma());
            tmpFilePerFirma.setFirOcsp(tmpFirOcsp);
            tmpFirOcsp.setFirFilePerFirma(tmpFilePerFirma);

            // persist
            entityManager.persist(tmpFirOcsp);
        }

        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */
        return tmpFirOcsp;

    }

    /**
     * Costruisce l'entity del firmatario caricadola da DB oppure costruendola ex-novo.
     *
     * @param tmpFirCertifCa
     *            informazioni della ca
     * @param certifCaType
     *            entity della ca
     *
     * @return entity FirCertifFirmatario
     */
    private FirCertifFirmatario buildFirCertifFirmatario(FirCertifCa tmpFirCertifCa, VFFirmaCompType firmaCompType)
            throws VerificaFirmaException {
        FirCertifFirmatario firCertifFirmatario = null;

        if (firmaCompType != null && firmaCompType.getCertifFirmatario() != null) {
            VFCertifFirmatarioType certifFirmatarioType = firmaCompType.getCertifFirmatario();
            if (certifFirmatarioType.getAdditionalInfo() != null
                    && certifFirmatarioType.getAdditionalInfo().getIdCertifFirmatario() != null) {
                firCertifFirmatario = entityManager.find(FirCertifFirmatario.class,
                        firmaCompType.getCertifFirmatario().getAdditionalInfo().getIdCertifFirmatario().longValue());

            } else {
                /* entità inserita in ciclo precedente */
                firCertifFirmatario = controlliPerFirme.getFirCertifFirmatario(tmpFirCertifCa,
                        certifFirmatarioType.getNiSerialCertifFirmatario());
            }

            if (firCertifFirmatario == null) {
                firCertifFirmatario = new FirCertifFirmatario();
                firCertifFirmatario.setNiSerialCertifFirmatario(certifFirmatarioType.getNiSerialCertifFirmatario());
                firCertifFirmatario.setDtIniValCertifFirmatario(
                        XmlDateUtility.xmlGregorianCalendarToDate(certifFirmatarioType.getDtIniValCertifFirmatario()));
                firCertifFirmatario.setDtFinValCertifFirmatario(
                        XmlDateUtility.xmlGregorianCalendarToDate(certifFirmatarioType.getDtFinValCertifFirmatario()));
                //
                firCertifFirmatario.setFirCertifCa(tmpFirCertifCa);

                tmpFirCertifCa.setFirCertifFirmatarios(new ArrayList<FirCertifFirmatario>());
                tmpFirCertifCa.getFirCertifFirmatarios().add(firCertifFirmatario);

                // se il file non presente -> non lo si persiste
                if (certifFirmatarioType.getFilePerFirma() != null) {
                    FirFilePerFirma blobCertFirmatario = new FirFilePerFirma();
                    blobCertFirmatario
                            .setTiFilePerFirma(certifFirmatarioType.getFilePerFirma().getTiFilePerFirma().name());// TipoFileEnum.CERTIF_FIRMATARIO.name()
                    blobCertFirmatario.setBlFilePerFirma(certifFirmatarioType.getFilePerFirma().getBlFilePerFirma());
                    blobCertFirmatario.setFirCertifFirmatario(firCertifFirmatario);
                    //
                    firCertifFirmatario.setFirFilePerFirma(blobCertFirmatario);
                } else {
                    LOG.warn("File per firma non presente per Firmatario serial {}",
                            firCertifFirmatario.getNiSerialCertifFirmatario());
                }

                // persist
                entityManager.persist(firCertifFirmatario);

            }
        }

        /*
         * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean dovrebbe valore false?
         * Nel caso utilizzare VerificaFirmaException
         */
        return firCertifFirmatario;

    }

    private void buildFirReport(AroCompDoc tmpVerificaAroCompDoc, VerificaFirmaWrapper wrapper,
            StrutturaVersamento strutV) throws VerificaFirmaException {

        // servizio verifica firma determinato in precedenza
        DecServizioVerificaCompDoc servizioFirma = tmpVerificaAroCompDoc.getDecServizioVerificaCompDoc();

        // GET compDoc URNs
        String compUrnOrig = new ArrayList<AroCompUrnCalc>(tmpVerificaAroCompDoc.getAroAroCompUrnCalcs()).stream()
                .filter(c -> c.getTiUrn().equals(TiUrn.ORIGINALE)).collect(Collectors.toList()).get(0).getDsUrn();

        String compUrnNorm = new ArrayList<AroCompUrnCalc>(tmpVerificaAroCompDoc.getAroAroCompUrnCalcs()).stream()
                .filter(c -> c.getTiUrn().equals(TiUrn.NORMALIZZATO)).collect(Collectors.toList()).get(0).getDsUrn();

        // 0. zip tmp file
        // TOFIX: da rivedere
        Path reportZip = null;
        try {
            // TOFIX: da rivedere
            reportZip = Files.createTempFile("report_", ".zip");
            // TOFIX : verificare se esiste una metodologia migliore
            if (CdServizioVerificaCompDoc.EIDAS.equals(servizioFirma.getCdServizio())) {
                // 0. si "estre dall'oggetto il noto modello standard" (nota bene: se cambia la
                // versione
                // non impatterà sul pregresso già persistito)
                EidasWSReportsDTOTree dto = (EidasWSReportsDTOTree) wrapper.getAdditionalInfo().getReportContent();
                /*
                 * Nota : EIDAS prevede n livelli di buste contenti ciascuna i report DSS si rende necessaria una
                 * "elaborazione" ad-hoc
                 */

                // 1. "filtering" sui tipi report basata su valore del parametro configurato
                List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDocByType = new ArrayList<DecReportServizioVerificaCompDoc>(
                        servizioFirma.getDecReportServizioVerificaCompDocs())
                                .stream()
                                .filter(r -> strutV.getGenerazioneRerortVerificaFirma()
                                        .equalsIgnoreCase(GenReportVerificaFirma.ALL.name())
                                        || (r.getTiReport().equals(TiReportServizioVerificaCompDoc.SIMPLE)
                                                || r.getTiReport().equals(TiReportServizioVerificaCompDoc.DETAILED)))
                                .collect(Collectors.toList());

                // attraverso tale logica per ogni servizio+versione, viene creato un file
                // compresso con i report
                // che ha una determinata "struttura" (la medesima logica viene adottata in
                // lettura)
                // al momento è da considerarsi come la metodologia standard
                //
                verificaFirmaReportHelper.manageEidasReport(reportZip, servizioFirma,
                        reportServizioVerificaCompDocByType, dto, compUrnNorm);

            }

            // attraverso tale logica per ogni servizio+versione, viene creato un file
            // compresso con i report
            // che ha una determinata "struttura" (la medesima logica viene adottata in
            // lettura)
            // al momento è da considerarsi come la metodologia standard
            if (CdServizioVerificaCompDoc.CRYPTO.equals(servizioFirma.getCdServizio())) {
                //
                verificaFirmaReportHelper.manageCryptoReport(reportZip, wrapper, servizioFirma, compUrnNorm);
            }

            // check if file is not empty
            if (reportZip.toFile().length() != 0) {
                // build FirReport
                FirReport tmpFirReport = new FirReport();
                //
                tmpFirReport.setFirUrnReports(new ArrayList<>());
                tmpFirReport.setAroCompDoc(tmpVerificaAroCompDoc);
                // add on AroCompDoc
                tmpVerificaAroCompDoc.getFirReport().add(tmpFirReport);

                // URN
                buildFirUrnReport(tmpFirReport, compUrnOrig, compUrnNorm);
                // persist
                entityManager.persist(tmpFirReport);

                // aws
                Map<String, PutObjectResult> awsPutReportZipResult = verificaFirmaReportAwsClient
                        .sendReportZipToObjStorage(tmpVerificaAroCompDoc.getIdCompDoc(), tmpFirReport.getIdFirReport(),
                                strutV, servizioFirma, tmpFirReport.getFirUrnReports(), reportZip);
                if (awsPutReportZipResult != null) {
                    tmpFirReport.setNmBucket(verificaFirmaReportAwsClient.getBucketName());
                    tmpFirReport.setCdKeyFile((String) awsPutReportZipResult.keySet().toArray()[0]);
                } else {
                    LOG.warn("SalvataggioFirmaManager.buildFirRepor persistenza report verifica firma su BLOB");
                    // force insert before update
                    entityManager.flush();
                    // procedo alla memorizzazione del file sul blob, via JDBC
                    WriteCompBlbOracle.DatiAccessori datiAccessori = new WriteCompBlbOracle().new DatiAccessori();
                    datiAccessori.setTabellaBlob(WriteCompBlbOracle.TabellaBlob.FIR_REPORT);
                    datiAccessori.setIdPadre(tmpFirReport.getIdFirReport());
                    //
                    FileBinario fileB = new FileBinario();
                    fileB.setFileSuDisco(reportZip.toFile());
                    RispostaControlli tmpControlli = writeCompBlbOracle.aggiornaStreamSuBlobComp(datiAccessori, fileB);
                    if (!tmpControlli.isrBoolean()) {
                        throw new VerificaFirmaException(tmpControlli.getCodErr(), tmpControlli.getDsErr());
                    }
                }

                // flush
                entityManager.flush();
            }
        } /* try */ catch (IOException ex) {
            throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
                    MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                            "SalvataggioFirmaManager.buildFirReport errore generico durante elaborazione file report"
                                    + ": " + ExceptionUtils.getMessage(ex)),
                    ex);
        } finally {
            if (reportZip != null) {
                try {
                    Files.delete(reportZip);
                } catch (IOException e) {
                    LOG.warn("SalvataggioFirmaManager.buildFirReport il file {} non è stato correttamente cancellata",
                            reportZip.toFile().getName());
                }
            }
        }
    }

    /*
     * Elab/Calc URN report file compressed
     */
    private void buildFirUrnReport(FirReport tmpFirReport, String urnBase, String urnBaseNorm) {
        // generazione URN
        FirUrnReport tmpUrnReport = new FirUrnReport();
        // ORIGINALE
        String urn = MessaggiWSFormat.formattaUrnReportVerificaFirma(urnBase,
                Costanti.UrnFormatter.URN_REPORT_FMT_STRING);
        tmpUrnReport.setDsUrn(urn);
        tmpUrnReport.setTiUrn(TiUrnReport.ORIGINALE);
        tmpUrnReport.setFirReport(tmpFirReport);

        tmpFirReport.getFirUrnReports().add(tmpUrnReport);

        tmpUrnReport = new FirUrnReport();
        // NORMALIZZATO
        urn = MessaggiWSFormat.formattaUrnReportVerificaFirma(urnBaseNorm, Costanti.UrnFormatter.URN_REPORT_FMT_STRING);
        tmpUrnReport.setDsUrn(urn);
        tmpUrnReport.setTiUrn(TiUrnReport.NORMALIZZATO);
        tmpUrnReport.setFirReport(tmpFirReport);

        tmpFirReport.getFirUrnReports().add(tmpUrnReport);
    }

}
