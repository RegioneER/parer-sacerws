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

import it.eng.parer.ws.dto.RispostaControlli;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.Arrays;

import static it.eng.ArquillianTestUtils.*;

@RunWith(Arquillian.class)
public class ControlliRappVersTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(ControlliRappVersTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList(""), ControlliRappVersTest.class, ControlliRappVers.class),
                createSacerLogJavaArchive());
    }

    @EJB
    private ControlliRappVers controlliRappVers;

    @Test
    public void trovaVersSessUd() {
        final RispostaControlli rispostaControlli = controlliRappVers.trovaVersSessUd(0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void trovaVersSessDoc() {
        final RispostaControlli rispostaControlli = controlliRappVers.trovaVersSessDoc(0L, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void leggiXmlRappVersFromUd() {
        final RispostaControlli rispostaControlli = controlliRappVers.leggiXmlRappVersFromUd(0L, 0L);
        assertNoErr(rispostaControlli);
    }

    @Test
    public void leggiXmlRappVersFromDoc() {
        final RispostaControlli rispostaControlli = controlliRappVers.leggiXmlRappVersFromDoc(0L, 0L);
        assertNoErr(rispostaControlli);
    }
}
