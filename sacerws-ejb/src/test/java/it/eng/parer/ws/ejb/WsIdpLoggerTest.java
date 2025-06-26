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

package it.eng.parer.ws.ejb;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import it.eng.ArquillianTestUtils;

@ArquillianTest
public class WsIdpLoggerTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ArquillianTestUtils.createEnterpriseArchive(WsIdpLoggerTest.class.getSimpleName(),
                ArquillianTestUtils.createSacerLogJavaArchive(), ArquillianTestUtils.createPaginatorJavaArchive(),
                ArquillianTestUtils.createSacerWSJavaArchive(Arrays.asList(""), WsIdpLoggerTest.class,
                        WsIdpLogger.class, it.eng.parer.ws.ejb.ControlliSemantici.class,
                        it.eng.parer.util.ejb.AppServerInstance.class, it.eng.parer.idpjaas.logutils.LogDto.class));
    }

    @EJB
    private WsIdpLogger wsIdpLogger;

    @Test
    public void todo() {
        assertTrue(true);
    }

    @Test
    @Disabled("è complicato creare il caso su DB per tetare la parte di query")
    public void scriviLog() {
        assertTrue(true);
    }
}
