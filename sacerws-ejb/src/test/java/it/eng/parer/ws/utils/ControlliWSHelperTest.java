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

package it.eng.parer.ws.utils;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static it.eng.ArquillianTestUtils.exceptionMessageContains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.entity.DecControlloWs;

@ArquillianTest
public class ControlliWSHelperTest {
    @EJB
    private ControlliWSHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(
                "ControlliWSHelperTest", createSacerWSJavaArchive(Collections.emptyList(),
                        ControlliWSHelper.class, ControlliWSHelperTest.class),
                createSacerLogJavaArchive());
    }

    @Test
    void caricaListaControlli_queryIsOk() {
        final List<DecControlloWs> listaControlli = helper.caricaListaControlli();
        assertFalse(listaControlli.isEmpty());
    }

    @Test
    void caricaCdControlloWs_queryIsOk() {
        try {
            helper.caricaCdControlloWs("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "No entity found for query"));
        }
    }

    @Test
    void caricaCdControlloFamiglia_queryIsOk() {
        try {
            helper.caricaCdControlloFamiglia("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "No entity found for query"));
        }
    }
}
