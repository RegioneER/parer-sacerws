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
import static it.eng.ArquillianTestUtils.exceptionMessageContains;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import it.eng.parer.entity.AroUnitaDoc;
import it.eng.parer.entity.AroUpdUnitaDoc;
import it.eng.parer.entity.OrgSubStrut;
import it.eng.parer.ws.dto.CSChiave;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versamento.ejb.LogSessioneSyncTest;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;

@ArquillianTest
public class SalvataggioUpdVersamentoUpdHelperTest {

    @Deployment
    public static Archive<?> createTestArchive() {
	final JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(
		Arrays.asList("it.eng.parer.ws.versamentoUpd.ext",
			"it.eng.parer.ws.versamentoUpd.ext", "it.eng.parer.ws.xml.versUpdReq",
			"it.eng.parer.ws.xml.versUpdResp", "it.eng.parer.ws.xml.versReq",
			"it.eng.parer.ws.xml.versReqMultiMedia", "it.eng.parer.ws.xml.versResp"),
		SalvataggioUpdVersamentoUpdHelper.class,
		SalvataggioUpdVersamentoUpdHelperTest.class,
		SalvataggioUpdVersamentoBaseHelper.class, it.eng.parer.ws.ejb.XmlUpdVersCache.class,
		it.eng.parer.ws.ejb.XmlVersCache.class,
		it.eng.parer.util.ejb.AppServerInstance.class, LogSessioneUpdVersamentoHelper.class,
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
	return createEnterpriseArchive(SalvataggioUpdVersamentoUpdHelperTest.class.getSimpleName(),
		sacerWSJavaArchive, createSacerLogJavaArchive());
    }

    @EJB
    private SalvataggioUpdVersamentoUpdHelper helper;

    @Test
    void getNextPgAroUpdUnitaDoc_queryIsOk() {
	final RispostaControlli rispostaControlli = helper.getNextPgAroUpdUnitaDoc(0L);
	assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    void scriviMonKeyTotalUd_queryIsOk() {
	final AroUnitaDoc tmpAroUnitaDoc = new AroUnitaDoc();
	tmpAroUnitaDoc.setOrgSubStrut(new OrgSubStrut());
	tmpAroUnitaDoc.getOrgSubStrut().setIdSubStrut(0L);
	final AroUpdUnitaDoc tmpAroUpdUnitaDoc = new AroUpdUnitaDoc();
	tmpAroUpdUnitaDoc.setTsIniSes(new Date());
	final StrutturaUpdVers strutturaUpdVers = new StrutturaUpdVers();
	strutturaUpdVers.setIdStruttura(0L);
	strutturaUpdVers.setChiaveNonVerificata(new CSChiave());
	strutturaUpdVers.getChiaveNonVerificata().setAnno(2021L);
	strutturaUpdVers.setIdRegistro(0L);
	strutturaUpdVers.setIdTipologiaUnitaDocumentaria(0L);
	strutturaUpdVers.setIdTipoDocPrincipale(0L);
	try {
	    helper.scriviMonKeyTotalUd(tmpAroUnitaDoc, tmpAroUpdUnitaDoc, strutturaUpdVers);
	} catch (Exception e) {
	    assertTrue(exceptionMessageContains(e, "TransientPropertyValueException"));
	}
    }
}
