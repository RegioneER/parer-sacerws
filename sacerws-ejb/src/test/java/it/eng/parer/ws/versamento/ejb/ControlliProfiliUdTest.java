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

import it.eng.parer.entity.constraint.DecModelloXsdUd;
import it.eng.parer.ws.ejb.ControlliSemantici;
import it.eng.parer.ws.utils.CostantiDB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.util.Arrays;

import static it.eng.ArquillianTestUtils.*;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ControlliProfiliUdTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(ControlliProfiliUdTest.class.getSimpleName(),
                createSacerWSJavaArchive(Arrays.asList("it.eng.parer.ws.xml.versReq"), ControlliProfiliUdTest.class,
                        ControlliProfiliUd.class, ControlliSemantici.class),
                createSacerLogJavaArchive());
    }

    @EJB
    private ControlliProfiliUd controlliProfiliUd;

    @Test
    public void checkXsdProfileExistence_queryIsOk() {
        for (CostantiDB.TipiEntitaSacer tipiEntitaSacer : CostantiDB.TipiEntitaSacer.values()) {
            for (DecModelloXsdUd.TiModelloXsdUd tiModelloXsd : DecModelloXsdUd.TiModelloXsdUd.values()) {
                controlliProfiliUd.checkXsdProfileExistence(0L, tipiEntitaSacer, tiModelloXsd);
                assertTrue(true);
            }
        }

    }

    @Test
    public void getXsdProfileByVersion_queryIsOk() {
        for (CostantiDB.TipiEntitaSacer tipiEntitaSacer : CostantiDB.TipiEntitaSacer.values()) {
            for (DecModelloXsdUd.TiModelloXsdUd tiModelloXsd : DecModelloXsdUd.TiModelloXsdUd.values()) {
                controlliProfiliUd.getXsdProfileByVersion(Arrays.asList(0L, -1L), tipiEntitaSacer, tiModelloXsd,
                        "versione");
            }
        }
        assertTrue(true);
    }
}
