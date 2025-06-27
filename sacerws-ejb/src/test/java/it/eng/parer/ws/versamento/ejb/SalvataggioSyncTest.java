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

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.xadisk.bridge.proxies.interfaces.XADiskBasicIOOperations;
import org.xadisk.connector.outbound.XADiskConnectionFactory;

import it.eng.ArquillianTestUtils;
import it.eng.parer.firma.ejb.SalvataggioFirmaManager;
import it.eng.parer.firma.ejb.VerificaFirmaReportHelper;
import it.eng.parer.ws.versamento.ejb.oracleBlb.WriteCompBlbOracle;
import it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS;
import it.eng.parer.ws.versamentoTpi.ejb.StatoCreaCartelle;

@ArquillianTest
public class SalvataggioSyncTest {

    @EJB
    private SalvataggioSync salvataggioSync;

    @Test
    void inject_ok() {
	assertNotNull(salvataggioSync);
    }

    @Deployment
    public static Archive<?> createTestArchive() {
	final JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(
		Arrays.asList("it.eng.parer.eidas.model", "it.eng.parer.firma.exception",
			"it.eng.parer.firma.xml", "org.apache.commons.compress.archivers",
			"org.apache.commons.compress.archivers.zip",
			"eu.europa.esig.dss.ws.validation.dto", "it.eng.parer.ws.xml.versReq",
			"it.eng.parer.ws.versamento.ejb.oracleBlb"),
		SalvataggioSyncTest.class, SalvataggioSync.class, SalvataggioCompFS.class,
		XADiskBasicIOOperations.class, XADiskConnectionFactory.class,
		StatoCreaCartelle.class, WriteCompBlbOracle.class, SalvataggioFirmaManager.class,
		ControlliPerFirme.class, VerificaFirmaReportHelper.class,
		ObjectStorageService.class).addPackages(true, "com.amazonaws").addAsResource(
			ArquillianTestUtils.class.getClassLoader().getResource("jboss-ejb3.xml"),
			"META-INF/jboss-ejb3.xml");
	return createEnterpriseArchive(SalvataggioSyncTest.class.getSimpleName(),
		sacerWSJavaArchive, createSacerLogJavaArchive());
    }
    /*
     * "java.lang.IllegalArgumentException: Can not set org.xadisk.connector.outbound.XADiskConnectionFactory field it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS.xadCf to org.xadisk.connector.outbound.XADiskConnectionFactoryImpl"
     * )
     *
     * @Test void salvaDatiVersamento() { }
     *
     * @Test void testSalvaDatiVersamento() { }
     *
     * @Test void salvaWarningAARegistroUd() { }
     *
     * @Test void aggiornaRegistriFiscaliUd() { }
     *
     * @Test void riparaCollegamentiUdNonRisolti() { }
     *
     * @Test void rimuoviUdNonVersate() { }
     *
     * @Test void retrieveVrsVLisXmlUdUrnDaCalcByUd() { }
     *
     * @Test void retrieveVrsVLisXmlDocUrnDaCalcByDoc() { }
     *
     * @Test void retrieveAroUpdUnitaDocByUd() { }
     *
     * @Test void retrieveVrsVLisXmlUpdUrnDaCalcByUpd() { }
     *
     * @Test void rimuoviSessVersDocErrate() { }
     *
     * @Test void salvaDatiWarning() { }
     *
     * @Test void retrieveSerVLisVerserByUpdUd() { }
     *
     * @Test void retrieveFasVLisFascByUpdUd() { }
     *
     * @Test void recuperaProgressivoVersione() { }
     */
}
