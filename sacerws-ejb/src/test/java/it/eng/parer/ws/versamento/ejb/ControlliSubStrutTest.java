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

import static it.eng.ArquillianTestUtils.assertNoErr;
import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versamento.dto.DocumentoVers;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;

@ArquillianTest
public class ControlliSubStrutTest {

    @Deployment
    public static Archive<?> createTestArchive() {
	return createEnterpriseArchive(ControlliSubStrutTest.class.getSimpleName(),
		createSacerWSJavaArchive(Arrays.asList(""), ControlliSubStrutTest.class,
			ControlliSubStrut.class, it.eng.parer.util.DateUtilsConverter.class)
			.addPackages(true, "org.apache.poi.ss.formula"),
		createSacerLogJavaArchive());
    }

    @EJB
    private ControlliSubStrut controlliSubStrut;

    @Test
    public void calcolaSubStrut() {
	final StrutturaVersamento strutV = new StrutturaVersamento();
	strutV.setIdTipologiaUnitaDocumentaria(0L);
	strutV.setDocumentiAttesi(new ArrayList<>());
	strutV.getDocumentiAttesi().add(new DocumentoVers());
	strutV.getDocumentiAttesi().get(0).setIdTipoDocumentoDB(0L);
	strutV.setDataVersamento(ZonedDateTime.now());
	final RispostaControlli rispostaControlli = controlliSubStrut.calcolaSubStrut(strutV);
	assertNoErr(rispostaControlli);
    }
}
