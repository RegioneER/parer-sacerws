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

package it.eng.parer.ws.utils;

import it.eng.parer.entity.DecControlloWs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.Collections;
import java.util.List;

import static it.eng.ArquillianTestUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ControlliWSHelperTest {
    @EJB
    private ControlliWSHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive("ControlliWSHelperTest",
                createSacerWSJavaArchive(Collections.emptyList(), ControlliWSHelper.class, ControlliWSHelperTest.class),
                createSacerLogJavaArchive());
    }

    @Test
    public void caricaListaControlli_queryIsOk() {
        final List<DecControlloWs> listaControlli = helper.caricaListaControlli();
        assertFalse(listaControlli.isEmpty());
    }

    @Test
    public void caricaCdControlloWs_queryIsOk() {
        try {
            helper.caricaCdControlloWs("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "No entity found for query"));
        }
    }

    @Test
    public void caricaCdControlloFamiglia_queryIsOk() {
        try {
            helper.caricaCdControlloFamiglia("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "No entity found for query"));
        }
    }
}
