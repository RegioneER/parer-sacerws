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

package it.eng.parer.firma.crypto.helper;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.parer.firma.helper.CryptoRestConfiguratorHelper;

@RunWith(Arquillian.class)

public class CryptoRestConfiguratorHelperTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(Collections.emptyList(),
                CryptoRestConfiguratorHelper.class, CryptoRestConfiguratorHelperTest.class,
                it.eng.parer.util.ejb.help.ConfigurationHelper.class);
        sacerWSJavaArchive.addPackage("it.eng.parer.retry");
        return createEnterpriseArchive("CryptoRestConfiguratorHelperTest", sacerWSJavaArchive,
                createSacerLogJavaArchive());
    }

    @EJB
    private CryptoRestConfiguratorHelper helper;

    @Test
    public void getRetryTimeoutParam_queryIsOk() {
        helper.getRetryTimeoutParam();
        assertTrue(true);
    }

    @Test
    public void getMaxRetryParam_queryIsOk() {
        helper.getMaxRetryParam();
        assertTrue(true);
    }

    @Test
    public void getCircuitBreakerOpenTimeoutParam_queryIsOk() {
        helper.getCircuitBreakerOpenTimeoutParam();
        assertTrue(true);
    }

    @Test
    public void getCircuitBreakerResetTimeoutParam_queryIsOk() {
        helper.getCircuitBreakerResetTimeoutParam();
        assertTrue(true);
    }

    @Test
    public void getPeriodoBackOffParam_queryIsOk() {
        helper.getPeriodoBackOffParam();
        assertTrue(true);
    }

    @Test
    public void getClientTimeoutInMinutesParam_queryIsOk() {
        helper.getClientTimeoutInMinutesParam();
        assertTrue(true);
    }

    @Test
    public void isCompositePolicyOptimisticParam_queryIsOk() {
        assertNotNull(helper.isCompositePolicyOptimisticParam());
    }

    @Test
    public void endPoints_queryIsOk() {
        assertNotNull(helper.endPoints());
    }

    @Test
    public void preferredEndpoint_queryIsOk() {
        helper.preferredEndpoint();
        assertTrue(true);
    }
}
