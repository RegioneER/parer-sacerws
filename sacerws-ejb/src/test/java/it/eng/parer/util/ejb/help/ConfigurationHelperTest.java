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

package it.eng.parer.util.ejb.help;

import static it.eng.ArquillianTestUtils.createEnterpriseArchive;
import static it.eng.ArquillianTestUtils.createSacerLogJavaArchive;
import static it.eng.ArquillianTestUtils.createSacerWSJavaArchive;
import static it.eng.ArquillianTestUtils.exceptionMessageContains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;

import it.eng.parer.exception.ParamApplicNotFoundException;
import it.eng.parer.ws.utils.ParametroApplDB;

// @RunWith(Arquillian.class)
// @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigurationHelperTest {
    @EJB
    private ConfigurationHelper helper;

    @Deployment
    public static Archive<?> createTestArchive() {
        return createEnterpriseArchive(
                "configurationHelperTest", createSacerWSJavaArchive(Collections.emptyList(),
                        ConfigurationHelper.class, ConfigurationHelperTest.class),
                createSacerLogJavaArchive());
    }

    @Test
    void getConfiguration_queryIsOk() {
        try {
            Map<String, String> configuration = helper.getConfiguration();
            assertFalse(configuration.isEmpty());
        } catch (ParamApplicNotFoundException e) {
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValue_queryIsOk() {
        try {
            helper.getValoreParamApplicByApplic("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValueStrutAmbiente_queryIsOk() {
        try {
            helper.getValoreParamApplicByStrut("NON_ESISTE", 0l, 0L);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValueTipoUD_queryIsOk() {
        try {
            helper.getValoreParamApplicByTipoUd("NON_ESISTE", 0L, 0L, 0L);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValueAATipoFascicolo_queryIsOk() {
        try {
            helper.getValoreParamApplicByAaTipoFasc("NON_ESISTE", 0L, 0L, 0L);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getValoreParamApplicByTiParamApplicAsMap_queryIsOk() {
        Map<String, String> map = helper.getValoreParamApplicByTiParamApplicAsMap(
                Arrays.asList(ParametroApplDB.TipoParametroAppl.IAM));
        assertFalse(map.isEmpty());
    }

    @Test
    void getParamApplicValueAsFl_queryIsOk() {
        try {
            helper.getValoreParamApplicByApplicAsFl("NON_ESISTE");
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValueAsFlTipoUD_queryIsOk() {
        try {
            helper.getValoreParamApplicByTipoUdAsFl("NON_ESISTE", 0L, 0L, 0L);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }

    @Test
    void getParamApplicValueAsFlAAFascicolo_queryIsOk() {
        try {
            helper.getValoreParamApplicByAaTipoFascAsFl("NON_ESISTE", 0L, 0l, 0L);
        } catch (Exception e) {
            assertTrue(exceptionMessageContains(e, "non definito o non valorizzato"));
        }
    }
}
