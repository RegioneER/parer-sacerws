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

package it.eng.parer.ws.versamentoUpd.ejb.help;

import it.eng.parer.ws.versamento.ejb.LogSessioneSyncTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.Arrays;

import static it.eng.ArquillianTestUtils.*;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SalvataggioPregVersamentoAroHelperTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(
                Arrays.asList("it.eng.parer.ws.xml.versUpdReq", "it.eng.parer.ws.xml.versReq",
                        "it.eng.parer.ws.versamentoUpd.ext", "it.eng.parer.ws.xml.versUpdResp", "it.eng.parerxml.xsd"),
                SalvataggioPregVersamentoAroHelper.class, SalvataggioPregVersamentoAroHelperTest.class,
                it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioUpdVersamentoBaseHelper.class,
                it.eng.parer.ws.ejb.XmlUpdVersCache.class, it.eng.parer.ws.ejb.XmlVersCache.class,
                it.eng.parer.util.ejb.AppServerInstance.class,
                it.eng.parer.ws.versamentoUpd.ejb.help.LogSessioneUpdVersamentoHelper.class,
                it.eng.parer.ws.versamentoUpd.ejb.help.LogSessioneUpdVersamentoHelper.class,
                it.eng.parer.ws.ejb.ControlliSemantici.class, it.eng.parer.util.ejb.help.ConfigurationHelper.class,
                it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM.class)
                        .addAsResource(
                                LogSessioneSyncTest.class.getClassLoader()
                                        .getResource("WSRequestAggiornamentoVersamento_1.5.xsd"),
                                "/it/eng/parer/ws/xml/versAggiornamentoReq/WSRequestAggiornamentoVersamento_1.5.xsd")
                        .addAsResource(
                                LogSessioneSyncTest.class.getClassLoader()
                                        .getResource("WSResponseAggiornamentoVersamento_1.5.xsd"),
                                "it/eng/parer/ws/xml/versAggiornamentoResp/WSResponseAggiornamentoVersamento_1.5.xsd");
        return createEnterpriseArchive(SalvataggioPregVersamentoAroHelperTest.class.getSimpleName(), sacerWSJavaArchive,
                createSacerLogJavaArchive());
    }

    @Test
    public void todo() {
        assertTrue(true);
    }

    @EJB
    private SalvataggioPregVersamentoAroHelper helper;

    @Test
    public void retrieveVrsVLisXmlDocUrnDaCalcByDoc() {

        helper.retrieveVrsVLisXmlDocUrnDaCalcByDoc(0L);
        assertTrue(true);
    }
}
