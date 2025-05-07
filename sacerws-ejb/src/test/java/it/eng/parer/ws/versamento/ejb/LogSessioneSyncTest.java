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

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;

@ArquillianTest
public class LogSessioneSyncTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(LogSessioneSyncTest.class.getSimpleName(),
                createSacerWSJavaArchive(
                        Arrays.asList("it.eng.parer.ws.versamento.ejb.oracleBlb", "it.eng.parerxml.xsd",
                                "it.eng.spagoLite.security.exception", "it.eng.spagoLite.security",
                                "it.eng.parer.idpjaas.logutils", "org.apache.commons.io.output"),
                        LogSessioneSyncTest.class, LogSessioneSync.class, it.eng.parer.ws.ejb.ControlliSemantici.class,
                        it.eng.parer.util.ejb.AppServerInstance.class, org.apache.commons.io.IOUtils.class,
                        it.eng.parer.ws.ejb.XmlVersCache.class)
                                .addPackages(true, "it.eng.parer.ws.xml")
                                .addAsResource(
                                        LogSessioneSyncTest.class.getClassLoader()
                                                .getResource("WSRequestVersamento.xsd"),
                                        "/it/eng/parer/ws/xml/versReq/WSRequestVersamento.xsd")
                                .addAsResource(
                                        LogSessioneSyncTest.class.getClassLoader()
                                                .getResource("WSResponseVersamento.xsd"),
                                        "/it/eng/parer/ws/xml/versResp/WSResponseVersamento.xsd"),
                createSacerLogJavaArchive());
    }

    @EJB
    private LogSessioneSync logSessioneSync;

    /*
     * @Test public void getVrsDocNonVers() { logSessioneSync.getVrsDocNonVers(0l, "tipoRegistro", BigDecimal.ZERO,
     * "numero", "idDocumento"); assertTrue(true); }
     *
     * @Test public void getVrsUnitaDocNonVers() { logSessioneSync.getVrsUnitaDocNonVers(0l, "tipoRegistro",
     * BigDecimal.ZERO, "numero"); assertTrue(true); }
     */
}
