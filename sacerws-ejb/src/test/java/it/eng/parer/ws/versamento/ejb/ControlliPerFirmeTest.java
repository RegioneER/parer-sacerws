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

package it.eng.parer.ws.versamento.ejb;

import it.eng.parer.entity.*;
import it.eng.parer.entity.constraint.DecServizioVerificaCompDoc;
import it.eng.parer.firma.exception.VerificaFirmaException;
import it.eng.parer.ws.dto.RispostaControlli;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static it.eng.ArquillianTestUtils.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
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
    public void getOrgStrutt_queryIsOk() {
        final RispostaControlli rispostaControlli = helper.getOrgStrutt(2L);
        assertNotNull(rispostaControlli.getrObject());
    }

    @Test
    public void getOrgStruttAsEntity_queryIsOk() throws VerificaFirmaException {
        helper.getOrgStruttAsEntity(0L);
        assertTrue(true);
    }

    @Test
    public void getDecFormatoFileStandardAsEntity_queryIsOk() throws VerificaFirmaException {
        helper.getDecFormatoFileStandardAsEntity(0L);
        assertTrue(true);
    }

    @Test
    public void confrontaFormati_queryIsOk() {
        final RispostaControlli rispostaControlli = helper.confrontaFormati(ID_FORMATO_STD_PDF, ID_FORMATO_DOC_PDF);
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    public void getDecEstensioneFiles_queryIsOk() throws VerificaFirmaException {
        helper.getDecEstensioneFiles("PDF");
        assertTrue(true);
    }

    @Test
    public void getDecFmtFileStdFromEstensioneFiles_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStdFromEstensioneFiles("PDF");
        assertTrue(true);
    }

    @Test
    public void getDecFmtFileStandardFromTikaMimes_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStandardFromTikaMimes("application/pdf");
        assertTrue(true);
    }

    @Test
    public void getDecFmtFileStandardFromFmtMarcas_queryIsOk() throws VerificaFirmaException {
        final HashSet<String> tiFormatoFirmaMarca = new HashSet<>();
        tiFormatoFirmaMarca.add("pades");
        helper.getDecFmtFileStandardFromFmtMarcas(tiFormatoFirmaMarca);
        assertTrue(true);
    }

    @Test
    public void getDecFmtFileStandardFromFmtDocs_queryIsOk() throws VerificaFirmaException {
        helper.getDecFmtFileStandardFromFmtDocs(ID_FORMATO_DOC_PDF, "chiaveComp", "nmFormatoFileDoc");
        assertTrue(true);
    }

    @Test
    public void getDecFormatoFileDoc_queryIsOk() throws VerificaFirmaException {
        helper.getDecFormatoFileDoc(0L);
        assertTrue(true);
    }

    @Test
    public void getFirCertifCa_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifCa(BigDecimal.ZERO, "dlDnIssuerCertifCa");
        assertTrue(true);
    }

    @Test
    public void getFirCrl_queryIsOk() throws VerificaFirmaException {
        final FirCertifCa firCertifCa = aFirCertifCa();
        final BigDecimal niSerialCrl = BigDecimal.ZERO;
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
    public void getFirCertifOcsp_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifOcsp(aFirCertifCa(), BigDecimal.ZERO);
        assertTrue(true);
    }

    @Test
    public void getFirOcsp_queryIsOk() throws VerificaFirmaException {
        final FirCertifOcsp firCertifOcsp = new FirCertifOcsp();
        firCertifOcsp.setIdCertifOcsp(0l);
        helper.getFirOcsp(firCertifOcsp, "dsCertifIssuerName", "dsCertifSerialBase64", "dsCertifSkiBase64");
        assertTrue(true);
    }

    @Test
    public void getFirCertifFirmatario_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifFirmatario(aFirCertifCa(), BigDecimal.ZERO);
        assertTrue(true);
    }

    @Test
    public void getFirCertifFirmatarioIds_queryIsOk() throws VerificaFirmaException {
        helper.getFirCertifFirmatarioIds(BigDecimal.ZERO, aFirCertifCa());
        assertTrue(true);
    }

    @Test
    public void getDecServizioVerificaCompDoc_queryIsOk() throws VerificaFirmaException {
        for (DecServizioVerificaCompDoc.CdServizioVerificaCompDoc cdService : DecServizioVerificaCompDoc.CdServizioVerificaCompDoc
                .values()) {
            helper.getDecServizioVerificaCompDoc(cdService.name(), "cdLibrary");
        }
        assertTrue(true);
    }

    @Test
    public void retrieveOrgEnteFor_queryIsOk() {
        final OrgStrut os = new OrgStrut();
        os.setIdStrut(0L);
        os.setOrgEnte(new OrgEnte());
        os.getOrgEnte().setIdEnte(0l);
        helper.retrieveOrgEnteFor(os);
        assertTrue(true);
    }
}
