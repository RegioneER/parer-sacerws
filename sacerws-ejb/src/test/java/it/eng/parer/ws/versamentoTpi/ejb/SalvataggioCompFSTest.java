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

package it.eng.parer.ws.versamentoTpi.ejb;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Collections;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xadisk.bridge.proxies.interfaces.XADiskBasicIOOperations;

import it.eng.ArquillianTestUtils;
import it.eng.parer.ws.dto.RispostaControlli;
import it.eng.parer.ws.versamento.dto.StrutturaVersamento;

@ArquillianTest
public class SalvataggioCompFSTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        final JavaArchive sacerWSJavaArchive = createSacerWSJavaArchive(Collections.emptyList(),
                SalvataggioCompFSTest.class, SalvataggioCompFS.class, XADiskBasicIOOperations.class,
                it.eng.parer.ws.versamentoTpi.ejb.StatoCreaCartelle.class,
                org.xadisk.connector.outbound.XADiskConnectionFactory.class);
        sacerWSJavaArchive.addAsResource(ArquillianTestUtils.class.getClassLoader().getResource("jboss-ejb3.xml"),
                "META-INF/jboss-ejb3.xml");
        return createEnterpriseArchive(SalvataggioCompFSTest.class.getSimpleName(), sacerWSJavaArchive,
                createSacerLogJavaArchive());
    }

    @EJB
    private SalvataggioCompFS salvataggioCompFS;

    @Test
    void injectOk() {
        assertNotNull(salvataggioCompFS);
    }

    @Test
    @Disabled("java.lang.IllegalArgumentException: Can not set org.xadisk.connector.outbound.XADiskConnectionFactory field it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS.xadCf to org.xadisk.connector.outbound.XADiskConnectionFactoryImpl")
    void verificaDirTPIDataViaDb_queryIsOk() {
        final StrutturaVersamento versamento = mockStrutturaVersamento();
        final RispostaControlli rispostaControlli = salvataggioCompFS.verificaDirTPIDataViaDb(versamento,
                new StatoCreaCartelle(), 1);
        assertTrue(rispostaControlli.isrBoolean());
    }

    private StrutturaVersamento mockStrutturaVersamento() {
        final StrutturaVersamento versamento = new StrutturaVersamento();
        versamento.setDataVersamento(ZonedDateTime.now());
        versamento.setTpiRootTpiDaSacer("tpiRootSacer");
        versamento.setTpiRootVers("tpiRootVers");
        versamento.setSubPathDataVers("subPathDataVers");
        versamento.setIdStruttura(0L);
        versamento.setTpiRootTpi("tipiRootTpi");
        return versamento;
    }

    @Test
    @Disabled("java.lang.IllegalArgumentException: Can not set org.xadisk.connector.outbound.XADiskConnectionFactory field it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS.xadCf to org.xadisk.connector.outbound.XADiskConnectionFactoryImpl")
    void verificaDirTPIVersatoreViaDb_queryIsOk() {
        final RispostaControlli rispostaControlli = salvataggioCompFS
                .verificaDirTPIVersatoreViaDb(mockStrutturaVersamento(), new StatoCreaCartelle(), 1);
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    @Disabled("java.lang.IllegalArgumentException: Can not set org.xadisk.connector.outbound.XADiskConnectionFactory field it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS.xadCf to org.xadisk.connector.outbound.XADiskConnectionFactoryImpl")
    void generaDirDataTPIBlock_queryIsOk() {
        final RispostaControlli rispostaControlli = salvataggioCompFS
                .generaDirVersatoreTPIBlock(mockStrutturaVersamento(), new StatoCreaCartelle());
        assertTrue(rispostaControlli.isrBoolean());
    }

    @Test
    @Disabled("java.lang.IllegalArgumentException: Can not set org.xadisk.connector.outbound.XADiskConnectionFactory field it.eng.parer.ws.versamentoTpi.ejb.SalvataggioCompFS.xadCf to org.xadisk.connector.outbound.XADiskConnectionFactoryImpl")
    void generaDirVersatoreTPIBlock_queryIsOk() {
        final RispostaControlli rispostaControlli = salvataggioCompFS
                .generaDirVersatoreTPIBlock(mockStrutturaVersamento(), new StatoCreaCartelle());
        assertTrue(rispostaControlli.isrBoolean());
    }
}
