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

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.ws.dto.RispostaControlli;

@ArquillianTest
public class ControlliRappVersTest {
    @Deployment
    public static Archive<?> createTestArchive() {
	return createEnterpriseArchive(ControlliRappVersTest.class.getSimpleName(),
		createSacerWSJavaArchive(Arrays.asList(""), ControlliRappVersTest.class,
			ControlliRappVers.class),
		createSacerLogJavaArchive());
    }

    @EJB
    private ControlliRappVers controlliRappVers;

    @Test
    void trovaVersSessUd() {
	final RispostaControlli rispostaControlli = controlliRappVers.trovaVersSessUd(0L);
	assertNoErr(rispostaControlli);
    }

    @Test
    void trovaVersSessDoc() {
	final RispostaControlli rispostaControlli = controlliRappVers.trovaVersSessDoc(0L, 0L);
	assertNoErr(rispostaControlli);
    }

    @Test
    void leggiXmlRappVersFromUd() {
	final RispostaControlli rispostaControlli = controlliRappVers.leggiXmlRappVersFromUd(0L,
		0L);
	assertNoErr(rispostaControlli);
    }

    @Test
    void leggiXmlRappVersFromDoc() {
	final RispostaControlli rispostaControlli = controlliRappVers.leggiXmlRappVersFromDoc(0L,
		0L);
	assertNoErr(rispostaControlli);
    }
}
