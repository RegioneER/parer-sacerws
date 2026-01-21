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

package it.eng.parer.ws.versamentoUpd.ejb.help;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.utils.CostantiDB;
import it.eng.parer.ws.versamento.ejb.LogSessioneSyncTest;

@ArquillianTest
public class SalvataggioUpdVersamentoAroHelperTest {

    @EJB
    private SalvataggioUpdVersamentoAroHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
        final JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(
                Arrays.asList("it.eng.parer.ws.versamentoUpd.ext",
                        "it.eng.parer.ws.versamentoUpd.ext", "it.eng.parer.ws.xml.versUpdReq",
                        "it.eng.parer.ws.xml.versUpdResp", "it.eng.parer.ws.xml.versReq",
                        "it.eng.parer.ws.xml.versReqMultiMedia", "it.eng.parer.ws.xml.versResp"),
                SalvataggioUpdVersamentoAroHelper.class,
                SalvataggioUpdVersamentoAroHelperTest.class,
                it.eng.parer.ws.versamentoUpd.ejb.help.SalvataggioUpdVersamentoBaseHelper.class,
                it.eng.parer.ws.ejb.XmlUpdVersCache.class, it.eng.parer.ws.ejb.XmlVersCache.class,
                it.eng.parer.util.ejb.AppServerInstance.class,
                it.eng.parer.ws.versamentoUpd.ejb.help.LogSessioneUpdVersamentoHelper.class,
                it.eng.parer.ws.ejb.ControlliSemantici.class,
                it.eng.parer.util.ejb.help.ConfigurationHelper.class,
                it.eng.parerxml.xsd.FileXSD.class, it.eng.parerxml.xsd.FileXSDUtil.class,
                it.eng.parer.util.DateUtilsConverter.class,
                it.eng.parer.ws.xml.versReqMultiMedia.IndiceMM.class)
                .addAsResource(
                        LogSessioneSyncTest.class.getClassLoader()
                                .getResource("WSRequestAggiornamentoVersamento_1.5.xsd"),
                        "/it/eng/parer/ws/xml/versAggiornamentoReq/WSRequestAggiornamentoVersamento_1.5.xsd")
                .addAsResource(
                        LogSessioneSyncTest.class.getClassLoader()
                                .getResource("WSResponseAggiornamentoVersamento_1.5.xsd"),
                        "it/eng/parer/ws/xml/versAggiornamentoResp/WSResponseAggiornamentoVersamento_1.5.xsd")
                .addAsResource(
                        LogSessioneSyncTest.class.getClassLoader()
                                .getResource("WSRequestVersamento.xsd"),
                        "/it/eng/parer/ws/xml/versReq/WSRequestVersamento.xsd")
                .addAsResource(
                        LogSessioneSyncTest.class.getClassLoader()
                                .getResource("WSResponseVersamento.xsd"),
                        "/it/eng/parer/ws/xml/versResp/WSResponseVersamento.xsd");
        return createEnterpriseArchive(SalvataggioUpdVersamentoAroHelperTest.class.getSimpleName(),
                sacerWSJavaArchive, createSacerLogJavaArchive());
    }

    @Test
    void checkUsoXsdDatiSpecifici() {

        for (CostantiDB.TipiUsoDatiSpec tiUsoXsd : CostantiDB.TipiUsoDatiSpec.values()) {
            for (CostantiDB.TipiEntitaSacer tiEntitaSacer : CostantiDB.TipiEntitaSacer.values()) {
                final RispostaControlli rispostaControlli = helper.checkUsoXsdDatiSpecifici(0L,
                        tiUsoXsd, tiEntitaSacer);
                assertTrue(rispostaControlli.isrBoolean());
            }
        }
    }

    @Test
    void getAroValoreAttribDatiSpecs() {
        final RispostaControlli rispostaControlli = helper.getAroValoreAttribDatiSpecs(0L, 0L, 0L);
        assertTrue(rispostaControlli.isrBoolean());
    }
}
