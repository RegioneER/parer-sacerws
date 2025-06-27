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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.entity.DecEstensioneFile;
import it.eng.parer.entity.DecFormatoFileDoc;
import it.eng.parer.entity.DecFormatoFileStandard;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.firma.dto.CompDocMock;
import it.eng.parer.firma.dto.input.InvokeVerificaInput;
import it.eng.parer.firma.dto.input.InvokeVerificaRule;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.util.VerificaFormatiEnums;
import it.eng.parer.firma.util.VerificaFormatiEnums.EsitoControlloFormato;
import it.eng.parer.firma.util.VerificaFormatiEnums.FormatiStandardFirme;
import it.eng.parer.firma.util.VerificaFormatiEnums.IdoneitaFormato;
import it.eng.parer.firma.xml.VFAdditionalInfoBustaType;
import it.eng.parer.firma.xml.VFFirmaCompType;
import it.eng.parer.firma.xml.VFMarcaCompType;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.firma.xml.VerificaFirmaWrapper.VFBusta;
import it.eng.parer.util.ejb.help.ConfigurationHelper;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import it.eng.parer.ws.utils.ParametroApplDB.ParametroApplFl;
import it.eng.parer.ws.versamento.dto.AbsVersamentoExt;
import it.eng.parer.ws.versamento.dto.ComponenteVers;
import it.eng.parer.ws.versamento.ejb.ControlliPerFirme;
import it.eng.parer.ws.versamento.ejb.DocumentoVersVFirmeHash;

import java.time.ZonedDateTime;

/**
 *
 * @author Quaranta_M
 */
@Stateless
@LocalBean
public class FirmeFormatiVers {

    private static final Logger LOG = LoggerFactory.getLogger(FirmeFormatiVers.class);

    @EJB
    protected ControlliSemantici controlliSemantici; // serve per i parametri applicativi
    @EJB
    private ConfigurationHelper configurationHelper;
    @EJB
    protected ControlliPerFirme controlliPerFirme;
    @EJB
    protected VerificaFirmaWrapperManager verificaFirmaWrapperManager;

    /**
     * Il metodo attualmente si occupa di verificare la firma e validare il formato. Il metodo è
     * richiamato da
     * {@link DocumentoVersVFirmeHash#elabCompDocMockWithVerificaFirmaAndFormatoResult}
     *
     * @param componenteVers       componente principale
     * @param sottoComponentiFirma lista firme
     * @param sottoComponentiMarca lista di marche temporali
     * @param dataDiRiferimento    data di riferimento per la verifica
     * @param idTipoUd             (opzionale)
     * @param versamento           Informazioni relativi al versamento
     *
     * @return risposta controlli
     */
    public RispostaControlli verifica(ComponenteVers componenteVers,
	    List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
	    ZonedDateTime dataDiRiferimento, long idTipoUd, AbsVersamentoExt versamento) {
	RispostaControlli rs = new RispostaControlli();
	rs.setrLong(-1); // default

	// MEV#18660
	CompDocMock mock = componenteVers.withAcdEntity();
	OrgStrut struttura = null;
	try {
	    struttura = controlliPerFirme.getOrgStruttAsEntity(mock.getIdStrut().longValue());
	} catch (VerificaFirmaException ex) {
	    rs.setrLong(-1);
	    rs.setrBoolean(false);
	    rs.setCodErr(ex.getCodiceErrore());
	    rs.setDsErr(ex.getDescrizioneErrore());
	    return rs; // 666 code
	}
	// default
	InvokeVerificaRule rule = InvokeVerificaRule.defaultRule();

	String flAbilitaContrCrittogVers = configurationHelper.getValoreParamApplicByTipoUdAsFl(
		ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS, struttura.getIdStrut(),
		struttura.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUd);
	final boolean abilitatoControlloCrittografico = flAbilitaContrCrittogVers == null
		|| !flAbilitaContrCrittogVers.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_ABILITA_CONTR_CRITTOG_VERS,
		abilitatoControlloCrittografico);

	String flAbilitaContrTrustVers = configurationHelper.getValoreParamApplicByTipoUdAsFl(
		ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS, struttura.getIdStrut(),
		struttura.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUd);
	final boolean abilitatoControlloCatenaTrust = flAbilitaContrTrustVers == null
		|| !flAbilitaContrTrustVers.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_ABILITA_CONTR_TRUST_VERS,
		abilitatoControlloCatenaTrust);

	String flAbilitaContrCertifVers = configurationHelper.getValoreParamApplicByTipoUdAsFl(
		ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS, struttura.getIdStrut(),
		struttura.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUd);
	final boolean abilitatoControlloCertificato = flAbilitaContrCertifVers == null
		|| !flAbilitaContrCertifVers.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_ABILITA_CONTR_CERTIF_VERS,
		abilitatoControlloCertificato);

	String flAbilitaContrRevocaVers = configurationHelper.getValoreParamApplicByTipoUdAsFl(
		ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS, struttura.getIdStrut(),
		struttura.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUd);
	final boolean abilitatoControlloRevoca = flAbilitaContrRevocaVers == null
		|| !flAbilitaContrRevocaVers.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_ABILITA_CONTR_REVOCA_VERS,
		abilitatoControlloRevoca);

	String flEidasIncludiBase64 = configurationHelper
		.getValoreParamApplicByApplicAsFl(ParametroApplFl.FL_EIDAS_INCLUDI_FILEBASE64);
	final boolean eidasIncludiBase64 = flEidasIncludiBase64 == null
		|| !flEidasIncludiBase64.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_EIDAS_INCLUDI_FILEBASE64, eidasIncludiBase64);

	String flCryptoIncludiBase64 = configurationHelper
		.getValoreParamApplicByApplicAsFl(ParametroApplFl.FL_CRYPTO_INCLUDI_FILEBASE64);
	final boolean cryptoIncludiBase64 = flCryptoIncludiBase64 == null
		|| !flCryptoIncludiBase64.equals("0");
	rule.getAbilitazioni().put(ParametroApplFl.FL_CRYPTO_INCLUDI_FILEBASE64,
		cryptoIncludiBase64);

	// regole per chiamata
	rule.withEseguiVerificaFirmaOnlyCrypto(
		versamento.getStrutturaComponenti().isFlagAbilitaVerificaFirmaSoloCrypto())
		.withFirmaAndMarcaDetached(CollectionUtils.isNotEmpty(sottoComponentiFirma)
			&& CollectionUtils.isNotEmpty(sottoComponentiMarca))
		.withHasMultipleFirmaDetached(CollectionUtils.isNotEmpty(sottoComponentiFirma)
			&& sottoComponentiFirma.size() > 1)
		.withHasMarcaDetached(CollectionUtils.isNotEmpty(sottoComponentiMarca));

	return verifica(componenteVers, sottoComponentiFirma, sottoComponentiMarca,
		dataDiRiferimento, idTipoUd, versamento, rule);
    }

    private RispostaControlli verifica(ComponenteVers componenteVers,
	    List<ComponenteVers> sottoComponentiFirma, List<ComponenteVers> sottoComponentiMarca,
	    ZonedDateTime dataDiRiferimento, long idTipoUd, AbsVersamentoExt versamento,
	    InvokeVerificaRule rule) {
	RispostaControlli rs = new RispostaControlli();
	rs.setrLong(-1); // default
	// calc urn ref (just for logging)
	String tmpUrn = StringUtils.EMPTY;
	// MEV#18660
	CompDocMock mock = componenteVers.withAcdEntity();
	// start time
	final long start = System.currentTimeMillis();
	try {

	    boolean verificaAllaDataDiFirma = mock.getFlRifTempDataFirmaVers() != null
		    && mock.getFlRifTempDataFirmaVers().equals(CostantiDB.Flag.TRUE);
	    // input
	    InvokeVerificaInput in = new InvokeVerificaInput(componenteVers, sottoComponentiFirma,
		    sottoComponentiMarca, dataDiRiferimento, rule.getAbilitazioni(),
		    verificaAllaDataDiFirma);

	    // call ws
	    LOG.debug("Inizio verifica firma");
	    VerificaFirmaWrapper wrapper = verificaFirmaWrapperManager.invokeVerifica(rule, in,
		    versamento);

	    // urn ref (for logging purpose)
	    tmpUrn = MessaggiWSFormat.formattaBaseUrnDoc(
		    versamento.getStrutturaComponenti().getUrnPartVersatore(),
		    versamento.getStrutturaComponenti().getUrnPartChiaveUd(),
		    componenteVers.getUrnPartComponenteNiOrdDoc(),
		    Costanti.UrnFormatter.URN_DOC_FMT_STRING_V2);

	    LOG.info(
		    "Fine verifica firma tipo {} versione libreria v.{} versione servizio v.{} eseguita in {} "
			    + "ms sull'host {} urn {}",
		    wrapper.getAdditionalInfo().getServiceCode(), wrapper.getLibraryVersion(),
		    wrapper.getServiceVersion(), (System.currentTimeMillis() - start),
		    wrapper.getSelfLink(), tmpUrn != null ? tmpUrn : "non calcolabile");

	    // set wrapper on componente e sottocomponenti
	    componenteVers.setVfWrapper(wrapper);
	    if (sottoComponentiFirma != null) {
		sottoComponentiFirma.stream().forEach(sf -> sf.setVfWrapper(wrapper));
	    }
	    if (sottoComponentiMarca != null) {
		sottoComponentiMarca.stream().forEach(sm -> sm.setVfWrapper(wrapper));
	    }

	    // calculate additional infos
	    verificaFirmaWrapperManager.buildAdditionalInfoOnVFWrapper(wrapper);

	    LOG.trace("Verifico il formato dei componente");

	    // Componente principale
	    this.extractComponenteFormatInfo(componenteVers, idTipoUd, wrapper);
	    // Marche detached
	    if (sottoComponentiMarca != null && !sottoComponentiMarca.isEmpty()) {
		for (ComponenteVers sottoComponenteMarca : sottoComponentiMarca) {
		    this.extractSottoComponenteFormatInfo(sottoComponenteMarca, idTipoUd, wrapper);
		}
	    }
	    // Firme detached
	    if (sottoComponentiFirma != null && !sottoComponentiFirma.isEmpty()) {
		for (ComponenteVers firma : sottoComponentiFirma) {
		    this.extractSottoComponenteFormatInfo(firma, idTipoUd, wrapper);
		}
	    }

	    rs.setrLong(0); // OK
	} catch (VerificaFirmaException ex) {
	    LOG.warn("Errore riscontrato durante verifica firma eseguita in {} ms urn {}",
		    (System.currentTimeMillis() - start),
		    tmpUrn != null ? tmpUrn : "non presente/calcolato", ex);
	    rs.setrLong(-1);
	    rs.setrBoolean(false);
	    rs.setCodErr(ex.getCodiceErrore());
	    rs.setDsErr(ex.getDescrizioneErrore());
	}
	return rs;
    }

    private List<VFBustaExt> filterWrapper(String idcomponente, List<VFBusta> busteWrapper) {
	List<VFBustaExt> buste = new ArrayList<>();
	for (VFBusta busta : busteWrapper) {
	    VFBustaExt bustaExt = new VFBustaExt(idcomponente, busta);
	    /**
	     * Restituisce una lista di buste se e solo se per l'idcomponente esistono firme e/o
	     * marche associate
	     */
	    if (!bustaExt.getFirmaComps().isEmpty() || !bustaExt.getMarcaComps().isEmpty()) {
		buste.add(bustaExt);
	    }
	}
	return buste;
    }

    /**
     * Estensione della busta per ricreare il legame tra componente versato ed informazione
     * restituita dal wrapper
     */
    private class VFBustaExt extends VFBusta {

	private String idComponenteVersato;
	private VFBusta bustaWrapper;

	public VFBustaExt(final String idComponenteVersato, final VFBusta bustaWrapper) {
	    this.idComponenteVersato = idComponenteVersato;
	    this.bustaWrapper = bustaWrapper;
	}

	@Override
	public VFAdditionalInfoBustaType getAdditionalInfo() {
	    return bustaWrapper.getAdditionalInfo();
	}

	@Override
	public BigDecimal getPgBusta() {
	    return bustaWrapper.getPgBusta();
	}

	@Override
	public List<VFFirmaCompType> getFirmaComps() {
	    return bustaWrapper.getFirmaComps().stream()
		    .filter(f -> f.getId().equals(idComponenteVersato))
		    .collect(Collectors.toList());
	}

	@Override
	public List<VFMarcaCompType> getMarcaComps() {
	    return bustaWrapper.getMarcaComps().stream()
		    .filter(m -> m.getId().equals(idComponenteVersato))
		    .collect(Collectors.toList());
	}

    }

    /**
     * Estrazione/valutazione risultati verifica formato per il componente principale. A differenza
     * del sottocomponente il tikamime fornito è quello dedotto dal file sbustato in ambito di
     * verifica della firma. Il tika mime, parte dei dati "additional" del wrapper viene verificato
     * secondo due scenari: 1) firma undetached: mimetype rilevato con Tika su unico componente
     * firmato con sbustamento 2) - detached: come sopra ma del componente principale (originale
     * unsigned)
     *
     * @param componenteVers
     * @param idTipoUd
     * @param wrapper
     *
     * @throws VerificaFirmaException
     */
    private void extractComponenteFormatInfo(ComponenteVers componenteVers, long idTipoUd,
	    VerificaFirmaWrapper wrapper) throws VerificaFirmaException {
	extractFormatInfo(componenteVers, idTipoUd, wrapper,
		wrapper.getAdditionalInfo().getTikaMime());
    }

    /**
     * Estrazione/valutazione risultati verifica formato per il sottocomponente (se esiste, di tipo
     * FIRMA / MARCA). A differenza del componente principale il tikamime non viene fornito ma
     * dedotto all'interno della logica.
     *
     * @param componenteVers
     * @param idTipoUd
     * @param wrapper
     *
     * @throws VerificaFirmaException
     */
    private void extractSottoComponenteFormatInfo(ComponenteVers componenteVers, long idTipoUd,
	    VerificaFirmaWrapper wrapper) throws VerificaFirmaException {
	extractFormatInfo(componenteVers, idTipoUd, wrapper, null);
    }

    /**
     *
     * Il metodo decora il componenteVers (che può essere un componente piuttosto che un
     * sottocomponente) con i controlli di formato.
     *
     * @param componenteVers
     * @param idTipoUd
     *
     * @return
     *
     * @throws VerificaFirmaException
     */
    private void extractFormatInfo(ComponenteVers componenteVers, long idTipoUd,
	    VerificaFirmaWrapper wrapper, String tikaMime) throws VerificaFirmaException {
	CompDocMock mock = componenteVers.withAcdEntity();
	OrgStrut struttura = null;
	if (mock.getIdStrut() != null) {
	    struttura = controlliPerFirme.getOrgStruttAsEntity(mock.getIdStrut().longValue());
	}

	StringBuilder formatoRapprEsteso = new StringBuilder();
	StringBuilder formatoRappr = new StringBuilder();
	StringBuilder listaFormatiMimeTypeTika = new StringBuilder();
	HashMap<Integer, String> formatoRapprBuilder = new HashMap<>();
	HashMap<Integer, String> builderMessaggio = new HashMap<>();
	String appendEsitoNeg = "";
	// Cerco il formato file versato (ad es DOC.P7M.P7M o .TSR)
	DecFormatoFileDoc formatoVersato = controlliPerFirme
		.getDecFormatoFileDoc(componenteVers.getIdFormatoFileVers());

	/*
	 * MEV#18660 : info ricavata dal wrapper Ciò significa che la detection del mime-type, verrà
	 * effettuata in fase di VERIFICA e non più come processo a parte
	 */
	// 1- PROCESSO LE BUSTE - Setto i formati delle buste e li aggiungo nel formato
	// di rappresentazione
	for (VFBustaExt busta : filterWrapper(componenteVers.getId(), wrapper.getVFBusta())) {

	    // MEV#18660
	    List<DecFormatoFileStandard> dffs = getFormatiBusta(busta);
	    String nomeFormato = this.buildMessage(dffs);
	    DecFormatoFileStandard formatoStd = this.getFormatoStdDaVersato(dffs,
		    formatoVersato.getNmFormatoFileDoc());
	    if (formatoStd != null) {
		busta.getAdditionalInfo().setIdFormatoFileStandard(
			BigInteger.valueOf(formatoStd.getIdFormatoFileStandard()));
		mock.setIdDecFormatoFileStandard(
			BigDecimal.valueOf(formatoStd.getIdFormatoFileStandard()));
	    }

	    // init default msg
	    formatoRapprBuilder.put(busta.getPgBusta().intValue(), StringUtils.EMPTY);
	    builderMessaggio.put(busta.getPgBusta().intValue(), StringUtils.EMPTY);
	    //
	    if (formatoStd == null) {
		formatoRapprBuilder.put(busta.getPgBusta().intValue(),
			VerificaFormatiEnums.SEPARATORE_FORMATI
				+ VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
		builderMessaggio.put(busta.getPgBusta().intValue(),
			VerificaFormatiEnums.SEPARATORE_FORMATI + nomeFormato);
	    } else if (!formatoStd.getNmFormatoFileStandard()
		    .equals(FormatiStandardFirme.XML.name())
		    && !formatoStd.getNmFormatoFileStandard()
			    .equals(FormatiStandardFirme.PDF.name())) {
		// sono esclusi i formati XML e PDF altrimenti avrei XML.XML o PDF.PDF
		formatoRapprBuilder.put(busta.getPgBusta().intValue(),
			VerificaFormatiEnums.SEPARATORE_FORMATI
				+ formatoStd.getNmFormatoFileStandard());
		builderMessaggio.put(busta.getPgBusta().intValue(),
			VerificaFormatiEnums.SEPARATORE_FORMATI + nomeFormato);
	    } else if (formatoStd.getNmFormatoFileStandard()
		    .equals(FormatiStandardFirme.XML.name())) {
		tikaMime = VerificaFormatiEnums.XML_MIME;
	    } else if (formatoStd.getNmFormatoFileStandard()
		    .equals(FormatiStandardFirme.PDF.name())) {
		tikaMime = VerificaFormatiEnums.PDF_MIME;
	    }
	}
	// Eventuale normalizzazione mime type xml
	tikaMime = normalizeTika(tikaMime);

	// 2- PROCESSO I CONTENUTI
	// 2a- Se non si tratta di un sotto-componente di tipo FIRMA o MARCA
	if (tikaMime != null) {
	    DecFormatoFileStandard formatoStd = null;
	    // se il formato versato è ammesso per il componente...
	    if (!componenteVers.isFormatoFileVersNonAmmesso()) {
		// Calcolo il mimetype del formato standard versato del contenuto del componente
		// (passo 1b).. ottieni il primo
		formatoStd = controlliPerFirme.getDecFmtFileStandardFromFmtDocs(
			componenteVers.getIdFormatoFileVers(), componenteVers.getChiaveComp(),
			formatoVersato.getNmFormatoFileDoc()).get(0);

		builderMessaggio.put(0, formatoStd.getNmFormatoFileStandard());
		// .. confronto il suo mimetype con quello di tika per costruire il formato di
		// rappresentazione e
		if (!tikaMime.equals(formatoStd.getNmMimetypeFile())) {
		    // se NON coincidono ricalcolo il formato std da quello di tika (passo 1d)
		    String mimeTypeVersato = formatoStd.getNmMimetypeFile();
		    // Calcolo il formato standard corrispondente al mimetype tika
		    List<DecFormatoFileStandard> formati = controlliPerFirme
			    .getDecFmtFileStandardFromTikaMimes(tikaMime);

		    formatoStd = this.getFormatoStdDaVersato(formati,
			    formatoVersato.getNmFormatoFileDoc());
		    appendEsitoNeg = " - Il mimetype del formato dichiarato " + "(\""
			    + mimeTypeVersato + "\") non coincide con il mimetype calcolato (\""
			    + tikaMime + "\")";
		    String nomeFormato = this.buildMessage(formati);
		    builderMessaggio.put(0, nomeFormato);
		}
	    } else { // se il formato versato NON è ammesso oppure non è stato dichiarato...
		if (componenteVers.getDescFormatoFileVers() == null
			|| componenteVers.getDescFormatoFileVers().isEmpty()) {
		    appendEsitoNeg += " - Il mimetype calcolato è " + tikaMime;
		} else {
		    // Calcolo il formato standard corrispondente al formato versato
		    Optional<DecFormatoFileStandard> formatoStdDaEstensione = getFormatoStdDaEstensione(
			    componenteVers.getDescFormatoFileVers());
		    if (formatoStdDaEstensione.isPresent()) {
			DecFormatoFileStandard formatoStdVersato = formatoStdDaEstensione.get();
			appendEsitoNeg += " - Il mimetype del formato dichiarato " + "(\""
				+ formatoStdVersato.getNmMimetypeFile() + "\") ";
			if (!tikaMime.equals(formatoStdVersato.getNmMimetypeFile())) {
			    appendEsitoNeg += "non ";
			}
			appendEsitoNeg += "coincide con il mimetype calcolato (\"" + tikaMime
				+ "\")";
		    }
		}
		// Calcolo il formato standard corrispondente al mimetype tika
		List<DecFormatoFileStandard> formati = controlliPerFirme
			.getDecFmtFileStandardFromTikaMimes(tikaMime);
		if (!formati.isEmpty()) {
		    formatoStd = formati.get(0); // get first
		}

		String nomeFormato = this.buildMessage(formati);
		builderMessaggio.put(0, nomeFormato);
	    }

	    // formatoStd può essere null se e solo se il mime tika non è censito tra i
	    // formati standard
	    if (formatoStd == null) {
		formatoRapprBuilder.put(0, VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
	    } else {
		formatoRapprBuilder.put(0, formatoStd.getNmFormatoFileStandard());
		mock.setIdDecFormatoFileStandard(
			new BigDecimal(formatoStd.getIdFormatoFileStandard()));
	    }
	    // Setto nel componente il formato file standard
	    // MEV#18660
	    // costruisco, partendo dalla posizione 0 e poi in senso contrario al numero
	    // di busta (dalla più interna) il formato di rappresentazione esteso e quello
	    // compatto
	    String nthFormat = formatoRapprBuilder.get(0);
	    formatoRappr.append(nthFormat);
	    formatoRapprEsteso.append(nthFormat);
	    listaFormatiMimeTypeTika.append(builderMessaggio.get(0));
	    for (int i = formatoRapprBuilder.size() - 1; i > 0; i--) {
		nthFormat = formatoRapprBuilder.get(i);
		formatoRapprEsteso.append(nthFormat);
		listaFormatiMimeTypeTika.append(builderMessaggio.get(i));
		if (formatoRappr.indexOf(nthFormat) == -1) {
		    formatoRappr.append(nthFormat);
		}
	    }
	} else if (componenteVers.getTipoUso().equals("FIRMA")
		|| componenteVers.getTipoUso().equals("MARCA")) {
	    // 2b- se è un sottocomponente di tipo FIRMA o MARCA setto il formatoStd della
	    // busta nel componente
	    // MEV#18660
	    DecFormatoFileStandard formatoStd = null;
	    if (mock.getIdDecFormatoFileStandard() != null) {
		formatoStd = controlliPerFirme.getDecFormatoFileStandardAsEntity(
			mock.getIdDecFormatoFileStandard().longValue());
		// TOFIX: questo è superfluo dato che è stato eseguito in precedenza ..
		// (ereditato dalla vecchia logica)
		mock.setIdDecFormatoFileStandard(
			new BigDecimal(formatoStd.getIdFormatoFileStandard()));
	    }
	    if (formatoStd != null) {
		formatoRapprEsteso.append(formatoStd.getNmFormatoFileStandard());
		formatoRappr.append(formatoStd.getNmFormatoFileStandard());
		listaFormatiMimeTypeTika.append(formatoStd.getNmFormatoFileStandard());
	    } else {
		formatoRapprEsteso.append(VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
		formatoRappr.append(VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
		listaFormatiMimeTypeTika.append(builderMessaggio.get(1));
	    }
	} else {
	    // 2c - se è un componente che non ha contenuto [ie. caso di marca detached
	    // versata come componente, caso di formato di firma non conforme]
	    // MEV#18660
	    mock.setIdDecFormatoFileStandard(null);
	    formatoRapprBuilder.put(0, VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
	    builderMessaggio.put(0, VerificaFormatiEnums.FORMATO_SCONOSCIUTO);
	    String nthFormat = formatoRapprBuilder.get(0);
	    formatoRappr.append(nthFormat);
	    formatoRapprEsteso.append(nthFormat);
	    listaFormatiMimeTypeTika.append(builderMessaggio.get(0));
	    for (int i = formatoRapprBuilder.size() - 1; i > 0; i--) {
		nthFormat = formatoRapprBuilder.get(i);
		formatoRapprEsteso.append(nthFormat);
		listaFormatiMimeTypeTika.append(builderMessaggio.get(i));
		if (formatoRappr.indexOf(nthFormat) == -1) {
		    formatoRappr.append(nthFormat);
		}
	    }
	}

	LOG.debug("Formato di rappresentazione esteso calcolato: {}", formatoRapprEsteso);
	LOG.debug("Formato di rappresentazione compatto calcolato: {}", formatoRappr);

	// se per la struttura fl_abilita_contr_fmt = false
	if (struttura != null) {
	    String flAbilitaContrFmt = configurationHelper.getValoreParamApplicByTipoUdAsFl(
		    ParametroApplFl.FL_ABILITA_CONTR_FMT, struttura.getIdStrut(),
		    struttura.getOrgEnte().getOrgAmbiente().getIdAmbiente(), idTipoUd);
	    if (flAbilitaContrFmt.equals("0")) {
		mock.setTiEsitoContrFormatoFile(EsitoControlloFormato.DISABILITATO.name());
		mock.setDsMsgEsitoContrFormato(EsitoControlloFormato.DISABILITATO.message());
	    } else {
		// test formato
		boolean formatoCompatibile = testFormati(formatoRappr.toString(),
			formatoVersato.getNmFormatoFileDoc());

		if (formatoCompatibile) {
		    // se per la struttura fl_abilita_contr_fmt = true
		    // e se il formato di rappresentazione compatto coincide con quello versato
		    // (caso 6. appunti controllo formato)
		    // setto l'esito e i valori di verificaCrypto formato in base all'idoneità del
		    // formato del contenuto
		    // MEV#18660
		    DecFormatoFileStandard formatoStd = controlliPerFirme
			    .getDecFormatoFileStandardAsEntity(
				    mock.getIdDecFormatoFileStandard().longValue());

		    if (formatoStd.getTiEsitoContrFormato()
			    .equals(IdoneitaFormato.DEPRECATO.name())) {
			mock.setTiEsitoContrFormatoFile(EsitoControlloFormato.WARNING.name());
			mock.setDsMsgEsitoContrFormato("Il formato "
				+ formatoStd.getNmFormatoFileStandard() + " è deprecato");
		    } else {
			mock.setTiEsitoContrFormatoFile(EsitoControlloFormato.POSITIVO.name());
			mock.setDsMsgEsitoContrFormato(EsitoControlloFormato.POSITIVO.message());
		    }
		} else {
		    // se per la struttura fl_abilita_contr_fmt = true
		    // e se il formato di rappresentazione compatto NON coincide con quello versato
		    // (caso 7. appunti controllo formato)
		    mock.setTiEsitoContrFormatoFile(EsitoControlloFormato.NEGATIVO.name());
		    StringBuilder esitoNegativo = new StringBuilder();
		    esitoNegativo.append("Il formato di rappresentazione");
		    if (formatoRappr.indexOf(VerificaFormatiEnums.FORMATO_SCONOSCIUTO) > -1) {
			esitoNegativo.append(" è sconosciuto");
			if (!formatoRappr.toString()
				.equalsIgnoreCase(listaFormatiMimeTypeTika.toString())
				&& !VerificaFormatiEnums.OCTET_STREAM_MIME
					.equalsIgnoreCase(tikaMime)) {
			    esitoNegativo.append(", può corrispondere a ");
			    esitoNegativo.append(listaFormatiMimeTypeTika);
			}
		    } else {
			esitoNegativo.append(" calcolato è ");
			esitoNegativo.append(formatoRappr);
			if (!formatoRappr.toString()
				.equalsIgnoreCase(listaFormatiMimeTypeTika.toString())
				&& !VerificaFormatiEnums.OCTET_STREAM_MIME
					.equalsIgnoreCase(tikaMime)) {
			    esitoNegativo.append(", corrisponde a ");
			    esitoNegativo.append(listaFormatiMimeTypeTika);
			}
		    }

		    if (!componenteVers.isFormatoFileVersNonAmmesso()) {
			esitoNegativo.append(" ed è diverso da quello versato ");
			esitoNegativo.append(formatoVersato.getNmFormatoFileDoc());
		    } else if (componenteVers.getDescFormatoFileVers() == null
			    || componenteVers.getDescFormatoFileVers().isEmpty()) {
			esitoNegativo
				.append(" e non è stato dichiarato un formato per il versamento ");
		    } else {
			esitoNegativo.append(" ed il formato dichiarato (")
				.append(componenteVers.getDescFormatoFileVers())
				.append(") non è ammesso per il tipo componente ");
		    }
		    // MEV#18660
		    if (tikaMime == null && mock.getIdDecFormatoFileStandard() == null) {
			if (componenteVers.getTipoUso().equals("FIRMA")) {
			    appendEsitoNeg = " - almeno una firma ha formato sconosciuto";
			}
			if (componenteVers.getTipoUso().equals("MARCA")) {
			    appendEsitoNeg = " - il formato della marca è sconosciuto";
			}
		    }
		    mock.setDsMsgEsitoContrFormato(esitoNegativo + appendEsitoNeg);
		}

	    } // struttura
	}
	// in ogni caso setto il formato di rappresentazione esteso e quello compatto
	mock.setDsFormatoRapprEstesoCalc(formatoRapprEsteso.toString());
	mock.setDsFormatoRapprCalc(formatoRappr.toString());
    }

    /**
     * SacerWS accetta il formato <strong>application/xml</strong> mentre, per esempio, EIDAS può
     * restituisce <strong>text/xml</strong>. Aggiungo questa forzatura per poter continuare la
     * verifica dei formati.
     *
     * @param tikaMime mime type ottenuto da tika
     *
     * @return stringa normalizzata (solo nel caso di <strong>text/xml</strong>)
     */
    private String normalizeTika(String tikaMime) {
	if (tikaMime != null && tikaMime.equals("text/xml")) {
	    LOG.debug("Sovrascrittura del mime/type nel caso di text/xml");
	    return "application/xml";
	}
	return tikaMime;
    }

    /**
     * Il metodo setta all'interno di una busta il formato Standard determinamdo i formati standard
     * ammessi per il formato di firma / marca
     *
     * @param busta          Busta crittografica
     * @param tiFormatoMarca Formato della firma / marca
     * @param formatoVersato Formato versato
     */
    private List<DecFormatoFileStandard> getFormatiBusta(VFBusta busta)
	    throws VerificaFirmaException {
	Set<String> tiFormatoFirmaMarca = new HashSet<>();
	for (VFFirmaCompType firma : busta.getFirmaComps()) {
	    tiFormatoFirmaMarca.add(firma.getTiFormatoFirma());

	}
	// per ogni marca
	for (VFMarcaCompType marca : busta.getMarcaComps()) {
	    tiFormatoFirmaMarca.add(marca.getTiFormatoMarca());
	}
	return controlliPerFirme.getDecFmtFileStandardFromFmtMarcas(tiFormatoFirmaMarca);
    }

    /**
     * Partendo da una lista di formatiStd restituisce:
     *
     * <br/>
     * (1) quello coerente al formatoVersato se la lista contiene più di un elemento e questo
     * coincide con una delle estensioni del formato rapp. compatto fornito oppure <br/>
     * (2) uno a caso della lista se la lista contiene più di un elemento e questo NON coincide con
     * una delle estensioni del formato rapp. compatto fornito oppure <br/>
     * (3) se la lista non contiene elementi, rende NULL - che verrà poi gestito come
     * VerFormatiEnums.FORMATO_SCONOSCIUTO e rifiutato anche in caso di forzatura
     *
     * Migliorata l'euristica: nel caso in cui non venga trovato alcun formato viene valutata anche
     * l'estensione successiva alla prima
     *
     * @param formati
     * @param formatoVersato
     *
     * @return
     */
    private DecFormatoFileStandard getFormatoStdDaVersato(List<DecFormatoFileStandard> formati,
	    String formatoVersato) {
	// Formato casuale, predefinito.
	String[] formVers = formatoVersato.split("\\.");
	if (formati.isEmpty()) {
	    return null;
	}
	DecFormatoFileStandard formato = formati.get(0);

	foundFormat: for (DecFormatoFileStandard f : formati) {
	    for (DecEstensioneFile ext : f.getDecEstensioneFiles()) {
		// Miglioro l'euristica; ora utilizza anche le eventuali altre estensioni (i.e.
		// P7M di XML.P7M).
		for (String formaVersItem : formVers) {
		    if (formaVersItem.toUpperCase()
			    .contains(ext.getCdEstensioneFile().toUpperCase())) {
			formato = f;
			break foundFormat;
		    }
		}
	    }

	}
	return formato;
    }

    /**
     * Ottieni il primo formato standard ottenuto filtrando per formatoVersato.
     *
     * @param formatoVersato estensione del file indicata nel sip di versamento.
     *
     * @return il primo formato standard ottenuto (sotto-forma di Optional)
     *
     * @throws VerificaFirmaException in caso di errore bloccante.
     */
    private Optional<DecFormatoFileStandard> getFormatoStdDaEstensione(String formatoVersato)
	    throws VerificaFirmaException {
	DecFormatoFileStandard formato = null;
	if (formatoVersato != null && !formatoVersato.isEmpty()) {
	    String[] formVers = formatoVersato.split("\\.");
	    List<DecFormatoFileStandard> formati = controlliPerFirme
		    .getDecFmtFileStdFromEstensioneFiles(formVers[0].toUpperCase());
	    if (formati != null && !formati.isEmpty()) {
		formato = formati.get(0);
	    }
	}
	return Optional.ofNullable(formato);
    }

    /**
     * Testa il formato di rappresentazione compatto calcolato con quello versato. La verificaCrypto
     * si basa sulle estensioni dei formati standard che compongono il formato di rappresentazione.
     *
     * @param formatoRappr
     * @param formatoVers
     *
     * @return true se i formati coincidono, false altrimenti
     */
    private boolean testFormati(String formatoRappr, String formatoVers)
	    throws VerificaFirmaException {
	if (formatoRappr.equals(formatoVers)) {
	    return true;
	}
	String[] formRappr = formatoRappr.split("\\.");
	String[] formVers = formatoVers.split("\\.");
	if (formRappr.length != formVers.length) {
	    return false;
	}
	for (int i = 0; i < formRappr.length; i++) {
	    if (!formRappr[i].equals(formVers[i])) {
		List<DecEstensioneFile> exts = controlliPerFirme
			.getDecEstensioneFiles(formRappr[i]);
		for (DecEstensioneFile ext : exts) {
		    if (ext.getCdEstensioneFile().equalsIgnoreCase(formVers[i])) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    private String buildMessage(List<DecFormatoFileStandard> res) {
	if (res.isEmpty()) {
	    return VerificaFormatiEnums.FORMATO_SCONOSCIUTO;
	}
	if (res.size() == 1) {
	    return res.get(0).getNmFormatoFileStandard();
	}
	StringBuilder nomeFormato = new StringBuilder("[");
	String sep = "";
	for (DecFormatoFileStandard f : res) {
	    nomeFormato.append(sep);
	    nomeFormato.append(f.getNmFormatoFileStandard());
	    sep = ", ";
	}
	nomeFormato.append("]");
	return nomeFormato.toString();
    }
}
