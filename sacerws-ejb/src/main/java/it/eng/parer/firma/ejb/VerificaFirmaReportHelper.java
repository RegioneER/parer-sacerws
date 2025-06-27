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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.firma.ejb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.namespace.QName;

import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import it.eng.parer.eidas.model.EidasWSReportsDTOTree;
import it.eng.parer.entity.DecReportServizioVerificaCompDoc;
import it.eng.parer.entity.DecServizioVerificaCompDoc;
import it.eng.parer.entity.constraint.DecReportServizioVerificaCompDoc.TiReportServizioVerificaCompDoc;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.firma.util.EidasUtils.DetailedReportFacedUnsecure;
import it.eng.parer.firma.util.EidasUtils.DiagnosticReportFacedUnsecure;
import it.eng.parer.firma.util.EidasUtils.SimpleReportFacedUnsecure;
import it.eng.parer.firma.xml.VerificaFirmaWrapper;
import it.eng.parer.ws.utils.Costanti;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.utils.MessaggiWSBundle;
import it.eng.parer.ws.utils.MessaggiWSFormat;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Marshaller;

/*
 * EJB di supporto per la gestione del report di verifica firma in formato ZIP.
 *
 */
@Stateless(mappedName = "VerificaFirmaReportHelper")
@LocalBean
public class VerificaFirmaReportHelper {

    private static final Logger LOG = LoggerFactory.getLogger(VerificaFirmaReportHelper.class);

    private static final String DIRECTORY_REPORT = "FileOriginali";
    private static final String DIRECTORY_XSLT = "FileTrasformatori";

    // The ZipArchiveEntry.setXxxTime() methods write the time taking into account
    // the local time zone,
    // so we must first convert the desired timestamp value in the local time zone
    // to have the
    // same timestamps in the ZIP file when the project is built on another computer
    // in a
    // different time zone.
    private static final long DEFAULT_ZIP_TIMESTAMP = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0)
	    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    /**
     * Gestione report verifica firma eidas
     *
     * @param reportZip                     report verifica firma (zip)
     * @param servizioVerifica              servizio verifica firma
     * @param reportServizioVerificaCompDoc report servizio verifica firma su componente
     * @param dto                           verifia firma eidas response (dto)
     * @param compUrnNorm                   urn componente
     *
     * @throws IOException            eccezione generica
     * @throws VerificaFirmaException eccezione generica
     */
    public void manageEidasReport(Path reportZip, DecServizioVerificaCompDoc servizioVerifica,
	    List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDoc,
	    EidasWSReportsDTOTree dto, String compUrnNorm)
	    throws IOException, VerificaFirmaException {
	//
	Path tmpBaseEidasReportDir = null;

	try (FileOutputStream outStream = new FileOutputStream(reportZip.toFile());
		ZipArchiveOutputStream tmpZipOutputStream = new ZipArchiveOutputStream(outStream)) {

	    // 1. creare zip con i report
	    // si utilizza per ogni xml generato una naming convention
	    // TOFIX: da rivedere
	    tmpBaseEidasReportDir = Files.createTempDirectory("eidasreport");

	    /*
	     * Attraverso questa logica è possibile generare lo zip sulla base delle versioni del
	     * servizio. Se una versione è decodificata su banca dati ma non su codice, il report
	     * non verrà generato
	     */
	    if (CostantiDB.ReportvfZipVersion
		    .getByServiceAndVersion(servizioVerifica.getCdServizio().name(),
			    servizioVerifica.getNmVersione())
		    .equals(CostantiDB.ReportvfZipVersion.V_10)) {
		/*
		 * default per tutte le versioni censite in essere (in caso di particolarità
		 * occorrerà aggiungere il relativo if oppure gestire con switch/case)
		 */
		elabReport10EidasAsZip(tmpBaseEidasReportDir, tmpZipOutputStream, dto, compUrnNorm,
			reportServizioVerificaCompDoc, BigDecimal.ONE/* default 1 busta */);
		// add XSLT
		addXslt10EidasOnZip(tmpBaseEidasReportDir, tmpZipOutputStream,
			reportServizioVerificaCompDoc);
	    }

	} finally {
	    // delete recursively report dir with files
	    if (tmpBaseEidasReportDir != null) {
		try {
		    FileUtils.deleteDirectory(tmpBaseEidasReportDir.toFile());
		} catch (IOException e) {
		    LOG.warn(
			    "SalvataggioFirmaManager.manageEidasReport la directory {} non è stata correttamente cancellata",
			    tmpBaseEidasReportDir.toFile().getName());
		}
	    }
	}
    }

    /**
     * Gestione report verifica firma crypto
     *
     * @param reportZip        report verifica firma (zip)
     * @param wrapper          wrapper standard verifica firma (come da modello xsd)
     * @param servizioVerifica servizio verifica firma
     * @param compUrnNorm      urn componente
     *
     * @throws IOException            eccezione generica
     * @throws VerificaFirmaException eccezione generica
     */
    public void manageCryptoReport(Path reportZip, VerificaFirmaWrapper wrapper,
	    DecServizioVerificaCompDoc servizioVerifica, String compUrnNorm)
	    throws IOException, VerificaFirmaException {
	//
	Path tmpBaseCryptoReportDir = null;
	try (FileOutputStream outStream = new FileOutputStream(reportZip.toFile());
		ZipArchiveOutputStream tmpZipOutputStream = new ZipArchiveOutputStream(outStream)) {

	    // TOFIX: da rivedere
	    tmpBaseCryptoReportDir = Files.createTempDirectory("cryptoreport");
	    /*
	     * Attraverso questa logica è possibile generare lo zip sulla base delle versioni del
	     * servizio. Se una versione è decodificata su banca dati ma non su codice, il report
	     * non verrà generato
	     */
	    if (CostantiDB.ReportvfZipVersion
		    .getByServiceAndVersion(servizioVerifica.getCdServizio().name(),
			    servizioVerifica.getNmVersione())
		    .equals(CostantiDB.ReportvfZipVersion.V_10)) {
		/*
		 * default per tutte le versioni censite in essere (in caso di particolarità
		 * occorrerà aggiungere il relativo if oppure gestire con switch/case)
		 */
		elabReport10CryptoAsZip(tmpBaseCryptoReportDir, tmpZipOutputStream, wrapper,
			servizioVerifica, compUrnNorm);
		addXslt10CryptoOnZip(tmpBaseCryptoReportDir, tmpZipOutputStream, servizioVerifica);
	    }
	} finally {
	    // delete recursively report dir with files
	    if (tmpBaseCryptoReportDir != null) {
		try {
		    FileUtils.deleteDirectory(tmpBaseCryptoReportDir.toFile());
		} catch (IOException e) {
		    LOG.warn(
			    "SalvataggioFirmaManager.manageCryptoReport la directory {} non è stata correttamente cancellata",
			    tmpBaseCryptoReportDir.toFile().getName());
		}
	    }
	}
    }

    private void elabReport10CryptoAsZip(Path tmpBaseCryptoReportDir,
	    ZipArchiveOutputStream tmpZipOutputStream, VerificaFirmaWrapper wrapper,
	    DecServizioVerificaCompDoc servizioFirma, String compUrnNorm)
	    throws VerificaFirmaException, IOException {
	// per ogni tipo report previsto .... si crea il relativo FirReport
	for (DecReportServizioVerificaCompDoc report : servizioFirma
		.getDecReportServizioVerificaCompDocs()) {
	    //
	    File cryptoReportXml = File.createTempFile("cryptoreport_" + report.getTiReport(),
		    ".tmp", tmpBaseCryptoReportDir.toFile());
	    try (BufferedOutputStream bwriter = new BufferedOutputStream(
		    new FileOutputStream(cryptoReportXml))) {
		//
		marshalReport(wrapper.getAdditionalInfo().getReportContent(), bwriter, true);
	    } catch (JAXBException | IOException ex) {
		throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
			MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
				"SalvataggioFirmaManager.elabReport01CryptoAsZip response di "
					+ servizioFirma.getCdServizio() + ": "
					+ ExceptionUtils.getMessage(ex)),
			ex);
	    }
	    // generate final file name
	    String reportFilename = generateReportFileName(report.getTiReport().name(), compUrnNorm,
		    BigDecimal.ZERO.longValue()/* crypto ha un solo report per tutte le buste */);
	    // add entry on zip
	    addEntryOnZip(tmpZipOutputStream, cryptoReportXml.toPath(),
		    DIRECTORY_REPORT + "/" + reportFilename + ".xml");
	} // for
    }

    private void addXslt10CryptoOnZip(Path tmpBaseCryptoReportDir,
	    ZipArchiveOutputStream tmpZipOutputStream, DecServizioVerificaCompDoc servizioFirma)
	    throws IOException {
	// per ogni tipo report previsto .... si crea il relativo FirReport
	for (DecReportServizioVerificaCompDoc report : servizioFirma
		.getDecReportServizioVerificaCompDocs()) {
	    File cryptoReportXslt = File.createTempFile("cryptoxslt_" + report.getTiReport(),
		    ".tmp", tmpBaseCryptoReportDir.toFile());

	    // write content on file
	    FileUtils.writeStringToFile(cryptoReportXslt, report.getBlXsltReport(),
		    StandardCharsets.UTF_8);
	    // add entry on zip (xlst)
	    addEntryOnZip(tmpZipOutputStream, cryptoReportXslt.toPath(),
		    DIRECTORY_XSLT + "/" + report.getTiReport() + ".xslt");
	}
    }

    private void elabReport10EidasAsZip(Path tmpBaseEidasReportDir,
	    ZipArchiveOutputStream tmpZipOutputStream, EidasWSReportsDTOTree dto,
	    String compUrnNorm,
	    List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDoc,
	    BigDecimal pgBusta) throws VerificaFirmaException, IOException {
	// root element
	if (!dto.isParent()) {
	    createReportEidasAsFileAndAddOnZip(tmpBaseEidasReportDir, tmpZipOutputStream,
		    dto.getReport(), compUrnNorm, pgBusta, reportServizioVerificaCompDoc);
	}
	// per ogni figlio (signed)
	for (EidasWSReportsDTOTree child : dto.getChildren()) {
	    if (child.isUnsigned() /* ultimo livello documento non firmato */) {
		break;
	    }
	    // add one on progressive
	    pgBusta = pgBusta.add(BigDecimal.ONE);
	    //
	    createReportEidasAsFileAndAddOnZip(tmpBaseEidasReportDir, tmpZipOutputStream,
		    child.getReport(), compUrnNorm, pgBusta, reportServizioVerificaCompDoc);

	    if (child.isParent() && child.getChildren() != null && !child.getChildren().isEmpty()) {
		elabReport10EidasAsZip(tmpBaseEidasReportDir, tmpZipOutputStream, child,
			compUrnNorm, reportServizioVerificaCompDoc, pgBusta);
	    }
	}
    }

    private void addXslt10EidasOnZip(Path tmpBaseEidasReportDir,
	    ZipArchiveOutputStream tmpZipOutputStream,
	    List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDoc)
	    throws IOException {
	// per ogni tipo report previsto .... si crea il relativo FirReport
	for (DecReportServizioVerificaCompDoc report : reportServizioVerificaCompDoc) {
	    File eidasReportXslt = File.createTempFile("eidasxslt_" + report.getTiReport(), ".tmp",
		    tmpBaseEidasReportDir.toFile());

	    // write content on file
	    FileUtils.writeStringToFile(eidasReportXslt, report.getBlXsltReport(),
		    StandardCharsets.UTF_8);
	    // add entry on zip (xlst)
	    addEntryOnZip(tmpZipOutputStream, eidasReportXslt.toPath(),
		    DIRECTORY_XSLT + "/" + report.getTiReport() + ".xslt");
	}
    }

    private void createReportEidasAsFileAndAddOnZip(Path tmpBaseEidasXmlReportDir,
	    ZipArchiveOutputStream tmpZipOutputStream, WSReportsDTO wsReportsDTO,
	    String compUrnNorm, BigDecimal pgBusta,
	    List<DecReportServizioVerificaCompDoc> reportServizioVerificaCompDoc)
	    throws VerificaFirmaException, IOException {

	// per ogni tipo report previsto .... si crea il relativo FirReport
	for (DecReportServizioVerificaCompDoc report : reportServizioVerificaCompDoc) {

	    // al fine di riportare su db il report EIDAS per tipo occorre
	    // una logica "cablata"
	    // nota: in caso di nuovi tipi report si renderà necessario un intervento su
	    // questo
	    // blocco di codice
	    File eidasReportXml = File.createTempFile("eidasreport_" + report.getTiReport(), ".tmp",
		    tmpBaseEidasXmlReportDir.toFile());

	    try (BufferedOutputStream bwriter = new BufferedOutputStream(
		    new FileOutputStream(eidasReportXml))) {
		//
		switch (report.getTiReport()) {
		case SIMPLE:
		    SimpleReportFacedUnsecure.newUnsecureFacade()
			    .marshall(wsReportsDTO.getSimpleReport(), bwriter);
		    break;
		case DETAILED:
		    DetailedReportFacedUnsecure.newUnsecureFacade()
			    .marshall(wsReportsDTO.getDetailedReport(), bwriter);
		    break;
		case DIAG_DATA:
		    DiagnosticReportFacedUnsecure.newUnsecureFacade()
			    .marshall(wsReportsDTO.getDiagnosticData(), bwriter);
		    break;
		default:
		    /*
		     * report ORIGINALE individuato con il typo ORIG = riporto l'intero oggetto
		     * EidasWSReportsDTOTree
		     */
		    marshalReport(wsReportsDTO, bwriter, true);
		    break;
		}
	    } catch (JAXBException | SAXException ex) {
		throw new VerificaFirmaException(MessaggiWSBundle.ERR_666,
			MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
				"SalvataggioFirmaManager.createReportEidasAsFileAndAddOnZip errore generico durante elaborazione file report XML "
					+ report.getTiReport() + ": "
					+ ExceptionUtils.getMessage(ex)),
			ex);
	    }
	    // generate final file name
	    String reportFilename = generateReportFileName(report.getTiReport().name(), compUrnNorm,
		    pgBusta.longValue());
	    // add entry on zip (report)
	    addEntryOnZip(tmpZipOutputStream, eidasReportXml.toPath(),
		    DIRECTORY_REPORT + "/" + reportFilename + ".xml");
	} // for
    }

    /*
     * Per la generazione del nome da associare ai singoli file XML "vige" una stringente
     * convenzione utilizzata anche da chi dovrà poi "parsare" lo ZIP ed effettuarne quindi lettura
     * e trasformazione
     */
    private String generateReportFileName(String tiReport, String urnBaseNorm, long pgBusta) {
	// generare baseUrn for file
	final String baseUrn = generateUrnReportFile(tiReport, urnBaseNorm, pgBusta);
	// file name
	return tiReport.concat("_").concat(baseUrn);
    }

    /*
     * Elab/Calc URN single report file (NORMALIZZATO per file)
     */
    private String generateUrnReportFile(String tiReport, String urnBaseNorm, long pgBusta) {
	String fmt = null;
	switch (TiReportServizioVerificaCompDoc.valueOf(tiReport)) {
	case SIMPLE:
	    fmt = Costanti.UrnFormatter.URN_REPORT_SIMPLE_FMT_STRING;
	    break;
	case DETAILED:
	    fmt = Costanti.UrnFormatter.URN_REPORT_DETAILED_FMT_STRING;
	    break;
	case DIAG_DATA:
	    fmt = Costanti.UrnFormatter.URN_REPORT_DIAG_DATA_FMT_STRING;
	    break;
	case CRYPTO:
	    fmt = Costanti.UrnFormatter.URN_REPORT_CRYPTO_FMT_STRING;
	    break;
	default:
	    fmt = Costanti.UrnFormatter.URN_REPORT_ORIG_FMT_STRING;
	    break;
	}

	return MessaggiWSFormat.formattaUrnReportVerificaFirma(urnBaseNorm, pgBusta, fmt);
    }

    private void addEntryOnZip(ZipArchiveOutputStream tmpZipOutputStream, Path tmpReportGen,
	    String name) throws IOException {
	ZipArchiveEntry zae = new ZipArchiveEntry(name);
	filterZipEntry(zae);
	tmpZipOutputStream.putArchiveEntry(zae);

	try (FileInputStream fis = new FileInputStream(tmpReportGen.toFile())) {
	    IOUtils.copy(fis, tmpZipOutputStream);
	    tmpZipOutputStream.closeArchiveEntry();
	}
    }

    private ZipArchiveEntry filterZipEntry(ZipArchiveEntry entry) {
	// Set times
	entry.setCreationTime(FileTime.fromMillis(DEFAULT_ZIP_TIMESTAMP));
	entry.setLastAccessTime(FileTime.fromMillis(DEFAULT_ZIP_TIMESTAMP));
	entry.setLastModifiedTime(FileTime.fromMillis(DEFAULT_ZIP_TIMESTAMP));
	entry.setTime(DEFAULT_ZIP_TIMESTAMP);
	// Remove extended timestamps
	for (ZipExtraField field : entry.getExtraFields()) {
	    if (field instanceof X5455_ExtendedTimestamp) {
		entry.removeExtraField(field.getHeaderId());
	    }
	}
	return entry;
    }

    @SuppressWarnings({
	    "rawtypes", "unchecked" })
    private void marshalReport(Object response, BufferedOutputStream bwriter, boolean prettyPrint)
	    throws JAXBException {
	JAXBContext jc = JAXBContext.newInstance(response.getClass());
	Marshaller tmpGenericMarshaller = jc.createMarshaller();
	tmpGenericMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);
	JAXBIntrospector introspector = jc.createJAXBIntrospector();
	if (null == introspector.getElementName(response)) {
	    QName qName = new QName(response.getClass().getCanonicalName(),
		    response.getClass().getSimpleName());
	    JAXBElement root = new JAXBElement(qName, response.getClass(), response);
	    tmpGenericMarshaller.marshal(root, bwriter);
	} else {
	    tmpGenericMarshaller.marshal(response, bwriter);
	}
    }

}
