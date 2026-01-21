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

package it.eng.parer.ws.versamento.ejb;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.ejb.EJB;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.entity.FirCertifCa;
import it.eng.parer.entity.FirCertifOcsp;
import it.eng.parer.entity.OrgEnte;
import it.eng.parer.entity.OrgStrut;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.ws.dto.RispostaControlli;

@ArquillianTest
public class ControlliPerFirmeTest {
    public static final long ID_FORMATO_DOC_PDF = 1L;
    public static final long ID_FORMATO_STD_PDF = 1L;
    @EJB
    private ControlliPerFirme helper;

    @Deployment
    public static Archive<?> createTestArchive_queryIsOk() {
        return createEnterpriseArchive(ControlliPerFirmeTest.class.getSimpleName(),
                createSacerWSJavaArchive(
                        Arrays.asList("it.eng.parer.firma.exception", "com.fasterxml.uuid.impl",
                                "org.apache.commons.text", "org.apache.commons.text.translate"),
                        ControlliPerFirme.class, ControlliPerFirmeTest.class),
                createSacerLogJavaArchive());
    }

    @Test
    void getOrgStruttAsEntity_queryIsOk() throws VerificaFirmaException {
        helper.getOrgStruttAsEntity(0L);
        assertTrue(true);
    }

    @Test
    void getDecFormatoFileStandardAsEntity_queryIsOk() throws VerificaFirmaException {
        helper.getDecFormatoFileStandardAsEntity(0L);
        assertTrue(true);
    }

    @Test
    void confrontaFormati_queryIsOk() {
        final RispostaControlli rispostaControlli = helper.confrontaFormati(ID_FORMATO_STD_PDF,
                ID_FORMATO_DOC_PDF);
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    void getDecEstensioneFiles_queryIsOk() throws VerificaFirmaException {
        helper.getDecEstensioneFiles("PDF");
        assertTrue(true);
    }

    @Test
    void getDecFmtFileStdFromEstensioneFiles_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStdFromEstensioneFiles("PDF");
        assertTrue(true);
    }

    @Test
    void getDecFmtFileStandardFromTikaMimes_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStandardFromTikaMimes("application/pdf");
        assertTrue(true);
    }

    @Test
    void getDecFmtFileStandardFromFmtMarcas_queryIsOk() throws VerificaFirmaException {
        final HashSet<String> tiFormatoFirmaMarca = new HashSet<>();
        tiFormatoFirmaMarca.add("pades");
        helper.getDecFmtFileStandardFromFmtMarcas(tiFormatoFirmaMarca);
        assertTrue(true);
    }

    @Test
    void getDecFmtFileStandardFromFmtDocs_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStandardFromFmtDocs(ID_FORMATO_DOC_PDF, "chiaveComp",
                "nmFormatoFileDoc");
        assertTrue(true);
    }

    @Test
    void getDecFormatoFileDoc_queryIsOk() throws VerificaFirmaException {
        helper.getDecFormatoFileDoc(0L);
        assertTrue(true);
    }

    @Test
    void getFirCertifCa_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifCa(StringUtils.EMPTY, "dlDnIssuerCertifCa");
        assertTrue(true);
    }

    @Test
    void getFirCrl_queryIsOk() throws VerificaFirmaException {
        final FirCertifCa firCertifCa = aFirCertifCa();
        final String niSerialCrl = StringUtils.EMPTY;
        final Date dtIniCrl = new Date();
        final Date dtScadCrl = new Date();
        helper.getFirCrl(firCertifCa, niSerialCrl, dtIniCrl, dtScadCrl);
        assertTrue(true);
    }

    private FirCertifCa aFirCertifCa() {
        final FirCertifCa firCertifCa = new FirCertifCa();
        firCertifCa.setIdCertifCa(0L);
        return firCertifCa;
    }

    @Test
    void getFirCertifOcsp_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifOcsp(aFirCertifCa(), StringUtils.EMPTY);
        assertTrue(true);
    }

    @Test
    void getFirOcsp_queryIsOk() throws VerificaFirmaException {
        final FirCertifOcsp firCertifOcsp = new FirCertifOcsp();
        firCertifOcsp.setIdCertifOcsp(0l);
        helper.getFirOcsp(firCertifOcsp, "dsCertifIssuerName", "dsCertifSerialBase64",
                "dsCertifSkiBase64");
        assertTrue(true);
    }

    @Test
    void getFirCertifFirmatario_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifFirmatario(aFirCertifCa(), StringUtils.EMPTY);
        assertTrue(true);
    }

    @Test
    void getFirCertifFirmatarioIds_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifFirmatarioIds(BigDecimal.ZERO, aFirCertifCa());
        assertTrue(true);
    }

    @Test
    void getDecServizioVerificaCompDoc_queryIsOk() throws VerificaFirmaException {
        for (DecServizioVerificaCompDoc.CdServizioVerificaCompDoc cdService : DecServizioVerificaCompDoc.CdServizioVerificaCompDoc
                .values()) {
            helper.getDecServizioVerificaCompDoc(cdService.name(), "cdLibrary");
        }
        assertTrue(true);
    }

}
