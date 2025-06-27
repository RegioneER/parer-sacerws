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

package it.eng.parer.firma.ejb;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import it.eng.parer.entity.DecBackend;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.DecReportServizioVerificaCompDoc;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.FirCertifCa;
import it.eng.parer.entity.FirCertifFirmatario;
import it.eng.parer.entity.FirCertifOcsp;
import it.eng.parer.entity.FirCrl;
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
import it.eng.parer.firma.util.VerificaFirmaEnums.SacerIndication;
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
import it.eng.parer.ws.versamento.dto.BackendStorage;
import it.eng.parer.ws.versamento.dto.FileBinario;
import it.eng.parer.ws.versamento.dto.ObjectStorageResource;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;
import it.eng.parer.ws.versamento.ejb.ObjectStorageService;
import it.eng.parer.ws.versamento.ejb.oracleBlb.WriteCompBlbOracle;
import it.eng.parer.ws.versamento.exceptions.ObjectStorageException;

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
    private ObjectStorageService objectStorageService;

    @EJB
    private WriteCompBlbOracle writeCompBlbOracle;

    /**
     * Note : 1 - popola le entità secondo le logiche previste (vedi
     * FirmeFormatiVers.extractVerifyInfo) 1.1 - si sostituisce alle logiche di questa API
     * utilizzando quanto estratto in fase di verifica firma sul wrapper 2 - si prevede che il
     * metodo sotto venga invocato nell'ambito di SalvataggioSync.salvaComponenti
     *
     *
     * @param risposta           risposta controlli
     * @param idCompVers         id componente
     * @param wrapper            wrapper verifica della firma
     * @param tmpTabCDComponente componente
     * @param tmpTabCDSottoComp  sotto componente
     *
     * @return boolean risultato salvataggio true/falase
     */
    public boolean salvaBustaCrittografica(RispostaControlli risposta, String idCompVers,
	    VerificaFirmaWrapper wrapper, AroCompDoc tmpTabCDComponente,
	    AroCompDoc tmpTabCDSottoComp) {
	//
	boolean result = true;

	try {
	    // forEach busta
	    for (VFBusta busta : wrapper.getVFBusta()) {
		// check (esistono firme/marche per il componente <ID> analizzato?)
		if (busta.getFirmaComps().stream().filter(f -> f.getId().equals(idCompVers))
			.count() != 0
			|| busta.getMarcaComps().stream().filter(m -> m.getId().equals(idCompVers))
				.count() != 0) {

		    // create AroBustaCrittog componente
		    AroBustaCrittog tmpBustaCrittogComp = buildAroBustaCrittog(tmpTabCDComponente,
			    busta);

		    // create AroBustaCrittog sotto componente
		    AroBustaCrittog tmpBustaCrittogSottoComp = null;
		    if (tmpTabCDSottoComp != null) {
			// create empty list
			if (tmpTabCDSottoComp.getAroBustaCrittogs() == null) {
			    tmpTabCDSottoComp.setAroBustaCrittogs(new ArrayList<>());
			}

			tmpBustaCrittogSottoComp = buildAroBustaCrittog(tmpTabCDSottoComp, busta);

		    }

		    // FIRMA
		    List<VFFirmaCompType> firmaCompById = busta.getFirmaComps().stream()
			    .filter(f -> f.getId().equals(idCompVers)).collect(Collectors.toList());
		    //
		    for (VFFirmaCompType firmaCompType : firmaCompById) {
			// FIRMA
			AroFirmaComp tmpFirmaComp = buildAroFirmaComp(wrapper, busta, firmaCompType,
				tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
				null);

			// MARCHE
			buildAroMarcaCompFromFirma(idCompVers, wrapper, busta, firmaCompType,
				tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
				tmpFirmaComp);

			// CONTRO FIRME
			for (VFFirmaCompType firmaControCompType : firmaCompType
				.getControfirmaFirmaFiglios()) {
			    // FIRMA
			    AroFirmaComp tmpControFirmaComp = buildAroFirmaComp(wrapper, busta,
				    firmaControCompType, tmpTabCDComponente, tmpBustaCrittogComp,
				    tmpBustaCrittogSottoComp, tmpFirmaComp);

			    // MARCHE
			    buildAroMarcaCompFromFirma(idCompVers, wrapper, busta,
				    firmaControCompType, tmpTabCDComponente, tmpBustaCrittogComp,
				    tmpBustaCrittogSottoComp, tmpControFirmaComp);
			}

		    }

		    // MARCHE
		    for (VFMarcaCompType marcaCompType : busta.getMarcaComps().stream()
			    .filter(m -> m.getId().equals(idCompVers))
			    .collect(Collectors.toList())) {

			// MARCA
			AroMarcaComp tmpMarcaComp = this.buildAroMarcaComp(busta, marcaCompType,
				tmpTabCDComponente, tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
				wrapper.getAdditionalInfo().isIsDetached());
			// persist
			entityManager.persist(tmpMarcaComp);

		    }
		}
	    }
	    // flush
	    entityManager.flush();
	} catch (VerificaFirmaException ex) {
	    LOG.error("Errore durante il popolamento della busta", ex);
	    risposta.setrBoolean(false);
	    risposta.setCodErr(ex.getCodiceErrore());
	    risposta.setDsErr(ex.getDescrizioneErrore());
	    result = false;
	} catch (Exception ex) {
	    LOG.error("Errore generico durante il popolamento della busta", ex);
	    risposta.setrBoolean(false);
	    risposta.setCodErr(MessaggiWSBundle.ERR_666P);
	    risposta.setDsErr("Errore generico durante il popolamento della busta "
		    + ExceptionUtils.getRootCauseMessage(ex));
	    result = false;
	}

	return result;
    }

    /**
     * Gestione della persistenza del report di verifica firma (object storage vs database)
     *
     * @param risposta      riposta dei controlli effettuati (vedi {@link RispostaControlli})
     * @param wrapper       wrapper standard verifica firma
     * @param strutV        oggetto standard con dati di versamento
     * @param tmpAroCompDoc componente
     * @param nomeWs        nome del servizio che invoca il salvataggio del report
     *
     * @return true/false con risultato dell'operazione
     */
    public boolean salvaReportVerificaCompDoc(RispostaControlli risposta,
	    VerificaFirmaWrapper wrapper, StrutturaVersamento strutV, AroCompDoc tmpAroCompDoc,
	    String nomeWs) {
	boolean result = true;

	try {
	    /*
	     * Persisto servizio di verifica e report per il componente firmato. Tipicamente
	     * dovrebbe essere sempre il componente principale.
	     */
	    // identifica il servizio di verifica sul componente
	    // verifica compdoc
	    DecServizioVerificaCompDoc servizioVerificaCompDoc = entityManager.find(
		    DecServizioVerificaCompDoc.class,
		    wrapper.getAdditionalInfo().getIdServizioFirmaCompDoc());
	    tmpAroCompDoc.setDecServizioVerificaCompDoc(servizioVerificaCompDoc);

	    // result
	    if (StringUtils.isNotBlank(strutV.getGenerazioneRerortVerificaFirma())
		    && !strutV.getGenerazioneRerortVerificaFirma()
			    .equalsIgnoreCase(GenReportVerificaFirma.OFF.name())
		    && wrapper.getAdditionalInfo().getReportContent() != null) {
		buildFirReport(tmpAroCompDoc, wrapper, strutV, nomeWs);
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
	    risposta.setDsErr(
		    "Errore generico durante la creazione del report verifica firma compdoc "
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
    private void buildAroMarcaCompFromFirma(String idCompVers, VerificaFirmaWrapper wrapper,
	    VFBusta busta, VFFirmaCompType firmaCompType, AroCompDoc tmpTabCDComponente,
	    AroBustaCrittog tmpBustaCrittogComp, AroBustaCrittog tmpBustaCrittogSottoComp,
	    AroFirmaComp tmpFirmaComp) throws VerificaFirmaException {

	List<VFMarcaCompType> marcaCompTypeById = firmaCompType.getMarcaComps().stream()
		.filter(m -> m.getId().equals(idCompVers)).collect(Collectors.toList());
	//
	for (VFMarcaCompType marcaCompType : marcaCompTypeById) {

	    // MARCA
	    AroMarcaComp tmpMarcaComp = buildAroMarcaComp(busta, marcaCompType, tmpTabCDComponente,
		    tmpBustaCrittogComp, tmpBustaCrittogSottoComp,
		    wrapper.getAdditionalInfo().isIsDetached());
	    // aggiungo la marca alla firma
	    tmpFirmaComp.setAroMarcaComp(tmpMarcaComp);

	}
    }

    private AroBustaCrittog buildAroBustaCrittog(AroCompDoc tmpAroCompDoc, VFBusta busta) {
	// restituisci AroBustaCrittog se già presente
	if (tmpAroCompDoc.getAroBustaCrittogs() != null) {
	    Optional<AroBustaCrittog> findedBustaCrittog = tmpAroCompDoc.getAroBustaCrittogs()
		    .stream().filter(b -> b.getPgBustaCrittog().compareTo(busta.getPgBusta()) == 0)
		    .findFirst();
	    if (findedBustaCrittog.isPresent()) {
		return findedBustaCrittog.get();
	    }
	}
	// create new entity
	AroBustaCrittog tmpBustaCrittog = new AroBustaCrittog();
	tmpBustaCrittog.setAroCompDoc(tmpAroCompDoc);
	tmpBustaCrittog.setAroFirmaComps(new ArrayList<>());
	tmpBustaCrittog.setAroMarcaComps(new ArrayList<>());
	tmpBustaCrittog.setPgBustaCrittog(busta.getPgBusta());
	tmpBustaCrittog.setIdStrut(tmpAroCompDoc.getIdStrut());

	if (busta.getAdditionalInfo() != null
		&& busta.getAdditionalInfo().getIdFormatoFileStandard() != null) {
	    // additional info (ottenute in precedenza -> vedi check formati)
	    tmpBustaCrittog
		    .setDecFormatoFileStandard(entityManager.find(DecFormatoFileStandard.class,
			    busta.getAdditionalInfo().getIdFormatoFileStandard().longValue()));
	}

	tmpAroCompDoc.getAroBustaCrittogs().add(tmpBustaCrittog);

	return tmpBustaCrittog;
    }

    /*
     * Popola, all'interno dei componenti, le buste per le firme.
     */
    private AroFirmaComp buildAroFirmaComp(VerificaFirmaWrapper wrapper, VFBusta busta,
	    VFFirmaCompType firmaCompType, AroCompDoc tmpTabCDComponente,
	    AroBustaCrittog tmpBustaCrittogComp, AroBustaCrittog tmpBustaCrittogSottoComp,
	    AroFirmaComp tmpFirmaCompPadre) throws VerificaFirmaException {

	// certifCaType
	VFCertifCaType certifCaType = firmaCompType.getCertifFirmatario().getCertifCaFirmatario();
	// CERTIFICATO CA + BLOB
	FirCertifCa certificatoCa = buildFirCertifCa(certifCaType);

	// CERTIFICATO FIRMATARIO + BLOB (necessario ottenerlo prima di creare la nuova
	// entity AroFirmaComp!)
	FirCertifFirmatario tmpCertifFirmatario = buildFirCertifFirmatario(certificatoCa,
		firmaCompType);

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
	tmpFirmaComp.setAroContrFirmaComps(new ArrayList<>());
	tmpFirmaComp.setAroControfirmaFirmaFiglios(new ArrayList<>());
	tmpFirmaComp.setAroControfirmaFirmaPadres(new ArrayList<>());
	tmpFirmaComp.setAroVerifFirmaDtVers(new ArrayList<>());
	// Aggiungo la firma al componente e viceversa
	tmpFirmaComp.setAroCompDoc(tmpTabCDComponente);
	// Setto id struttura
	tmpFirmaComp.setIdStrut(tmpTabCDComponente.getIdStrut());
	tmpTabCDComponente.getAroFirmaComps().add(tmpFirmaComp);
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
	    tmpFirmaComp.setDtFirma(
		    XmlDateUtility.xmlGregorianCalendarToDate(firmaCompType.getDtFirma()));
	}
	if (firmaCompType.getTmRifTempUsato() != null) {
	    tmpFirmaComp.setTmRifTempUsato(
		    XmlDateUtility.xmlGregorianCalendarToDate(firmaCompType.getTmRifTempUsato()));
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

	// Creo una mappa di CONTR_FIRMA_COMP identificati dalla costante enumerata in
	// VFTipoControlloType
	Map<VFTipoControlloType, VFContrFirmaCompType> controlliFirma = firmaCompType
		.getContrFirmaComps().stream()
		.collect(Collectors.toMap(VFContrFirmaCompType::getTiContr, t -> t));

	// CONTROLLI FIRMA - CRITTOGRAFICO
	{
	    VFContrFirmaCompType vfControllo = controlliFirma
		    .get(VFTipoControlloType.CRITTOGRAFICO);
	    AroContrFirmaComp controllo = new AroContrFirmaComp();
	    controllo.setAroFirmaComp(tmpFirmaComp);
	    controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO.name());
	    controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
	    controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());
	    // persist
	    entityManager.persist(controllo);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
	}
	// CONTROLLI FIRMA - CRITTOGRAFICO ABILITATO
	{
	    VFContrFirmaCompType vfControllo = controlliFirma
		    .get(VFTipoControlloType.CRITTOGRAFICO_ABILITATO);
	    AroContrFirmaComp controllo = new AroContrFirmaComp();
	    controllo.setAroFirmaComp(tmpFirmaComp);
	    controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO_ABILITATO.name());
	    controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
	    controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());
	    // persist
	    entityManager.persist(controllo);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
	}
	// CONTROLLI FIRMA - CATENA TRUSTED
	{
	    VFContrFirmaCompType vfControllo = controlliFirma
		    .get(VFTipoControlloType.CATENA_TRUSTED);
	    AroContrFirmaComp controllo = new AroContrFirmaComp();
	    controllo.setAroFirmaComp(tmpFirmaComp);
	    controllo.setTiContr(VFTipoControlloType.CATENA_TRUSTED.name());
	    controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
	    controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());

	    ArrayList<AroUsoCertifCaContrComp> usoCerifiCaContrComp = new ArrayList<>();

	    AroUsoCertifCaContrComp usoCertifCatena = new AroUsoCertifCaContrComp();
	    usoCertifCatena.setPgCertifCa(BigDecimal.ONE);
	    // CA
	    usoCertifCatena.setFirCertifCa(caUso);
	    // CRL (se presente)
	    usoCertifCatena.setFirCrl(crlUso);
	    // OCSP (se presente)
	    usoCertifCatena.setFirOcsp(ocspUso);
	    //
	    usoCerifiCaContrComp.add(usoCertifCatena);
	    // persist
	    entityManager.persist(controllo);
	    usoCertifCatena.setAroContrFirmaComp(controllo);
	    entityManager.persist(usoCertifCatena);
	    controllo.setAroUsoCertifCaContrComps(usoCerifiCaContrComp);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
	}
	// CONTROLLI FIRMA - CATENA TRUSTED ABILITATO
	{
	    VFContrFirmaCompType vfControllo = controlliFirma
		    .get(VFTipoControlloType.CATENA_TRUSTED_ABILITATO);
	    AroContrFirmaComp controllo = new AroContrFirmaComp();
	    controllo.setAroFirmaComp(tmpFirmaComp);
	    controllo.setTiContr(VFTipoControlloType.CATENA_TRUSTED_ABILITATO.name());
	    controllo.setTiEsitoContrFirma(vfControllo.getTiEsitoContrFirma());
	    controllo.setDsMsgEsitoContrFirma(vfControllo.getDsMsgEsitoContrFirma());
	    // persist
	    entityManager.persist(controllo);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
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
	    // persist
	    entityManager.persist(controllo);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
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
	    // persist
	    entityManager.persist(controllo);
	    tmpFirmaComp.getAroContrFirmaComps().add(controllo);
	}

	//

	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
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
    private AroMarcaComp buildAroMarcaComp(VFBusta busta, VFMarcaCompType marcaCompType,
	    AroCompDoc tmpTabCDComponente, AroBustaCrittog tmpBustaCrittogComp,
	    AroBustaCrittog tmpBustaCrittogSottoComp, boolean isDetached)
	    throws VerificaFirmaException {
	//
	AroMarcaComp tmpMarcaComp = new AroMarcaComp();
	tmpMarcaComp.setAroContrMarcaComps(new ArrayList<>());

	tmpMarcaComp.setDsMarcaBase64(marcaCompType.getDsMarcaBase64());
	tmpMarcaComp.setDsAlgoMarca(marcaCompType.getDsAlgoMarca());
	tmpMarcaComp.setTmMarcaTemp(
		XmlDateUtility.xmlGregorianCalendarToDate(marcaCompType.getTmMarcaTemp()));
	tmpMarcaComp.setTiFormatoMarca(marcaCompType.getTiFormatoMarca());
	tmpMarcaComp.setDtScadMarca(
		XmlDateUtility.xmlGregorianCalendarToDate(marcaCompType.getDtScadMarca()));

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

	// Creo una mappa di CONTR_MARCA_COMP identificati dalla costante enumerata in
	// VFTipoControlloType
	Map<VFTipoControlloType, VFContrMarcaCompType> controlliMarca = marcaCompType
		.getContrMarcaComps().stream()
		.collect(Collectors.toMap(VFContrMarcaCompType::getTiContr, t -> t));

	// TOFIX : corretto ?!
	if (!marcaCompType.getTiEsitoContrConforme()
		.equals(SacerIndication.FORMATO_NON_CONOSCIUTO.name())) {

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
		VFContrMarcaCompType vfControllo = controlliMarca
			.get(VFTipoControlloType.CRITTOGRAFICO);
		// CONTROLLI MARCA - CRITTOGRAFICO signatureValidations
		AroContrMarcaComp controllo = new AroContrMarcaComp();
		controllo.setAroMarcaComp(tmpMarcaComp);
		tmpMarcaComp.getAroContrMarcaComps().add(controllo);
		controllo.setTiContr(VFTipoControlloType.CRITTOGRAFICO.name());
		controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
		controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());
	    }

	    // CONTROLLI MARCA - CATENA_TRUSTED CertificateAssociation &&
	    // CertificateReliability
	    {
		VFContrMarcaCompType vfControllo = controlliMarca
			.get(VFTipoControlloType.CATENA_TRUSTED);
		AroContrMarcaComp controllo = new AroContrMarcaComp();
		controllo.setAroUsoCertifCaContrMarcas(new ArrayList<>());
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
		controllo.getAroUsoCertifCaContrMarcas().add(usoCertifCatena);
	    }

	    // CONTROLLI MARCA - CERTIFICATO CertificateExpiration
	    {
		VFContrMarcaCompType vfControllo = controlliMarca
			.get(VFTipoControlloType.CERTIFICATO);
		AroContrMarcaComp controllo = new AroContrMarcaComp();
		controllo.setAroMarcaComp(tmpMarcaComp);
		tmpMarcaComp.getAroContrMarcaComps().add(controllo);
		controllo.setTiContr(VFTipoControlloType.CERTIFICATO.name());
		controllo.setTiEsitoContrMarca(vfControllo.getTiEsitoContrMarca());
		controllo.setDsMsgEsitoContrMarca(vfControllo.getDsMsgEsitoContrMarca());
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
	    }

	    // CONTROLLI MARCA - OCSP CertificateRevocation
	    {
		VFContrMarcaCompType vfControllo = controlliMarca.get(VFTipoControlloType.OCSP);
		AroContrMarcaComp controllo = new AroContrMarcaComp();
		controllo.setAroMarcaComp(tmpMarcaComp);
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

		    controllo.setAroUsoCertifCaContrMarcas(usoCerifiCaContrComp);
		}
		//
		tmpMarcaComp.getAroContrMarcaComps().add(controllo);
	    }

	    tmpMarcaComp.setFirCertifCa(certificatoTsa);

	    // } // crl
	}

	tmpMarcaComp.setAroCompDoc(tmpTabCDComponente);

	tmpTabCDComponente.getAroMarcaComps().add(tmpMarcaComp);

	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
	 */
	return tmpMarcaComp;
    }

    /*
     * Ottiene dal DB con la PK ottenuta dal wrapper o tenta di ricercare l'entità già creata nella
     * stessa transazione (caso in cui non esiste su DB, PK non presente su additional info, e
     * quindi già inserita precedentemente (stessa transaction) o crea l'entity per il certificato
     * della CA
     *
     */
    private FirCertifCa buildFirCertifCa(VFCertifCaType certifCaType)
	    throws VerificaFirmaException {

	// FirCertifCa
	FirCertifCa tmpCertificatoCa = null;
	if (certifCaType.getAdditionalInfo() != null
		&& certifCaType.getAdditionalInfo().getIdCertifCa() != null) {
	    tmpCertificatoCa = entityManager.find(FirCertifCa.class,
		    certifCaType.getAdditionalInfo().getIdCertifCa().longValue());
	} else {
	    /* entità inserita in ciclo precedente */
	    tmpCertificatoCa = controlliPerFirme.getFirCertifCa(certifCaType.getDsSerialCertifCa(),
		    certifCaType.getDlDnIssuerCertifCa());
	}

	// not found
	if (tmpCertificatoCa == null) {
	    tmpCertificatoCa = new FirCertifCa();
	    tmpCertificatoCa.setFirUrlDistribCrls(new ArrayList<>());
	    tmpCertificatoCa.setFirUrlDistribOcsps(new ArrayList<>());
	    tmpCertificatoCa.setFirCertifFirmatarios(new ArrayList<>());
	    tmpCertificatoCa.setFirCrls(new ArrayList<>());

	    tmpCertificatoCa.setDsSerialCertifCa(certifCaType.getDsSerialCertifCa());
	    tmpCertificatoCa.setDsSubjectKeyId(certifCaType.getDsSubjectKeyId());
	    tmpCertificatoCa.setDtFinValCertifCa(
		    XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtFinValCertifCa()));
	    tmpCertificatoCa.setDtIniValCertifCa(
		    XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()));
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
	} else {
	    /*
	     * assumo che la CA trovata sul DB non sia correttamente censita; se ci sono differenze
	     * aggiorno con le informazioni prevenienti dalla verifica delle firme.
	     *
	     */
	    if (tmpCertificatoCa.getDlDnSubjectCertifCa() == null || !tmpCertificatoCa
		    .getDlDnSubjectCertifCa().equals(certifCaType.getDlDnSubjectCertifCa())) {
		tmpCertificatoCa.setDlDnSubjectCertifCa(certifCaType.getDlDnSubjectCertifCa());
	    }
	    boolean isSubjectKeyIdEsistenteNullOrNonValorizzato = tmpCertificatoCa
		    .getDsSubjectKeyId() == null
		    || tmpCertificatoCa.getDsSubjectKeyId().equals("NON_VALORIZZATO");
	    boolean isSubjectKeyIdNuovoNotNullAndValorizzato = certifCaType
		    .getDsSubjectKeyId() != null
		    && !certifCaType.getDsSubjectKeyId().equals("NON_VALORIZZATO");
	    if (isSubjectKeyIdEsistenteNullOrNonValorizzato
		    && isSubjectKeyIdNuovoNotNullAndValorizzato) {
		tmpCertificatoCa.setDsSubjectKeyId(certifCaType.getDsSubjectKeyId());
	    }
	    /*
	     * aggiornamento di inizio e fine validità del certificato della CA con quelle recepite
	     * dalla verifica
	     */
	    tmpCertificatoCa.setDtIniValCertifCa(
		    XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtIniValCertifCa()));
	    tmpCertificatoCa.setDtFinValCertifCa(
		    XmlDateUtility.xmlGregorianCalendarToDate(certifCaType.getDtFinValCertifCa()));
	    /*
	     * gestione del pregresso URL OCSP (qualora non presenti per la CA presente in
	     * anagrafica)
	     */
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
	// persist
	entityManager.persist(tmpCertificatoCa);
	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
	 */
	return tmpCertificatoCa;
    }

    private FirCrl buildFirCrl(VFCrlType crl) throws VerificaFirmaException {

	FirCrl tmpFirCrl = null;

	FirCertifCa tmpCertifCa = buildFirCertifCa(crl.getCertifCa());

	if (crl.getAdditionalInfo() != null && crl.getAdditionalInfo().getIdCrl() != null) {
	    tmpFirCrl = entityManager.find(FirCrl.class,
		    crl.getAdditionalInfo().getIdCrl().longValue());
	} else {
	    /* entità inserita in ciclo precedente */
	    tmpFirCrl = controlliPerFirme.getFirCrl(tmpCertifCa, crl.getDsSerialCrl(),
		    XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtIniCrl()),
		    XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtScadCrl()));
	}

	if (tmpFirCrl == null) {
	    tmpFirCrl = new FirCrl();
	    tmpFirCrl.setDtIniCrl(XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtIniCrl()));
	    tmpFirCrl.setDtScadCrl(XmlDateUtility.xmlGregorianCalendarToDate(crl.getDtScadCrl()));
	    tmpFirCrl.setDsSerialCrl(crl.getDsSerialCrl());
	    tmpFirCrl.setFirCertifCa(tmpCertifCa);

	    // add on list parent
	    tmpCertifCa.getFirCrls().add(tmpFirCrl);

	    // persist
	    entityManager.persist(tmpFirCrl);
	}

	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
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
		    ocsp.getCertifOcsp().getDsSerialCertifOcsp());
	}

	if (tmpFirCertifOcsp == null) {
	    tmpFirCertifOcsp = new FirCertifOcsp();
	    tmpFirCertifOcsp.setDsSerialCertifOcsp(ocsp.getCertifOcsp().getDsSerialCertifOcsp());
	    tmpFirCertifOcsp.setDtIniValCertifOcsp(XmlDateUtility
		    .xmlGregorianCalendarToDate(ocsp.getCertifOcsp().getDtIniValCertifOcsp()));
	    tmpFirCertifOcsp.setDtFinValCertifOcsp(XmlDateUtility
		    .xmlGregorianCalendarToDate(ocsp.getCertifOcsp().getDtFinValCertifOcsp()));
	    tmpFirCertifOcsp.setDlDnSubjectCertifOcsp(ocsp.getCertifOcsp().getDlDnSubject());
	    tmpFirCertifOcsp.setFirCertifCa(tmpCertifCa);

	    // add on list parent
	    tmpCertifCa.getFirCertifOcsps().add(tmpFirCertifOcsp);

	    // persist
	    entityManager.persist(tmpFirCertifOcsp);
	}

	FirOcsp tmpFirOcsp = null;

	if (ocsp.getAdditionalInfo() != null && ocsp.getAdditionalInfo().getIdOcsp() != null) {
	    tmpFirOcsp = entityManager.find(FirOcsp.class,
		    ocsp.getAdditionalInfo().getIdOcsp().longValue());
	} else {
	    /* entità inserita in ciclo precedente */
	    tmpFirOcsp = controlliPerFirme.getFirOcsp(tmpFirCertifOcsp,
		    ocsp.getDsCertifIssuername(), ocsp.getDsCertifSerialBase64(),
		    ocsp.getDsCertifSkiBase64());
	}

	if (tmpFirOcsp == null) {
	    tmpFirOcsp = new FirOcsp();
	    tmpFirOcsp.setDsCertifIssuername(ocsp.getDsCertifIssuername());
	    tmpFirOcsp.setDsCertifSerialBase64(ocsp.getDsCertifSerialBase64());
	    tmpFirOcsp.setDsCertifSkiBase64(ocsp.getDsCertifSkiBase64());
	    tmpFirOcsp.setFirCertifOcsp(tmpFirCertifOcsp);

	    // add parent
	    tmpFirCertifOcsp.getFirOcsps().add(tmpFirOcsp);

	    // persist
	    entityManager.persist(tmpFirOcsp);
	}

	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
	 */
	return tmpFirOcsp;

    }

    /**
     * Costruisce l'entity del firmatario caricadola da DB oppure costruendola ex-novo.
     *
     * @param tmpFirCertifCa informazioni della ca
     * @param certifCaType   entity della ca
     *
     * @return entity FirCertifFirmatario
     */
    private FirCertifFirmatario buildFirCertifFirmatario(FirCertifCa tmpFirCertifCa,
	    VFFirmaCompType firmaCompType) throws VerificaFirmaException {
	FirCertifFirmatario firCertifFirmatario = null;

	if (firmaCompType != null && firmaCompType.getCertifFirmatario() != null) {
	    VFCertifFirmatarioType certifFirmatarioType = firmaCompType.getCertifFirmatario();
	    if (certifFirmatarioType.getAdditionalInfo() != null
		    && certifFirmatarioType.getAdditionalInfo().getIdCertifFirmatario() != null) {
		firCertifFirmatario = entityManager.find(FirCertifFirmatario.class,
			firmaCompType.getCertifFirmatario().getAdditionalInfo()
				.getIdCertifFirmatario().longValue());

	    } else {
		/* entità inserita in ciclo precedente */
		firCertifFirmatario = controlliPerFirme.getFirCertifFirmatario(tmpFirCertifCa,
			certifFirmatarioType.getDsSerialCertifFirmatario());
	    }

	    if (firCertifFirmatario == null) {
		firCertifFirmatario = new FirCertifFirmatario();
		firCertifFirmatario.setDsSerialCertifFirmatario(
			certifFirmatarioType.getDsSerialCertifFirmatario());
		firCertifFirmatario
			.setDtIniValCertifFirmatario(XmlDateUtility.xmlGregorianCalendarToDate(
				certifFirmatarioType.getDtIniValCertifFirmatario()));
		firCertifFirmatario
			.setDtFinValCertifFirmatario(XmlDateUtility.xmlGregorianCalendarToDate(
				certifFirmatarioType.getDtFinValCertifFirmatario()));
		//
		firCertifFirmatario.setFirCertifCa(tmpFirCertifCa);

		tmpFirCertifCa.setFirCertifFirmatarios(new ArrayList<>());
		tmpFirCertifCa.getFirCertifFirmatarios().add(firCertifFirmatario);

		// persist
		entityManager.persist(firCertifFirmatario);

	    }
	}

	/*
	 * Esitono eccezioni gestibili per questi casi (quelle per cui RispostaControlli#rBoolean
	 * dovrebbe valore false? Nel caso utilizzare VerificaFirmaException
	 */
	return firCertifFirmatario;

    }

    private void buildFirReport(AroCompDoc tmpVerificaAroCompDoc, VerificaFirmaWrapper wrapper,
	    StrutturaVersamento strutV, String nomeWs) throws VerificaFirmaException {

	// servizio verifica firma determinato in precedenza
	DecServizioVerificaCompDoc servizioFirma = tmpVerificaAroCompDoc
		.getDecServizioVerificaCompDoc();

	// GET compDoc URNs
	String compUrnOrig = new ArrayList<AroCompUrnCalc>(
		tmpVerificaAroCompDoc.getAroAroCompUrnCalcs()).stream()
		.filter(c -> c.getTiUrn().equals(TiUrn.ORIGINALE)).collect(Collectors.toList())
		.get(0).getDsUrn();

	String compUrnNorm = new ArrayList<AroCompUrnCalc>(
		tmpVerificaAroCompDoc.getAroAroCompUrnCalcs()).stream()
		.filter(c -> c.getTiUrn().equals(TiUrn.NORMALIZZATO)).collect(Collectors.toList())
		.get(0).getDsUrn();

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
		EidasWSReportsDTOTree dto = (EidasWSReportsDTOTree) wrapper.getAdditionalInfo()
			.getReportContent();
		/*
		 * Nota : EIDAS prevede n livelli di buste contenti ciascuna i report DSS si rende
		 * necessaria una "elaborazione" ad-hoc
		 */

		// 1. "filtering" sui tipi report basata su valore del parametro configurato
		List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDocByType = new ArrayList<DecReportServizioVerificaCompDoc>(
			servizioFirma.getDecReportServizioVerificaCompDocs())
			.stream()
			.filter(r -> strutV.getGenerazioneRerortVerificaFirma()
				.equalsIgnoreCase(GenReportVerificaFirma.ALL.name())
				|| (r.getTiReport().equals(TiReportServizioVerificaCompDoc.SIMPLE)
					|| r.getTiReport()
						.equals(TiReportServizioVerificaCompDoc.DETAILED)))
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
		verificaFirmaReportHelper.manageCryptoReport(reportZip, wrapper, servizioFirma,
			compUrnNorm);
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
		BackendStorage backendVerificaFirme = objectStorageService
			.lookupBackendByServiceName(strutV.getIdTipologiaUnitaDocumentaria(),
				nomeWs);
		//
		if (backendVerificaFirme.isObjectStorage()) {

		    ObjectStorageResource savedReport = objectStorageService
			    .createResourceInRerportvf(backendVerificaFirme.getBackendName(),
				    tmpVerificaAroCompDoc, strutV, servizioFirma, reportZip);
		    if (savedReport == null) {
			// Caso 1: il salvataggio su object storage non è andato a buon fine
			saveReportFallback(tmpFirReport, reportZip);
		    } else {
			tmpFirReport.setNmBucket(savedReport.getBucket());
			tmpFirReport.setCdKeyFile(savedReport.getKey());
			buildDecBackend(tmpFirReport, backendVerificaFirme.getBackendName());

			// persist
			entityManager.persist(tmpFirReport);
		    }
		} else {
		    // Caso 2: il backend è Database
		    saveReportFallback(tmpFirReport, reportZip);
		}

		// flush
		entityManager.flush();
	    }
	} /* try */ catch (IOException | ObjectStorageException ex) {
	    throw new VerificaFirmaException(MessaggiWSBundle.ERR_666, MessaggiWSBundle.getString(
		    MessaggiWSBundle.ERR_666,
		    "SalvataggioFirmaManager.buildFirReport errore generico durante elaborazione file report"
			    + ": " + ExceptionUtils.getMessage(ex)),
		    ex);
	} finally {
	    if (reportZip != null) {
		try {
		    Files.delete(reportZip);
		} catch (IOException e) {
		    LOG.warn(
			    "SalvataggioFirmaManager.buildFirReport il file {} non è stato correttamente cancellata",
			    reportZip.toFile().getName());
		}
	    }
	}
    }

    /**
     * Effettua il salvataggio del report su DB. Questo può accadere in due casi:
     * <ul>
     * <li>C'è stato un errore sul salvataggio su Object Storage</li>
     * <li>Il backend configurato per il salvataggio dei report è il Database</li>
     * </ul>
     *
     * @param tmpFirReport tabella FirReport
     * @param reportZip    Path file zip
     *
     * @throws VerificaFirmaException
     */
    private void saveReportFallback(FirReport tmpFirReport, Path reportZip)
	    throws VerificaFirmaException {
	LOG.warn("SalvataggioFirmaManager.buildFirRepor persistenza report verifica firma su BLOB");

	// Se ricadiamo in questo caso dobbiamo essere sicuri di rispettare il vincolo
	// CK_BL_OR_BUCKET
	tmpFirReport.setCdKeyFile(null);
	tmpFirReport.setNmBucket(null);
	entityManager.persist(tmpFirReport);
	// force insert before update
	entityManager.flush();
	// procedo alla memorizzazione del file sul blob, via JDBC
	FileBinario fileB = new FileBinario();
	fileB.setFileSuDisco(reportZip.toFile());
	RispostaControlli tmpControlli = writeCompBlbOracle.aggiornaStreamSuBlobComp(fileB,
		tmpFirReport.getIdFirReport());
	if (!tmpControlli.isrBoolean()) {
	    throw new VerificaFirmaException(tmpControlli.getCodErr(), tmpControlli.getDsErr());
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
	urn = MessaggiWSFormat.formattaUrnReportVerificaFirma(urnBaseNorm,
		Costanti.UrnFormatter.URN_REPORT_FMT_STRING);
	tmpUrnReport.setDsUrn(urn);
	tmpUrnReport.setTiUrn(TiUrnReport.NORMALIZZATO);
	tmpUrnReport.setFirReport(tmpFirReport);

	tmpFirReport.getFirUrnReports().add(tmpUrnReport);
    }

    private void buildDecBackend(FirReport tmpFirReport, String nomeBackend) {
	TypedQuery<DecBackend> query = entityManager.createQuery(
		"Select d from DecBackend d where d.nmBackend = :nomeBackend", DecBackend.class);
	query.setParameter("nomeBackend", nomeBackend);
	DecBackend backend = query.getSingleResult();
	tmpFirReport.setDecBackend(backend);

    }

}
